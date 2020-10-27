package SiteConnectors.Betfair;

import Bet.Bet;
import Bet.Bet.BetType;
import Bet.BetPlan;
import Bet.PlacedBet;
import SiteConnectors.BettingSite;
import SiteConnectors.RequestHandler;
import SiteConnectors.SiteEventTracker;
import Sport.FootballMatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import tools.Requester;
import tools.printer;

import javax.net.ssl.*;
import java.io.*;
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

import static tools.printer.*;
import static tools.BigDecimalTools.*;

public class Betfair extends BettingSite {

    public static final int FOOTBALL_ID = 1;
    public final static String name = "betfair";
    public final static String id = "BF";
    public final static String BETFAIR_EVENT_ID = "BETFAIR_EVENT_ID";
    public final static String BETFAIR_MARKET_ID = "BETFAIR_MARKET_ID";
    public final static String BETFAIR_SELECTION_ID = "BETFAIR_SELECTION_ID";
    public final static String BETFAIR_HANDICAP = "BETFAIR_HANDICAP";

    public static String loginurl = "https://identitysso-cert.betfair.com/api/certlogin";
    public static String hostname = "https://api.betfair.com/";
    public static String betting_endpoint = hostname + "/exchange/betting/json-rpc/v1";
    public static String accounts_endpoint = hostname + "/exchange/account/json-rpc/v1";
    public static String app_id_prod = "3BD65v2qKzw9ETp9";
    public static String app_id_dev = "DfgkZAnb0qi6Wmk1";
    public static String app_id = app_id_prod;
    public String token;

    public Long MAX_WAIT_TIME;
    public Long REQUEST_TIMEOUT;

    public RPCRequestHandler rpcRequestHandler;

    public BlockingQueue<Object[]> eventSearchHandlerQueue;

    public BigDecimal base_commission_rate = new BigDecimal("0.05");
    static BigDecimal bf_min_back_stake = new BigDecimal("2.00");
    static BigDecimal bf_min_odds = new BigDecimal("1.01");
    static BigDecimal bf_max_odds = new BigDecimal(1000);
    public BigDecimal commission_discount = BigDecimal.ZERO;
    public long betfairPoints = 0;

    int weight_per_market = 17;          // Get marketbook request weight per market requested
    int max_weight_per_req = 200;        // Max weight
    int markets_per_req = (int) (max_weight_per_req / weight_per_market);



    public Betfair() throws IOException, CertificateException, UnrecoverableKeyException,
            NoSuchAlgorithmException, KeyStoreException, KeyManagementException, URISyntaxException,
            InterruptedException, org.json.simple.parser.ParseException {

        super();
        log.info("Creating new Betfair Connector");
        balance = BigDecimal.ZERO;

        setupConfig("config.json");


        requester = Requester.JSONRequester();
        requester.setHeader("X-Application", app_id);
        login();
    }


    private void setupConfig(String config_filename) throws FileNotFoundException, org.json.simple.parser.ParseException {
        JSONObject config = getJSONResource(config_filename);
        MAX_WAIT_TIME = ((Long) config.get("BETFAIR_RH_WAIT"));
        REQUEST_TIMEOUT = ((Long) config.get("REQUEST_TIMEOUT"));
    }


    public void setupRPCRequestHandler(){
        rpcRequestHandler = new RPCRequestHandler();
        rpcRequestHandler.start();
    }



    public class RPCRequestHandler implements Runnable{

        public int MAX_BATCH_SIZE = 10;
        public int REQUEST_THREADS = 10;

        public BlockingQueue<RequestHandler> requestQueue;
        public BlockingQueue<ArrayList<RequestHandler>> workerQueue;
        public Thread thread;
        public List<RPCRequestSender> rpcRequestSenders;
        public boolean exit_flag;

        public RPCRequestHandler(){
            exit_flag = false;
            requestQueue = new LinkedBlockingQueue<>();
            workerQueue = new LinkedBlockingQueue<>();
            thread = new Thread(this);
            thread.setDaemon(true);
            thread.setName("BF ReqHandler");
        }


        public void start(){
            thread.start();
        }


        public void safe_exit(){
            exit_flag = true;
            thread.interrupt();
            for (RPCRequestSender rpcRequestSender: rpcRequestSenders){
                rpcRequestSender.safe_exit();
            }
        }



        @Override
        public void run() {
            log.info("Running RPC Request Handler for betfair.");

            Instant wait_until = null;
            ArrayList<RequestHandler> jsonHandlers = new ArrayList<>();
            RequestHandler new_handler = null;
            long milliseconds_to_wait;

            // Start workers
            rpcRequestSenders = new ArrayList<>();
            for (int i=0; i<REQUEST_THREADS; i++){
                RPCRequestSender rpcRequestSender = new RPCRequestSender(workerQueue);
                rpcRequestSender.thread.setName("Bf RS-" + String.valueOf(i+1));
                rpcRequestSender.start();
                rpcRequestSenders.add(rpcRequestSender);
            }

            while (!exit_flag) {

                try {

                    // Wait for queue item until interrupted if no time limit set.
                    if (wait_until == null){
                        new_handler = requestQueue.take();
                        wait_until = Instant.now().plus(MAX_WAIT_TIME, ChronoUnit.MILLIS);
                    }
                    // Wait for queue item until time limit or interrupted.
                    else {
                        milliseconds_to_wait = wait_until.toEpochMilli() - Instant.now().toEpochMilli();
                        new_handler = requestQueue.poll(milliseconds_to_wait, TimeUnit.MILLISECONDS);
                    }

                    if (new_handler != null) {
                        jsonHandlers.add(new_handler);
                    }

                    if ((new_handler == null || jsonHandlers.size() >= MAX_BATCH_SIZE || Instant.now().isAfter(wait_until))
                        && !exit_flag){

                        workerQueue.put(jsonHandlers);
                        wait_until = null;
                        jsonHandlers = new ArrayList<>();
                    }

                } catch (InterruptedException e) {
                    log.warning("Betfair RPC Request Handler interrupted.");
                }
            }
            log.info("Ending betfair request handler.");
        }
    }


    public class RPCRequestSender implements Runnable{

        public BlockingQueue<ArrayList<RequestHandler>> jobQueue;
        public Thread thread;
        public boolean exit_flag;

        public RPCRequestSender(BlockingQueue jobQueue){
            exit_flag = false;
            this.jobQueue = jobQueue;
            thread = new Thread(this);
        }


        public void start(){
            thread.start();
        }


        public void safe_exit(){
            exit_flag = true;
            thread.interrupt();
        }


        @Override
        public void run() {
            ArrayList<RequestHandler> jsonHandlers = null;

            mainloop:
            while (!exit_flag){
                JSONArray final_request = new JSONArray();
                jsonHandlers = null;
                try {
                    jsonHandlers = jobQueue.take();

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


                } catch (InterruptedException e) {
                    continue;
                } catch (IOException | URISyntaxException e){
                    e.printStackTrace();
                }
            }
            log.info("Ending betfair request sender.");
        }
    }


    @Override
    public void login() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, IOException, URISyntaxException, InterruptedException,
            org.json.simple.parser.ParseException {

        token = getSessionToken();
        requester.setHeader("X-Authentication", token);
        updateAccountInfo();
        log.info(String.format("Successfully logged into Betfair. Balance: %s  Exposure: %s",
                balance.toString(), exposure.toString()));
    }


    @Override
    public String getSessionToken() throws CertificateException, NoSuchAlgorithmException, KeyStoreException,
            IOException, UnrecoverableKeyException, KeyManagementException, org.json.simple.parser.ParseException,
            URISyntaxException {



        Map login_details = printer.getJSON(ssldir + "betfair-login.json");
        String username = login_details.get("u").toString();
        String password = login_details.get("p").toString();


        KeyStore ks = KeyStore.getInstance("JKS");
        FileInputStream fis = new FileInputStream(ssldir + "bf-ks.jks");
        ks.load(fis, "password".toCharArray());
        fis.close();
        SSLContext sslContext = SSLContexts.custom().loadKeyMaterial(ks, "password".toCharArray()).build();
        CloseableHttpClient httpclient = HttpClients.custom().setSSLContext(sslContext).build();


        //String uri = String.format("%s?username=%s&password=%s", loginurl, username, password);
        String uri = new URIBuilder(loginurl)
                .setParameter("username", username)
                .setParameter("password", password)
                .toString();

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
    public BigDecimal winCommissionRate() {
        return base_commission_rate.subtract(commission_discount);
    }

    @Override
    public BigDecimal lossCommissionRate() {
        return BigDecimal.ZERO;
    }


    @Override
    public String getName() {
        return name;
    }


    @Override
    public String getID() {
        return id;
    }


    @Override
    public BigDecimal minBackersStake() {
        return bf_min_back_stake;
    }




    @Override
    public void safe_exit() {
        exit_flag = true;
        if (rpcRequestHandler != null) {
            rpcRequestHandler.safe_exit();
        }
    }


    @Override
    public SiteEventTracker getEventTracker(){
        return new BetfairEventTracker(this);
    }


    public JSONArray getMarketOdds(Collection<String> market_ids) throws InterruptedException {
        log.fine(String.format("Getting market odds for market ids: %s", market_ids.toString()));

        if (market_ids.isEmpty()){
            log.warning("Attempted to get market odds for 0 market ids in betfair.");
            return new JSONArray();
        }
        List<List<String>> market_id_chunks = shard(market_ids, markets_per_req);

        // Build rpc request from market id chunks
        JSONArray rpc_requests = new JSONArray();
        for (List<String> market_ids_chunk: market_id_chunks){

            JSONArray priceData = new JSONArray();
            priceData.add("EX_BEST_OFFERS");

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

        // Setup request handler if not already
        if (rpcRequestHandler == null){
            setupRPCRequestHandler();
        }


        // Create JSON handler and put it in the queue to be sent off
        RequestHandler rh = new RequestHandler(rpc_requests);
        rpcRequestHandler.requestQueue.put(rh);

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

        // Build event object for each return
        ArrayList<FootballMatch> footballMatches = new ArrayList<FootballMatch>();
        for (Object event_obj: events){
            JSONObject event = (JSONObject) ((JSONObject) event_obj).get("event");

            FootballMatch fm;
            try {
                fm = FootballMatch.parse((String) event.get("openDate"),
                                         (String) event.get("name"));
                fm.metadata.put(BETFAIR_EVENT_ID, (String) event.get("id"));
            }
            catch (ParseException e){
                continue;
            }
            footballMatches.add(fm);
        }
        return footballMatches;
    }


    public JSONArray getMarketCatalogue(Collection<String> event_ids, Collection<String> market_types)
            throws IOException, URISyntaxException {


        JSONArray marketProjection = new JSONArray();
        marketProjection.add("MARKET_DESCRIPTION");
        marketProjection.add("RUNNER_DESCRIPTION");

        JSONArray marketTypeCodes = new JSONArray();
        //marketTypeCodes.addAll(market_types);

        JSONArray event_ids_jsonarray = new JSONArray();
        event_ids_jsonarray.addAll(event_ids);

        JSONObject filters = new JSONObject();
        filters.put("marketTypeCodes", marketTypeCodes);
        filters.put("eventIds", event_ids_jsonarray);

        // Build params for market catalogue request
        JSONObject params = new JSONObject();
        params.put("maxResults", 1000);
        params.put("marketProjection", marketProjection);
        params.put("filter", filters);


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


    public JSONObject betPlan2PlaceInstruction(BetPlan betPlan, BigDecimal odds_buffer_ratio){
        Long selection_id = betPlan.betExchange.getMetadataLong(Betfair.BETFAIR_SELECTION_ID);
        BigDecimal handicap = betPlan.betExchange.getMetadataBD(Betfair.BETFAIR_HANDICAP);

        if (selection_id == null || handicap == null){
            log.severe(String.format("Invalid betOrder metadata: Selectionid=%s  handicap=%s",
                    selection_id, handicap));
            return null;
        }

        if (odds_buffer_ratio == null){
            odds_buffer_ratio = BigDecimal.ZERO;
        }

        BigDecimal valid_odds_with_buffer = betPlan.getValidOddsWithBuffer(odds_buffer_ratio);
        if (valid_odds_with_buffer == null){
            log.severe("Could not create BF place instruction, couldn't find valid odds.");
            return null;
        }

        BigDecimal valid_stake = getValidStake(betPlan.getBackersStake(), RoundingMode.HALF_UP);

        JSONObject limit_order = new JSONObject();
        limit_order.put("size", valid_stake);
        limit_order.put("price", valid_odds_with_buffer.toString());
        limit_order.put("persistenceType", "PERSIST");
        limit_order.put("timeInForce", "FILL_OR_KILL");
        limit_order.put("minFillSize", valid_stake);

        JSONObject instruction = new JSONObject();
        instruction.put("orderType", "LIMIT");
        instruction.put("selectionId", selection_id);
        instruction.put("handicap", handicap);
        instruction.put("side", betPlan.betType().toString());
        instruction.put("limitOrder", limit_order);
        instruction.put("customerOrderRef", betPlan.getID());

        return instruction;
    }


    public JSONArray betPlans2RPCArray(List<BetPlan> betPlans, BigDecimal odds_buffer_ratio){

        // Bets in each market go in a different RPC request, so split betOrders by their market
        Map<String, List<BetPlan>> betOrders_by_market = BetPlan.splitListByMetadata(betPlans, BETFAIR_MARKET_ID);

        JSONArray RPC_Array = new JSONArray();
        for (String marke_id: betOrders_by_market.keySet()){
            List<BetPlan> market_betPlans = betOrders_by_market.get(marke_id);

            // Create instruction list for each betOrder
            JSONArray instruction_list = new JSONArray();
            for (BetPlan betPlan : market_betPlans){
                instruction_list.add(betPlan2PlaceInstruction(betPlan, odds_buffer_ratio));
            }

            // Wrap each set of instructions for a market into a RPC request
            JSONObject placeOrder = new JSONObject();
            placeOrder.put("marketId", marke_id);
            placeOrder.put("instructions", instruction_list);

            JSONObject RPC_item = new JSONObject();
            RPC_item.put("jsonrpc", "2.0");
            RPC_item.put("method", "SportsAPING/v1.0/placeOrders");
            RPC_item.put("id", 1);
            RPC_item.put("params", placeOrder);

            RPC_Array.add(RPC_item);
        }

        return RPC_Array;
    }



    public PlacedBet instructionReport2PlacedBet(JSONObject report){
        PlacedBet pb = new PlacedBet();
        pb.setSite(this);
        pb.raw_response = report;

        String status = (String) report.get("status");
        String orderStatus = (String) report.get("orderStatus");

        if (status.equals("FAILURE")){
            pb.state = PlacedBet.State.FAIL;
            pb.error = "Betfair: " + report.get("errorCode").toString();
        }
        else if (!orderStatus.equals("EXECUTION_COMPLETE")){
            BigDecimal size_matched = new BigDecimal(String.valueOf(report.get("sizeMatched")));
            pb.state = PlacedBet.State.FAIL;
            pb.error = String.format("Betfair: %s with %s remaining.", orderStatus, BDString(size_matched));
        }
        else {
            pb.setSuccess();
            pb.bet_type = BetType.valueOf((String) ((JSONObject) report.get("instruction")).get("side"));
            pb.bet_id = (String) report.get("betId");
            pb.set_backersStake_layersProfit(new BigDecimal(String.valueOf(report.get("sizeMatched"))));
            pb.avg_odds = new BigDecimal(String.valueOf(report.get("averagePriceMatched")));
            pb.time_placed = Instant.parse((String) report.get("placedDate"));
        }

        return pb;
    }

    public List<PlacedBet> placeBets(List<BetPlan> betPlans, BigDecimal odds_buffer_ratio)
            throws IOException, URISyntaxException {

        JSONArray RPCs = betPlans2RPCArray(betPlans, odds_buffer_ratio);

        // Send off request to place bets on betfair exchange
        Instant time_sent = Instant.now();
        JSONArray response = (JSONArray) requester.post(betting_endpoint, RPCs);


        // Get responses and generate PlaceBet for each
        ArrayList<PlacedBet> placedBets = new ArrayList<>();
        for (Object rpc_obj: response){
            JSONObject rpc_response = (JSONObject) rpc_obj;
            JSONObject rpc_result = (JSONObject) rpc_response.get("result");

            // Created placed bet for the each result
            for (Object report_obj: (JSONArray) rpc_result.get("instructionReports")){
                JSONObject report = (JSONObject) report_obj;

                // Collect the order ref (which we set as the betOrder ID) from the reply
                JSONObject instruction = (JSONObject) report.get("instruction");
                String order_ref = instruction.get("customerOrderRef").toString();

                PlacedBet placedBet = instructionReport2PlacedBet(report);
                placedBet.time_sent = time_sent;
                placedBet.betPlan = BetPlan.find(betPlans, order_ref);
                placedBet.raw_request = RPCs;

                placedBets.add(placedBet);
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


    @Override
    public BigDecimal getValidOdds(BigDecimal odds, RoundingMode roundingMode) {

        if (odds.compareTo(BigDecimal.ONE) < 0 || odds.compareTo(maxValidOdds()) > 0){
            log.severe(String.format("Could not return valid BF odds for input %s, outside valid range %s-%s",
                    BDString(odds), BDString(minValidOdds()), BDString(maxValidOdds())));
            return null;
        }

        if (odds.compareTo(minValidOdds()) <= 0){
            return minValidOdds();
        }

        String increment;
        if (odds.compareTo(new BigDecimal(2)) <= 0){
            increment = "0.01";
        }
        else if (odds.compareTo(new BigDecimal(3)) <= 0){
            increment = "0.02";
        }
        else if (odds.compareTo(new BigDecimal(4)) <= 0){
            increment = "0.05";
        }
        else if (odds.compareTo(new BigDecimal(6)) <= 0){
            increment = "0.1";
        }
        else if (odds.compareTo(new BigDecimal(10)) <= 0){
            increment = "0.2";
        }
        else if (odds.compareTo(new BigDecimal(20)) <= 0){
            increment = "0.5";
        }
        else if (odds.compareTo(new BigDecimal(30)) <= 0){
            increment = "1";
        }
        else if (odds.compareTo(new BigDecimal(50)) <= 0){
            increment = "2";
        }
        else if (odds.compareTo(new BigDecimal(100)) <= 0){
            increment = "5";
        }
        else {
            increment = "10";
        }

        return round(odds, new BigDecimal(increment), roundingMode);
    }

    @Override
    public BigDecimal minValidOdds() {
        return bf_min_odds;
    }

    @Override
    public BigDecimal maxValidOdds() {
        return bf_max_odds;
    }


    public void testbet(String side, Double size, Double price, String market, String selection, Double handicap) throws IOException, URISyntaxException {

        if (handicap == null){
            handicap = 0.0;
        }

        JSONObject limitOrder = new JSONObject();
        limitOrder.put("size", String.valueOf(size));
        limitOrder.put("price", String.valueOf(price));
        limitOrder.put("persistenceType", "PERSIST");
        limitOrder.put("timeInForce", "FILL_OR_KILL");

        JSONObject instructions = new JSONObject();
        instructions.put("orderType", "LIMIT");
        instructions.put("handicap", handicap.toString());
        instructions.put("selectionId", selection);
        instructions.put("side", side.toUpperCase());
        instructions.put("limitOrder", limitOrder);

        JSONArray instructions_list = new JSONArray();
        instructions_list.add(instructions);


        JSONObject params = new JSONObject();
        params.put("marketId", market);
        params.put("instructions", instructions_list);

        JSONObject rpc = new JSONObject();
        rpc.put("jsonrpc", "2.0");
        rpc.put("method", "SportsAPING/v1.0/placeOrders");
        rpc.put("id", 1);
        rpc.put("params", params);


        JSONArray RPCs = new JSONArray();
        RPCs.add(rpc);


        // Send off request to place bets on betfair exchange
        JSONArray response = (JSONArray) requester.post(betting_endpoint, RPCs);
        pp(response);

        JSONObject response_json = (JSONObject) response.get(0);
        JSONObject result = (JSONObject) response_json.get("result");
        JSONArray instructionReports = (JSONArray) result.get("instructionReports");
        JSONObject instructionReport = (JSONObject) instructionReports.get(0);

        PlacedBet pb = instructionReport2PlacedBet(instructionReport);
        pp(pb.toJSON());
    }





    public static void main(String[] args){
        try {

            URIBuilder builder = new URIBuilder("https://apache.org/shindig")
                    .addParameter("hello world", "foo&bar")
                    .setFragment("foo");
            String s = builder.toString();
            print(s);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
