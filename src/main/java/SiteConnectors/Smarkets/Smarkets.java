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

import static tools.printer.*;

public class Smarkets extends BettingSite {

    public final static String name = "smarkets";
    public final static String id = "SM";

    public static String baseurl = "https://api.smarkets.com/v3/";
    public static String FOOTBALL = "football";
    public static String CONTRACT_ID = "CONTRACT_ID";
    public static String MARKET_ID = "MARKET_ID";
    public static String SMARKETS_PRICE = "SMARKETS_PRICE";
    public static String SMARKETS_EVENT_ID = "SMARKETS_EVENT_ID";
    public final static String RATE_LIMITED = "RATE_LIMITED";
    public JSONObject RATE_LIMITED_JSON;

    public static int[] prices_enum = new int[] {1, 10, 20, 33, 34, 36, 37, 38, 40, 42, 43, 45, 48, 50, 53, 56, 59, 62, 67, 71, 77, 83, 91, 100, 105, 111, 118, 125, 133, 143, 154, 167, 182, 200, 208, 217, 227, 238, 250, 263, 278, 294, 312, 333, 345, 357, 370, 385, 400, 417, 435, 455, 476, 500, 513, 526, 541, 556, 571, 588, 606, 625, 645, 667, 690, 714, 741, 769, 800, 833, 870, 909, 952, 1000, 1020, 1042, 1064, 1087, 1111, 1136, 1163, 1190, 1220, 1250, 1282, 1316, 1351, 1389, 1429, 1471, 1515, 1562, 1613, 1667, 1695, 1724, 1754, 1786, 1818, 1852, 1887, 1923, 1961, 2000, 2041, 2083, 2128, 2174, 2222, 2273, 2326, 2381, 2439, 2500, 2532, 2564, 2597, 2632, 2667, 2703, 2740, 2778, 2817, 2857, 2899, 2941, 2985, 3030, 3077, 3125, 3175, 3226, 3279, 3333, 3356, 3378, 3401, 3425, 3448, 3472, 3497, 3521, 3546, 3571, 3597, 3623, 3650, 3676, 3704, 3731, 3759, 3788, 3817, 3846, 3876, 3906, 3937, 3968, 4000, 4032, 4065, 4098, 4132, 4167, 4202, 4237, 4274, 4310, 4348, 4386, 4425, 4464, 4505, 4545, 4587, 4630, 4673, 4717, 4762, 4808, 4854, 4902, 4950, 5000, 5025, 5051, 5076, 5102, 5128, 5155, 5181, 5208, 5236, 5263, 5291, 5319, 5348, 5376, 5405, 5435, 5464, 5495, 5525, 5556, 5587, 5618, 5650, 5682, 5714, 5747, 5780, 5814, 5848, 5882, 5917, 5952, 5988, 6024, 6061, 6098, 6135, 6173, 6211, 6250, 6289, 6329, 6369, 6410, 6452, 6494, 6536, 6579, 6623, 6667, 6711, 6757, 6803, 6849, 6897, 6944, 6993, 7042, 7092, 7143, 7194, 7246, 7299, 7353, 7407, 7463, 7519, 7576, 7634, 7692, 7752, 7812, 7874, 7937, 8000, 8065, 8130, 8197, 8264, 8333, 8403, 8475, 8547, 8621, 8696, 8772, 8850, 8929, 9009, 9091, 9174, 9259, 9346, 9434, 9524, 9615, 9709, 9804, 9901, 9999};

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
        priceQuotesRequestHandler.safe_exit();
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



    @Override
    public BigDecimal ROI(BetOffer bet_offer, BigDecimal investment, boolean real){
        // Default ROI, commission on profits only

        return ROI(bet_offer.bet.getType(), bet_offer.odds, bet_offer.site.winCommissionRate(), investment, real);
    }


    public static BigDecimal ROI(BetType betType, BigDecimal odds, BigDecimal commission_rate, BigDecimal investment,
                                 boolean real){
        // (From smarkets support email on commission)
        // For back bets that is the back stake if the bet loses or the profit if the bet wins.
        // For lay bets it's the liability if the bet loses or the lay stake if the bet wins
        //
        // Pro Tier commission band where 1% commission is charged on winnings or losses per
        // each individual bet that settles, and order rate is limited to 1 bet/s

        BigDecimal roi = null;

        // BACK
        if (betType == BetType.BACK){
            BigDecimal ratio = BigDecimal.ONE.add(commission_rate);
            //print("ratio: " + ratio.toString());
            BigDecimal loss_commission_multiplier = commission_rate.divide(ratio, 20, RoundingMode.HALF_UP);
            //print("loss comm mult: " + loss_commission_multiplier.toString());
            BigDecimal loss_commission = investment.multiply(loss_commission_multiplier);
            //print("loss_com: " + loss_commission.toString());
            BigDecimal backers_stake = investment.subtract(loss_commission);
            //print("back_stke: " + backers_stake.toString());
            BigDecimal backers_profit = Bet.backStake2LayStake(backers_stake, odds);
            //print("back_prof: " + backers_profit.toString());
            BigDecimal win_commission = backers_profit.multiply(commission_rate);
            //print("win_com: " + win_commission.toString());
            roi = investment.add(backers_profit).subtract(win_commission);
        }

        // LAY
        else{
            BigDecimal ratio = BigDecimal.ONE.add(commission_rate);
            //print("ratio: " + ratio.toString());
            BigDecimal loss_commission_multiplier = commission_rate.divide(ratio, 20, RoundingMode.HALF_UP);
            //print("loss comm mult: " + loss_commission_multiplier.toString());
            BigDecimal loss_commission = investment.multiply(loss_commission_multiplier);
            //print("loss_com: " + loss_commission.toString());
            BigDecimal layers_stake = investment.subtract(loss_commission);
            //print("lay_stake: " + layers_stake.toString());
            BigDecimal layers_profit = Bet.layStake2backStake(layers_stake, odds);
            //print("layers_profit: " + layers_profit.toString());
            BigDecimal win_commission = layers_profit.multiply(commission_rate);
            //print("win_com: " + win_commission.toString());
            roi = investment.add(layers_profit).subtract(win_commission);
        }

        // Round to nearest penny if 'real' value;
        if (real){
            roi = roi.setScale(2, RoundingMode.HALF_UP);
        }
        return roi;
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


    public static long dec2price(BigDecimal odds){
        return BigDecimal.ONE.divide(odds, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(10000)).setScale(0, RoundingMode.HALF_UP)
                .longValue();
    }


    public static BigDecimal price2dec(long price){
        return new BigDecimal(10000).divide(new BigDecimal(price), 20, RoundingMode.HALF_UP);
    }


    public static long size2quantity(BigDecimal size, BigDecimal odds){
        return size.multiply(odds).multiply(new BigDecimal(10000))
                .setScale(0, RoundingMode.HALF_UP).intValue();
    }


    public static BigDecimal quantity2size(long quantity, long price){
        return new BigDecimal(quantity * price)
                .divide(new BigDecimal(100000000), 2, RoundingMode.DOWN);
    }


    public static BigDecimal dec2perc(BigDecimal odds){
        return BigDecimal.ONE.divide(odds, 20, RoundingMode.HALF_UP);
    }


    public static BigDecimal smarketsDecimal(BigDecimal decimal_odds){
        // (From https://help.smarkets.com/hc/en-gb/articles/212079549-Why-haven-t-I-been-paid-the-correct-return-)
        // Our underlying system matches bets between customers in percentage prices.
        // If a user is displaying the odds in decimal form, we round the percentage
        // prices to two decimal places, as displayed on the Exchange. So although
        // you’re backing at 1.65, the percentage price would in fact be 60.61%

        // Convert to decimal to 4dp (percentage + 2dp)
        BigDecimal percentage_odds = dec2perc(decimal_odds).setScale(4, RoundingMode.HALF_UP);

        print(percentage_odds.toString());

        // Convert the rounded percentage back to exact decimal
        return BigDecimal.ONE.divide(percentage_odds, 20, RoundingMode.HALF_UP);
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


    private static long validPrice(long price) {
        if (price <= prices_enum[0]){
            log.warning("Smarkets converting price to valid price found it to be higher than highest.");
            return prices_enum[0];
        }
        for (int i=0; i<prices_enum.length-1; i++){
            long below = prices_enum[i];
            long above = prices_enum[i+1];

            if (price <= above){
                long distance_to_below = price - below;
                long distance_to_above = above - price;

                if (distance_to_below < distance_to_above){
                    return below;
                }
                else{
                    return above;
                }
            }
        }
        log.warning("Smarkets converting price to valid price found it to be lower than lowest.");
        return prices_enum[prices_enum.length-1];
    }


    public JSONObject placeOrder(JSONObject payload) throws IOException, URISyntaxException {
        JSONObject response = (JSONObject) requester.post(baseurl + "orders/", payload, true);
        return response;
    }


    public class PlaceBetRunnable implements Runnable{

        public JSONObject payload;
        public JSONObject response;
        public Exception exception;
        public Thread thread;
        public BetOrder betOrder;

        public Instant time_sent;
        public Instant time_response;

        public PlaceBetRunnable(BetOrder betOrder, JSONObject payload){
            this.betOrder = betOrder;
            this.payload = payload;
            thread = new Thread(this);
        }

        public void start(){
            thread.start();
        }

        @Override
        public void run() {
            time_sent = Instant.now();
            try {
                response = (JSONObject) requester.post(baseurl + "orders/", payload, true);
                time_response = Instant.now();
            } catch (IOException | URISyntaxException e) {
                log.severe("IO or URI exception when placing smarket part bet.");
                response = null;
                exception = e;
            }
        }

        public Instant server_time_estimate(){
            return time_sent.plusMillis((time_response.toEpochMilli() - time_sent.toEpochMilli()) / 2);
        }
    }


    public static JSONArray pbrs2JSONp(ArrayList<PlaceBetRunnable> pbrs){
        JSONArray j = new JSONArray();
        for (PlaceBetRunnable pbr: pbrs){
            j.add(pbr.payload);
        }
        return j;
    }


    public static JSONArray pbrs2JSONr(ArrayList<PlaceBetRunnable> pbrs){
        JSONArray j = new JSONArray();
        for (PlaceBetRunnable pbr: pbrs){
            j.add(pbr.response);
        }
        return j;
    }


    public void testplacebets(String contract_id, String market_id, BigDecimal odds, BigDecimal back_stake,
                              String side){

        JSONObject payload = new JSONObject();
        payload.put("contract_id", contract_id);
        payload.put("market_id", market_id);
        payload.put("price", validPrice(dec2price(odds)));
        payload.put("quantity", size2quantity(back_stake, odds));
        payload.put("side", side);
        payload.put("type", "immediate_or_cancel");

        JSONObject response = null;
        try {
            response = (JSONObject) requester.post(baseurl + "orders/", payload, true);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        pp(response);
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
        payload.put("price", validPrice(dec2price(odds)));
        payload.put("quantity", quantity);
        payload.put("side", side);
        payload.put("type", "immediate_or_cancel");

        return payload;
    }



    public List<PlacedBet> placeBets(List<BetOrder> betOrders, BigDecimal MIN_ODDS_RATIO)
            throws IOException, URISyntaxException {

        ArrayList<PlaceBetRunnable> placeBetRunnables = new ArrayList<>();
        for (BetOrder betOrder: betOrders) {

            // Change 'side' and odds multiplier depending on type of bet (back or lay)
            BigDecimal odds = betOrder.bet_offer.odds;
            if (betOrder.isBack()){
                odds = odds.subtract(BigDecimal.ONE).multiply(MIN_ODDS_RATIO).add(BigDecimal.ONE);
            }
            else{
                odds = odds.subtract(BigDecimal.ONE).divide(MIN_ODDS_RATIO, 20, RoundingMode.HALF_UP).add(BigDecimal.ONE);
            }


            JSONObject payload = orderPayload(betOrder.betType(),
                    betOrder.bet_offer.metadata.get(Smarkets.CONTRACT_ID),
                    betOrder.bet_offer.metadata.get(Smarkets.MARKET_ID),
                    odds,
                    size2quantity(betOrder.getBackersStake(), betOrder.bet_offer.odds));


            // Create thread to run this single request and add runnable to list.
            PlaceBetRunnable placeBetRunnable = new PlaceBetRunnable(betOrder, payload);
            placeBetRunnable.thread.setName("Smarkets Bet Placer");
            placeBetRunnable.start();
            placeBetRunnables.add(placeBetRunnable);

            betOrder.site_json_request = payload;
        }

        // Gather responses;
        for (PlaceBetRunnable placeBetRunnable: placeBetRunnables){
            JSONObject response = null;
            try {
                placeBetRunnable.thread.join();
            } catch (InterruptedException e) {
                log.severe("Error getting response for smarkets bet part.");
                e.printStackTrace();
            }
        }

        ArrayList<PlacedBet> placedBets = new ArrayList<>();
        for (PlaceBetRunnable placeBetRunnable: placeBetRunnables) {
            PlacedBet pb = null;
            BetOrder betOrder = placeBetRunnable.betOrder;
            JSONObject response = placeBetRunnable.response;

            if (response == null) {
                log.severe(String.format("Failed to placed %s on bet %s in smarkets. Response null.",
                        betOrder.investment.toString(), betOrder.bet_offer.bet.id()));
                pb = null;
            }
            else if (response.containsKey("error_type")) {
                String error = (String) response.get("error_type");

                log.severe(String.format("Failed to place %s @ %s on bet %s in smarkets. '%s'.",
                        betOrder.investment.toString(), betOrder.odds().toString(),
                        betOrder.bet_offer.bet.id(), error));
                pb = null;
            }
            else {
                String bet_id = (String) response.get("order_id");
                long price = (long) response.get("executed_avg_price");
                long quantity = (long) response.get("total_executed_quantity");
                BigDecimal odds = price2dec(price);
                BigDecimal backers_stake = quantity2size(quantity, price);
                BigDecimal investment;
                if (betOrder.isBack()){
                    investment = investmentNeededForStake(backers_stake);
                }
                else {
                    investment = investmentNeededForStake(Bet.backStake2LayStake(backers_stake, odds));
                }
                BigDecimal returns = this.ROI(betOrder.bet_offer.newOdds(odds), investment, true);

                log.info(String.format("Successfully invested £%s @ %s on %s '%s' in smarkets (returns %s).",
                        investment.toString(), odds.toString(), betOrder.bet_offer.bet.id(),
                        betOrder.match().name, returns.toString()));

                pb = null;
            }
            pb.site_json_response = jsonConverter(response);
            placedBets.add(pb);
        }

        try {
            updateAccountInfo();
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.severe(e.toString());
        }
        return placedBets;
    }


    public static JSONObject jsonConverter(JSONObject json){

        if (json.containsKey("orig_price")) {
            long orig_price = (long) json.get("orig_price");
            json.put("orig_price_dec", price2dec(orig_price).toString());
        }

        if (json.containsKey("price")) {
            long price = (long) json.get("price");
            json.put("price_dec", price2dec(price).toString());

            if (json.containsKey("quantity")) {
                long quantity = (long) json.get("quantity");
                json.put("quantity_dec", quantity2size(quantity, price).toString());
            }
        }

        if (json.containsKey("executed_avg_price")) {
            long executed_avg_price = (long) json.get("executed_avg_price");
            json.put("executed_avg_price_dec", price2dec(executed_avg_price).toString());

            if (json.containsKey("total_executed_quantity")) {
                long total_executed_quantity = (long) json.get("total_executed_quantity");
                json.put("total_executed_quantity_dec", quantity2size(total_executed_quantity, executed_avg_price).toString());
            }
        }

        return json;
    }


    public void betTest() throws IOException, URISyntaxException {
        JSONObject response = placeOrder(orderPayload(BetType.LAY,
                "34615938",
                "9918245",
                new BigDecimal("2.7"),
                size2quantity(new BigDecimal("0.60"), new BigDecimal("2.70"))));

        pp(response);
    }


    public static void main(String[] args){

        try{
            Smarkets s = new Smarkets();
            FootballMatch fm = FootballMatch.parse("2020-05-28T18:30:00.0Z", "VfB Stuttgart v Hamburger SV");
            SmarketsEventTracker set = (SmarketsEventTracker) s.getEventTracker();
            set.setupMatch(fm);

            MarketOddsReport mor = set._getMarketOddsReport(new ArrayList<Bet>(FootballBetGenerator._getAllBets()));
            toFile(mor.toJSON());

            s.betTest();

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}
