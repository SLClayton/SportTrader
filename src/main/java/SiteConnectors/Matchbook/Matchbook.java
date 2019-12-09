package SiteConnectors.Matchbook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import Bet.Bet;
import Bet.BetOffer;
import Bet.BetOrder;
import Bet.FootballBet.FootballResultBet;
import Bet.PlacedBet;
import SiteConnectors.Betfair.Betfair;
import SiteConnectors.BettingSite;
import SiteConnectors.RequestHandler;
import SiteConnectors.SiteEventTracker;
import Sport.FootballMatch;
import Sport.Match;
import Trader.EventTrader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import tools.MyLogHandler;
import tools.Requester;
import tools.printer;

import static tools.printer.*;

public class Matchbook extends BettingSite {

    public final static String name = "matchbook";
    public final static String id = "MB";
    public final static String MATCHBOOK_EVENT_ID = "MATCHBOOK_EVENT_ID";

    public static String baseurl = "https://api.matchbook.com/edge/rest";
    public static String[] marketTypes = new String[]{
            "one_x_two",
            "total",
            "handicap",
            "both_to_score",
            "correct_score"};
    public final static String FOOTBALL_ID = "15";
    public final static String RUNNER_ID = "RUNNER_ID";
    public final static String MARKET_ID = "MARKET_ID";
    public final static int[] valid_american_odds = new int[] {-99900, -94900, -89900, -84900, -79900, -74900, -69900, -64900, -59900, -54900, -49900, -48900, -47900, -46900, -45900, -44900, -43900, -42900, -41900, -40900, -39900, -38900, -37900, -36900, -35900, -34900, -33900, -32900, -31900, -30900, -29900, -28900, -27900, -26900, -25900, -24900, -23900, -22900, -21900, -20900, -19900, -18900, -17900, -16900, -15900, -14900, -13900, -12900, -11900, -10900, -9900, -9400, -8900, -8400, -7900, -7400, -6900, -6400, -5900, -5400, -4900, -4700, -4500, -4300, -4100, -3900, -3700, -3500, -3300, -3100, -2900, -2800, -2700, -2600, -2500, -2400, -2300, -2200, -2100, -2000, -1900, -1850, -1800, -1750, -1700, -1650, -1600, -1550, -1500, -1450, -1400, -1350, -1300, -1250, -1200, -1150, -1100, -1050, -1000, -950, -900, -880, -860, -840, -820, -800, -780, -760, -740, -720, -700, -680, -660, -640, -620, -600, -580, -560, -540, -520, -500, -490, -480, -470, -460, -450, -440, -430, -420, -410, -400, -390, -380, -370, -360, -350, -340, -330, -320, -310, -300, -295, -290, -285, -280, -275, -270, -265, -260, -255, -250, -245, -240, -235, -230, -225, -220, -215, -210, -205, -200, -198, -196, -194, -192, -190, -188, -186, -184, -182, -180, -178, -176, -174, -172, -170, -168, -166, -164, -162, -160, -158, -156, -154, -152, -150, -148, -146, -144, -142, -140, -138, -136, -134, -132, -130, -128, -126, -124, -122, -120, -118, -116, -114, -112, -110, -108, -106, -104, -102, 100, 102, 104, 106, 108, 110, 112, 114, 116, 118, 120, 122, 124, 126, 128, 130, 132, 134, 136, 138, 140, 142, 144, 146, 148, 150, 152, 154, 156, 158, 160, 162, 164, 166, 168, 170, 172, 174, 176, 178, 180, 182, 184, 186, 188, 190, 192, 194, 196, 198, 200, 205, 210, 215, 220, 225, 230, 235, 240, 245, 250, 255, 260, 265, 270, 275, 280, 285, 290, 295, 300, 310, 320, 330, 340, 350, 360, 370, 380, 390, 400, 410, 420, 430, 440, 450, 460, 470, 480, 490, 500, 520, 540, 560, 580, 600, 620, 640, 660, 680, 700, 720, 740, 760, 780, 800, 820, 840, 860, 880, 900, 950, 1000, 1050, 1100, 1150, 1200, 1250, 1300, 1350, 1400, 1450, 1500, 1550, 1600, 1650, 1700, 1750, 1800, 1850, 1900, 2000, 2100, 2200, 2300, 2400, 2500, 2600, 2700, 2800, 2900, 3100, 3300, 3500, 3700, 3900, 4100, 4300, 4500, 4700, 4900, 5400, 5900, 6400, 6900, 7400, 7900, 8400, 8900, 9400, 9900, 10900, 11900, 12900, 13900, 14900, 15900, 16900, 17900, 18900, 19900, 20900, 21900, 22900, 23900, 24900, 25900, 26900, 27900, 28900, 29900, 30900, 31900, 32900, 33900, 34900, 35900, 36900, 37900, 38900, 39900, 40900, 41900, 42900, 43900, 44900, 45900, 46900, 47900, 48900, 49900, 54900, 59900, 64900, 69900, 74900, 79900, 84900, 89900, 94900, 99900};
    public BigDecimal[] valid_decimal_odds;

    public long MAX_WAIT_TIME;

    public marketDataRequestHandler marketDataRequestHandler;
    public BlockingQueue<RequestHandler> marketDataRequestHandlerQueue;


    public Matchbook() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException,
            IOException, KeyManagementException, KeyStoreException, URISyntaxException, InterruptedException,
            org.json.simple.parser.ParseException {

        if (log == null){
            log = Logger.getLogger(Matchbook.class.getName());
            //log.setUseParentHandlers(false);
            log.setLevel(Level.INFO);
            log.addHandler(new MyLogHandler());
        }
        log.info("Creating new Matchbook Connector");

        min_back_stake = new BigDecimal("0.10");
        commission_rate = new BigDecimal("0.02");

        setupConfig("config.json");

        // Set up a requester to handle HTTP requests
        requester = new Requester();
        login();


        // Generate list of valid decimal odds from valid american odds.
        valid_decimal_odds = new BigDecimal[valid_american_odds.length];
        for (int i=0; i<valid_american_odds.length; i++){
            valid_decimal_odds[i] = BetOffer.americ2dec(new BigDecimal(valid_american_odds[i]))
                    .setScale(5, RoundingMode.HALF_UP);
        }


        // Setup and start marketdata request handler to pool mulitple
        // market data requests to one concurrently.
        marketDataRequestHandlerQueue = new LinkedBlockingQueue<>();
        marketDataRequestHandler = new marketDataRequestHandler(marketDataRequestHandlerQueue);
        marketDataRequestHandler.thread.setDaemon(true);
        marketDataRequestHandler.thread.setName("MB-RH");
        marketDataRequestHandler.start();
    }


    private void setupConfig(String config_filename) throws FileNotFoundException, org.json.simple.parser.ParseException {
        JSONObject config = getJSONResource(config_filename);
        MAX_WAIT_TIME = ((Long) config.get("MATCHBOOK_RH_WAIT"));
    }


    public class marketDataRequestHandler implements Runnable{

        public int MAX_BATCH_SIZE = 10;
        public int REQUEST_THREADS = 10;

        public BlockingQueue<RequestHandler> requestQueue;
        public BlockingQueue<ArrayList<RequestHandler>> workerQueue;
        public Thread thread;
        public List<MarketDataRequestSender> marketDataRequestSenders;
        public boolean exit_flag;

        public marketDataRequestHandler(BlockingQueue requestQueue){
            exit_flag = false;
            this.requestQueue = requestQueue;
            thread = new Thread(this);
        }


        public void start(){
            thread.start();
        }


        public void safe_exit(){
            exit_flag = true;
            thread.interrupt();
            for (MarketDataRequestSender marketDataRequestSender: marketDataRequestSenders){
                marketDataRequestSender.safe_exit();
            }
        }


        @Override
        public void run() {
            Instant wait_until = null;
            ArrayList<RequestHandler> requestHandlers = new ArrayList<>();
            RequestHandler new_handler;
            long milliseconds_to_wait;

            // Start workers
            workerQueue = new LinkedBlockingQueue<>();
            marketDataRequestSenders = new ArrayList<>();
            for (int i=0; i<REQUEST_THREADS; i++){
                MarketDataRequestSender requestSender = new MarketDataRequestSender(workerQueue);
                requestSender.thread.setName("MB-RS-" + String.valueOf(i+1));
                requestSender.start();
                marketDataRequestSenders.add(requestSender);
            }

            while (!exit_flag) {

                try {
                    new_handler = null;
                    if (wait_until == null){
                        new_handler = requestQueue.take();
                        wait_until = Instant.now().plus(MAX_WAIT_TIME, ChronoUnit.MILLIS);
                    }
                    else {
                        milliseconds_to_wait = wait_until.toEpochMilli() - Instant.now().toEpochMilli();
                        new_handler = requestQueue.poll(milliseconds_to_wait, TimeUnit.MILLISECONDS);
                    }

                    if (new_handler != null) {
                        requestHandlers.add(new_handler);
                    }

                    if (new_handler == null || requestHandlers.size() > MAX_BATCH_SIZE || Instant.now().isAfter(wait_until)){
                        workerQueue.put(requestHandlers);
                        wait_until = null;
                        requestHandlers = new ArrayList<>();
                    }

                } catch (InterruptedException e) {
                    continue;
                }
            }
            log.info("Ending matchbook request handler.");
        }
    }

    public class MarketDataRequestSender implements Runnable{

        public BlockingQueue<ArrayList<RequestHandler>> jobQueue;
        public Thread thread;
        public boolean exit_flag;

        public MarketDataRequestSender(BlockingQueue jobQueue){
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
            JSONObject final_request = new JSONObject();

            mainloop:
            while (!exit_flag){
                try {
                    requestHandlers = null;
                    requestHandlers = jobQueue.take();

                    // list event ids from handlers to get data from.
                    String[] event_ids = new String[requestHandlers.size()];
                    for (int i=0; i<requestHandlers.size(); i++){
                        event_ids[i] = (String) requestHandlers.get(i).request;
                    }

                    // Send request
                    JSONArray full_response = getMarketData(event_ids);

                    // Return responses to request handlers which requested each.
                    for (Object event_md_obj: full_response){
                        boolean returned = false;
                        JSONObject event_md = (JSONObject) event_md_obj;

                        String event_id = String.valueOf(event_md.get("id"));
                        for (RequestHandler rh: requestHandlers){
                            if (rh.request.equals(event_id)){
                                rh.setResponse(event_md);
                                returned = true;
                            }
                        }
                        if (!returned){
                            log.warning(String.format("Matchbook request handler given event_id %s but could not find where to return it", event_id));
                        }
                    }


                } catch (InterruptedException e) {
                    continue;
                } catch (IOException | URISyntaxException e){
                    e.printStackTrace();
                }
            }
            log.info("Ending matchbook request sender.");
        }
    }


    @Override
    public void login() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, IOException, URISyntaxException, InterruptedException,
            org.json.simple.parser.ParseException {

        requester.setHeader("session-token", getSessionToken());
        updateAccountInfo();
        log.info(String.format("Successfully logged into Matchbook. Balance: %s  Exposure: %s",
                balance.toString(), exposure.toString()));
    }


    @Override
    public String getID() {
        return id;
    }


    @Override
    public void updateAccountInfo() throws InterruptedException, IOException, URISyntaxException {

        JSONObject response = (JSONObject) requester.get(baseurl + "/account");

        setBalance(new BigDecimal(String.valueOf((double) response.get("free-funds"))));
        exposure = new BigDecimal(String.valueOf((double) response.get("exposure")));
    }


    @Override
    public String getSessionToken() throws IOException, URISyntaxException, org.json.simple.parser.ParseException {

        String path = ssldir + "/matchbook-login.json";
        Map creds = printer.getJSON(path);
        JSONObject data = new JSONObject();
        data.put("username", creds.get("u"));
        data.put("password", creds.get("p"));

        String url = "https://api.matchbook.com/bpapi/rest/security/session";
        Requester requester = new Requester();
        requester.setHeader("Content-Type", "application/json");

        JSONObject r = (JSONObject) requester.post(url, data);
        pp(r);

        if (!r.containsKey("session-token")){
            String msg = String.format("No session token found in matchbook login response.\n%s", jstring(r));
            log.severe(msg);
            throw new IOException(msg);
        }

        return (String) r.get("session-token");
    }


    @Override
    public BigDecimal commission() {
        return commission_rate;
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public BigDecimal minBackersStake() {
        return min_back_stake;
    }


    @Override
    public void safe_exit() {
        exit_flag = true;
        marketDataRequestHandler.safe_exit();
    }


    @Override
    public SiteEventTracker getEventTracker() {
        return new MatchbookEventTracker(this);
    }



    @Override
    public ArrayList<FootballMatch> getFootballMatches(Instant from, Instant until) throws IOException,
            URISyntaxException, InterruptedException {

        JSONArray events_json = getEvents(from, until, new String[] {FOOTBALL_ID});

        // Build footballmatch objects from return json events
        ArrayList<FootballMatch> events = new ArrayList<FootballMatch>();
        for (Object json_event_obj: events_json){
            JSONObject json_event = (JSONObject) json_event_obj;
            String name = (String) json_event.get("name");
            String start = (String) json_event.get("start");

            try {
                FootballMatch new_fm = FootballMatch.parse(start, name);
                new_fm.metadata.put(MATCHBOOK_EVENT_ID, String.valueOf(json_event.get("id")));
                events.add(new_fm);
            } catch (ParseException e) {
                String msg = String.format("Could not parse match '%s' starting at '%s'.", name, start);
                log.warning(e.toString());
                continue;
            }
        }
        return events;
    }


    public JSONArray getEvents(Instant before, Instant after, String[] event_types) throws IOException,
            URISyntaxException, InterruptedException {

        // Setup paramters
        JSONObject params = new JSONObject();
        params.put("after", before.toEpochMilli() / 1000);
        params.put("before", after.toEpochMilli() / 1000);
        params.put("offset", 0);
        params.put("per-page", 1000);
        params.put("states", "open");
        params.put("exchange-type", "back-lay");
        params.put("odds-type", "DECIMAL");
        params.put("include-prices", "false");
        params.put("include-event-participants", "false");

        if (event_types != null){
            StringBuilder s = new StringBuilder();
            for (int i=0; i<event_types.length; i++){
                s.append(event_types[i]);

                if (i < event_types.length-1){
                    s.append(",");
                }
            }
            params.put("sport-ids", s.toString());
        }

        JSONObject r = (JSONObject) requester.get(baseurl + "/events", params);

        return (JSONArray) r.get("events");
    }


    public ArrayList<FootballMatch> getEvents(Instant before, Instant after, String event_type) throws IOException,
            URISyntaxException, InterruptedException {

        return getEvents(before, after, new String[] {event_type});
    }


    public JSONArray getMarketData(String[] event_ids) throws IOException, URISyntaxException, InterruptedException {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<event_ids.length; i++){
            sb.append(event_ids[i]);

            if (i < event_ids.length - 1){
                sb.append(",");
            }
        }

        JSONObject params = new JSONObject();
        params.put("offset", "0");
        params.put("per-page", "1000");
        params.put("include-prices", "true");
        params.put("price-depth", "3");
        params.put("price-mode", "expanded");
        params.put("ids", sb.toString());

        JSONObject r = (JSONObject) requester.get(baseurl + "/events", params);

        return (JSONArray) r.get("events");
    }


    public JSONObject getMarketDataFromHandler(String event_id) throws InterruptedException {
        // Create Request handler, input the event id as the request.
        RequestHandler rh = new RequestHandler();
        rh.request = event_id;

        // Send that request handler to the queue for processing
        marketDataRequestHandlerQueue.put(rh);

        // Wait for its response. (getResponse waits)
        JSONObject response = (JSONObject) rh.getResponse();
        return response;
    }


    public BigDecimal closestMatchbookOdds(BigDecimal odds){
        // Matchbook uses US oddson their ticker, so convert decimal odds to the nearest valid value.
        // They use 5dp on their converted decimal odds

        if (odds.compareTo(valid_decimal_odds[0]) == -1){
            log.warning("closestMatchbookOdds odds lower than lowest.");
            return valid_decimal_odds[0];
        }

        for (int i=0; i<valid_decimal_odds.length-1; i++){
            BigDecimal above = valid_decimal_odds[i+1];

            if (odds.compareTo(above) != 1){
                BigDecimal below = valid_decimal_odds[i];
                BigDecimal distance_to_below = odds.subtract(below);
                BigDecimal distance_to_above = above.subtract(odds);

                if (distance_to_above.compareTo(distance_to_below) == -1){
                    return above;
                }
                else{
                    return below;
                }
            }
        }

        log.warning("closestMatchbookOdds odds higher than highest.");
        return valid_decimal_odds[valid_decimal_odds.length-1];
    }


    @Override
    public List<PlacedBet> placeBets(List<BetOrder> betOrders, BigDecimal MIN_ODDS_RATIO)
            throws IOException, URISyntaxException {

        // Create a map to keep track of runners to which betOrder they come from
        Map<String, BetOrder> runner_betOrder_map = new HashMap<>();

        JSONArray offers = new JSONArray();
        for (BetOrder betOrder: betOrders){


            // Multiply odds up or down by a multiplier depending on BACK or LAY
            // so there's a margin of error. (Site takes best odds anyway).
            BigDecimal odds = betOrder.bet_offer.odds;
            odds = odds.subtract(BigDecimal.ONE);
            if (betOrder.isBack()){
                odds = odds.multiply(MIN_ODDS_RATIO);
            }
            else {
                odds = odds.divide(MIN_ODDS_RATIO, 20, RoundingMode.HALF_UP);
            }
            odds = odds.add(BigDecimal.ONE);


            JSONObject offer = new JSONObject();
            offer.put("runner-id", betOrder.bet_offer.metadata.get(Matchbook.RUNNER_ID));
            offer.put("side", betOrder.betType().toLowerCase());
            offer.put("odds", odds.setScale(3, RoundingMode.HALF_UP).doubleValue());
            offer.put("stake", betOrder.getBackersStake().doubleValue());
            offer.put("keep-in-play", true);

            offers.add(offer);
            betOrder.site_json_request = offer;

            runner_betOrder_map.put(
                    betOrder.bet_offer.metadata.get(Matchbook.RUNNER_ID) + betOrder.betType().toLowerCase(),
                    betOrder);
        }

        JSONObject json = new JSONObject();
        json.put("odds-type", "DECIMAL");
        json.put("exchange-type", "back-lay");
        json.put("offers", offers);

        Instant time_sent = Instant.now();
        JSONObject response = (JSONObject) requester.post(baseurl + "/v2/offers/", json);


        boolean any_failures = false;
        ArrayList<PlacedBet> placedBets = new ArrayList<>();
        for (Object offer_obj: (JSONArray) response.get("offers")){
            JSONObject offer = (JSONObject) offer_obj;

            String status = (String) offer.get("status");
            String runner_id = String.valueOf((long) offer.get("runner-id"));
            String side = (String) offer.get("side");
            BetOrder betOrder = runner_betOrder_map.get(runner_id + side);

            if (status.equals("matched")){

                // From all the Matched bet parts, sum up the total stake for back, and lay if present
                BigDecimal total_back_stake = BigDecimal.ZERO;
                BigDecimal total_lay_stake = BigDecimal.ZERO;
                for (Object matchedBet_obj: (JSONArray) offer.get("matched-bets")) {
                    JSONObject matchedBet = (JSONObject) matchedBet_obj;

                    // Sum the back stake
                    BigDecimal back_stake_part = new BigDecimal(String.valueOf((Double) matchedBet.get("stake")));
                    total_back_stake = total_back_stake.add(back_stake_part);

                    // If a LAY bet, sum up the lay stake
                    if (betOrder.isLay()){
                        BigDecimal lay_stake_part = new BigDecimal(String.valueOf((Double) matchedBet.get("potential-liability")));
                        total_lay_stake = total_lay_stake.add(lay_stake_part);
                    }
                }

                // Now we have the totals, we can work out the average odds
                BigDecimal avg_odds = BigDecimal.ZERO;
                for (Object matchedBet_obj: (JSONArray) offer.get("matched-bets")){
                    JSONObject matchedBet = (JSONObject) matchedBet_obj;

                    // Extract odds and stake of this part of the matched bet.
                    BigDecimal odds_part = new BigDecimal(String.valueOf((Double) matchedBet.get("decimal-odds")));
                    BigDecimal back_stake_part = new BigDecimal(String.valueOf((Double) matchedBet.get("stake")));

                    // Calculate the weighted average size of this matched bet and multiply the odds by it.
                    BigDecimal weighted_ratio = back_stake_part.divide(total_back_stake, 20, RoundingMode.HALF_UP);
                    avg_odds = avg_odds.add(odds_part.multiply(weighted_ratio));
                }

                // Set investment depending on side of bet
                BigDecimal investment;
                if (betOrder.isBack()){
                    investment = total_back_stake;
                }
                else{
                    investment = total_lay_stake;
                }

                String bet_id = String.valueOf((long) offer.get("id"));
                BigDecimal returns = ROI(betOrder.betType(), avg_odds, betOrder.commission(), investment, false)
                        .setScale(2, RoundingMode.DOWN);
                Instant time_placed = Instant.parse((String) offer.get("created-at"));

                log.info(String.format("Successfully placed £%s @ %s on %s '%s' in matchbook (returns %s).",
                        investment.toString(), avg_odds.toString(), betOrder.betID(),
                        betOrder.match().name, returns.toString()));


                PlacedBet pb = new PlacedBet(PlacedBet.SUCCESS_STATE, bet_id, betOrder, total_back_stake,
                        total_lay_stake, avg_odds, returns, time_placed, time_sent);
                pb.site_json_response = offer;
                placedBets.add(pb);
            }
            else{

                any_failures = true;
                log.severe(String.format("Failed to place %s on bet %s in matchbook. Bet not fully matched.",
                        betOrder.investment.toString(), betOrder.bet_offer.bet.id(), jstring(response)));

                PlacedBet pb = new PlacedBet(PlacedBet.FAILED_STATE, betOrder, status, null, time_sent);
                pb.site_json_response = offer;
                placedBets.add(pb);
            }
        }


        // Remove all offers on market if any failures appear which leave the offer open.
        if (any_failures){
            JSONObject delete_response = (JSONObject) requester.delete(baseurl + "/v2/offers/");

            for (Object offer_obj: (JSONArray) delete_response.get("offers")){
                JSONObject offer = (JSONObject) offer_obj;

                String status = (String) offer.get("status");
                String id = String.valueOf((long) offer.get("id"));
                if (!status.equals("cancelled")){
                    log.severe(String.format("Failed to cancel matchbook bet %s\n%s", id, jstring(offer)));
                }
                else{
                    log.info(String.format("Successfully cancelled matchbook bet %s for not being matched.", id));
                }
            }
            log.severe((jstring(response)));
        }


        try {
            updateAccountInfo();
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.severe(e.toString());
        }

        return placedBets;
    }




    public static void main(String[] args){

        try {
            Matchbook m = new Matchbook();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
