package SiteConnectors.Betfair;

import Bet.BetOffer;
import Bet.BetOrder;
import Bet.FootballBet.FootballResultBet;
import Bet.PlacedBet;
import SiteConnectors.BettingSite;
import SiteConnectors.JsonHandler;
import SiteConnectors.SiteEventTracker;
import Sport.FootballMatch;
import org.apache.http.client.methods.HttpPost;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import tools.Requester;
import tools.printer;

import javax.net.ssl.*;
import java.io.*;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import static net.dongliu.commons.Prints.print;
import static tools.printer.*;

public class Betfair extends BettingSite {

    public static final int FOOTBALL_ID = 1;


    public String hostname = "https://api.betfair.com/";
    public String betting_endpoint = "https://api.betfair.com/exchange/betting/json-rpc/v1";
    public String accounts_endpoint = hostname + "/exchange/account/json-rpc/v1";
    public BigDecimal min_bet = new BigDecimal("2.00");
    public String app_id = "3BD65v2qKzw9ETp9";
    public String app_id_dev = "DfgkZAnb0qi6Wmk1";
    public String token;

    public RPCRequestHandler rpcRequestHandler;
    public BlockingQueue<JsonHandler> rpcRequestHandlerQueue;

    public EventSearchHandler eventSearchHandler;
    public BlockingQueue<Object[]> eventSearchHandlerQueue;

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
        log.info("Creating new Betfair Connector");
        name = "betfair";

        requester = new Requester();
        requester.setHeader("X-Application", app_id_dev);
        login();

        balance = BigDecimal.ZERO;
        updateAccountDetails();

        eventSearchHandlerQueue = new LinkedBlockingQueue<>();
        eventSearchHandler = new EventSearchHandler(this, eventSearchHandlerQueue);
        Thread eventSearchHandlerThread = new Thread(eventSearchHandler);
        eventSearchHandlerThread.setName("BF EvntSrchHndlr");
        eventSearchHandlerThread.start();


        rpcRequestHandlerQueue = new LinkedBlockingQueue<>();
        rpcRequestHandler = new RPCRequestHandler(rpcRequestHandlerQueue);
        Thread rpcRequestHandlerThread = new Thread(rpcRequestHandler);
        rpcRequestHandlerThread.setDaemon(true);
        rpcRequestHandlerThread.setName("BF ReqHandler");
        rpcRequestHandlerThread.start();
    }


    public class EventSearchHandler implements Runnable{

        long time_interval = 300;

        Betfair betfair;
        BlockingQueue<Object[]> jobQueue;

        public EventSearchHandler(Betfair BETFAIR, BlockingQueue<Object[]> queue){
            betfair = BETFAIR;
            jobQueue = queue;
        }

        @Override
        public void run() {
            log.info("Betfair event search handler started");
            Instant last_request = Instant.now().minus(10, ChronoUnit.DAYS);

            while (true){

                try {
                    // Get job and split up query and where to put response
                    Object[] job = jobQueue.take();
                    String query = (String) job[0];
                    BlockingQueue responseQueue = (BlockingQueue) job[1];

                    // If last request was in the time interval in the past, wait.
                    long mill_difference = Instant.now().toEpochMilli() - last_request.toEpochMilli();
                    if (mill_difference < time_interval){
                        long wait_time = time_interval - mill_difference;
                        Thread.sleep(wait_time);
                    }

                    // Put response into response queue
                    String response = _getEventFromSearch(query, betfair);
                    responseQueue.put(response);

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

        }
    }


    public class RPCRequestHandler implements Runnable{

        public int MAX_BATCH_SIZE = 10;
        public int REQUEST_THREADS = 10;
        public long WAIT_MILLISECONDS = 5;

        public BlockingQueue<JsonHandler> requestQueue;
        public BlockingQueue<ArrayList<JsonHandler>> workerQueue;

        public RPCRequestHandler(BlockingQueue requestQueue){
            this.requestQueue = requestQueue;
        }

        @Override
        public void run() {
            log.info("Running RPC Request Handler for betfair.");

            Instant wait_until = null;
            ArrayList<JsonHandler> jsonHandlers = new ArrayList<>();
            JsonHandler new_handler;
            long milliseconds_to_wait;

            // Start workers
            workerQueue = new LinkedBlockingQueue<>();
            RPCRequestSender rs = new RPCRequestSender(workerQueue);
            for (int i=0; i<REQUEST_THREADS; i++){
                Thread t = new Thread(rs);
                t.setName("Betfair RH Sender " + String.valueOf(i));
                t.start();
            }

            while (true) {

                try {
                    if (wait_until == null){
                        new_handler = requestQueue.take();
                        wait_until = Instant.now().plus(WAIT_MILLISECONDS, ChronoUnit.MILLIS);
                    }
                    else {
                        milliseconds_to_wait = wait_until.toEpochMilli() - Instant.now().toEpochMilli();
                        new_handler = requestQueue.poll(milliseconds_to_wait, TimeUnit.MILLISECONDS);
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

            while (true){
                JSONArray final_request = new JSONArray();
                try {
                    jsonHandlers = jobQueue.take();

                    // Build final rpc request, give each rpc request the index of the jsonhandler as its id
                    // This can mean multiple rpc requests have the same id
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

                        if (responses[id] == null) {
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
    public void login() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, IOException {

        token = getSessionToken();
        requester.setHeader("X-Authentication", token);
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
        log.fine(String.format("Getting market odds for market ids: %s", market_ids.toString()));

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

    @Override
    public BigDecimal getAmountToBet(BigDecimal investment) {
        return investment;
    }


    public JSONArray getMarketCatalogue(JSONObject params) throws IOException, URISyntaxException {
        params.put("maxResults", 1000);

        JSONObject j = new JSONObject();
        j.put("id", 1);
        j.put("jsonrpc", "2.0");
        j.put("method", "SportsAPING/v1.0/listMarketCatalogue");
        j.put("params", params);

        JSONObject r = (JSONObject) requester.post(betting_endpoint, j);

        if (r.containsKey("error")){
            String msg = String.format("Error getting market catalogue from betfair.\nparams\n%s\nresult\n%s", ps(params), ps(r));
            throw new IOException(msg);
        }

        return (JSONArray) r.get("result");
    }


    public static String getEventFromSearch(String query, Betfair bf){

        ArrayBlockingQueue<String> responseQueue = new ArrayBlockingQueue<>(1);
        Object[] params = new Object[] {query, responseQueue};

        try {
            bf.eventSearchHandlerQueue.put(params);
            String response = responseQueue.take();
            return response;
        } catch (InterruptedException e) {
            log.severe("Interrupt while getting event from search in betfair");
            return null;
        }
    }


    public static String _getEventFromSearch(String query, Betfair bf){

        // Create HTTP GET to search betfairs regular search for query
        // returning pure html
        Map<String, Object> params = new HashMap<>();
        params.put("query", query);
        String html;
        Requester requester = new Requester();
        try {
            html = requester.getRaw("https://www.betfair.com/exchange/search", params);
        } catch (IOException | URISyntaxException | InterruptedException e) {
            log.warning(String.format("Could not find betfair event id for search query '%s'", query));
            return null;
        }

        // Find the correct part of the html for the first item in the search list
        Document doc = Jsoup.parse(html);
        Elements firstResult = doc.getElementsByClass("mod-searchresults-ebs-link i13n-ltxt-Event i13n-pos-1 i13n-gen-15 i13n-R-1");
        Element element = firstResult.get(0);

        // Select the href from the first item
        // (the url endpoint to the event page, this contains the event ID)
        String href = element.attributes().get("href");

        // Whichever tag in the href will tell us what to do next
        String event_tag = "/event?id=";
        String market_tag = "market/";
        String event_id = null;

        if (href.contains(event_tag)){
            // Find where the ID begins in the href
            int id_start = href.indexOf(event_tag) + event_tag.length();

            // Extract the event ID
            event_id = href.substring(id_start);
        }
        else if (href.contains(market_tag)){
            // Find where the ID begins in the href
            int id_start = href.indexOf(market_tag) + market_tag.length();

            // Extract the market id and use function to get event id
            String market_id = href.substring(id_start);
            event_id = bf.getEventFromMarket(market_id);
        }
        else{
            log.warning(String.format("Could not find either '%s' or '%s' in href '%s' when searching betfair for '%s'",
                    event_tag, market_tag, href, query));
            return null;
        }

        // Check event_id is valid (numeric)
        if (StringUtil.isNumeric(event_id)){
            return event_id;
        }

        log.warning(String.format("Could not find event id in href '%s' from html when searching betfair for '%s'",
                href, query));
        return null;
    }


    public String getEventFromMarket(String market_id){
        JSONObject filter = new JSONObject();
        JSONArray market_ids = new JSONArray();
        market_ids.add(market_id);
        filter.put("marketIds", market_ids);

        JSONArray eventsResponse;
        try {
            eventsResponse = getEvents(filter);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            log.severe(String.format("Error while getting event id from market id in betfair for %s", market_id));
            return null;
        }

        if (eventsResponse.size() == 0){
            log.severe(String.format("No Events found in betfair for market id %s", market_id));
            return null;
        }
        if (eventsResponse.size() > 1){
            log.severe(String.format("Multiple events found in betfair for market id %s\n%s",
                    market_id, ps(eventsResponse)));
            return null;
        }

        String event_id = (String) ((JSONObject) ((JSONObject) eventsResponse.get(0)).get("event")).get("id");

        return event_id;
    }


    public ArrayList<PlacedBet> placeBet(ArrayList<BetOrder> betOrders, BigDecimal MIN_ODDS_RATIO)
            throws IOException, URISyntaxException {

        // Sort bets into groups by their market
        Map<String, ArrayList<BetOrder>> market_betOrders = new HashMap<>();
        for (BetOrder betOrder: betOrders){
            String marketId = betOrder.bet_offer.metadata.get("marketId");

            if (!market_betOrders.containsKey(marketId)) {
                market_betOrders.put(marketId, new ArrayList<BetOrder>());
            }
            market_betOrders.get(marketId).add(betOrder);
        }

        // Clear list so it can be reordered.
        betOrders = new ArrayList<>();

        JSONArray RPCs = new JSONArray();
        for (Map.Entry<String, ArrayList<BetOrder>> entry: market_betOrders.entrySet()){
            String marketId = entry.getKey();
            ArrayList<BetOrder> marketBetOrders = entry.getValue();

            JSONArray instructions_list = new JSONArray();
            for (BetOrder betOrder: marketBetOrders){

                // Set the price to be an acceptable percentage under (lower limit),
                // and it will fill to the best price it has available
                BigDecimal price = betOrder.bet_offer.odds.multiply(MIN_ODDS_RATIO).setScale(2, RoundingMode.HALF_UP);

                JSONObject limitOrder = new JSONObject();
                limitOrder.put("size", betOrder.investment.toString());
                limitOrder.put("price", price.toString());
                limitOrder.put("persistenceType", "PERSIST");
                limitOrder.put("timeInForce", "FILL_OR_KILL");

                JSONObject instructions = new JSONObject();
                instructions.put("orderType", "LIMIT");
                instructions.put("handicap", "0");
                instructions.put("selectionId", betOrder.bet_offer.metadata.get("selectionId"));
                instructions.put("side", betOrder.bet_offer.bet.type);
                instructions.put("limitOrder", limitOrder);

                betOrders.add(betOrder);
                instructions.put("customerOrderRef", String.valueOf(betOrders.size()-1));
                instructions_list.add(instructions);
            }


            JSONObject params = new JSONObject();
            params.put("marketId", marketId);
            params.put("instructions", instructions_list);

            JSONObject rpc = new JSONObject();
            rpc.put("jsonrpc", "2.0");
            rpc.put("method", "SportsAPING/v1.0/placeOrders");
            rpc.put("id", 1);
            rpc.put("params", params);

            RPCs.add(rpc);
        }


        pp(RPCs);
        print("\n\n");

        JSONArray response = (JSONArray) requester.post(betting_endpoint, RPCs);

        pp(response);
        p(response);


        // Get responses and generate PlaceBet for each
        ArrayList<PlacedBet> placedBets = new ArrayList<>();
        for (Object rpc_obj: response){
            JSONObject rpc_response = (JSONObject) rpc_obj;
            JSONObject rpc_result = (JSONObject) rpc_response.get("result");
            String market_id = (String) rpc_result.get("marketId");
            String rpc_status = (String) rpc_result.get("status");

            if (!rpc_status.equals("SUCCESS")){
                log.severe(String.format("Non success for bet placing RPC request for market '%s'\n%s",
                        market_id, ps(rpc_response)));
                continue;
            }

            for (Object report_obj: (JSONArray) rpc_result.get("instructionReports")){
                JSONObject report = (JSONObject) report_obj;

                String orderStatus = (String) report.get("orderStatus");
                int bet_order_id = Integer.valueOf((String) report.get("customerOrderRef"));
                BetOffer betOffer = betOrders.get(bet_order_id).bet_offer;

                if (!orderStatus.equals("EXECUTION_COMPLETE")){
                    log.warning(String.format("Non successful bet placed in betfair.\n%s",
                            ps(report)));

                    placedBets.add(new PlacedBet("FAILED", betOffer);
                }
                else{
                    String bet_id = (String) report.get("betId");
                    BigDecimal size = new BigDecimal((String) report.get("sizeMatched"));
                    Instant time = Instant.parse((String) report.get("placedDate"));

                    placedBets.add(new PlacedBet("SUCCESS", bet_id, betOffer, size, time));
                }
            }
        }

        return placedBets;

        //TODO: Test this make sure it works, placed bets should be returned now.
    }



    public static void main(String[] args){
        try {
            Betfair b = new Betfair();


            BetOffer bo = new BetOffer();
            bo.odds = new BigDecimal("4.1");
            bo.bet = new FootballResultBet("BACK", "DRAW", false);
            HashMap<String, String> md = new HashMap<String, String>();
            md.put("selectionId", "58805");
            md.put("marketId", "1.162787531");
            bo.metadata = md;

            BetOrder betOrder = new BetOrder();
            betOrder.bet_offer = bo;
            betOrder.real_return = new BigDecimal("8.00");
            betOrder.investment = new BigDecimal("2.50");



            BetOffer bo2 = new BetOffer();
            bo2.odds = new BigDecimal("4.3");
            bo2.bet = new FootballResultBet("BACK", "TEAM-B", false);
            HashMap<String, String> md2 = new HashMap<String, String>();
            md2.put("selectionId", "44507");
            md2.put("marketId", "1.162787531");
            bo2.metadata = md2;

            BetOrder betOrder2 = new BetOrder();
            betOrder2.bet_offer = bo2;
            betOrder2.real_return = new BigDecimal("8.00");
            betOrder2.investment = new BigDecimal("2.00");




            ArrayList<BetOrder> betOrders = new ArrayList<>();
            betOrders.add(betOrder);
            //betOrders.add(betOrder2);
            b.placeBet(betOrders, new BigDecimal("0.9"));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
