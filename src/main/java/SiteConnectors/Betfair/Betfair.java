package SiteConnectors.Betfair;

import Bet.Bet;
import Bet.Bet.BetType;
import Bet.BetOrder;
import Bet.FootballBet.FootballBetGenerator;
import Bet.PlacedBet;
import Bet.MarketOddsReport;
import SiteConnectors.BettingSite;
import SiteConnectors.RequestHandler;
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

import static java.lang.System.err;
import static java.lang.System.exit;
import static tools.printer.*;

public class Betfair extends BettingSite {

    public static final int FOOTBALL_ID = 1;
    public final static String name = "betfair";
    public final static String id = "BF";
    public final static String BETFAIR_EVENT_ID = "BETFAIR_EVENT_ID";
    public final static String BETFAIR_MARKET_ID = "BETFAIR_MARKET_ID";
    public final static String BETFAIR_SELECTION_ID = "BETFAIR_SELECTION_ID";


    public String hostname = "https://api.betfair.com/";
    public String betting_endpoint = "https://api.betfair.com/exchange/betting/json-rpc/v1";
    public String accounts_endpoint = hostname + "/exchange/account/json-rpc/v1";
    public String app_id = "3BD65v2qKzw9ETp9";
    public String app_id_dev = "DfgkZAnb0qi6Wmk1";
    public String token;

    public Long MAX_WAIT_TIME;
    public Long REQUEST_TIMEOUT;

    public RPCRequestHandler rpcRequestHandler;
    public BlockingQueue<RequestHandler> rpcRequestHandlerQueue;

    public BlockingQueue<Object[]> eventSearchHandlerQueue;

    public BigDecimal base_commission_rate = new BigDecimal("0.05");
    static BigDecimal bf_min_back_stake = new BigDecimal("2.00");
    public BigDecimal commission_discount = BigDecimal.ZERO;
    public long betfairPoints = 0;

    int weight_per_market = 17;  // Get marketbook request weight per market requested
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


        rpcRequestHandlerQueue = new LinkedBlockingQueue<>();
        rpcRequestHandler = new RPCRequestHandler(rpcRequestHandlerQueue);
        rpcRequestHandler.thread.setDaemon(true);
        rpcRequestHandler.thread.setName("BF ReqHandler");
        rpcRequestHandler.start();
    }

    private void setupConfig(String config_filename) throws FileNotFoundException, org.json.simple.parser.ParseException {
        JSONObject config = getJSONResource(config_filename);
        MAX_WAIT_TIME = ((Long) config.get("BETFAIR_RH_WAIT"));
        REQUEST_TIMEOUT = ((Long) config.get("REQUEST_TIMEOUT"));
    }



    public class RPCRequestHandler implements Runnable{

        public int MAX_BATCH_SIZE = 10;
        public int REQUEST_THREADS = 10;

        public BlockingQueue<RequestHandler> requestQueue;
        public BlockingQueue<ArrayList<RequestHandler>> workerQueue;
        public Thread thread;
        public List<RPCRequestSender> rpcRequestSenders;
        public boolean exit_flag;

        public RPCRequestHandler(BlockingQueue requestQueue){
            exit_flag = false;
            this.requestQueue = requestQueue;
            workerQueue = new LinkedBlockingQueue<>();
            thread = new Thread(this);
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
                    if (wait_until == null){
                        new_handler = null;
                        while (!exit_flag && new_handler == null) {
                            new_handler = requestQueue.poll(1, TimeUnit.SECONDS);
                        }
                        wait_until = Instant.now().plus(MAX_WAIT_TIME, ChronoUnit.MILLIS);
                    }
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
                    e.printStackTrace();
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
            IOException, UnrecoverableKeyException, KeyManagementException, org.json.simple.parser.ParseException {

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
    public BigDecimal minLayersStake(BigDecimal odds) {
        return Bet.backStake2LayStake(bf_min_back_stake, odds).setScale(2, RoundingMode.UP);
    }




    @Override
    public void safe_exit() {
        exit_flag = true;
        rpcRequestHandler.safe_exit();
    }


    @Override
    public SiteEventTracker getEventTracker(){
        return new BetfairEventTracker(this);
    }


    public JSONArray getMarketOdds(Collection<String> market_ids) throws InterruptedException {
        log.fine(String.format("Getting market odds for market ids: %s", market_ids.toString()));

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



    public PlacedBet instructionReport2PlacedBet(JSONObject report){
        PlacedBet pb = new PlacedBet();
        pb.setSite(this);
        pb.raw_response = report.toJSONString();

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
            pb.bet_type = BetType.valueOf((String) ((JSONObject) report.get("instruction")).get("side"));
            pb.bet_id = (String) report.get("betId");
            pb.set_backersStake_layersProfit(new BigDecimal(String.valueOf(report.get("sizeMatched"))));
            pb.avg_odds = new BigDecimal(String.valueOf(report.get("averagePriceMatched")));
            pb.time_placed = Instant.parse((String) report.get("placedDate"));
        }

        return pb;
    }



    public List<PlacedBet> placeBets(List<BetOrder> betOrders, BigDecimal MIN_ODDS_RATIO)
            throws IOException, URISyntaxException {

        // Sort bets into groups depending on their market ID
        Map<String, ArrayList<BetOrder>> market_betOrders_map = new HashMap<>();
        for (BetOrder betOrder: betOrders){
            String marketId = betOrder.bet_offer.metadata.get("marketId");
            if (!market_betOrders_map.containsKey(marketId)) {
                market_betOrders_map.put(marketId, new ArrayList<BetOrder>());
            }
            market_betOrders_map.get(marketId).add(betOrder);
        }


        // Clear list so it can be reordered as they're put into payload
        betOrders = new ArrayList<>();


        // Create whole RPC request
        JSONArray RPCs = new JSONArray();
        for (Map.Entry<String, ArrayList<BetOrder>> entry: market_betOrders_map.entrySet()){
            String marketId = entry.getKey();
            ArrayList<BetOrder> marketBetOrders = entry.getValue();

            // One part of the RPC request per market
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
        Instant time_sent = Instant.now();
        JSONArray response = (JSONArray) requester.post(betting_endpoint, RPCs);

        // Get responses and generate PlaceBet for each
        ArrayList<PlacedBet> placedBets = new ArrayList<>();
        for (Object rpc_obj: response){
            JSONObject rpc_response = (JSONObject) rpc_obj;
            JSONObject rpc_result = (JSONObject) rpc_response.get("result");

            String market_id = (String) rpc_result.get("marketId");

            for (Object report_obj: (JSONArray) rpc_result.get("instructionReports")){
                JSONObject report = (JSONObject) report_obj;

                String cust_order_ref = report.get("customerOrderRef").toString();

                PlacedBet placedBet = instructionReport2PlacedBet(report);
                placedBet.time_sent = time_sent;
                placedBet.betOrder = betOrders.get(Integer.parseInt(cust_order_ref));

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


            Betfair b = new Betfair();
            FootballMatch fm = FootballMatch.parse("2020-05-29T18:30:00.0Z", "Freiburg v Leverkusen");
            BetfairEventTracker bet = (BetfairEventTracker) b.getEventTracker();
            bet.setupMatch(fm);

            MarketOddsReport mor = bet._getMarketOddsReport(new ArrayList<Bet>(FootballBetGenerator._getAllBets()));
            toFile(mor.toJSON());


            b.testbet("BACK", 2.00, 4.8, "1.170529627", "44520", null);



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
