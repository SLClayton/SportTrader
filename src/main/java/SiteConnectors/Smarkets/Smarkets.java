package SiteConnectors.Smarkets;

import Bet.Bet;
import Bet.Bet.BetType;
import Bet.BetOffer;
import Bet.BetOrder;
import Bet.MarketOddsReport;
import Bet.FootballBet.FootballBetGenerator;
import Bet.PlacedBet;
import SiteConnectors.Betfair.Betfair;
import SiteConnectors.Betfair.BetfairEventTracker;
import SiteConnectors.BettingSite;
import SiteConnectors.RequestHandler;
import SiteConnectors.SiteEventTracker;
import Sport.FootballMatch;
import Sport.FootballTeam;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import tools.Requester;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.lang.System.exit;
import static tools.printer.*;
import static tools.printer.round;

public class Smarkets extends BettingSite {

    public final static String name = "smarkets";
    public final static String id = "SM";

    public final static String baseurl = "https://api.smarkets.com/v3/";
    public final static String FOOTBALL = "football";
    public final static String CONTRACT_ID = "CONTRACT_ID";
    public final static String MARKET_ID = "MARKET_ID";
    public final static String SMARKETS_PRICE = "SMARKETS_PRICE";
    public final static String SMARKETS_EVENT_ID = "SMARKETS_EVENT_ID";
    public final static String RATE_LIMITED = "RATE_LIMITED";
    public final static String FULLNAME = "FULLNAME";
    public JSONObject RATE_LIMITED_JSON;

    public static int[] prices_enum = new int[] {1, 10, 20, 33, 34, 36, 37, 38, 40, 42, 43, 45, 48, 50, 53, 56, 59, 62, 67, 71, 77, 83, 91, 100, 105, 111, 118, 125, 133, 143, 154, 167, 182, 200, 208, 217, 227, 238, 250, 263, 278, 294, 312, 333, 345, 357, 370, 385, 400, 417, 435, 455, 476, 500, 513, 526, 541, 556, 571, 588, 606, 625, 645, 667, 690, 714, 741, 769, 800, 833, 870, 909, 952, 1000, 1020, 1042, 1064, 1087, 1111, 1136, 1163, 1190, 1220, 1250, 1282, 1316, 1351, 1389, 1429, 1471, 1515, 1562, 1613, 1667, 1695, 1724, 1754, 1786, 1818, 1852, 1887, 1923, 1961, 2000, 2041, 2083, 2128, 2174, 2222, 2273, 2326, 2381, 2439, 2500, 2532, 2564, 2597, 2632, 2667, 2703, 2740, 2778, 2817, 2857, 2899, 2941, 2985, 3030, 3077, 3125, 3175, 3226, 3279, 3333, 3356, 3378, 3401, 3425, 3448, 3472, 3497, 3521, 3546, 3571, 3597, 3623, 3650, 3676, 3704, 3731, 3759, 3788, 3817, 3846, 3876, 3906, 3937, 3968, 4000, 4032, 4065, 4098, 4132, 4167, 4202, 4237, 4274, 4310, 4348, 4386, 4425, 4464, 4505, 4545, 4587, 4630, 4673, 4717, 4762, 4808, 4854, 4902, 4950, 5000, 5025, 5051, 5076, 5102, 5128, 5155, 5181, 5208, 5236, 5263, 5291, 5319, 5348, 5376, 5405, 5435, 5464, 5495, 5525, 5556, 5587, 5618, 5650, 5682, 5714, 5747, 5780, 5814, 5848, 5882, 5917, 5952, 5988, 6024, 6061, 6098, 6135, 6173, 6211, 6250, 6289, 6329, 6369, 6410, 6452, 6494, 6536, 6579, 6623, 6667, 6711, 6757, 6803, 6849, 6897, 6944, 6993, 7042, 7092, 7143, 7194, 7246, 7299, 7353, 7407, 7463, 7519, 7576, 7634, 7692, 7752, 7812, 7874, 7937, 8000, 8065, 8130, 8197, 8264, 8333, 8403, 8475, 8547, 8621, 8696, 8772, 8850, 8929, 9009, 9091, 9174, 9259, 9346, 9434, 9524, 9615, 9709, 9804, 9901, 9999};
    public static BigDecimal[] decOdds_enum = priceArray2DecOddsArray(prices_enum);

    public static final BigDecimal base_commission_rate = new BigDecimal("0.01");
    static final BigDecimal smarkets_min_back_stake = new BigDecimal("0.05");
    static final BigDecimal smarkets_min_lay_stake = new BigDecimal("0.05");

    // For the request handlers
    Instant expiry_time = null;
    public int REQ_BATCH_SIZE;
    public long MAX_WAIT_TIME;
    public int REQUEST_THREADS = 10;

    int rh_total = 0;
    int rh_success = 0;
    int markets_total = 0;
    int markets_success = 0;
    Instant start_time;

    public  PriceQuotesRequestHandler priceQuotesRequestHandler;


    public Smarkets() throws URISyntaxException, IOException,
            InterruptedException, ParseException {

        RATE_LIMITED_JSON = new JSONObject();
        RATE_LIMITED_JSON.put(RATE_LIMITED, RATE_LIMITED);

        setupConfig("config.json");

        requester = Requester.JSONRequester();

        login();
    }


    private void setupConfig(String config_filename) throws FileNotFoundException, ParseException {
        JSONObject config = getJSONResource(config_filename);
        REQ_BATCH_SIZE = ((Long) config.get("SMARKETS_REQ_SIZE")).intValue();
        MAX_WAIT_TIME = ((Long) config.get("SMARKETS_RH_WAIT"));
    }



    public class PriceQuotesRequestHandler implements Runnable{

        public BlockingQueue<RequestHandler> requestQueue;
        public BlockingQueue<ArrayList<RequestHandler>> workerQueue;
        public Thread thread;
        public List<PriceQuoteRequestSender> priceQuoteRequestSenders;
        public boolean exit_flag;


        public PriceQuotesRequestHandler(){
            exit_flag = false;
            this.requestQueue = new LinkedBlockingQueue<>();
            thread = new Thread(this);
            thread.setName("Smarkets RH");
            thread.setDaemon(true);
        }


        public void start(){
            thread.start();
        }


        public void safe_exit(){
            exit_flag = true;
            thread.interrupt();
            for (PriceQuoteRequestSender priceQuoteRequestSender: priceQuoteRequestSenders){
                priceQuoteRequestSender.safe_exit();
            }
        }


        @Override
        public void run() {

            log.info(String.format("Running smarkets request handler."));

            ArrayList<RequestHandler> requestHandlers = new ArrayList<>();
            int markets_in_queue = 0;

            // Start workers
            priceQuoteRequestSenders = new ArrayList<>();
            workerQueue = new LinkedBlockingQueue<>();
            for (int i=0; i<REQUEST_THREADS; i++){
                PriceQuoteRequestSender requestSender = new PriceQuoteRequestSender(workerQueue);
                requestSender.thread.setName("smkts RS-" + String.valueOf(i+1));
                requestSender.start();
                priceQuoteRequestSenders.add(requestSender);
            }


            RequestHandler new_handler;
            Instant next_request_time;

            while (!exit_flag) {
                try {
                    // Reset variables
                    requestHandlers = new ArrayList<>();
                    markets_in_queue = 0;
                    next_request_time = null;


                    // Gather up requests from queue
                    while (!exit_flag && (next_request_time == null || Instant.now().isBefore(next_request_time))){

                        new_handler = null;

                        // Wait as long as required for first handler to arrive, set request time when it does.
                        if (next_request_time == null){
                            new_handler = requestQueue.take();
                            next_request_time = Instant.now().plusMillis(MAX_WAIT_TIME);
                        }
                        // Subsequent handlers, wait for more until request time comes around.
                        else if (Instant.now().isBefore(next_request_time)) {
                            long milliseconds_to_wait = next_request_time.toEpochMilli() - Instant.now().toEpochMilli();
                            new_handler = requestQueue.poll(milliseconds_to_wait, TimeUnit.MILLISECONDS);
                        }


                        // If a new handler is passed in, decide what to do with it.
                        if (new_handler != null){
                            int num_new_markets = ((ArrayList<String>) new_handler.request).size();

                            // Check the new request handler will not overflow the current batch.
                            if (markets_in_queue + num_new_markets <= REQ_BATCH_SIZE) {
                                requestHandlers.add(new_handler);
                                markets_in_queue += num_new_markets;
                            }
                            else{
                                // This request is full so break and send now.
                                break;
                            }
                        }
                    }
                    if (exit_flag){
                        break;
                    }

                    // Send off request if any are in list
                    if (requestHandlers.size() > 0){
                        workerQueue.put(requestHandlers);
                    }


                } catch (InterruptedException e) {
                    continue;
                } catch (Exception e){
                    e.printStackTrace();
                    log.severe("Exception found in Smarkets request handler.");
                    continue;
                }
            }
            log.info("Ending Smarkets request handler.");
        }
    }


    public class PriceQuoteRequestSender implements Runnable{

        public BlockingQueue<ArrayList<RequestHandler>> jobQueue;
        public Thread thread;
        public boolean exit_flag;

        public PriceQuoteRequestSender(BlockingQueue jobQueue){
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
            ArrayList<RequestHandler> requestHandlers = null;


            mainloop:
            while (!exit_flag){
                try {

                    // Wait for next requestHandler or command to exit
                    requestHandlers = null;
                    requestHandlers = jobQueue.take();

                    if (start_time == null){
                        start_time = Instant.now();
                    }


                    // Get tally of request handlers and markets requested
                    for (RequestHandler rh: requestHandlers){
                        rh_total += 1;
                        markets_total += ((ArrayList<String>) rh.request).size();
                    }



                    if (expiry_time != null && Instant.now().isBefore(expiry_time)){
                        // Fail all these handlers
                        for (RequestHandler rh: requestHandlers){
                            rh.setResponse(RATE_LIMITED_JSON);
                        }
                    }
                    else{

                        // list event ids from handlers to get data from.
                        ArrayList<String> market_ids = new ArrayList<>();
                        for (RequestHandler rh: requestHandlers){
                            market_ids.addAll((ArrayList<String>) rh.request);
                        }

                        // Send request
                        JSONObject market_prices = _getPrices(market_ids);

                        // If rate limit has been hit
                        if (market_prices.containsKey("error_type")
                                && ((String) market_prices.get("error_type")).equals("RATE_LIMIT_EXCEEDED")){

                            // Find what time rate limit expires
                            String expiry = (String) ((JSONObject) market_prices.get("data")).get("expiry");
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("E, d MMM yyyy H:m:s VV");
                            expiry_time = ZonedDateTime.parse(expiry, dtf).toInstant()
                                    .plus(1, ChronoUnit.SECONDS);

                            log.info(String.format("Smarkets request limit reached, waiting until %s    %s",
                                    expiry_time.toString(), market_prices.toString()));



                            // Sanity check expiry time
                            if (expiry_time.isAfter(Instant.now().plus(2, ChronoUnit.MINUTES))){
                                log.warning("Rate limit expiry for smarkets is set to %s. Using 61 seconds instead.");
                                expiry_time = Instant.now().plus(61, ChronoUnit.SECONDS);
                            }

                            // Fail all these handlers
                            for (RequestHandler rh: requestHandlers){
                                rh.setResponse(RATE_LIMITED_JSON);
                            }
                        }
                        else{
                            // Just send back the whole response to each handler
                            for (RequestHandler rh: requestHandlers){
                                rh.setResponse(market_prices);
                                rh_success += 1;
                                markets_success += ((ArrayList<String>) rh.request).size();
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    continue;
                } catch (IOException | URISyntaxException e){
                    e.printStackTrace();
                }
            }
            log.info("Ending Smarkets request sender.");
        }
    }


    public static BigDecimal[] priceArray2DecOddsArray(int[] int_array){
        BigDecimal[] bd_array = new BigDecimal[int_array.length];
        for (int i=0; i<int_array.length; i++){
            bd_array[i] = price2DecOdds(int_array[i]);
        }
        return bd_array;
    }


    @Override
    public void login() throws IOException, URISyntaxException, InterruptedException, ParseException {

        requester.setHeader("Authorization", getSessionToken());
        updateAccountInfo();
        log.info(String.format("Successfully logged into Smarkets. Balance: %s  Exposure: %s",
                balance.toString(), exposure.toString()));
    }


    @Override
    public void updateAccountInfo() throws InterruptedException, IOException, URISyntaxException {
        JSONObject response = (JSONObject) requester.get(baseurl + "accounts/");
        JSONObject account = (JSONObject) response.get("account");

        setBalance(new BigDecimal((String) account.get("available_balance")));
        exposure = new BigDecimal((String) account.get("exposure")).multiply(new BigDecimal(-1));
    }


    @Override
    public String getSessionToken() throws IOException, URISyntaxException, ParseException {

        Map<String, String> credentials = getJSON(ssldir + "/smarkets-login.json");

        JSONObject body = new JSONObject();
        body.put("username", credentials.get("u"));
        body.put("password", credentials.get("p"));
        body.put("remember", false);

        JSONObject response = (JSONObject) requester.post(baseurl + "sessions/", body);
        String token = null;

        if (response != null && response.containsKey("token")){
            token = (String) response.get("token");
            return token;
        }

        throw new IOException("Failed to get new token for smarkets");
    }


    @Override
    public BigDecimal winCommissionRate() {
        return base_commission_rate;
    }

    @Override
    public BigDecimal lossCommissionRate() {
        return base_commission_rate;
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
        return smarkets_min_back_stake;
    }

    @Override
    public BigDecimal minLayersStake(BigDecimal odds) {
        return smarkets_min_lay_stake;
    }


    @Override
    public void safe_exit() {
        exit_flag = true;
        if (priceQuotesRequestHandler != null) {
            priceQuotesRequestHandler.safe_exit();
        }
    }


    @Override
    public SiteEventTracker getEventTracker() {
        return new SmarketsEventTracker(this);
    }



    @Override
    public ArrayList<FootballMatch> getFootballMatches(Instant from, Instant until) throws IOException,
            URISyntaxException, InterruptedException {

        JSONArray events = getEvents(from, until, FOOTBALL);

        // Create list of Football Event objects to send back
        ArrayList<FootballMatch> footballMatches = new ArrayList<>();
        for (Object event_obj: events){
            JSONObject event = (JSONObject) event_obj;

            Instant time = Instant.parse(((String) event.get("start_datetime")));
            String name = (String) event.get("name");
            String[] teams = name.split(" vs.? ");
            if (teams.length != 2){
                log.warning(String.format("Cannot parse football event name '%s' in smarkets.", name));
                continue;
            }

            FootballMatch fm = new FootballMatch(time, new FootballTeam(teams[0]), new FootballTeam(teams[1]));
            fm.metadata.put(SMARKETS_EVENT_ID, (String) event.get("id"));
            footballMatches.add(fm);
        }

        return footballMatches;
    }





    public JSONArray getEvents(Instant from, Instant until, String sport) throws InterruptedException,
            IOException, URISyntaxException {

        // Create parameters for http request and send it
        Map<String, Object> params = new HashMap();
        params.put("start_datetime_min", from.toString());
        params.put("start_datetime_max", until.toString());
        params.put("limit", "1000");
        params.put("type_domain", sport);
        params.put("type_scope", "single_event");
        params.put("with_new_type", true);
        JSONObject response = (JSONObject) requester.get(baseurl + "events/", params);
        if (!response.containsKey("events")) {
            String msg = String.format("No 'events' field found in smarkets response.\n%s", jstring(response));
            throw new IOException(msg);
        }

        JSONArray events = (JSONArray) response.get("events");
        return events;
    }


    public JSONArray getMarkets(String event_id) throws
            IOException, URISyntaxException, InterruptedException {

        JSONObject r = (JSONObject) requester.get(String.format("%sevents/%s/markets/", baseurl, event_id));
        if (!r.containsKey("markets")){
            String msg = String.format("No 'markets' field found in response when looking for " +
                            "markets in smarkets.\n%s",
                    jstring(r));
            log.warning(msg);
            throw new IOException(msg);
        }

        JSONArray markets = (JSONArray) r.get("markets");
        return markets;
    }


    public JSONArray getContracts(ArrayList<String> market_ids) throws InterruptedException, IOException,
            URISyntaxException {

        if (market_ids.size() <= 0){
            return new JSONArray();
        }

        String market_ids_list = "";
        for (int i=0; i<market_ids.size(); i++){
            market_ids_list += market_ids.get(i);
            if (i < market_ids.size()-1){
                market_ids_list += ",";
            }
        }

        return getContracts(market_ids_list);
    }


    public JSONArray getContracts(String market_ids) throws InterruptedException, IOException,
            URISyntaxException {

        JSONObject response = (JSONObject) requester.get(String.format("%smarkets/%s/contracts/",
                baseurl, market_ids));

        if (!response.containsKey("contracts")){
            String msg = String.format("contracts field not found in smarkets response.\n%s", jstring(response));
            log.warning(msg);
            throw new IOException(msg);
        }

        JSONArray contracts = (JSONArray) response.get("contracts");
        return contracts;
    }


    public JSONObject _getPrices(List<String> market_ids) throws InterruptedException, IOException,
            URISyntaxException {

        if (market_ids.size() <= 0){
            return new JSONObject();
        }


        String market_ids_list = "";
        for (int i=0; i<market_ids.size(); i++){
            market_ids_list += market_ids.get(i);
            if (i < market_ids.size()-1){
                market_ids_list += ",";
            }
        }

        return _getPrices(market_ids_list);
    }



    public JSONObject _getPrices(String market_ids) throws InterruptedException, IOException,
            URISyntaxException {

        String url = String.format("%smarkets/%s/quotes/", baseurl, market_ids);
        JSONObject response = (JSONObject) requester.get(url);
        return response;
    }


    public JSONObject getPrices(Collection<String> market_ids) throws InterruptedException {
        if (priceQuotesRequestHandler == null){
            priceQuotesRequestHandler = new PriceQuotesRequestHandler();
            priceQuotesRequestHandler.start();
        }

        RequestHandler rh = new RequestHandler();
        rh.request = market_ids;
        priceQuotesRequestHandler.requestQueue.put(rh);
        JSONObject response = (JSONObject) rh.getResponse();
        return response;
    }


    public static BigDecimal decOdds2Price(BigDecimal odds){
        // returns exact price value of Decimal odds input (may not be valid price)

        if (odds == null){
            return null;
        }
        if (odds.signum() == 0){
            return BigDecimal.ZERO;
        }

        return BigDecimal.ONE.divide(odds, 12, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(10000));
    }


    public static BigDecimal price2DecOdds(Integer price){
        if (price == null){
            return null;
        }
        return price2DecOdds(new BigDecimal(price));
    }


    public static BigDecimal price2DecOdds(BigDecimal price){
        if (price == null){
            return null;
        }

        return new BigDecimal(10000).divide(price, 12, RoundingMode.HALF_UP);
    }


    public static long backStake2Quantity(BigDecimal back_stake, BigDecimal odds){
        return back_stake.multiply(odds)
                .multiply(new BigDecimal(10000))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
    }


    public static BigDecimal quantity2BackStake(long quantity, long price){

        if (price < 1 || price > 9999){
            log.severe(String.format("Trying to convert quantity with invalid price %s.", price));
            return null;
        }

        BigDecimal quant_x_price = new BigDecimal(quantity * price);
        return quant_x_price.divide(new BigDecimal(100000000), 12, RoundingMode.HALF_UP);
    }

    public static BigDecimal quantity2LayStake(long quantity, long price){
        // Price is out of 100.00 %. To find Lay price from regular price just find remainder.

        if (price < 1 || price > 9999){
            log.severe(String.format("Trying to convert quantity with invalid price %s.", price));
            return null;
        }

        long opposite_price = 10000 - price;
        return quantity2BackStake(quantity, opposite_price);
    }

    public static BigDecimal quantity2Cash(long quantity){
        return new BigDecimal(quantity).divide(new BigDecimal(10000), 12, RoundingMode.HALF_UP);
    }



    @Override
    public BigDecimal getValidOdds(BigDecimal odds, RoundingMode roundingMode) {

        if (odds.compareTo(minValidOdds()) < 0 || odds.compareTo(maxValidOdds()) > 0){
            log.severe(String.format("Could not return valid SM odds for input %s, outside valid range %s-%s",
                    BDString(odds), BDString(minValidOdds()), BDString(maxValidOdds())));
            return null;
        }

        // Convert to smarkets price and find closest valid prices above and below
        // on odd ladder
        BigDecimal exact_price = decOdds2Price(odds);
        Integer[] below_above_prices = aboveBelowTargetValues(prices_enum, exact_price);
        Integer next_lower_price = below_above_prices[0];
        Integer next_higher_price = below_above_prices[1];


        // Convert closest prices to equiv decimal odds
        // Price and DecOdds FLIP directions in terms of abs value and chance. 1.0001 = 99.99%
        BigDecimal next_lower_decOdds = price2DecOdds(next_higher_price);
        BigDecimal next_higher_decOdds = price2DecOdds(next_lower_price);

        return getClosest(next_lower_decOdds, next_higher_decOdds, odds, roundingMode);
    }


    @Override
    public BigDecimal minValidOdds() {
        return decOdds_enum[decOdds_enum.length - 1];
    }

    @Override
    public BigDecimal maxValidOdds() {
        return decOdds_enum[0];
    }


    public JSONObject placeOrder(JSONObject payload) throws IOException, URISyntaxException {
        JSONObject response = (JSONObject) requester.post(baseurl + "orders/",
                payload,
                Arrays.asList(200, 400, 403, 429, 500));
        return response;
    }


    public PlacedBet placeOrder(BetOrder betOrder, BigDecimal odds_buffer_ratio) throws IOException, URISyntaxException {

        // Convert betorder to json payload
        JSONObject payload = betOrder2Payload(betOrder, odds_buffer_ratio);

        // Send off payload to smarkets
        Instant time_sent = Instant.now();
        JSONObject response = placeOrder(payload);

        // Convert response to Placedbet
        PlacedBet placedBet = orderResp2PlacedBet(response);
        placedBet.raw_request = payload;
        placedBet.time_sent = time_sent;
        placedBet.betOrder = betOrder;

        return placedBet;
    }


    public class PlaceBetRunnable implements Runnable{

        // Runs the process of sending off a bet and

        public BetOrder betOrder;

        public PlacedBet placedBet;
        public Exception exception;
        public Thread thread;
        public BigDecimal odds_buffer_ratio;


        public PlaceBetRunnable(BetOrder betOrder, BigDecimal odds_buffer_ratio){
            this.betOrder = betOrder;
            this.odds_buffer_ratio = odds_buffer_ratio;
            exception = null;
            placedBet = null;
            thread = new Thread(this);
            thread.setName("Smrkts-PlcBtR");
        }

        public void start(){
            thread.start();
        }

        @Override
        public void run() {
            try {
                placedBet = placeOrder(betOrder, odds_buffer_ratio);
            } catch (Exception e) {
                e.printStackTrace();
                log.severe("Exception while placebetrunnable for smarkets bet - " + e.toString());
                exception = e;
            }
        }
    }


    public static long[] splitBackLayQuantities(long total_quantity, long price){
        // Price is back percentage out of 100.00 % as an integer
        // lay perc is opposite

        BigDecimal bd_total_quantity = new BigDecimal(total_quantity);
        BigDecimal bd_price = new BigDecimal(price);
        BigDecimal back_ratio = bd_price.divide(new BigDecimal(10000), 12, RoundingMode.HALF_UP);

        long back_quantity = bd_total_quantity.multiply(back_ratio)
                .setScale(0, RoundingMode.HALF_UP).longValue();
        long lay_quantity = total_quantity - back_quantity;

        return new long[] {back_quantity, lay_quantity};


    }


    public static JSONObject orderPayload(BetType betType, String contract_id, String market_id, BigDecimal odds,
                                   long quantity){

        String side = "buy";
        if (betType == BetType.LAY){
            side = "sell";
        }

        JSONObject payload = new JSONObject();
        payload.put("contract_id", contract_id);
        payload.put("market_id", market_id);
        payload.put("price", decOdds2Price(odds));
        payload.put("quantity", quantity);
        payload.put("side", side);
        payload.put("type", "immediate_or_cancel");

        return payload;
    }


    public static BetType side2BetType(String side){
        side = side.toLowerCase();
        if (side.equals("buy")){
            return BetType.BACK;
        }
        else if (side.equals("sell")){
            return BetType.LAY;
        }
        else{
            log.severe(String.format("Smarkets returned invalid side '%s'.", side));
            return null;
        }
    }


    public static String betType2Side(BetType betType){
        if (betType == BetType.BACK){
            return "buy";
        }
        else if (betType == BetType.LAY){
            return "sell";
        }
        return null;
    }



    public List<PlacedBet> placeBets(List<BetOrder> betOrders, BigDecimal odds_buffer_ratio)
            throws IOException, URISyntaxException {

        // Smarkets bets are 1 per request, so send them all concurrently

        // Create payload and runnable for each betOrder
        ArrayList<PlaceBetRunnable> placeBetRunnables = new ArrayList<>();
        for (BetOrder betOrder: betOrders) {
            PlaceBetRunnable pbr = new PlaceBetRunnable(betOrder, odds_buffer_ratio);
            pbr.start();
            placeBetRunnables.add(pbr);
        }

        // Gather responses
        ArrayList<PlacedBet> placedBets = new ArrayList<>();
        for (PlaceBetRunnable placeBetRunnable: placeBetRunnables){

            try {
                placeBetRunnable.thread.join();
                PlacedBet pb = placeBetRunnable.placedBet;
                placedBets.add(pb);
            }
            catch (InterruptedException e) {
                log.severe("Error getting response for smarkets bet part.");
                e.printStackTrace();
            }
        }

        return placedBets;
    }


    public static JSONObject betOrder2Payload(BetOrder betOrder, BigDecimal odds_buffer_ratio){

        String contract_id = betOrder.betExchange.getMetadata(Smarkets.CONTRACT_ID);
        String market_id = betOrder.betExchange.getMetadata(Smarkets.MARKET_ID);
        if (contract_id == null || market_id == null){
            log.severe(String.format("invalid betoffer metadata for smarkets: contract_id=%s  market_id=%s",
                    stringValue(contract_id), stringValue(market_id)));
            return null;
        }

        // Buffer odds value to ratio either side of bet to a valid smarkets size
        BigDecimal valid_buffered_odds = betOrder.getValidOddsWithBuffer(odds_buffer_ratio);
        int valid_buffered_price = decOdds2Price(valid_buffered_odds).setScale(0, RoundingMode.HALF_UP).intValue();

        // Use the original odds to calculate smarkets quantity
        long quantity = backStake2Quantity(betOrder.getBackersStake(), betOrder.getOdds());

        JSONObject payload = new JSONObject();
        payload.put("contract_id", contract_id);
        payload.put("market_id", market_id);
        payload.put("label", betOrder.getID());
        payload.put("side", betType2Side(betOrder.betType()));
        payload.put("price", valid_buffered_price);
        payload.put("quantity", quantity);
        payload.put("minimum_accepted_quantity", quantity);
        payload.put("type", "immediate_or_cancel");

        return payload;
    }


    public PlacedBet orderResp2PlacedBet(JSONObject order_resp){
        if (order_resp == null){
            return PlacedBet.failedBet("Null response JSON");
        }

        PlacedBet pb = new PlacedBet();
        pb.raw_response = order_resp;
        pb.setSite(this);

        if (order_resp.containsKey("error_type")){
            String msg = String.format("Smarkets error: %s", order_resp.get("error_type").toString());
            if (order_resp.containsKey("data")){
                msg += String.format(" data: %s", stringValue(order_resp.get("data")));
            }
            pb.setFail(msg);
        }
        else if (!order_resp.get("quantity").equals(order_resp.get("total_executed_quantity"))){
            pb.setFail(String.format("Smarkets error: Only matched %s of %s",
                    order_resp.get("total_executed_quantity").toString(),
                    order_resp.get("quantity").toString()));
        }
        else {
            int avg_price = ((Long) order_resp.get("executed_avg_price")).intValue();
            long total_quantity = (long) order_resp.get("total_executed_quantity");
            long[] backLayQuantities = splitBackLayQuantities(total_quantity, avg_price);
            long back_quantity = backLayQuantities[0];
            long lay_quantity = backLayQuantities[1];

            pb.setSuccess();
            pb.bet_type = side2BetType(order_resp.get("side").toString());
            pb.set_backersStake_layersProfit(quantity2Cash(back_quantity));
            pb.set_backersProfit_layersStake(quantity2Cash(lay_quantity));
            pb.avg_odds = price2DecOdds(avg_price);
            pb.bet_id = order_resp.get("order_id").toString();
        }

        return pb;
    }



    public void testBet() throws IOException, URISyntaxException {
        String type = "BACK";
        String contract = "34625528";
        String market = "9920510";
        String odds = "1.55";
        String stake = "0.55";


        JSONObject payload = orderPayload(
                BetType.valueOf(type),
                contract,
                market,
                new BigDecimal(odds),
                backStake2Quantity(new BigDecimal(stake), new BigDecimal(odds)));

        Instant time_sent = Instant.now();
        JSONObject response = placeOrder(payload);
        pp(response);

        PlacedBet pb = orderResp2PlacedBet(response);
        pb.time_sent = time_sent;
        pp(pb.toJSON());
    }


    public static void main(String[] args){

        try{

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}
