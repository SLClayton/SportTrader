package SiteConnectors;

import Bet.Bet;
import Sport.FootballMatch;
import net.dongliu.requests.Requests;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import tools.MyLogHandler;
import tools.Requester;
import tools.printer;

import javax.net.ssl.*;
import java.io.*;
import java.math.BigDecimal;
import java.net.*;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import static net.dongliu.commons.Prints.print;
import static tools.printer.*;

public class Betfair extends BettingSite {

    public static final String name = "betfair";
    public static final int FOOTBALL_ID = 1;


    public String hostname = "https://api.betfair.com/";
    public String betting_endpoint = "https://api.betfair.com/exchange/betting/json-rpc/v1";
    public String accounts_endpoint = hostname + "/exchange/account/json-rpc/v1";
    public BigDecimal min_bet = new BigDecimal("2.00");
    public String app_id = "3BD65v2qKzw9ETp9";
    public String app_id_dev = "DfgkZAnb0qi6Wmk1";
    public String token;

    public Requester requester;
    public RPCRequestHandler rpcRequestHandler;
    public BlockingQueue<JsonHandler> rpcRequestHandlerQueue;

    public BigDecimal commission_discount = BigDecimal.ZERO;
    public BigDecimal balance;
    public long betfairPoints = 0;

    int weight_per_market = 17;  // Get marketbook request weight per market requested
    int max_weight_per_req = 200;        // Max weight
    int markets_per_req = (int) (max_weight_per_req / weight_per_market);


    public static String[] football_market_types = new String[] {
            "OVER_UNDER_05",
            "OVER_UNDER_15",
            "OVER_UNDER_25",
            "OVER_UNDER_35",
            "OVER_UNDER_45",
            "OVER_UNDER_55",
            "OVER_UNDER_65",
            "OVER_UNDER_75",
            "OVER_UNDER_85",
            "MATCH_ODDS",
            "CORRECT_SCORE"};


    public Betfair() throws IOException, CertificateException, UnrecoverableKeyException,
            NoSuchAlgorithmException, KeyStoreException, KeyManagementException, URISyntaxException {

        super();
        log.info(String.format("Creating new instance of %s.", this.getClass().getName()));

        token = getSessionToken();
        requester = new Requester(hostname);
        requester.setHeader("X-Application", app_id);
        requester.setHeader("X-Authentication", token);

        balance = BigDecimal.ZERO;
        updateAccountDetails();

        rpcRequestHandlerQueue = new LinkedBlockingQueue<>();
        rpcRequestHandler = new RPCRequestHandler(rpcRequestHandlerQueue);
        Thread rpcRequestHandlerThread = new Thread(rpcRequestHandler);
        rpcRequestHandlerThread.start();
    }

    @Override
    public void initialize() {

    }


    public class RPCRequestHandler implements Runnable{

        public int MAX_BATCH_SIZE = 10;
        public int REQUEST_THREADS = 10;
        public long WAIT_MILLISECONDS = 5;

        public BlockingQueue<JsonHandler> requestQueue;

        public RPCRequestSender[] workers = new RPCRequestSender[REQUEST_THREADS];
        public BlockingQueue<ArrayList<JsonHandler>> workerQueue;

        public RPCRequestHandler(BlockingQueue requestQueue){
            this.requestQueue = requestQueue;
        }

        @Override
        public void run() {
            Instant wait_until = null;
            ArrayList<JsonHandler> jsonHandlers = new ArrayList<>();
            JsonHandler new_handler;
            long milliseconds_to_wait;

            // Start workers
            workerQueue = new LinkedBlockingQueue<ArrayList<JsonHandler>>();
            RPCRequestSender rs = new RPCRequestSender(workerQueue);
            for (int i=0; i<REQUEST_THREADS; i++){
                Thread t = new Thread(rs);
                t.start();
            }

            while (true) {

                try {
                    if (wait_until == null){
                        new_handler = (JsonHandler) requestQueue.take();
                        wait_until = Instant.now().plus(WAIT_MILLISECONDS, ChronoUnit.MILLIS);
                    }
                    else {
                        milliseconds_to_wait = wait_until.toEpochMilli() - Instant.now().toEpochMilli();
                        new_handler = (JsonHandler) requestQueue.poll(milliseconds_to_wait, TimeUnit.MILLISECONDS);
                    }

                    if (new_handler != null) {
                        jsonHandlers.add(new_handler);
                    }

                    if (new_handler == null || jsonHandlers.size() > MAX_BATCH_SIZE || Instant.now().isAfter(wait_until)){
                        workerQueue.put(jsonHandlers);
                        wait_until = null;
                        jsonHandlers = new ArrayList<>();
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public class RPCRequestSender implements Runnable{

        public BlockingQueue<ArrayList<JsonHandler>> jobQueue;

        public RPCRequestSender(BlockingQueue jobQueue){
            this.jobQueue = jobQueue;
        }

        @Override
        public void run() {
            ArrayList<JsonHandler> jsonHandlers;
            JSONArray final_request = new JSONArray();

            while (true){
                try {
                    jsonHandlers = jobQueue.take();

                    // Build final rpc request
                    for (int i=0; i<jsonHandlers.size(); i++){
                        for (Object single_rpc_obj: jsonHandlers.get(i).request){
                            JSONObject single_rpc = (JSONObject) single_rpc_obj;

                            single_rpc.put("id", i);
                            single_rpc.put("jsonrpc", "2.0");
                            final_request.add(single_rpc);
                        }
                    }

                    // Send request
                    JSONArray full_response = (JSONArray) requester.post(betting_endpoint, final_request);

                    // Prepare empty responses to be added to
                    JSONArray[] responses = new JSONArray[jsonHandlers.size()];

                    // For each response, put in correct JSONArray for responding back to handler
                    for (Object response_obj: full_response){
                        JSONObject response = (JSONObject) response_obj;
                        int id = ((Long) response.get("id")).intValue();

                        if (responses[id] == null){
                            responses[id] = new JSONArray();
                        }
                        responses[id].add(response);
                    }

                    // Send each JSONArray back off to handler
                    for (int i=0; i<responses.length; i++){
                        JsonHandler jh = jsonHandlers.get(i);
                        jh.setResponse(responses[i]);
                    }


                } catch (InterruptedException | IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public String getSessionToken() throws CertificateException, NoSuchAlgorithmException, KeyStoreException,
            IOException, UnrecoverableKeyException, KeyManagementException {

        String loginurl = "https://identitysso-cert.betfair.com/api/certlogin";

        Map login_details = printer.getJSON(ssldir + "betfair-login.json");
        String username = login_details.get("u").toString();
        String password = login_details.get("p").toString();




        KeyStore ks = KeyStore.getInstance("JKS");
        FileInputStream fis = new FileInputStream(ssldir + "bf-ks.jks");
        ks.load(fis, "password".toCharArray());
        fis.close();
        SSLContext sslContext = SSLContexts.custom().loadKeyMaterial(ks, "password".toCharArray()).build();
        CloseableHttpClient httpclient = HttpClients.custom().setSSLContext(sslContext).build();


        String uri = String.format("%s?username=%s&password=%s", loginurl, username, password);

        HttpPost request = new HttpPost(uri);
        request.addHeader("X-Application", app_id);
        request.addHeader("Application-Type", "application/x-www-form-urlencoded");

        CloseableHttpResponse response = httpclient.execute(request);

        int status_code = response.getStatusLine().getStatusCode();
        if (status_code < 200 || status_code >= 300){
            String msg = String.format("ERROR in HTTP request betfair login - %s - %s",
                    response.toString(), response.getStatusLine().toString());
            log.severe(msg);
            throw new IOException(msg);
        }

        String response_body = EntityUtils.toString(response.getEntity());
        response.close();
        JSONObject jsonresponse = (JSONObject) JSONValue.parse(response_body);
        String loginstatus = (String) jsonresponse.get("loginStatus");

        if (!(loginstatus.toUpperCase().equals("SUCCESS"))){
            String msg = String.format("Error Login to betfair status %s", loginstatus);
            log.severe(msg);
            throw new IOException(msg);
        }

        return (String) jsonresponse.get("sessionToken");
    }


    @Override
    public BigDecimal commission() {
        return new BigDecimal("0.05").subtract(commission_discount);
    }


    @Override
    public BigDecimal minBet() {
        return min_bet;
    }


    @Override
    public SiteEventTracker getEventTracker(){
        return new BetfairEventTracker(this);
    }


    public JSONArray getMarketOdds(String[] market_ids) throws InterruptedException {
        log.fine(String.format("Getting market odds for market ids: %s", market_ids));

        ArrayList<ArrayList<String>> market_id_chunks = shard(market_ids, markets_per_req);

        // Build rpc request from market id chunks
        JSONArray rpc_requests = new JSONArray();
        for (ArrayList<String> market_ids_chunk: market_id_chunks){

            JSONArray priceData = new JSONArray();
            priceData.add("EX_ALL_OFFERS");

            JSONObject priceProjection = new JSONObject();
            priceProjection.put("priceData", priceData);

            JSONArray marketIds = new JSONArray();
            for (String id: market_ids_chunk){
                marketIds.add(id);
            }

            JSONObject params = new JSONObject();
            params.put("priceProjection", priceProjection);
            params.put("marketIds", marketIds);

            JSONObject rpc_request = new JSONObject();
            rpc_request.put("method", "SportsAPING/v1.0/listMarketBook");
            rpc_request.put("params", params);

            rpc_requests.add(rpc_request);
        }

        // Create JSON handler and put it in the queue to be sent off
        JsonHandler jh = new JsonHandler();
        jh.request = rpc_requests;
        rpcRequestHandlerQueue.put(jh);

        // Put all responses together in one jsonarray
        JSONArray response = jh.getResponse();
        JSONArray results = new JSONArray();
        for (Object rpc_return_obj: response){
            JSONObject rpc_return = (JSONObject) rpc_return_obj;
            if (rpc_return.containsKey("result")){
                JSONArray result = (JSONArray) rpc_return.get("result");
                results.addAll(result);
            }
        }

        p(results);
        return results;
    }

    public JSONArray getMarketOdds(Set<String> market_ids) throws InterruptedException {
        String[] market_id_array = new String[market_ids.size()];

        int i = 0;
        for (String s: market_ids){
            market_id_array[i] = s;
            i++;
        }
        return getMarketOdds(market_id_array);
    }


    public void updateAccountDetails() throws IOException, URISyntaxException {
        JSONObject j = new JSONObject();
        j.put("id", 1);
        j.put("jsonrpc", "2.0");
        j.put("method", "AccountAPING/v1.0/getAccountFunds");
        j.put("params", new JSONObject());


        JSONObject r = (JSONObject) ((JSONObject) requester.post(accounts_endpoint, j)).get("result");

        balance = new BigDecimal(Double.toString((double)r.get("availableToBetBalance")));
        betfairPoints = (long) r.get("pointsBalance");
        commission_discount = new BigDecimal(Double.toString((double)r.get("discountRate")));
    }


    public JSONArray getEvents(JSONObject filter) throws IOException, URISyntaxException {
        JSONObject params = new JSONObject();
        params.put("filter", filter);

        JSONObject j = new JSONObject();
        j.put("id", 1);
        j.put("jsonrpc", "2.0");
        j.put("method", "SportsAPING/v1.0/listEvents");
        j.put("params", params);

        JSONObject r = (JSONObject) requester.post(betting_endpoint, j);
        return (JSONArray) r.get("result");
    }


    public ArrayList<FootballMatch> getFootballMatches(Instant start, Instant end) throws IOException, URISyntaxException {

        // Build filter for request to get events
        JSONObject time = new JSONObject();
        time.put("from", start.toString());
        time.put("to", end.toString());
        JSONArray event_types = new JSONArray();
        event_types.add(1);
        JSONObject filter = new JSONObject();
        filter.put("marketStartTime", time);
        filter.put("eventTypeIds", event_types);

        // Get response
        JSONArray events = getEvents(filter);

        // Build match object for each return
        ArrayList<FootballMatch> footballMatches = new ArrayList<FootballMatch>();
        for (Object event_obj: events){
            JSONObject event = (JSONObject) ((JSONObject) event_obj).get("event");

            FootballMatch fm;
            try {
                fm = FootballMatch.parse((String) event.get("openDate"),
                                         (String) event.get("name"));
            }
            catch (ParseException e){
                continue;
            }
            footballMatches.add(fm);
        }
        return footballMatches;
    }


    public JSONArray getMarketCatalogue(JSONObject params) throws Exception {
        params.put("maxResults", 1000);

        JSONObject j = new JSONObject();
        j.put("id", 1);
        j.put("jsonrpc", "2.0");
        j.put("method", "SportsAPING/v1.0/listMarketCatalogue");
        j.put("params", params);

        JSONObject r = (JSONObject) requester.post(betting_endpoint, j);

        if (r.containsKey("error")){
            String msg = String.format("Error getting market catalogue from betfair.\nparams\n%s\nresult\n%s", ps(params), ps(r));
            throw new Exception(msg);
        }

        return (JSONArray) r.get("result");
    }





    public static void main(String[] args){
        String token = null;
        try {
            Betfair b = new Betfair();

            JSONObject params = new JSONObject();
            JSONObject filter = new JSONObject();
            JSONArray eventTypes = new JSONArray();
            eventTypes.add(1);
            filter.put("eventTypeIds", eventTypes);
            params.put("filter", filter);

            b.getEvents(params);



        } catch (URISyntaxException | IOException | CertificateException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            e.printStackTrace();
        }
    }
}
