package SiteConnectors.Smarkets;

import Bet.BetOffer;
import Bet.BetOrder;
import Bet.FootballBet.FootballResultBet;
import Bet.FootballBet.FootballScoreBet;
import Bet.PlacedBet;
import SiteConnectors.BettingSite;
import SiteConnectors.RequestHandler;
import SiteConnectors.SiteEventTracker;
import Sport.FootballMatch;
import Sport.Team;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import tools.Requester;

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

    public static BigDecimal commission_rate = new BigDecimal("0.01");
    public static BigDecimal min_bet = new BigDecimal("0.05");

    public Instant lastPriceQuoteRequest;
    public long rate_limit = 301;

    BlockingQueue<RequestHandler> priceQuotesRequestHandlerQueue;
    PriceQuotesRequestHandler priceQuotesRequestHandler;




    public Smarkets() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, URISyntaxException, IOException {

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
        public int REQUEST_THREADS = 3;
        public long WAIT_MILLISECONDS = 75;

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
            RequestHandler new_handler;
            long milliseconds_to_wait;

            // Start workers
            workerQueue = new LinkedBlockingQueue<>();
            for (int i=0; i<REQUEST_THREADS; i++){
                PriceQuoteRequestSender requestSender = new PriceQuoteRequestSender(workerQueue);
                Thread t = new Thread(requestSender);
                t.start();
            }


            Instant next_request_time = next_request_time = Instant.now().plus(WAIT_MILLISECONDS, ChronoUnit.MILLIS);
            while (true) {
                try {

                    new_handler = null;

                    // Get request handler from queue or wait until next request time
                    if (Instant.now().isBefore(next_request_time)) {
                        milliseconds_to_wait = next_request_time.toEpochMilli() - Instant.now().toEpochMilli();
                        new_handler = requestQueue.poll(milliseconds_to_wait, TimeUnit.MILLISECONDS);
                    }

                    // Sort out what to do with each new Request Handler passed in
                    if (new_handler != null){
                        int num_new_markets = ((ArrayList<String>) new_handler.request).size();

                        // If space is left and it is within time window. Add to list.
                        if (Instant.now().isAfter(next_request_time.minus(WAIT_MILLISECONDS, ChronoUnit.MILLIS))){
                            if (markets_in_queue + num_new_markets <= MAX_BATCH_SIZE) {

                                requestHandlers.add(new_handler);
                                markets_in_queue += num_new_markets;
                                if (requestHandlers.size() == 1) {
                                    next_request_time = Instant.now().plus(WAIT_MILLISECONDS, ChronoUnit.MILLIS);
                                }
                            }
                            else{
                                new_handler.setFail();
                            }
                        }
                        // Set response handler to failed.
                        else{
                            new_handler.setFail();
                        }
                    }

                    // Once the next send time comes around, add list to queue and reset varaibles.
                    if (!Instant.now().isBefore(next_request_time)){
                        if (requestHandlers.size() > 0){
                            workerQueue.put(requestHandlers);
                        }
                        requestHandlers = new ArrayList<>();
                        markets_in_queue = 0;
                        next_request_time = Instant.now().plus(rate_limit, ChronoUnit.MILLIS);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class PriceQuoteRequestSender implements Runnable{

        public BlockingQueue<ArrayList<RequestHandler>> jobQueue;

        public PriceQuoteRequestSender(BlockingQueue jobQueue){
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
    public void login() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, URISyntaxException {
        requester.setHeader("Authorization", getSessionToken());
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
    public BigDecimal minBet() {
        return min_bet;
    }

    @Override
    public SiteEventTracker getEventTracker() {
        return new SmarketsEventTracker(this);
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

    public BigDecimal getInvestedFromStaked(BigDecimal staked) {
        // total investment is stake but also the commission as this is taken for losses too

        BigDecimal ratio = BigDecimal.ONE.add(commission_rate).divide(BigDecimal.ONE, 20, RoundingMode.HALF_UP);
        return staked.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
    }



    @Override
    public BigDecimal ROI(BetOffer bet_offer, BigDecimal investment, boolean real){
        // (From smarkets support email on commission)
        // For back bets that is the back stake if the bet loses or the profit if the bet wins.
        // For lay bets it's the liability if the bet loses or the lay stake if the bet wins

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
            BigDecimal lay = bet_offer.getLayFromStake(stake, real);
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


    public static long odds2price(BigDecimal odds){
        return new BigDecimal(10000).divide(odds, 0, RoundingMode.HALF_UP).intValue();
    }


    public static BigDecimal price2odds(long price){
        return new BigDecimal(10000).divide(new BigDecimal(price), 2, RoundingMode.HALF_UP);
    }


    public static long size2quantity(BigDecimal size, BigDecimal odds){
        return size.multiply(odds).multiply(new BigDecimal(10000))
                .setScale(0, RoundingMode.HALF_UP).intValue();
    }


    public static BigDecimal quantity2size(long quantity, long price){
        return new BigDecimal(quantity * price)
                .divide(new BigDecimal(100000000), 2, RoundingMode.HALF_UP);
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


    private static BigDecimal validOdds(BigDecimal price) {

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
        else if (price.compareTo(new BigDecimal(500)) == -1){
            price = new BigDecimal(300);
        }
        else if (price.compareTo(new BigDecimal(1000)) == -1){
            price = new BigDecimal(500);
        }
        else if (price.compareTo(new BigDecimal(10000)) == -1){
            price = new BigDecimal(1000);
        }
        else {
            price = new BigDecimal(10000);
        }
        return price;
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

    public ArrayList<PlacedBet> placeBets(ArrayList<BetOrder> betOrders, BigDecimal MIN_ODDS_RATIO)
            throws IOException, URISyntaxException {

        ArrayList<PlaceBetRunnable> placeBetRunnables = new ArrayList<>();
        for (BetOrder betOrder: betOrders) {

            JSONObject payload = new JSONObject();
            payload.put("contract_id", betOrder.bet_offer.metadata.get(Smarkets.CONTRACT_ID));
            payload.put("market_id", betOrder.bet_offer.metadata.get(Smarkets.MARKET_ID));
            payload.put("price", odds2price(validOdds(betOrder.bet_offer.odds.multiply(MIN_ODDS_RATIO))));
            payload.put("quantity", size2quantity(getAmountToBet(betOrder.investment), betOrder.bet_offer.odds));
            if (betOrder.bet_offer.bet.isBack()) {
                payload.put("side", "buy");
            } else if (betOrder.bet_offer.bet.isLay()) {
                payload.put("side", "sell");
            } else {
                log.severe("BET IS NOT BACK OR LAY WTFFFFF");
            }
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
                BigDecimal odds = price2odds(price);
                BigDecimal investment = getInvestedFromStaked(quantity2size((long) response.get("total_executed_quantity"), price));
                Instant time = Instant.now();

                BigDecimal returns = this.ROI(betOrder.bet_offer.newOdds(odds), investment, true);

                log.info(String.format("Successfully placed %s on bet %s in smarkets (returns %s).",
                        investment.toString(), betOrder.bet_offer.bet.id(), returns.toString()));

                pb = new PlacedBet(PlacedBet.SUCCESS_STATE, bet_id, betOrder, investment, odds, returns, time);
            }

            placedBets.add(pb);
        }

        return placedBets;
    }



    public static void main(String[] args){

        try {

            Smarkets s = new Smarkets();

            BetOffer bo = new BetOffer();
            bo.odds = new BigDecimal("3.65");
            bo.bet = new FootballResultBet("BACK", "DRAW", false);
            HashMap<String, String> md = new HashMap<String, String>();
            md.put(Smarkets.CONTRACT_ID, "31121592");
            md.put(Smarkets.MARKET_ID, "8950555");
            bo.metadata = md;
            bo.site = s;

            BetOrder betOrder = new BetOrder();
            betOrder.bet_offer = bo;
            betOrder.investment = new BigDecimal("1.20");


            BetOffer bo2 = new BetOffer();
            bo2.odds = new BigDecimal("18.5");
            bo2.bet = new FootballScoreBet("BACK", 0, 2, false);
            HashMap<String, String> md2 = new HashMap<String, String>();
            md2.put(Smarkets.CONTRACT_ID, "31121621");
            md2.put(Smarkets.MARKET_ID, "8950561");
            bo2.metadata = md2;
            bo2.site = s;

            BetOrder betOrder2 = new BetOrder();
            betOrder2.bet_offer = bo2;
            betOrder2.investment = new BigDecimal("0.60");


            ArrayList<BetOrder> betOrders = new ArrayList<>();
            betOrders.add(betOrder);
            betOrders.add(betOrder2);

            //ArrayList<PlacedBet> placedBets = s.placeBets(betOrders, new BigDecimal("0.9"));

            //p(PlacedBet.list2JSON(placedBets));



        } catch (CertificateException e) {
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
