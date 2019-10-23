package SiteConnectors.Betfair;

import Bet.Bet;
import Bet.BetOffer;
import Bet.BetOrder;
import Bet.FootballBet.FootballResultBet;
import Bet.FootballBet.FootballScoreBet;
import Bet.PlacedBet;
import SiteConnectors.BettingSite;
import SiteConnectors.RequestHandler;
import SiteConnectors.SiteEventTracker;
import Sport.FootballMatch;
import Trader.EventTrader;
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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
    public String app_id = "3BD65v2qKzw9ETp9";
    public String app_id_dev = "DfgkZAnb0qi6Wmk1";
    public String token;

    public RPCRequestHandler rpcRequestHandler;
    public BlockingQueue<RequestHandler> rpcRequestHandlerQueue;

    public BlockingQueue<Object[]> eventSearchHandlerQueue;

    public BigDecimal commission_discount = BigDecimal.ZERO;
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
            NoSuchAlgorithmException, KeyStoreException, KeyManagementException, URISyntaxException,
            InterruptedException {

        super();
        log.info("Creating new Betfair Connector");
        name = "betfair";
        balance = BigDecimal.ZERO;
        commission_rate = new BigDecimal("0.02");
        min_back_stake = new BigDecimal("2.00");

        requester = new Requester();
        requester.setHeader("X-Application", app_id);
        login();


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
        public int REQUEST_THREADS = 8;
        public long WAIT_MILLISECONDS = 5;

        public BlockingQueue<RequestHandler> requestQueue;
        public BlockingQueue<ArrayList<RequestHandler>> workerQueue;

        public RPCRequestHandler(BlockingQueue requestQueue){
            this.requestQueue = requestQueue;
        }

        @Override
        public void run() {
            log.info("Running RPC Request Handler for betfair.");

            Instant wait_until = null;
            ArrayList<RequestHandler> jsonHandlers = new ArrayList<>();
            RequestHandler new_handler = null;
            long milliseconds_to_wait;

            // Start workers
            workerQueue = new LinkedBlockingQueue<>();
            RPCRequestSender rs = new RPCRequestSender(workerQueue);
            for (int i=0; i<REQUEST_THREADS; i++){
                Thread t = new Thread(rs);
                t.setName("Bf RS-" + String.valueOf(i+1));
                t.start();
            }

            while (!exit_flag) {

                try {
                    if (wait_until == null){
                        new_handler = null;
                        while (!exit_flag && new_handler == null) {
                            new_handler = requestQueue.poll(1, TimeUnit.SECONDS);
                        }
                        wait_until = Instant.now().plus(WAIT_MILLISECONDS, ChronoUnit.MILLIS);
                    }
                    else {
                        milliseconds_to_wait = wait_until.toEpochMilli() - Instant.now().toEpochMilli();
                        new_handler = requestQueue.poll(milliseconds_to_wait, TimeUnit.MILLISECONDS);
                    }

                    if (new_handler != null) {
                        jsonHandlers.add(new_handler);
                    }

                    if ((new_handler == null || jsonHandlers.size() > MAX_BATCH_SIZE || Instant.now().isAfter(wait_until))
                        && !exit_flag){

                        workerQueue.put(jsonHandlers);
                        wait_until = null;
                        jsonHandlers = new ArrayList<>();
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.info("Ending betfair request handler.");
        }
    }


    public class RPCRequestSender implements Runnable{

        public BlockingQueue<ArrayList<RequestHandler>> jobQueue;

        public RPCRequestSender(BlockingQueue jobQueue){
            this.jobQueue = jobQueue;
        }

        @Override
        public void run() {
            ArrayList<RequestHandler> jsonHandlers = null;

            mainloop:
            while (!exit_flag){
                JSONArray final_request = new JSONArray();
                try {
                    jsonHandlers = null;
                    while (!exit_flag && jsonHandlers == null){
                        jsonHandlers = jobQueue.poll(1, TimeUnit.SECONDS);
                    }
                    if (jsonHandlers == null){
                        continue;
                    }

                    // Build final rpc request, give each rpc request the index of the jsonhandler as its id
                    // This can mean multiple rpc requests have the same id
                    for (int i=0; i<jsonHandlers.size(); i++){
                        for (Object single_rpc_obj: (JSONArray) jsonHandlers.get(i).request){
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
                        RequestHandler rh = jsonHandlers.get(i);
                        rh.setResponse(responses[i]);
                    }


                } catch (InterruptedException | IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }
            log.info("Ending betfair request sender.");
        }
    }


    @Override
    public void login() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, IOException, URISyntaxException, InterruptedException {

        token = getSessionToken();
        requester.setHeader("X-Authentication", token);
        updateAccountInfo();
        log.info(String.format("Successfully logged into Betfair. Balance: %s  Exposure: %s",
                balance.toString(), exposure.toString()));
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
        return commission_rate.subtract(commission_discount);
    }


    @Override
    public BigDecimal minBackersStake() {
        return min_back_stake;
    }


    @Override
    public SiteEventTracker getEventTracker(EventTrader eventTrader){
        return new BetfairEventTracker(this, eventTrader);
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
        RequestHandler rh = new RequestHandler(rpc_requests);
        rpcRequestHandlerQueue.put(rh);

        // Put all responses together in one jsonarray
        JSONArray response = (JSONArray) rh.getResponse();
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


    @Override
    public void updateAccountInfo() throws InterruptedException, IOException, URISyntaxException {
        JSONObject j = new JSONObject();
        j.put("id", 1);
        j.put("jsonrpc", "2.0");
        j.put("method", "AccountAPING/v1.0/getAccountFunds");
        j.put("params", new JSONObject());

        JSONObject r = (JSONObject) ((JSONObject) requester.post(accounts_endpoint, j)).get("result");

        setBalance(new BigDecimal(Double.toString((double) r.get("availableToBetBalance"))));
        exposure = new BigDecimal(Double.toString((double) r.get("exposure"))).multiply(new BigDecimal(-1));
        betfairPoints = (long) r.get("pointsBalance");
        commission_discount = new BigDecimal(Double.toString((double) r.get("discountRate")));
        commission_rate = new BigDecimal(Double.toString((double) r.get("retainedCommission")))
                .divide(new BigDecimal(100));
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
                fm.metadata.put("betfair_id", (String) event.get("id"));
            }
            catch (ParseException e){
                continue;
            }
            footballMatches.add(fm);
        }
        return footballMatches;
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
            String msg = String.format("Error getting market catalogue from betfair.\nparams\n%s\nresult\n%s",
                    jstring(params), jstring(r));
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
                    market_id, jstring(eventsResponse)));
            return null;
        }

        String event_id = (String) ((JSONObject) ((JSONObject) eventsResponse.get(0)).get("event")).get("id");

        return event_id;
    }


    public ArrayList<PlacedBet> placeBets(ArrayList<BetOrder> betOrders, BigDecimal MIN_ODDS_RATIO)
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

        // Clear list so it can be reordered as they're put into payload
        betOrders = new ArrayList<>();

        // Create RPC request
        JSONArray RPCs = new JSONArray();
        for (Map.Entry<String, ArrayList<BetOrder>> entry: market_betOrders.entrySet()){
            String marketId = entry.getKey();
            ArrayList<BetOrder> marketBetOrders = entry.getValue();

            // One rpc request part, per market
            JSONArray instructions_list = new JSONArray();
            for (BetOrder betOrder: marketBetOrders){

                // Multiply odds by a small amount to undercut price and for margin of error.
                BigDecimal odds = betOrder.bet_offer.odds;
                if (betOrder.isBack()){
                    odds = validPrice(odds.subtract(BigDecimal.ONE).multiply(MIN_ODDS_RATIO).add(BigDecimal.ONE));
                }
                else {
                    odds = validPrice(odds.subtract(BigDecimal.ONE)
                            .divide(MIN_ODDS_RATIO, 20, RoundingMode.HALF_UP).add(BigDecimal.ONE));
                }

                JSONObject limitOrder = new JSONObject();
                limitOrder.put("size", betOrder.getBackersStake().setScale(2, RoundingMode.HALF_UP).toString());
                limitOrder.put("price", odds.toString());
                limitOrder.put("persistenceType", "PERSIST");
                limitOrder.put("timeInForce", "FILL_OR_KILL");

                JSONObject instructions = new JSONObject();
                instructions.put("orderType", "LIMIT");
                instructions.put("handicap", "0");
                instructions.put("selectionId", betOrder.bet_offer.metadata.get("selectionId"));
                instructions.put("side", betOrder.betType());
                instructions.put("limitOrder", limitOrder);


                betOrder.site_json_request = instructions;
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

        // Send off request to place bets on betfair exchange
        JSONArray response = (JSONArray) requester.post(betting_endpoint, RPCs);

        // Get responses and generate PlaceBet for each
        ArrayList<PlacedBet> placedBets = new ArrayList<>();
        for (Object rpc_obj: response){
            JSONObject rpc_response = (JSONObject) rpc_obj;
            JSONObject rpc_result = (JSONObject) rpc_response.get("result");
            String market_id = (String) rpc_result.get("marketId");
            String rpc_status = (String) rpc_result.get("status");

            if (!rpc_status.equals("SUCCESS")){
                String errorCode = (String) rpc_result.get("errorCode");
                log.severe(String.format("Failed 1 or more bets in betfair. '%s' in market '%s'\n%s\n%s\n%s",
                        jstring(BetOrder.list2JSON(betOrders)), String.valueOf(errorCode),
                        market_id, jstring(RPCs), jstring(rpc_response)));
            }

            for (Object report_obj: (JSONArray) rpc_result.get("instructionReports")){
                JSONObject bet_report = (JSONObject) report_obj;


                Instant time = Instant.parse((String) bet_report.get("placedDate"));
                String orderStatus = (String) bet_report.get("orderStatus");
                String status = (String) bet_report.get("status");
                int bet_order_id = Integer.valueOf((String) ((JSONObject) bet_report.get("instruction")).get("customerOrderRef"));
                BetOrder betOrder = betOrders.get(bet_order_id);

                // Complete matched Bet
                if (status.equals("SUCCESS") && orderStatus.equals("EXECUTION_COMPLETE")){
                    // Collect data from return json
                    String bet_id = (String) bet_report.get("betId");
                    BigDecimal size_matched = new BigDecimal((String.valueOf((Double) bet_report.get("sizeMatched"))));
                    BigDecimal avg_odds = new BigDecimal((String.valueOf((Double) bet_report.get("averagePriceMatched"))));

                    // Find the invested amount from backers stake depending on if bet is back or lay
                    BigDecimal invested;
                    if (betOrder.isBack()){
                        invested = size_matched;
                    }
                    else {
                        invested = BetOffer.backStake2LayStake(size_matched, avg_odds);
                    }

                    BigDecimal returns = this.ROI(betOrder.betType(), avg_odds, betOrder.commission(),
                            invested, true);


                    log.info(String.format("Successful invested £%s @ %S on %s '%s' in betfair (returns %s).",
                            invested.toString(), avg_odds.toString(), betOrder.bet_offer.bet.id(),
                            betOrder.bet_offer.match.name, returns.toString()));


                    PlacedBet pb = new PlacedBet("SUCCESS", bet_id, betOrder, size_matched, avg_odds, returns, time);
                    pb.site_json_response = bet_report;
                    placedBets.add(pb);
                }

                // Expired Bet
                else if (status.equals("SUCCESS") && orderStatus.equals("EXPIRED")){
                    log.warning(String.format("unsuccessful bet placed in betfair '%s'.\n%s",
                            String.valueOf(orderStatus), jstring(bet_report)));

                    PlacedBet pb = new PlacedBet("FAILED", betOrder, String.valueOf(orderStatus), time);
                    pb.site_json_response = bet_report;
                    placedBets.add(pb);
                }

                // Any other error
                else {
                    String error = (String) bet_report.get("errorCode");
                    log.warning(String.format("unsuccessful bet placed in betfair '%s'.\n%s",
                            String.valueOf(error), jstring(bet_report)));

                    PlacedBet pb = new PlacedBet("FAILED", betOrder, String.valueOf(error), time);
                    pb.site_json_response = bet_report;
                    placedBets.add(pb);
                }
            }
        }

        // Update account info
        try {
            updateAccountInfo();
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.severe(e.toString());
        }

        return placedBets;
    }


    public static BigDecimal round(BigDecimal value, BigDecimal increment, RoundingMode roundingMode) {
        if (increment.signum() == 0) {
            // 0 increment does not make much sense, but prevent division by 0
            return value;
        } else {
            BigDecimal divided = value.divide(increment, 0, roundingMode);
            BigDecimal result = divided.multiply(increment);
            return result;
        }
    }


    private static BigDecimal validPrice(BigDecimal price) {

        if (price.compareTo(new BigDecimal(2)) == -1){
            price = round(price, new BigDecimal("0.01"), RoundingMode.DOWN);
        }
        else if (price.compareTo(new BigDecimal(3)) == -1){
            price = round(price, new BigDecimal("0.02"), RoundingMode.DOWN);
        }
        else if (price.compareTo(new BigDecimal(4)) == -1){
            price = round(price, new BigDecimal("0.05"), RoundingMode.DOWN);
        }
        else if (price.compareTo(new BigDecimal(6)) == -1){
            price = round(price, new BigDecimal("0.1"), RoundingMode.DOWN);
        }
        else if (price.compareTo(new BigDecimal(10)) == -1){
            price = round(price, new BigDecimal("0.2"), RoundingMode.DOWN);
        }
        else if (price.compareTo(new BigDecimal(20)) == -1){
            price = round(price, new BigDecimal("0.5"), RoundingMode.DOWN);
        }
        else if (price.compareTo(new BigDecimal(30)) == -1){
            price = round(price, new BigDecimal("1"), RoundingMode.DOWN);
        }
        else if (price.compareTo(new BigDecimal(50)) == -1){
            price = round(price, new BigDecimal("2"), RoundingMode.DOWN);
        }
        else if (price.compareTo(new BigDecimal(100)) == -1){
            price = round(price, new BigDecimal("5"), RoundingMode.DOWN);
        }
        else {
            price = round(price, new BigDecimal("10"), RoundingMode.DOWN);
        }
        return price;
    }



    public static void main(String[] args){
        try {


            BigDecimal a = null;
            BigDecimal b = new BigDecimal(23);

            BigDecimal c = b.max(a);

            print(c.toString());



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
