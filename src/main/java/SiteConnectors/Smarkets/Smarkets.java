package SiteConnectors.Smarkets;

import Bet.Bet;
import Bet.BetOffer;
import Bet.BetOrder;
import Bet.FootballBet.FootballResultBet;
import Bet.FootballBet.FootballScoreBet;
import Bet.PlacedBet;
import SiteConnectors.Betfair.Betfair;
import SiteConnectors.BettingSite;
import SiteConnectors.RequestHandler;
import SiteConnectors.SiteEventTracker;
import Sport.FootballMatch;
import Sport.Team;
import Trader.EventTrader;
import org.apache.axis2.databinding.types.xsd.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import tools.Requester;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static tools.printer.*;

public class Smarkets extends BettingSite {

    public static String baseurl = "https://api.smarkets.com/v3/";
    public static String FOOTBALL = "football_match";
    public static String CONTRACT_ID = "CONTRACT_ID";
    public static String MARKET_ID = "MARKET_ID";
    public static String SMARKETS_PRICE = "SMARKETS_PRICE";

    public static BigDecimal commission_rate = new BigDecimal("0.01");
    public static BigDecimal min_back_stake = new BigDecimal("0.05");
    public static int[] prices_enum = new int[] {1, 10, 20, 33, 34, 36, 37, 38, 40, 42, 43, 45, 48, 50, 53, 56, 59, 62, 67, 71, 77, 83, 91, 100, 105, 111, 118, 125, 133, 143, 154, 167, 182, 200, 208, 217, 227, 238, 250, 263, 278, 294, 312, 333, 345, 357, 370, 385, 400, 417, 435, 455, 476, 500, 513, 526, 541, 556, 571, 588, 606, 625, 645, 667, 690, 714, 741, 769, 800, 833, 870, 909, 952, 1000, 1020, 1042, 1064, 1087, 1111, 1136, 1163, 1190, 1220, 1250, 1282, 1316, 1351, 1389, 1429, 1471, 1515, 1562, 1613, 1667, 1695, 1724, 1754, 1786, 1818, 1852, 1887, 1923, 1961, 2000, 2041, 2083, 2128, 2174, 2222, 2273, 2326, 2381, 2439, 2500, 2532, 2564, 2597, 2632, 2667, 2703, 2740, 2778, 2817, 2857, 2899, 2941, 2985, 3030, 3077, 3125, 3175, 3226, 3279, 3333, 3356, 3378, 3401, 3425, 3448, 3472, 3497, 3521, 3546, 3571, 3597, 3623, 3650, 3676, 3704, 3731, 3759, 3788, 3817, 3846, 3876, 3906, 3937, 3968, 4000, 4032, 4065, 4098, 4132, 4167, 4202, 4237, 4274, 4310, 4348, 4386, 4425, 4464, 4505, 4545, 4587, 4630, 4673, 4717, 4762, 4808, 4854, 4902, 4950, 5000, 5025, 5051, 5076, 5102, 5128, 5155, 5181, 5208, 5236, 5263, 5291, 5319, 5348, 5376, 5405, 5435, 5464, 5495, 5525, 5556, 5587, 5618, 5650, 5682, 5714, 5747, 5780, 5814, 5848, 5882, 5917, 5952, 5988, 6024, 6061, 6098, 6135, 6173, 6211, 6250, 6289, 6329, 6369, 6410, 6452, 6494, 6536, 6579, 6623, 6667, 6711, 6757, 6803, 6849, 6897, 6944, 6993, 7042, 7092, 7143, 7194, 7246, 7299, 7353, 7407, 7463, 7519, 7576, 7634, 7692, 7752, 7812, 7874, 7937, 8000, 8065, 8130, 8197, 8264, 8333, 8403, 8475, 8547, 8621, 8696, 8772, 8850, 8929, 9009, 9091, 9174, 9259, 9346, 9434, 9524, 9615, 9709, 9804, 9901, 9999};

    public Instant lastPriceQuoteRequest;
    public long rate_limit = 301;

    BlockingQueue<RequestHandler> priceQuotesRequestHandlerQueue;
    PriceQuotesRequestHandler priceQuotesRequestHandler;



    public Smarkets() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, URISyntaxException, IOException, InterruptedException {

        name = "smarkets";
        requester = new Requester();

        login();

        // Setup price quotes handler
        priceQuotesRequestHandlerQueue = new LinkedBlockingQueue<>();
        priceQuotesRequestHandler = new PriceQuotesRequestHandler(priceQuotesRequestHandlerQueue);
        Thread priceQuotesRequestHandlerThread = new Thread(priceQuotesRequestHandler);
        priceQuotesRequestHandlerThread.setName("Smarkets RH");
        priceQuotesRequestHandlerThread.setDaemon(true);
        priceQuotesRequestHandlerThread.start();
    }



    public class PriceQuotesRequestHandler implements Runnable{

        public int MAX_BATCH_SIZE = 300;
        public int REQUEST_THREADS = 5;
        public long MAX_WAIT_TIME = 10;
        public long MIN_REQ_INTERVAL = 0;

        public BlockingQueue<RequestHandler> requestQueue;
        public BlockingQueue<ArrayList<RequestHandler>> workerQueue;

        public PriceQuotesRequestHandler(BlockingQueue requestQueue){
            this.requestQueue = requestQueue;
        }

        @Override
        public void run() {

            log.info(String.format("Running smarkets request handler."));

            ArrayList<RequestHandler> requestHandlers = new ArrayList<>();
            int markets_in_queue = 0;

            // Start workers
            workerQueue = new LinkedBlockingQueue<>();
            for (int i=0; i<REQUEST_THREADS; i++){
                PriceQuoteRequestSender requestSender = new PriceQuoteRequestSender(workerQueue);
                Thread t = new Thread(requestSender);
                t.start();
            }


            RequestHandler new_handler;
            Instant next_request_time = null;

            while (true) {
                try {

                    while (next_request_time == null || Instant.now().isBefore(next_request_time)){

                        new_handler = null;

                        // Wait as long as required for first handler to arrive, set request time when it does.
                        if (next_request_time == null){
                            new_handler = requestQueue.take();
                            next_request_time = Instant.now().plus(MAX_WAIT_TIME, ChronoUnit.MILLIS);
                        }
                        // Subsequent handlers, wait or break at certain time
                        else if (Instant.now().isBefore(next_request_time)) {
                            long milliseconds_to_wait = next_request_time.toEpochMilli() - Instant.now().toEpochMilli();
                            new_handler = requestQueue.poll(milliseconds_to_wait, TimeUnit.MILLISECONDS);
                        }

                        // If a new handler is passed in, decide what to do with it.
                        if (new_handler != null){
                            int num_new_markets = ((ArrayList<String>) new_handler.request).size();

                            if (markets_in_queue + num_new_markets <= MAX_BATCH_SIZE) {
                                requestHandlers.add(new_handler);
                                markets_in_queue += num_new_markets;
                            }
                            else{
                                // This request is full so break and send now.
                                break;
                            }
                        }
                    }

                    // Send off request
                    if (requestHandlers.size() > 0){
                        workerQueue.put(requestHandlers);
                    }
                    requestHandlers = new ArrayList<>();
                    markets_in_queue = 0;
                    next_request_time = null;


                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public class PriceQuoteRequestSender implements Runnable{

        public BlockingQueue<ArrayList<RequestHandler>> jobQueue;

        public int req_limit_time = 60;
        public int req_limit_requests = 200;

        public PriceQuoteRequestSender(BlockingQueue jobQueue){
            this.jobQueue = jobQueue;
        }

        @Override
        public void run() {
            ArrayList<RequestHandler> requestHandlers;
            JSONObject final_request = new JSONObject();

            int reqs_sent = 0;
            Instant first_request_time = null;
            Instant expiry_time = null;

            while (true){
                try {
                    requestHandlers = jobQueue.take();

                    if (expiry_time != null && Instant.now().isBefore(expiry_time)){
                        // Fail all these handlers
                        for (RequestHandler rh: requestHandlers){
                            rh.setFail();
                        }
                    }
                    else{

                        // list event ids from handlers to get data from.
                        ArrayList<String> market_ids = new ArrayList<>();
                        for (RequestHandler rh: requestHandlers){
                            market_ids.addAll((ArrayList<String>) rh.request);
                        }

                        // Send request
                        JSONObject market_prices = getPrices(market_ids);


                        // If rate limit has been hit
                        if (market_prices.containsKey("error_type")
                                && ((String) market_prices.get("error_type")).equals("RATE_LIMIT_EXCEEDED")){

                            // Find what time rate limit expires
                            String expiry = (String) ((JSONObject) market_prices.get("data")).get("expiry");
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("E, d MMM yyyy H:m:s VV");
                            expiry_time = ZonedDateTime.parse(expiry, dtf).toInstant()
                                    .plus(1, ChronoUnit.SECONDS);

                            // Sanity check expiry time
                            if (expiry_time.isAfter(Instant.now().plus(2, ChronoUnit.MINUTES))){
                                log.warning("Rate limit expiry for smarkets is set to %s. Using 61 seconds instead.");
                                expiry_time = Instant.now().plus(61, ChronoUnit.SECONDS);
                            }

                            // Fail all these handlers
                            for (RequestHandler rh: requestHandlers){
                                rh.setFail();
                            }
                        }
                        else{
                            // Just send back the whole response to each handler
                            for (RequestHandler rh: requestHandlers){
                                rh.setResponse(market_prices);
                            }
                        }
                    }
                } catch (InterruptedException | IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // Backup done the usual non limiting way
    public class PriceQuotesRequestHandlerFULLON implements Runnable{

        public int MAX_BATCH_SIZE = 300;
        public int REQUEST_THREADS = 3;
        public long WAIT_MILLISECONDS = 50;

        public BlockingQueue<RequestHandler> requestQueue;
        public BlockingQueue<ArrayList<RequestHandler>> workerQueue;

        public PriceQuotesRequestHandlerFULLON(BlockingQueue requestQueue){
            this.requestQueue = requestQueue;
        }

        @Override
        public void run() {

            log.info(String.format("Running smarkets request handler."));

            Instant wait_until = null;
            ArrayList<RequestHandler> requestHandlers = new ArrayList<>();
            int markets_in_queue = 0;
            RequestHandler new_handler;
            long milliseconds_to_wait;

            // Start workers
            workerQueue = new LinkedBlockingQueue<>();
            for (int i=0; i<REQUEST_THREADS; i++){
                PriceQuoteRequestSender requestSender = new PriceQuoteRequestSender(workerQueue);
                Thread t = new Thread(requestSender);
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

                    // Add new handler to list and add the number of markets it contains to counter
                    if (new_handler != null) {
                        int new_handler_size = ((ArrayList<String>) new_handler.request).size();

                        if (markets_in_queue + new_handler_size >= MAX_BATCH_SIZE){
                            workerQueue.put(requestHandlers);
                            wait_until = null;
                            requestHandlers = new ArrayList<>();
                            markets_in_queue = 0;
                        }

                        requestHandlers.add(new_handler);
                        markets_in_queue += new_handler_size;

                    }

                    if (Instant.now().isAfter(wait_until)){
                        workerQueue.put(requestHandlers);
                        wait_until = null;
                        requestHandlers = new ArrayList<>();
                        markets_in_queue = 0;
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // Backup done the usual non limiting way
    public class PriceQuoteRequestSenderFULLON implements Runnable{

        public BlockingQueue<ArrayList<RequestHandler>> jobQueue;

        public PriceQuoteRequestSenderFULLON(BlockingQueue jobQueue){
            this.jobQueue = jobQueue;
        }

        @Override
        public void run() {
            ArrayList<RequestHandler> requestHandlers;
            JSONObject final_request = new JSONObject();

            while (true){
                try {
                    requestHandlers = jobQueue.take();

                    // list event ids from handlers to get data from.
                    ArrayList<String> market_ids = new ArrayList<>();
                    for (RequestHandler rh: requestHandlers){
                        market_ids.addAll((ArrayList<String>) rh.request);
                    }

                    // Send request
                    JSONObject market_prices = getPrices(market_ids);


                    for (RequestHandler rh: requestHandlers){


                        // The below method tries to split up the answer into the parts each
                        // request asked for before sending back to each one.
                        /*
                        JSONObject response = new JSONObject();
                        for (String market_id: (ArrayList<String>) rh.request){
                            JSONObject market_price = (JSONObject) market_prices.get(market_id);
                            if (market_price == null){
                                log.severe(String.format("Could not find market_id %s in smarkets" +
                                                "response when it was asked for.\n%s",
                                        market_id, market_prices.toJSONString()));
                                p(market_prices);
                                System.exit(0);
                                continue;
                            }
                            response.put(market_id, market_price);
                        }
                        */

                        // Just send the complete response back to each request handler
                        rh.setResponse(market_prices);
                    }

                } catch (InterruptedException | IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public void login() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, IOException, URISyntaxException, InterruptedException {

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
    public String getSessionToken() throws IOException, CertificateException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, UnrecoverableKeyException, URISyntaxException {

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
    public BigDecimal commission() {
        return commission_rate;
    }


    @Override
    public BigDecimal minBackersStake() {
        return min_back_stake;
    }


    @Override
    public SiteEventTracker getEventTracker() {
        return new SmarketsEventTracker(this);
    }


    @Override
    public BigDecimal investment2Stake(BigDecimal investment) {
        // Smarkets has commission on losing bets, so this has to be taken out of the
        // investment to find the stake.

        BigDecimal ratio = BigDecimal.ONE.divide(BigDecimal.ONE.add(commission()), 20, RoundingMode.HALF_UP);
        return investment.multiply(ratio);
    }


    @Override
    public BigDecimal stake2Investment(BigDecimal stake) {
        // Smarkets has commission on losing bets, so this has to be added to the stake to
        // get the full investment

        BigDecimal loss_commission = stake.multiply(commission());
        return stake.add(loss_commission);
    }


    public ArrayList<FootballMatch> getFootballMatches(Instant from, Instant until) throws IOException,
            URISyntaxException, InterruptedException {

        return getEvents(from, until, FOOTBALL);
    }


    public BigDecimal getAmountToBet(BigDecimal investment) {
        // This website has commission on losing bets so total investment is slightly more than the amount
        // needed to actually place on the bet.

        BigDecimal ratio = BigDecimal.ONE.divide(BigDecimal.ONE.add(commission_rate), 20, RoundingMode.HALF_UP);
        return investment.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
    }



    public BigDecimal ROI_old(BetOffer bet_offer, BigDecimal investment, boolean real){
        // (From smarkets support email on commission)
        // For back bets that is the back stake if the bet loses or the profit if the bet wins.
        // For lay bets it's the liability if the bet loses or the lay stake if the bet wins
        //
        // Pro Tier commission band where 1% commission is charged on winnings or losses per
        // each individual bet that settles, and order rate is limited to 1 bet/s

        BigDecimal stake;
        BigDecimal ret;
        BigDecimal profit;
        BigDecimal lose_commission;
        BigDecimal win_commission;
        BigDecimal roi;

        if (bet_offer.isBack()){

            //print("-------------------------------");
            BigDecimal ratio = (commission_rate.divide(commission_rate.add(BigDecimal.ONE), 20, RoundingMode.HALF_UP));
            //print("ratio: " + ratio.toString());

            lose_commission = ratio.multiply(investment);
            //print("lose_commission: " + lose_commission.toString());

            stake = investment.subtract(lose_commission);
            //print("stake: " + stake.toString());

            ret = stake.multiply(bet_offer.odds);
            //print("ret: " + ret.toString());

            profit = ret.subtract(stake);
            //print("profit: " + profit.toString());

            win_commission = profit.multiply(commission_rate);
            //print("win_commission: " + win_commission.toString());

            roi = ret.add(lose_commission).subtract(win_commission);
            //print("roi: " + roi.toString());
        }
        else{ // Lay Bet

            lose_commission = (commission_rate.divide(BigDecimal.ONE.add(commission_rate), 20, RoundingMode.HALF_UP))
                    .multiply(investment);
            stake = investment.subtract(lose_commission);
            BigDecimal lay = BetOffer.backStake2LayStake(stake, bet_offer.odds);
            profit = lay;
            win_commission = profit.multiply(commission());
            ret = stake.add(profit);
            roi = ret.add(lose_commission).subtract(win_commission);
        }

        if (real){
            roi = roi.setScale(2, RoundingMode.DOWN);
        }

        return roi;
    }


    @Override
    public BigDecimal ROI(BetOffer bet_offer, BigDecimal investment, boolean real){
        // Default ROI, commission on profits only

        return ROI(bet_offer.betType(), bet_offer.odds, bet_offer.commission(), investment, real);
    }


    public static BigDecimal ROI(String BACK_LAY, BigDecimal odds, BigDecimal commission_rate, BigDecimal investment,
                                 boolean real){
        // (From smarkets support email on commission)
        // For back bets that is the back stake if the bet loses or the profit if the bet wins.
        // For lay bets it's the liability if the bet loses or the lay stake if the bet wins
        //
        // Pro Tier commission band where 1% commission is charged on winnings or losses per
        // each individual bet that settles, and order rate is limited to 1 bet/s

        BigDecimal roi = null;

        // BACK
        if (BACK_LAY.equals(Bet.BACK)){
            BigDecimal ratio = BigDecimal.ONE.add(commission_rate);
            //print("ratio: " + ratio.toString());
            BigDecimal loss_commission_multiplier = commission_rate.divide(ratio, 20, RoundingMode.HALF_UP);
            //print("loss comm mult: " + loss_commission_multiplier.toString());
            BigDecimal loss_commission = investment.multiply(loss_commission_multiplier);
            //print("loss_com: " + loss_commission.toString());
            BigDecimal backers_stake = investment.subtract(loss_commission);
            //print("back_stke: " + backers_stake.toString());
            BigDecimal backers_profit = BetOffer.backStake2LayStake(backers_stake, odds);
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
            BigDecimal layers_profit = BetOffer.layStake2backStake(layers_stake, odds);
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



    public ArrayList<FootballMatch> getEvents(Instant from, Instant until, String sport) throws InterruptedException,
            IOException, URISyntaxException {

        Map<String, Object> params = new HashMap();
        params.put("start_datetime_min", from.toString());
        params.put("start_datetime_max", until.toString());
        params.put("limit", "1000");
        params.put("type", sport);

        JSONObject response = (JSONObject) requester.get(baseurl + "events/", params);
        if (!response.containsKey("events")) {
            String msg = String.format("No 'events' field found in smarkets response.\n%s", ps(response));
            throw new IOException(msg);
        }
        JSONArray events = (JSONArray) response.get("events");
        ArrayList<FootballMatch> footballMatches = new ArrayList<>();
        for (Object event_obj: events){
            JSONObject event = (JSONObject) event_obj;

            Instant time = Instant.parse(((String) event.get("start_datetime")));
            String name = (String) event.get("name");
            String[] teams = name.split(" vs. ");
            if (teams.length != 2){
                log.warning(String.format("Cannot parse football match name '%s' in smarkets.", name));
                continue;
            }

            FootballMatch fm = new FootballMatch(time, new Team(teams[0]), new Team(teams[1]));
            fm.metadata.put("smarkets_event_id", (String) event.get("id"));
            footballMatches.add(fm);
        }

        return footballMatches;

    }


    public JSONArray getMarkets(String event_id) throws InterruptedException,
            IOException, URISyntaxException {

        JSONObject r = (JSONObject) requester.get(String.format("%sevents/%s/markets/", baseurl, event_id));
        if (!r.containsKey("markets")){
            String msg = String.format("No 'markets' field found in response when looking for " +
                            "markets in smarkets.\n%s",
                    ps(r));
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
            String msg = String.format("contracts field not found in smarkets response.\n%s", ps(response));
            log.warning(msg);
            throw new IOException(msg);
        }

        JSONArray contracts = (JSONArray) response.get("contracts");
        return contracts;
    }


    public JSONObject getPrices(ArrayList<String> market_ids) throws InterruptedException, IOException,
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

        return getPrices(market_ids_list);
    }


    public JSONObject getPrices(Set<String> market_ids) throws InterruptedException, IOException,
            URISyntaxException {

        if (market_ids.size() <= 0){
            return new JSONObject();
        }

        String market_ids_list = "";
        for (String id: market_ids){
            market_ids_list += id;
        }
        if (market_ids_list.length() > 0 && market_ids_list.substring(market_ids_list.length()-1).equals(",")){
            market_ids_list = market_ids_list.substring(0, market_ids_list.length()-1);
        }

        return getPrices(market_ids_list);
    }


    public JSONObject getPrices(String market_ids) throws InterruptedException, IOException,
            URISyntaxException {

        JSONObject response = (JSONObject) requester.get(String.format("%smarkets/%s/quotes/",
                baseurl, market_ids));

        return response;
    }


    public JSONObject getPricesFromHandler(ArrayList<String> market_ids) throws InterruptedException {
        RequestHandler rh = new RequestHandler();
        rh.request = market_ids;
        priceQuotesRequestHandlerQueue.put(rh);
        JSONObject response = rh.getResponse();
        if (rh.valid_response){
            return response;
        }
        else{
            return null;
        }


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


    public class PlaceBetRunnable implements Runnable{

        public JSONObject payload;
        public JSONObject response;
        public Exception exception;
        public Thread thread;
        public BetOrder betOrder;

        public PlaceBetRunnable(BetOrder betOrder, JSONObject payload){
            this.betOrder = betOrder;
            this.payload = payload;
        }

        @Override
        public void run() {
            try {
                response = (JSONObject) requester.post(baseurl + "orders/", payload);
            } catch (IOException | URISyntaxException e) {
                log.severe("IO or URI exception when placing smarket part bet.");
                response = null;
                exception = e;
            }
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


    public ArrayList<PlacedBet> placeBets(ArrayList<BetOrder> betOrders, BigDecimal MIN_ODDS_RATIO)
            throws IOException, URISyntaxException {

        ArrayList<PlaceBetRunnable> placeBetRunnables = new ArrayList<>();
        for (BetOrder betOrder: betOrders) {

            // Change 'side' and odds multiplier depending on type of bet (back or lay)
            BigDecimal odds = betOrder.bet_offer.odds;
            String side;
            if (betOrder.isBack()){
                side = "buy";
                odds = odds.subtract(BigDecimal.ONE).multiply(MIN_ODDS_RATIO).add(BigDecimal.ONE);
            }
            else{
                side = "sell";
                odds = odds.subtract(BigDecimal.ONE).divide(MIN_ODDS_RATIO, 20, RoundingMode.HALF_UP).add(BigDecimal.ONE);
            }


            // TODO: Re-check smarkets


            JSONObject payload = new JSONObject();
            payload.put("contract_id", betOrder.bet_offer.metadata.get(Smarkets.CONTRACT_ID));
            payload.put("market_id", betOrder.bet_offer.metadata.get(Smarkets.MARKET_ID));
            payload.put("price", validPrice(dec2price(odds)));
            payload.put("quantity", size2quantity(betOrder.getBackersStake(), betOrder.bet_offer.odds));
            payload.put("side", side);
            payload.put("type", "immediate_or_cancel");


            // Create thread to run this single request and add runnable to list.
            PlaceBetRunnable placeBetRunnable = new PlaceBetRunnable(betOrder, payload);
            placeBetRunnable.thread = new Thread(placeBetRunnable);
            placeBetRunnable.thread.setName("Smarkets Bet Placer");
            placeBetRunnable.thread.start();
            placeBetRunnables.add(placeBetRunnable);
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
        Instant time = Instant.now();

        ArrayList<PlacedBet> placedBets = new ArrayList<>();
        for (PlaceBetRunnable placeBetRunnable: placeBetRunnables) {
            PlacedBet pb = null;
            BetOrder betOrder = placeBetRunnable.betOrder;
            JSONObject response = placeBetRunnable.response;

            if (response == null) {
                log.severe(String.format("Failed to placed %s on bet %s in smarkets. Response null.",
                        betOrder.investment.toString(), betOrder.bet_offer.bet.id()));
                pb = new PlacedBet(PlacedBet.FAILED_STATE, betOrder, "Error getting response.");
            }
            else if (response.containsKey("error_type")) {
                String error = (String) response.get("error_type");

                log.severe(String.format("Failed to placed %s on bet %s in smarkets. '%s'.",
                        betOrder.investment.toString(), betOrder.bet_offer.bet.id(), error));
                pb = new PlacedBet(PlacedBet.FAILED_STATE, betOrder, error);
            }
            else {
                String bet_id = (String) response.get("order_id");
                long price = (long) response.get("executed_avg_price");
                long quantity = (long) response.get("total_executed_quantity");
                BigDecimal odds = price2dec(price);
                BigDecimal backers_stake = quantity2size(quantity, price);
                BigDecimal investment;
                if (betOrder.isBack()){
                    investment = stake2Investment(backers_stake);
                }
                else {
                    investment = stake2Investment(BetOffer.backStake2LayStake(backers_stake, odds));
                }
                BigDecimal returns = this.ROI(betOrder.bet_offer.newOdds(odds), investment, true);

                log.info(String.format("Successfully invested £%s @ %s on %s '%s' in smarkets (returns %s).",
                        investment.toString(), odds.toString(), betOrder.bet_offer.bet.id(),
                        betOrder.match().name, returns.toString()));

                pb = new PlacedBet(PlacedBet.SUCCESS_STATE, bet_id, betOrder, backers_stake, odds, returns, time);
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

        long orig_price = (long) json.get("orig_price");
        json.put("orig_price_dec", price2dec(orig_price).toString());

        long price = (long) json.get("price");
        json.put("price_dec", price2dec(price).toString());

        long executed_avg_price = (long) json.get("executed_avg_price");
        json.put("executed_avg_price_dec", price2dec(executed_avg_price).toString());

        long quantity = (long) json.get("quantity");
        json.put("quantity_dec", quantity2size(quantity, price).toString());

        long total_executed_quantity = (long) json.get("total_executed_quantity");
        json.put("total_executed_quantity_dec", quantity2size(total_executed_quantity, executed_avg_price).toString());

        return json;
    }



    public static void main(String[] args){

        try {

            Smarkets s = new Smarkets();



        } catch (Exception e) {
            e.printStackTrace();
        }




    }
}
