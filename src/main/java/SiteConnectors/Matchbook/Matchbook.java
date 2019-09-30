package SiteConnectors.Matchbook;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import Bet.BetOrder;
import Bet.PlacedBet;
import SiteConnectors.BettingSite;
import SiteConnectors.RequestHandler;
import SiteConnectors.SiteEventTracker;
import Sport.FootballMatch;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import tools.MyLogHandler;
import tools.Requester;
import tools.printer;

import static tools.printer.*;

public class Matchbook extends BettingSite {

    public static String baseurl = "https://api.matchbook.com/edge/rest";
    public static String[] marketTypes = new String[]{
            "one_x_two",
            "total",
            "handicap",
            "both_to_score",
            "correct_score"};
    public static String FOOTBALL_ID = "15";

    public marketDataRequestHandler marketDataRequestHandler;
    public BlockingQueue<RequestHandler> marketDataRequestHandlerQueue;

    public BigDecimal commission = new BigDecimal("0.02");
    public BigDecimal min_bet = new BigDecimal("0.10");


    public Matchbook() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException,
            IOException, KeyManagementException, KeyStoreException, URISyntaxException {

        if (log == null){
            log = Logger.getLogger(Matchbook.class.getName());
            //log.setUseParentHandlers(false);
            log.setLevel(Level.INFO);
            log.addHandler(new MyLogHandler());
        }
        log.info("Creating new Matchbook Connector");

        name = "matchbook";

        // Set up a requester to handle HTTP requests
        requester = new Requester();
        login();


        // Setup and start marketdata request handler to pool mulitple
        // market data requests to one concurrently.
        marketDataRequestHandlerQueue = new LinkedBlockingQueue<>();
        marketDataRequestHandler = new marketDataRequestHandler(marketDataRequestHandlerQueue);
        Thread marketDataRequestHandlerThread = new Thread(marketDataRequestHandler);
        marketDataRequestHandlerThread.setDaemon(true);
        marketDataRequestHandlerThread.start();
    }




    public class marketDataRequestHandler implements Runnable{

        public int MAX_BATCH_SIZE = 10;
        public int REQUEST_THREADS = 10;
        public long WAIT_MILLISECONDS = 5;

        public BlockingQueue<RequestHandler> requestQueue;
        public BlockingQueue<ArrayList<RequestHandler>> workerQueue;

        public marketDataRequestHandler(BlockingQueue requestQueue){
            this.requestQueue = requestQueue;
        }

        @Override
        public void run() {
            Instant wait_until = null;
            ArrayList<RequestHandler> requestHandlers = new ArrayList<>();
            RequestHandler new_handler;
            long milliseconds_to_wait;

            // Start workers
            workerQueue = new LinkedBlockingQueue<>();
            marketDataRequestSender requestSender = new marketDataRequestSender(workerQueue);
            for (int i=0; i<REQUEST_THREADS; i++){
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

                    if (new_handler != null) {
                        requestHandlers.add(new_handler);
                    }

                    if (new_handler == null || requestHandlers.size() > MAX_BATCH_SIZE || Instant.now().isAfter(wait_until)){
                        workerQueue.put(requestHandlers);
                        wait_until = null;
                        requestHandlers = new ArrayList<>();
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class marketDataRequestSender implements Runnable{

        public BlockingQueue<ArrayList<RequestHandler>> jobQueue;

        public marketDataRequestSender(BlockingQueue jobQueue){
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


                } catch (InterruptedException | IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public void login() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, IOException, URISyntaxException {

        requester.setHeader("session-token", getSessionToken());
    }

    @Override
    public String getSessionToken() throws IOException, CertificateException,
            NoSuchAlgorithmException, KeyStoreException, KeyManagementException,
            UnrecoverableKeyException, URISyntaxException {

        String path = ssldir + "/matchbook-login.json";
        Map creds = printer.getJSON(path);
        JSONObject data = new JSONObject();
        data.put("username", creds.get("u"));
        data.put("password", creds.get("p"));

        String url = "https://api.matchbook.com/bpapi/rest/security/session";
        Requester requester = new Requester();
        requester.setHeader("Content-Type", "application/json");

        JSONObject r = (JSONObject) requester.post(url, data);

        if (!r.containsKey("session-token")){
            String msg = String.format("No session token found in matchbook login response.\n%s",
                    ps(r));
            log.severe(msg);
            throw new IOException(msg);
        }

        return (String) r.get("session-token");
    }

    @Override
    public BigDecimal commission() {
        return commission;
    }

    @Override
    public BigDecimal minBet() {
        return min_bet;
    }

    @Override
    public SiteEventTracker getEventTracker() {
        return new MatchbookEventTracker(this);
    }

    @Override
    public ArrayList<FootballMatch> getFootballMatches(Instant from, Instant until) throws IOException,
            URISyntaxException, InterruptedException {

        return getEvents(from, until, new String[] {FOOTBALL_ID});
    }

    @Override
    public BigDecimal getAmountToBet(BigDecimal investment) {
        return investment;
    }

    @Override
    public ArrayList<PlacedBet> placeBets(ArrayList<BetOrder> betOrders, BigDecimal MIN_ODDS_RATIO)
            throws IOException, URISyntaxException {

        return null;
    }


    public ArrayList<FootballMatch> getEvents(Instant before, Instant after, String[] event_types) throws IOException,
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

        // Build footballmatch objects from return json events
        ArrayList<FootballMatch> events = new ArrayList<FootballMatch>();
        for (Object json_event_obj: (JSONArray) r.get("events")){
            JSONObject json_event = (JSONObject) json_event_obj;
            String name = (String) json_event.get("name");
            String start = (String) json_event.get("start");

            try {
                FootballMatch new_fm = FootballMatch.parse(start, name);
                new_fm.metadata.put("matchbook_event_id", String.valueOf(json_event.get("id")));
                events.add(new_fm);
            } catch (ParseException e) {
                String msg = String.format("Could not parse match '%s' starting at '%s'.", name, start);
                log.warning(e.toString());
                continue;
            }


        }



        return events;
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
        JSONObject response = rh.getResponse();
        return response;
    }


    public static void main(String[] args){

    }
}
