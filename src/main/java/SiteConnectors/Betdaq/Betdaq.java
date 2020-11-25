package SiteConnectors.Betdaq;

import Bet.*;
import Bet.Bet.BetType;
import SiteConnectors.*;
import Sport.FootballMatch;
import Trader.Config;
import Trader.SportsTrader;
import com.globalbettingexchange.externalapi.*;
import org.apache.commons.collections.buffer.BoundedFifoBuffer;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import tools.Requester;


import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import static tools.printer.*;
import static tools.BigDecimalTools.*;

public class Betdaq extends BettingSite {

    public static final Config config = SportsTrader.config;

    public final static String name = "betdaq";
    public final static String id = "BD";

    public static final String extra_regex = "\\(i\\/r\\)";
    public static final String time_regex = "\\d\\d:\\d\\d";
    public static final Pattern time_pattern = Pattern.compile(time_regex);
    public static final List<String> days_of_week_prefixs =
            Arrays.asList("(mon)", "(tue)", "(wed)", "(thur)", "(fri)", "(sat)", "(sun)");

    public static final long FOOTBALL_ID = 100003;
    public static final long HORSE_RACING_ID = 100004;
    public static final long TENNIS_ID = 100005;
    public static final long GOLF_ID = 100006;
    public static final long CRICKET_ID = 100007;
    public static final long VIRTUAL_SPORTS_ID = 1457259;
    public static final long VIRTUAL_HORSES_FLATS_ID = 1461197;
    public static final long VIRTUAL_HORSES_JUMPS_ID = 3897608;
    public static final long VIRTUAL_GREYHOUND_ID = 1461198;
    public static final long VIRTUAL_SPEEDWAY_ID = 1461203;
    public static final long VIRTUAL_CYCLING_ID = 1461202;
    public static final long VIRTUAL_CARS_ID = 1461201;

    public static final short MATCH_ODDS_TYPE = 3;
    public static final short ASIAN_HANDICAP_TYPE = 10;
    public static final short OVER_UNDER_TYPE = 4;
    public static final short SCORE_TYPE = 1;

    public static final short MARKET_STATUS_INACTIVE = 1;
    public static final short MARKET_STATUS_ACTIVE = 2;
    public static final short MARKET_STATUS_SUSPENDED = 3;
    public static final short MARKET_STATUS_COMPLETED = 4;
    public static final short MARKET_STATUS_SETTLED = 6;
    public static final short MARKET_STATUS_VOIDED = 7;

    public static final short SELECTION_STATUS_INACTIVE = 1;
    public static final short SELECTION_STATUS_ACTIVE = 2;
    public static final short SELECTION_STATUS_SUSPENDED = 3;
    public static final short SELECTION_STATUS_WITHDRAWN = 4;
    public static final short SELECTION_STATUS_VOIDED = 5;
    public static final short SELECTION_STATUS_COMPLETED = 6;
    public static final short SELECTION_STATUS_SETTLED = 8;
    public static final short SELECTION_STATUS_BALLOTED_OUT = 9;

    public static final short KILLTYPE_FILLANDKILL = 2;
    public static final short KILLTYPE_FILLORKILL = 3;
    public static final short KILLTYPE_FILLORKILLDONTCANCEL = 4;
    public static final short KILLTYPE_SPIFUNMATCHED = 5;

    public static final short ORDER_UNMATCHED = 1;
    public static final short ORDER_MATCHED = 2;
    public static final short ORDER_CANCELLED = 3;
    public static final short ORDER_SETTLED = 4;
    public static final short ORDER_VOID = 5;
    public static final short ORDER_DEAD = 6;



    public static final String BETDAQ_EVENT_ID = "betdaq_event_id";
    public static final String BETDAQ_SELECTION_ID = "betdaq_selection_id";
    public static final String BETDAQ_SELECTION_RESET_COUNT = "betdaq_selection_reset_count";
    public static final String BETDAQ_SEQ_NUMBER = "betdaq_seq_number";

    public static final String WSDL_URL = "http://api.betdaq.com/v2.0/API.wsdl";
    public static final String readOnlyUrl = "https://api.betdaq.com/v2.0/ReadOnlyService.asmx";
    public static final String secureServiceUrl = "https://api.betdaq.com/v2.0/Secure/SecureService.asmx";

    private String username;
    private String password;

    public BigDecimal[] odds_ladder;
    public BigDecimal min_odds_ladder_step;
    private BigDecimal base_commission_rate = new BigDecimal("0.02");
    static BigDecimal betdaq_min_back_stake = new BigDecimal("0.50");
    static BigDecimal betdaq_min_lay_stake = new BigDecimal("0.50");

    public GetPricesRequestHandler getPricesRequestHandler = new GetPricesRequestHandler();

    public CircularFifoBuffer req_history;
    public Instant request_blackout_end;
    public static final int MARKET_REQ_LIMIT = 1950;
    public static final int MARKET_REQ_WINDOW = 60;
    public static final int MARKET_IDS_PER_REQ_MAX = 50;

    public final GetPricesResponse RATE_LIMITED_RESPONSE = ratelimitResponse();


    public GetEventSubTreeWithSelectionsResponse getEventSubTreeWithSelectionsResponse;
    public Lock event_tree_lock = new ReentrantLock();
    public Instant event_tree_update_time;
    public static final int MINS_BETWEEN_EVENT_TREE_UPDATE = 10;


    public Betdaq() throws IOException, ParseException, InterruptedException, URISyntaxException {

        // Read login info from file
        Map login_details = getJSON(ssldir + "betdaq-login.json");
        username = login_details.get("u").toString();
        password = login_details.get("p").toString();

        requester = Requester.SOAPRequester();

        getEventSubTreeWithSelectionsResponse = null;
        event_tree_update_time = null;

        login();
    }


    public class GetPricesRequestHandler implements Runnable{

        public static final int REQUEST_THREADS = 5;
        public final long MAX_WAIT_TIME_MS = config.BETDAQ_RH_WAIT;

        public Thread thread;
        public BlockingQueue<RequestHandler> request_queue;
        public BlockingQueue<List<RequestHandler>> batch_queue;
        public List<GetPricesRequestSender> requestSenders;


        public GetPricesRequestHandler(){
            exit_flag = false;
            request_queue = new LinkedBlockingQueue<>();
            batch_queue = new LinkedBlockingQueue<>();
            thread = new Thread(this);
            request_blackout_end = null;
            req_history = new CircularFifoBuffer(MARKET_REQ_LIMIT - MARKET_IDS_PER_REQ_MAX - 1);
        }


        public boolean start(){
            try {
                thread.start();
                return true;
            }
            catch (IllegalThreadStateException e){
                return false;
            }
        }

        public boolean isRunning(){
            return thread.isAlive();
        }

        public void safe_exit(){
            exit_flag = true;
            if (requestSenders != null) {
                for (GetPricesRequestSender worker : requestSenders) {
                    worker.safe_exit();
                    worker.thread.interrupt();
                }
            }
            thread.interrupt();
        }

        public boolean addToQueue(RequestHandler requestHandler){
            return request_queue.add(requestHandler);
        }


        public void send_batch(List<RequestHandler> requestHandlers, int batch_size){
            // Decide if requests should be rate limited and not sent

            Instant now = Instant.now();
            boolean LIMIT = false;

            // If we are within a blackout
            if (request_blackout_end != null && now.isBefore(request_blackout_end)){
                LIMIT = true;
            }

            // If the limit of requests sent was within the last 60 second window
            else if (req_history.isFull()){
                Instant oldest_time = (Instant) req_history.get();
                if (oldest_time.isAfter(now.minusSeconds(MARKET_REQ_WINDOW + 1))){
                    LIMIT = true;
                }
            }

            // Rate limit responses and don't send off.
            if (LIMIT) {
                for (RequestHandler rh : requestHandlers) {
                    rh.setResponse(RATE_LIMITED_RESPONSE);
                }
                return;
            }


            // Send off batch
            batch_queue.add(requestHandlers);
            now = Instant.now();

            // Add a timing into the history queue for each market requested.
            for (int i=0; i<batch_size; i++){
                req_history.add(now);
            }
        }


        @Override
        public void run() {
            log.info("Running getPrice handler for betdaq.");

            Instant wait_until = null;
            RequestHandler new_handler = null;
            int batch_size = 0;
            List<RequestHandler> requestHandlers = new ArrayList<>();

            // Start batch senders
            requestSenders = new ArrayList<>(REQUEST_THREADS);
            for (int i=1; i<=REQUEST_THREADS; i++){
                GetPricesRequestSender worker = new GetPricesRequestSender(batch_queue);
                log.info("Created new RS " + i);
                worker.thread.setName("BD RS-" + i);
                worker.start();
                requestSenders.add(worker);
            }


            while (!exit_flag) {
                try {
                    new_handler = null;

                    // Wait forever until next handler (new batch)
                    if (wait_until == null){
                        new_handler = request_queue.take();
                        wait_until = Instant.now().plusMillis(MAX_WAIT_TIME_MS);
                    }
                    // Wait until timeout for next handler (batch in process)
                    else{
                        long milliseconds_to_wait = wait_until.toEpochMilli() - Instant.now().toEpochMilli();
                        new_handler = request_queue.poll(milliseconds_to_wait, TimeUnit.MILLISECONDS);
                    }

                    // If not a timeout then we have a new handler to deal with
                    if (new_handler != null){
                        int handler_size = ((Collection<Long>) new_handler.request).size();

                        // If new handler puts us over max size, then send off old batch first
                        if (batch_size + handler_size > MARKET_IDS_PER_REQ_MAX){
                            send_batch(requestHandlers, batch_size);
                            wait_until = Instant.now().plusMillis(MAX_WAIT_TIME_MS);
                            requestHandlers = new ArrayList<>();
                            batch_size = 0;
                        }

                        // Add new handler into list
                        requestHandlers.add(new_handler);
                        batch_size += handler_size;
                    }

                    // Send off batch if due
                    if (Instant.now().isAfter(wait_until) && !exit_flag){

                        send_batch(requestHandlers, batch_size);
                        wait_until = null;
                        requestHandlers = new ArrayList<>();
                        batch_size = 0;
                    }

                } catch (InterruptedException e) {
                    log.info("Betdaq price request handler interrupted.");
                }
            }
            log.info("Ending betdaq request handler.");
        }
    }


    public class GetPricesRequestSender implements Runnable{

        public BlockingQueue<ArrayList<RequestHandler>> batch_queue;
        public Thread thread;
        public boolean exit_flag;

        public GetPricesRequestSender(BlockingQueue queue){
            exit_flag = false;
            batch_queue = queue;
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
            log.info("Starting betdaq request sender.");
            List<RequestHandler> request_handler_batch = null;

            while (!exit_flag){
                try {

                    //Wait for next batch from queue
                    request_handler_batch = null;
                    request_handler_batch = batch_queue.take();

                    // Extract market ids from each request handler
                    Set<Long> market_ids = new HashSet<>(request_handler_batch.size());
                    for (RequestHandler rh: request_handler_batch){
                        Collection<Long> rh_market_ids = (Collection<Long>) rh.request;
                        market_ids.addAll(rh_market_ids);
                    }

                    // Send off for prices
                    GetPricesResponse response = _getPrices(market_ids);

                    // Send results back to each request handler
                    for (RequestHandler rh: request_handler_batch){
                        rh.setResponse(response);
                    }

                } catch (InterruptedException e) {
                    continue;
                } catch (IOException | URISyntaxException e){
                    e.printStackTrace();
                }
            }
            log.info("Ending betdaq request sender.");
        }
    }


    public String getSOAPHeader(){
        String header = String.format(
                "<soapenv:Header><ext:ExternalApiHeader version=\"2\" languageCode=\"en\" " +
                "username=\"%s\" password=\"%s\" " +
                "applicationIdentifier=\"ST\"/></soapenv:Header>", username, password);
        return header;
    }


    @Override
    public void login() throws IOException, URISyntaxException, InterruptedException {

        updateAccountInfo();
        log.info(String.format("Successfully logged into Betdaq. Balance: %s  Exposure: %s",
                balance.toString(), exposure.toString()));
    }

    @Override
    public String getSessionToken() throws IOException, CertificateException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, UnrecoverableKeyException, URISyntaxException {
        return null;
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
        return betdaq_min_back_stake;
    }


    @Override
    public BigDecimal minLayersStake(BigDecimal odds) {
        return betdaq_min_lay_stake;
    }


    @Override
    public void safe_exit() {
        if (getPricesRequestHandler != null) {
            getPricesRequestHandler.safe_exit();
        }
    }


    @Override
    public void updateAccountInfo() throws InterruptedException, IOException, URISyntaxException {

        String soap_body =
                "<ext:GetAccountBalances>" +
                "<ext:getAccountBalancesRequest/>" +
                "</ext:GetAccountBalances>";

        GetAccountBalancesResponse b = (GetAccountBalancesResponse)
                requester.SOAPRequest(secureServiceUrl, getSOAPHeader(), soap_body, GetAccountBalancesResponse.class);

        balance = b.getGetAccountBalancesResult().getAvailableFunds();
        exposure = b.getGetAccountBalancesResult().getExposure();

        odds_ladder = getOddsLadder();
        min_odds_ladder_step = smallestStep(odds_ladder);
    }


    @Override
    public SiteEventTracker getEventTracker() {
        return new BetdaqEventTracker(this);
    }


    public List<MarketType> getMarketInfo(Collection<Long> market_ids) throws IOException, URISyntaxException {

        String market_ids_xml = "";
        for (Long market_id: market_ids){
            market_ids_xml += "<ext:MarketIds>" + market_id + "</ext:MarketIds>";
        }

        String soap_body = "<ext:GetMarketInformation><ext:getMarketInformationRequest>" +
                market_ids_xml +
                "</ext:getMarketInformationRequest></ext:GetMarketInformation>";

        GetMarketInformationResponse2 r = ((GetMarketInformationResponse)
                requester.SOAPRequest(readOnlyUrl, getSOAPHeader(), soap_body, GetMarketInformationResponse.class))
                .getGetMarketInformationResult();


        // Check response is successful
        ReturnStatus rs = r.getReturnStatus();
        if (rs.getCode() != 0){
            log.severe(String.format("Could not get market info from betdaq for ids %s. Error %s - '%s'",
                    market_ids.toString(), rs.getCode(), rs.getDescription()));
            return null;
        }

        return r.getMarkets();
    }


    public MarketType getMarketInfo(long market_id) throws IOException, URISyntaxException {
        List<Long> market_ids = new ArrayList<>(1);
        market_ids.add(market_id);
        return getMarketInfo(market_ids).get(0);
    }


    public EventClassifierType getEventTree(long event_id)
            throws IOException, URISyntaxException {

        // Add single id into a list and call other function
        Collection<Long> ids = new ArrayList<>(1);
        ids.add(event_id);
        List<EventClassifierType> eventClassifierTypes = getEventTree(ids);

        if (eventClassifierTypes.size() != 1){
            log.warning(sf("Betdaq event tree for %s returned %s results.", event_id, eventClassifierTypes.size()));
            return null;
        }
        return eventClassifierTypes.get(0);
    }


    public List<EventClassifierType> getEventTree_depr(Collection<Long> event_ids, boolean with_selections)
            throws IOException, URISyntaxException {

        // Create argument xml tags for each id
        String xml_id_args = "";
        for (Long id: event_ids){
            xml_id_args += String.format("<ext:EventClassifierIds>%s</ext:EventClassifierIds>", id);
        }

        String selections = "No";
        if (with_selections){
            selections = "With";
        }

        // Construct xml request
        String body = String.format(
                "<ext:GetEventSubTree%1$sSelections>" +
                "<ext:getEventSubTree%1$sSelectionsRequest WantDirectDescendentsOnly=\"false\" WantPlayMarkets=\"false\">" +
                xml_id_args +
                "</ext:getEventSubTree%1$sSelectionsRequest>" +
                "</ext:GetEventSubTree%1$sSelections>",
                selections);


        // Send request and get back response object
        ReturnStatus rs;
        List<EventClassifierType> events;
        if (with_selections) {
            GetEventSubTreeWithSelectionsResponse r = (GetEventSubTreeWithSelectionsResponse)
                    requester.SOAPRequest(readOnlyUrl, getSOAPHeader(), body,
                            GetEventSubTreeWithSelectionsResponse.class);
            rs = r.getGetEventSubTreeWithSelectionsResult().getReturnStatus();
            events = r.getGetEventSubTreeWithSelectionsResult().getEventClassifiers();
        }
        else {
            GetEventSubTreeNoSelectionsResponse r = (GetEventSubTreeNoSelectionsResponse)
                    requester.SOAPRequest(readOnlyUrl, getSOAPHeader(), body,
                            GetEventSubTreeNoSelectionsResponse.class);
            rs = r.getGetEventSubTreeNoSelectionsResult().getReturnStatus();
            events = r.getGetEventSubTreeNoSelectionsResult().getEventClassifiers();
        }

        // Check response is successful
        if (rs.getCode() != 0){
            log.severe(String.format("Could not get event tree from betdaq for ids %s. Error %s - '%s'",
                    event_ids.toString(), rs.getCode(), rs.getDescription()));
            return null;
        }

        return events;
    }


    public List<EventClassifierType> getEventTree(Collection<Long> event_ids)
            throws IOException, URISyntaxException {

        event_tree_lock.lock();

        // Update event tree if it needs updating
        if (event_tree_update_time == null ||
                Instant.now().isAfter(event_tree_update_time) ||
                getEventSubTreeWithSelectionsResponse == null){

            log.info("Betdaq is updating cached events.");
            getEventSubTreeWithSelectionsResponse = getCompleteEventTreeResponse();
            event_tree_update_time = Instant.now().plus(MINS_BETWEEN_EVENT_TREE_UPDATE, ChronoUnit.MINUTES);
            log.info("Betdaq cached events have been updated.");
        }

        // Get all the events that match one of the given ids
        List<EventClassifierType> full_event_tree = getEventSubTreeWithSelectionsResponse
                .getGetEventSubTreeWithSelectionsResult().getEventClassifiers();
        List<EventClassifierType> matching_events = getNestedEventsByID(full_event_tree, event_ids);

        event_tree_lock.unlock();
        return matching_events;
    }

    public GetEventSubTreeWithSelectionsResponse getCompleteEventTreeResponse()
            throws IOException, URISyntaxException {

        // Gets the entire event tree, to be cached and looked back at.

        List<Long> sport_ids_to_include = Arrays.asList(FOOTBALL_ID);

        // Create argument xml tags for each id
        String xml_id_args = "";
        for (Long id: sport_ids_to_include){
            xml_id_args += String.format("<ext:EventClassifierIds>%s</ext:EventClassifierIds>", id);
        }

        // Construct xml request
        String body = "<ext:GetEventSubTreeWithSelections>" +
                        "<ext:getEventSubTreeWithSelectionsRequest " +
                        "WantDirectDescendentsOnly=\"false\" WantPlayMarkets=\"false\">" +
                        xml_id_args +
                        "</ext:getEventSubTreeWithSelectionsRequest>" +
                        "</ext:GetEventSubTreeWithSelections>";


        // Send request and get back response object
        String raw_response = requester.SOAPRequestRaw(readOnlyUrl, getSOAPHeader(), body);
        GetEventSubTreeWithSelectionsResponse response = (GetEventSubTreeWithSelectionsResponse)
                requester.XML2SOAP(raw_response, GetEventSubTreeWithSelectionsResponse.class);

        toFile(raw_response, "output.xml");

        // Check response is successful
        ReturnStatus rs = response.getGetEventSubTreeWithSelectionsResult().getReturnStatus();
        if (rs.getCode() != 0){
            log.severe(String.format("Could not get event tree from betdaq for ids %s. Error %s - '%s'",
                    sport_ids_to_include.toString(), rs.getCode(), rs.getDescription()));
            return null;
        }

        return response;
    }

    @Override
    public List<FootballMatch> getFootballMatches(Instant from, Instant until)
            throws IOException, URISyntaxException {

        // Get all bottom level football events
        EventClassifierType all_football_events_tree = getEventTree(FOOTBALL_ID);
        if (all_football_events_tree == null){
            return null;
        }
        List<EventClassifierType>  all_football_matches =
                getNestedMatchEvents(all_football_events_tree.getEventClassifiers());


        List<FootballMatch> footballMatches = new ArrayList<>();
        for (EventClassifierType event: all_football_matches){

            // Check event has a Event odds Market (ensures its a event)
            MarketType matchOddsMarket = extractMatchOddsMarket(event);
            if (matchOddsMarket == null){
                continue;
            }

            // Ensure event is within parameterised time limits
            Instant time = matchOddsMarket.getStartTime().toGregorianCalendar().toInstant();
            if (time.isBefore(from) || time.isAfter(until)){
                continue;
            }

            // Extract event name, parse to FootballMatch obj and add to list
            String event_name = scrub_event_name(event.getName());
            try {
                FootballMatch fm = FootballMatch.parse(time, event_name);
                fm.addMetaData(BETDAQ_EVENT_ID, String.valueOf(event.getId()));
                footballMatches.add(fm);
            }
            catch (java.text.ParseException e){
                log.severe(sf("Could not parse betdaq football event name '%s'", event_name));
            }
        }

        return footballMatches;
    }



    public static List<EventClassifierType> getNestedEventsByID(List<EventClassifierType> eventClassifierTypes,
                                                            Collection<Long> ids){

        // Through the layers of nested Events, find the events that match the given ids
        List<EventClassifierType> matches = new ArrayList<>();
        for (EventClassifierType event: eventClassifierTypes){

            // If event id appears in whitelist, add in to matches.
            if (ids.contains(event.getId())){
                matches.add(event);
            }

            // If this event has nested events, recurse this function and add them to markets.
            List<EventClassifierType> child_events = event.getEventClassifiers();
            if (child_events != null && child_events.size() > 0){
                matches.addAll(getNestedEventsByID(child_events, ids));
            }
        }

        return matches;
    }

    public static List<EventClassifierType> getNestedMatchEvents(List<EventClassifierType> eventClassifierTypes){

        // Recursively checks all nested events in tree and extracts the singular matches
        // By checking if it contains a match odds market.

        List<EventClassifierType> match_events = new ArrayList<>();
        for (EventClassifierType event: eventClassifierTypes){

            // Check if event contains 'match odds' market
            if (extractMatchOddsMarket(event) != null){
                match_events.add(event);
            }

            List<EventClassifierType> child_events = event.getEventClassifiers();
            // If this event has nested events, recurse this function and add them to markets.
            if (child_events != null && child_events.size() > 0){
                match_events.addAll(getNestedMatchEvents(child_events));
            }
        }

        return match_events;
    }

    public static MarketType extractMatchOddsMarket(EventClassifierType eventClassifierType){
        MarketType matchOddsMarket = null;
        if (eventClassifierType.getMarkets() != null){
            for (MarketType marketType: eventClassifierType.getMarkets()){
                if (marketType.getType() == MATCH_ODDS_TYPE){
                    matchOddsMarket = marketType;
                    break;
                }
            }
        }
        return matchOddsMarket;
    }


    public BigDecimal[] getOddsLadder() throws IOException, URISyntaxException {

        String soap_body = "<ext:GetOddsLadder><ext:getOddsLadderRequest PriceFormat=\"1\"/></ext:GetOddsLadder>";

        GetOddsLadderResponse2 r = ((GetOddsLadderResponse)
                requester.SOAPRequest(readOnlyUrl, getSOAPHeader(), soap_body, GetOddsLadderResponse.class))
                .getGetOddsLadderResult();

        ReturnStatus rs = r.getReturnStatus();
        if (rs.getCode() != 0){
            log.severe(String.format("Could not get odds ladder from betdaq code: %s - %s - %s",
                    rs.getCode(), rs.getDescription(), rs.getExtraInformation()));
            return  null;
        }

        BigDecimal [] ladder = new BigDecimal[r.getLadder().size()];
        for (int i=0; i<r.getLadder().size(); i++){
            ladder[i] = r.getLadder().get(i).getPrice();
        }

        return ladder;
    }


    public GetPricesResponse getPrices(Collection<Long> market_ids) throws InterruptedException, IOException, URISyntaxException {
        // Start request handler if not already started.
        if (!getPricesRequestHandler.isRunning()){
            getPricesRequestHandler.start();
        }

        RequestHandler rh = new RequestHandler();
        rh.request = market_ids;
        getPricesRequestHandler.addToQueue(rh);
        return (GetPricesResponse) rh.getResponse();
    }


    public GetPricesResponse getPrices(long market_id) throws InterruptedException, IOException, URISyntaxException {
        List<Long> market_ids = new ArrayList<>(1);
        market_ids.add(market_id);
        return getPrices(market_ids);
    }


    public GetPricesResponse _getPrices(long market_id) throws IOException, URISyntaxException {
        List<Long> market_ids = new ArrayList<>(1);
        market_ids.add(market_id);
        return _getPrices(market_ids);
    }


    public GetPricesResponse _getPrices(Collection<Long> marketIds) throws IOException, URISyntaxException {

        // Create xml tags for each market id
        String market_ids_xml = "";
        for (Long market_id: marketIds){
            market_ids_xml += String.format("<ext:MarketIds>%s</ext:MarketIds>", String.valueOf(market_id));
        }

        // Create SOAP xml body
        String body = "<ext:GetPrices><ext:getPricesRequest ThresholdAmount=\"0\" NumberForPricesRequired=\"3\" " +
                          "NumberAgainstPricesRequired=\"3\" WantMarketMatchedAmount=\"true\" " +
                          "WantSelectionsMatchedAmounts=\"true\" WantSelectionMatchedDetails=\"true\">" +
                      market_ids_xml +
                      "</ext:getPricesRequest></ext:GetPrices>";

        // Send SOAP request and return object response
        GetPricesResponse r = (GetPricesResponse)
                requester.SOAPRequest(readOnlyUrl, getSOAPHeader(), body, GetPricesResponse.class);

        // Check return status of request
        ReturnStatus rs = r.getGetPricesResult().getReturnStatus();
        if (rs.getCode() == 406){
            log.severe(sf("Code:406 REQ LIMIT HIT. Setting blackout time +%s seconds.", MARKET_REQ_WINDOW));
            request_blackout_end = Instant.now().plusSeconds(MARKET_REQ_WINDOW + 1);
        }
        if (rs.getCode() != 0){
            log.severe(String.format("Error getting Betdaq prices %s %s - for market ids: %s",
                    rs.getCode(), rs.getDescription(), marketIds.toString()));
        }

        return r;
    }

    @Override
    public BigDecimal getValidOdds(BigDecimal odds, RoundingMode roundingMode) {

        if (odds.compareTo(minValidOdds()) < 0 || odds.compareTo(maxValidOdds()) > 0){
            log.severe(String.format("Could not return valid BDQ odds for input %s, outside valid range %s-%s",
                    BDString(odds), BDString(minValidOdds()), BDString(maxValidOdds())));
            return null;
        }

        return findClosest(odds_ladder, odds, roundingMode);
    }


    @Override
    public BigDecimal minValidOdds() {
        return odds_ladder[0];
    }

    @Override
    public BigDecimal maxValidOdds() {
        return odds_ladder[odds_ladder.length - 1];
    }

    public PlaceOrdersWithReceiptResponse placeOrders(PlaceOrdersWithReceiptRequestItem item) throws
            IOException, URISyntaxException {
        List<PlaceOrdersWithReceiptRequestItem> items = new ArrayList<>(1);
        items.add(item);
        return placeOrders(items);
    }


    public PlaceOrdersWithReceiptResponse placeOrders(List<PlaceOrdersWithReceiptRequestItem> items) throws
            IOException, URISyntaxException {

        String xml_orders = "";
        for (PlaceOrdersWithReceiptRequestItem item: items){
            xml_orders += requestItem2XML(item);
        }

        String xml_body = "<ext:PlaceOrdersWithReceipt><ext:orders><ext:Orders>";
        xml_body += xml_orders;
        xml_body += "</ext:Orders></ext:orders></ext:PlaceOrdersWithReceipt>";

        PlaceOrdersWithReceiptResponse r = ((PlaceOrdersWithReceiptResponse)
                requester.SOAPRequest(secureServiceUrl, getSOAPHeader(), xml_body,
                        PlaceOrdersWithReceiptResponse.class));

        return r;
    }


    public static Short betType2Polarity(BetType bet_type){
        if (bet_type == BetType.BACK){ return 1; }
        else{ return 2; }
    }

    public static BetType polarity2BetType(short polarity){
        if (polarity == 1){ return BetType.BACK; }
        else if (polarity == 2){ return BetType.LAY; }
        return null;
    }


    public static String requestItem2XML(PlaceOrdersWithReceiptRequestItem item){
        String xml = "<ext:Order ";

        xml += String.format("SelectionId=\"%s\" ", item.getSelectionId());
        xml += String.format("Stake=\"%s\" ",       item.getStake().toString());
        xml += String.format("Price=\"%s\" ",       item.getPrice().toString());
        xml += String.format("Polarity=\"%s\" ",    item.getPolarity());
        xml += String.format("ExpectedSelectionResetCount=\"%s\" ", item.getExpectedSelectionResetCount());
        xml += String.format("ExpectedWithdrawalSequenceNumber=\"%s\" ", item.getExpectedWithdrawalSequenceNumber());
        xml += String.format("KillType=\"%s\" ",    item.getKillType());

        if (item.getFillOrKillThreshold() != null) {
            xml += String.format("FillOrKillThreshold=\"%s\" ", item.getFillOrKillThreshold().toString());
        }
        if (item.isCancelOnInRunning() != null) {
            xml += String.format("CancelOnInRunning=\"%s\" ", Boolean.toString(item.isCancelOnInRunning()));
        }
        if (item.isCancelIfSelectionReset() != null) {
            xml += String.format("CancelIfSelectionReset=\"%s\" ", Boolean.toString(item.isCancelIfSelectionReset()));
        }
        if (item.getWithdrawalRepriceOption() != null) {
            xml += String.format("WithdrawalRepriceOption=\"%s\" ", item.getWithdrawalRepriceOption());
        }
        if (item.getExpiresAt() != null) {
            xml += String.format("ExpiresAt=\"%s\" ", item.getExpiresAt().toString());
        }
        if (item.isRestrictOrderToBroker() != null) {
            xml += String.format("RestrictOrderToBroker=\"%s\" ", Boolean.toString(item.isRestrictOrderToBroker()));
        }
        if (item.getChannelTypeInfo() != null) {
            xml += String.format("ChannelTypeInfo=\"%s\" ", item.getChannelTypeInfo());
        }
        if (item.getPunterReferenceNumber() != null) {
            xml += String.format("PunterReferenceNumber=\"%s\" ", item.getPunterReferenceNumber());
        }

        xml += "/>";
        return xml;
    }


    public PlaceOrdersWithReceiptRequestItem betOrder2BetdaqOrder(BetPlan betPlan, BigDecimal odds_buffer_ratio){

        // Get selection id from metadata
        String selection_id_string = betPlan.betExchange.getMetadata(Betdaq.BETDAQ_SELECTION_ID);
        String sequence_number_string = betPlan.betExchange.getMetadata(Betdaq.BETDAQ_SEQ_NUMBER);
        String reset_count_string = betPlan.betExchange.getMetadata(Betdaq.BETDAQ_SELECTION_RESET_COUNT);

        // Log error and return null if any of the metadata was not found
        if (betPlan.getSite() != this || selection_id_string == null || sequence_number_string == null ||
                reset_count_string == null){

            String msg = "BetOrder passed to betdaq is invalid: ";
            msg += String.format("site=%s  selectionID=%s  seq_num=%s  reset_count=%s",
                    betPlan.getSite().getName(), selection_id_string, sequence_number_string, reset_count_string);
            log.severe(msg);
            return null;
        }

        // Ensure stake is rounded
        BigDecimal backers_stake = betPlan.getBackersStake().setScale(2, RoundingMode.HALF_UP);

        // Put a buffer to the odds for slight changes in the market, and find nearest valid betdaq odds.
        BigDecimal valid_buffered_odds = betPlan.getValidOddsWithBuffer(odds_buffer_ratio);

        PlaceOrdersWithReceiptRequestItem item = new PlaceOrdersWithReceiptRequestItem();
        item.setSelectionId(Long.valueOf(selection_id_string));
        item.setStake(backers_stake);
        item.setPrice(valid_buffered_odds);
        item.setPolarity(betType2Polarity(betPlan.getBet().getType()));
        item.setExpectedSelectionResetCount(Short.valueOf(reset_count_string));
        item.setExpectedWithdrawalSequenceNumber(Short.valueOf(sequence_number_string));
        item.setKillType(KILLTYPE_FILLORKILL);
        item.setFillOrKillThreshold(backers_stake);
        item.setCancelOnInRunning(false);
        item.setCancelIfSelectionReset(true);
        item.setPunterReferenceNumber(Long.parseLong(betPlan.getID()));

        return item;
    }


    public PlacedBet betdaqResp2PlacedBet(PlaceOrdersWithReceiptResponseItem resp_item){
        // Set some defaults
        PlacedBet pb = new PlacedBet();
        pb.raw_response = resp_item;
        pb.setSite(this);
        pb.bet_type = polarity2BetType(resp_item.getPolarity());;

        if (resp_item.getStatus() != ORDER_MATCHED){
            pb.state = PlacedBet.State.FAIL;
            pb.error = String.format("Betdaq order not fully matched - status: %s, matched %s of %s",
                    resp_item.getStatus(), resp_item.getMatchedStake().toPlainString(),
                    resp_item.getUnmatchedStake().toPlainString());
        }
        else{
            pb.set_backersStake_layersProfit(resp_item.getMatchedStake());
            pb.set_backersProfit_layersStake(resp_item.getMatchedAgainstStake());
            pb.avg_odds = resp_item.getMatchedPrice();
            pb.bet_id = String.valueOf(resp_item.getOrderHandle());
        }

        return pb;
    }


    @Override
    public List<PlacedBet> placeBets(List<BetPlan> betPlans, BigDecimal odds_ratio_buffer)
            throws IOException, URISyntaxException {

        List<PlaceOrdersWithReceiptRequestItem> requestItems = new ArrayList<>();
        for (BetPlan betPlan : betPlans){
            PlaceOrdersWithReceiptRequestItem item = betOrder2BetdaqOrder(betPlan, odds_ratio_buffer);
            requestItems.add(item);
        }

        Instant time_sent = Instant.now();
        PlaceOrdersWithReceiptResponse response = placeOrders(requestItems);

        if (response == null){
            log.severe("Got no response for betdaq placed bets.");
            return null;
        }

        PlaceOrdersWithReceiptResponse2 resp2 = response.getPlaceOrdersWithReceiptResult();
        Instant time_placed = resp2.getTimestamp().toGregorianCalendar().toInstant();
        ReturnStatus ret_status = resp2.getReturnStatus();


        List<PlacedBet> placedBets = new ArrayList<>(betPlans.size());

        // Fail if return status is not 0;
        if (ret_status.getCode() != 0) {
            log.severe(String.format("Betdaq error on bets batch %s: %s - %s",
                        ret_status.getCode(), ret_status.getDescription(), ret_status.getExtraInformation()));
            return null;
        }

        for (PlaceOrdersWithReceiptResponseItem resp_item: resp2.getOrders().getOrder()) {

            String id = resp_item.getPunterReferenceNumber().toString();

            PlacedBet pb = betdaqResp2PlacedBet(resp_item);
            pb.betPlan = BetPlan.find(betPlans, id);
            pb.time_placed = time_placed;

            placedBets.add(pb);
        }


        return placedBets;
    }


    public static JSONObject eventJSON(EventClassifierType e, boolean show_children,
                                   boolean show_markets, boolean show_selections){

        JSONArray children = new JSONArray();
        for (EventClassifierType child: e.getEventClassifiers()){
            if (show_children) {
                children.add(eventJSON(child, show_children, show_markets, show_selections));
            }
            else{
                children.add(child.getName());
            }
        }

        JSONArray markets = new JSONArray();
        for (MarketType marketType: e.getMarkets()){
            if (show_markets){
                markets.add(marketJSON(marketType, show_selections));
            }
            else {
                markets.add(marketType.getName());
            }
        }

        JSONObject j = new JSONObject();
        j.put("id", e.getId());
        j.put("name", e.getName());
        j.put("display_order", e.getDisplayOrder());
        j.put("parent_id", e.getParentId());
        j.put("enabled_for_multiples", e.isIsEnabledForMultiples());
        if (children.size() > 0){
            j.put("events", children);
        }
        if (markets.size() > 0){
            j.put("markets", markets);
        }
        return j;
    }

    public static JSONObject marketJSON(MarketType m, boolean show_selections){
        JSONArray selections = new JSONArray();
        for (SelectionType selection: m.getSelections()){
            if (show_selections){
                selections.add(selectionJSON(selection));
            }
            else {
                selections.add(selection.getName());
            }
        }

        JSONObject j = new JSONObject();
        j.put("id", m.getId());
        j.put("name", m.getName());
        j.put("type", m.getType());
        j.put("play_market", m.isIsPlayMarket());
        j.put("status", getMarketStatus(m.getStatus()));
        j.put("racegrade", m.getRaceGrade());
        j.put("time", m.getStartTime().toGregorianCalendar().toInstant().toString());
        j.put("currently_in_running", m.isIsCurrentlyInRunning());
        j.put("event_id", m.getEventClassifierId());
        j.put("place_payout", m.getPlacePayout());
        if (selections.size() > 0){
            j.put("selections", selections);
        }
        return j;
    }

    public static JSONObject marketJSON(MarketTypeWithPrices m){
        JSONArray selections = new JSONArray();
        for (SelectionTypeWithPrices selection: m.getSelections()){
            selections.add(selection.getName());

        }

        JSONObject j = new JSONObject();
        j.put("id", m.getId());
        j.put("name", m.getName());
        j.put("type", m.getType());
        j.put("play_market", m.isIsPlayMarket());
        j.put("status", getMarketStatus(m.getStatus()));
        j.put("time", stringValue(m.getStartTime()));
        j.put("currently_in_running", m.isIsCurrentlyInRunning());
        j.put("event_id", m.getId());
        j.put("place_payout", m.getPlacePayout());
        if (selections.size() > 0){
            j.put("selections", selections);
        }
        return j;
    }


    public static JSONObject selectionJSON(SelectionType s){
        JSONObject j = new JSONObject();
        j.put("id", s.getId());
        j.put("name", s.getName());
        j.put("status", getSelectionStatus(s.getStatus()));
        j.put("reset_count", s.getResetCount());
        j.put("deduction_factor", s.getDeductionFactor());
        j.put("display_order", s.getDisplayOrder());
        return j;
    }


    public static String getMarketStatus(short id){
        switch (id){
            case MARKET_STATUS_ACTIVE: return "ACTIVE";
            case MARKET_STATUS_SETTLED: return "SETTLED";
            case MARKET_STATUS_COMPLETED: return "COMPLETED";
            case MARKET_STATUS_SUSPENDED: return "SUSPENDED";
            case MARKET_STATUS_INACTIVE: return "INACTIVE";
            case MARKET_STATUS_VOIDED: return "VOIDED";
            default: return "Status Code '" + id + "'";
        }
    }


    public static String getSelectionStatus(short id){
        switch (id){
            case SELECTION_STATUS_ACTIVE: return "ACTIVE";
            case SELECTION_STATUS_INACTIVE: return "INACTIVE";
            case SELECTION_STATUS_COMPLETED: return "COMPLETED";
            case SELECTION_STATUS_SETTLED: return "SETTLED";
            case SELECTION_STATUS_VOIDED: return "VOIDED";
            case SELECTION_STATUS_WITHDRAWN: return "WITHDRAWN";
            case SELECTION_STATUS_BALLOTED_OUT: return "BALLOTED OUT";
            case SELECTION_STATUS_SUSPENDED: return "SUSPENDED";
            default: return "Status Code '" + id + "'";
        }
    }


    public static GetPricesResponse ratelimitResponse(){
        GetPricesResponse resp = new GetPricesResponse();
        GetPricesResponse2 resp2 = new GetPricesResponse2();
        ReturnStatus r_status = new ReturnStatus();
        r_status.setCode(406);
        resp2.setReturnStatus(r_status);
        resp.setGetPricesResult(resp2);
        return resp;
    }


    public static PlaceOrdersWithReceiptRequestItem testBetItem(String type, long sel_id, String back_stake, String price) throws IOException, URISyntaxException {
        PlaceOrdersWithReceiptRequestItem item = new PlaceOrdersWithReceiptRequestItem();
        item.setSelectionId(sel_id);
        item.setStake(new BigDecimal(back_stake));
        item.setPrice(new BigDecimal(price));
        item.setPolarity(betType2Polarity(BetType.valueOf(type.toUpperCase())));
        item.setExpectedSelectionResetCount((short) 0);
        item.setExpectedWithdrawalSequenceNumber((short) 0);
        item.setKillType(KILLTYPE_FILLORKILL);
        item.setFillOrKillThreshold(new BigDecimal(back_stake));
        return item;
    }


    public void testBet() throws IOException, URISyntaxException {
        List<PlaceOrdersWithReceiptRequestItem> items = new ArrayList<>();
        items.add(testBetItem("BACK", 133575139, "1.50", "1.16"));
        PlaceOrdersWithReceiptResponse resp = placeOrders(items);
        ppxs(resp);
    }


    public static String scrub_event_name(String event_name){

        // Removes the extra shit betdaq puts in event names at the start and end
        // eg '19:45 Switzerland v Spain (Live)' -> 'Switzerland v Spain'

        // Check if name starts with day of week, remove it if so
        for (String day_prefix: days_of_week_prefixs) {
            if (event_name.toLowerCase().startsWith(day_prefix)) {
                event_name = event_name.substring(day_prefix.length());
            }
        }

        // Check if name starts with time and remove it if so
        if (event_name.length() >= 5 && event_name.substring(0, 5).matches(Betdaq.time_regex)){
            event_name = event_name.substring(5);
        }

        // Check if name ends with (live) and remove if so
        if (event_name.toLowerCase().endsWith("(live)")){
            event_name = event_name.substring(0, event_name.length()-6);
        }

        return event_name.trim();
    }




    public static void main(String[] args) throws Exception {

        Betdaq b = new Betdaq();


        Instant start = Instant.now();
        List<FootballMatch> fm1 = b.getFootballMatches(Instant.now().minusSeconds(999999999), Instant.now().plusSeconds(60*60*24*2*1000000));
        print(secs_since(start));

        print(sf("fm1 has %s matches", fm1.size()));


        start = Instant.now();
        EventClassifierType event = b.getEventTree(7795413);
        print(secs_since(start));

        print(event);


    }



}
