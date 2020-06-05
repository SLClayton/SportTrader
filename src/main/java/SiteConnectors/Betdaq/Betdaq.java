package SiteConnectors.Betdaq;

import Bet.*;
import Bet.Bet.BetType;
import Bet.FootballBet.*;
import SiteConnectors.*;
import Sport.FootballMatch;
import com.globalbettingexchange.externalapi.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
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
import java.util.regex.Pattern;

import static tools.printer.*;

public class Betdaq extends BettingSite {

    public final static String name = "betdaq";
    public final static String id = "BD";

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

    public GetPricesRequestHandler getPricesRequestHandler;

    public int getPriceReqs;


    public Betdaq() throws IOException, ParseException, InterruptedException, URISyntaxException {

        // Read login info from file
        Map login_details = getJSON(ssldir + "betdaq-login.json");
        username = login_details.get("u").toString();
        password = login_details.get("p").toString();

        requester = Requester.SOAPRequester();

        login();

        getPriceReqs = 0;
    }


    public class GetPricesRequestHandler implements Runnable{

        public int MAX_BATCH_SIZE = 10;
        public int REQUEST_THREADS = 10;
        public long MAX_WAIT_TIME = 20;

        public Thread thread;
        public BlockingQueue<RequestHandler> request_queue;
        public BlockingQueue<List<RequestHandler>> batch_queue;
        public List<GetPricesRequestSender> requestSenders;

        public GetPricesRequestHandler(){
            exit_flag = false;
            request_queue = new LinkedBlockingQueue<>();
            batch_queue = new LinkedBlockingQueue<>();
            thread = new Thread(this);
        }

        public void start(){
            thread.start();
        }

        public void safe_exit(){
            exit_flag = true;
            for (GetPricesRequestSender worker: requestSenders){
                worker.safe_exit();
                worker.thread.interrupt();
            }
            thread.interrupt();
        }

        public boolean addToQueue(RequestHandler requestHandler){
            return request_queue.add(requestHandler);
        }

        @Override
        public void run() {
            log.info("Running getPrice handler for betdaq.");

            Instant wait_until = null;
            RequestHandler new_handler = null;
            List<RequestHandler> requestHandlers = new ArrayList<>(MAX_BATCH_SIZE);

            // Start batch senders
            requestSenders = new ArrayList<>(REQUEST_THREADS);
            for (int i=1; i<=REQUEST_THREADS; i++){
                GetPricesRequestSender worker = new GetPricesRequestSender(batch_queue);
                worker.thread.setName("BD RS-" + i);
                worker.start();
                requestSenders.add(worker);
            }


            while (!exit_flag) {
                try {
                    new_handler = null;

                    // Collect next request from queue (wait or timeout)
                    if (wait_until == null){
                        new_handler = request_queue.take();
                        wait_until = Instant.now().plus(MAX_WAIT_TIME, ChronoUnit.MILLIS);
                    }
                    else{
                        long milliseconds_to_wait = wait_until.toEpochMilli() - Instant.now().toEpochMilli();
                        new_handler = request_queue.poll(milliseconds_to_wait, TimeUnit.MILLISECONDS);
                    }

                    // If a new handler has been taken out, then add to next batch
                    if (new_handler != null){
                        requestHandlers.add(new_handler);
                    }

                    // send batch off if conditions met.
                    // new_handler = null means its timed out
                    if ((requestHandlers.size() >= MAX_BATCH_SIZE || !Instant.now().isBefore(wait_until))
                        && !exit_flag){

                        batch_queue.add(requestHandlers);
                        wait_until = null;
                        requestHandlers = new ArrayList<>();
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
                    List<MarketTypeWithPrices> market_prices = _getPrices(market_ids);

                    // Create id map of results
                    Map<Long, MarketTypeWithPrices> marketId_Prices_map = new HashMap<>();
                    for (MarketTypeWithPrices mtwp: market_prices){
                        marketId_Prices_map.put(mtwp.getId(), mtwp);
                    }

                    // Send results back to each request handler
                    for (RequestHandler rh: request_handler_batch){

                        // Extract the market_ids this request handler wanted
                        Collection<Long> rh_market_ids = (Collection<Long>) rh.request;

                        // Create a list of responses from these IDs
                        List<MarketTypeWithPrices> responses = new ArrayList<>(rh_market_ids.size());
                        for (Long market_id: rh_market_ids){
                            responses.add(marketId_Prices_map.get(market_id));
                        }

                        rh.setResponse(responses);
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
        getPricesRequestHandler.safe_exit();
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


    public EventClassifierType getEventTree(long event_id, boolean with_selections)
            throws IOException, URISyntaxException {
        // Add single id into a list and call other function
        Collection<Long> ids = new ArrayList<>(1);
        ids.add(event_id);
        return getEventTree(ids, with_selections).get(0);
    }


    public List<EventClassifierType> getEventTree(Collection<Long> event_ids, boolean with_selections)
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
                            GetEventSubTreeWithSelectionsResponse.class, false);

            rs = r.getGetEventSubTreeWithSelectionsResult().getReturnStatus();
            events = r.getGetEventSubTreeWithSelectionsResult().getEventClassifiers();
        }
        else {
            GetEventSubTreeNoSelectionsResponse r = (GetEventSubTreeNoSelectionsResponse)
                    requester.SOAPRequest(readOnlyUrl, getSOAPHeader(), body,
                            GetEventSubTreeNoSelectionsResponse.class, false);
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

    @Override
    public List<FootballMatch> getFootballMatches(Instant from, Instant until)
            throws IOException, URISyntaxException {

        // Get event tree of all football events
        EventClassifierType football = getEventTree(FOOTBALL_ID, false);

        // From all event types returned, find the lowest level events which should be singular matches
        // by checking all nested events for ones with markets.
        List<EventClassifierType> events = getNestedEventsWithMarkets(football.getEventClassifiers());

        // Compile string regex for parts of name to remove
        String time_regex = "\\d\\d:\\d\\d";
        String day_regex = "\\((mon|tue|wed|thur|fri|sat|sun)\\)";
        String extra_regex = "\\(i\\/r\\)";
        Pattern illegal_front_words = Pattern.compile(String.format("(%s)|(%s)|(%s)", time_regex, day_regex, extra_regex));

        List<FootballMatch> footballMatches = new ArrayList<>();
        for (EventClassifierType event: events){

            // Check event has a Event odds Market (ensures its a event)
            MarketType matchOddsMarket = null;
            for (MarketType market: event.getMarkets()){
                if (market.getType() == MATCH_ODDS_TYPE){
                    matchOddsMarket = market;
                    break;
                }
            }
            if (matchOddsMarket == null){
                continue;
            }

            // Start time of event odds market is start time of event
            // Check event time falls between paramters, skip if not
            Instant starttime = matchOddsMarket.getStartTime().toGregorianCalendar().toInstant();
            if (starttime.isBefore(from) || starttime.isAfter(until)){
                continue;
            }

            // Find first and last words in name and remove if illegal add-ons
            String[] words = event.getName().split("\\s");
            if (illegal_front_words.matcher(words[0].toLowerCase()).matches()){
                words[0] = "";
            }
            if (words[words.length-1].toLowerCase().equals("(live)")){
                words[words.length-1] = "";
            }
            String name = String.join(" ", words).trim();

            try {
                FootballMatch fm = FootballMatch.parse(starttime, name);
                fm.metadata.put(BETDAQ_EVENT_ID, String.valueOf(event.getId()));
                footballMatches.add(fm);
            }
            catch (java.text.ParseException e){
                continue;
            }
        }

        return footballMatches;
    }


    public static List<EventClassifierType> getNestedEventsWithMarkets(List<EventClassifierType> eventClassifierTypes){

        // Through the layers of nested Events, find the events that have markets
        List<EventClassifierType> with_markets = new ArrayList<>();
        for (EventClassifierType event: eventClassifierTypes){

            List<EventClassifierType> child_events = event.getEventClassifiers();
            List<MarketType> markets = event.getMarkets();

            // If this event has nested events, recurse this function and add them to markets.
            if (child_events != null && child_events.size() > 0){
                with_markets.addAll(getNestedEventsWithMarkets(child_events));
            }
            // Add to list if event has any markets
            else if (markets != null && markets.size() > 0){
                with_markets.add(event);
            }
        }

        return with_markets;
    }


    public BigDecimal[] getOddsLadder() throws IOException, URISyntaxException {

        String soap_body = "<ext:GetOddsLadder><ext:getOddsLadderRequest PriceFormat=\"1\"/></ext:GetOddsLadder>";

        print(getSOAPHeader());

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


    public List<MarketTypeWithPrices> getPrices(Collection<Long> market_ids) throws InterruptedException, IOException, URISyntaxException {
        if (getPricesRequestHandler == null) {
            getPricesRequestHandler = new GetPricesRequestHandler();
            getPricesRequestHandler.start();
        }

        RequestHandler rh = new RequestHandler();
        rh.request = market_ids;
        getPricesRequestHandler.addToQueue(rh);
        List<MarketTypeWithPrices> result = (List<MarketTypeWithPrices>) rh.getResponse();
        return result;
    }


    public MarketTypeWithPrices getPrices(long market_id) throws InterruptedException, IOException, URISyntaxException {
        List<Long> market_ids = new ArrayList<>(1);
        market_ids.add(market_id);
        return getPrices(market_ids).get(0);
    }


    public MarketTypeWithPrices _getPrices(long market_id) throws IOException, URISyntaxException {
        List<Long> market_ids = new ArrayList<>(1);
        market_ids.add(market_id);
        return _getPrices(market_ids).get(0);
    }


    public List<MarketTypeWithPrices> _getPrices(Collection<Long> marketIds) throws IOException, URISyntaxException {

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
                requester.SOAPRequest(readOnlyUrl, getSOAPHeader(), body, GetPricesResponse.class, false);

        getPriceReqs += 1;

        // Check return status of request
        ReturnStatus rs = r.getGetPricesResult().getReturnStatus();
        if (rs.getCode() != 0){
            log.severe(String.format("Error getting Betdaq prices %s %s - for market ids: %s",
                    rs.getCode(), rs.getDescription(), marketIds.toString()));
            print("TOTAL REQS = " + String.valueOf(getPriceReqs));
            return null;
        }

        return r.getGetPricesResult().getMarketPrices();
    }

    @Override
    public BigDecimal getValidOdds(BigDecimal odds, RoundingMode roundingMode) {

        // Must be >1 and can't be >1000
        if (odds.compareTo(odds_ladder[0]) < 0 || odds.compareTo(odds_ladder[odds_ladder.length-1]) > 0){
            log.severe(String.format("Trying to get valid betdaq price for %s which is not in ladder range %s-%s",
                    odds, odds_ladder[0], odds_ladder[odds_ladder.length-1]));
            return null;
        }

        return findClosest(odds_ladder, odds, roundingMode);
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
        if (bet_type == BetType.BACK){
            return 1;
        }
        else{
            return 2;
        }
    }

    public static BetType polarity2BetType(short polarity){
        if (polarity == 1){
            return BetType.BACK;
        }
        else if (polarity == 2){
            return BetType.LAY;
        }
        else{
            return null;
        }
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


    public PlaceOrdersWithReceiptRequestItem betOrder2BetdaqOrder(BetOrder betOrder, BigDecimal odds_buffer_ratio, Integer id){

        // Get selection id from metadata
        String selection_id_string = betOrder.getBetOffer().getMetadata(Betdaq.BETDAQ_SELECTION_ID);
        String sequence_number_string = betOrder.getBetOffer().getMetadata(Betdaq.BETDAQ_SEQ_NUMBER);
        String reset_count_string = betOrder.getBetOffer().getMetadata(Betdaq.BETDAQ_SELECTION_RESET_COUNT);

        // Log error and return null if any of the metadata was not found
        if (betOrder.getSite() != this || selection_id_string == null || sequence_number_string == null ||
                reset_count_string == null){

            String msg = "BetOrder passed to betdaq is invalid: ";
            msg += String.format("site=%s  selectionID=%s  seq_num=%s  reset_count=%s",
                    betOrder.getSite().getName(), selection_id_string, sequence_number_string, reset_count_string);
            log.severe(msg);
            return null;
        }

        // Ensure stake is rounded
        BigDecimal backers_stake = betOrder.getBackersStake().setScale(2, RoundingMode.HALF_UP);

        // Put a buffer to the odds for slight changes in the market, and find nearest valid betdaq odds.
        if (odds_buffer_ratio == null){
            odds_buffer_ratio = BigDecimal.ZERO;
        }
        BigDecimal buffered_odds = betOrder.getOddsWithBuffer(odds_buffer_ratio);
        BigDecimal valid_betdaq_price;
        if (betOrder.isBack()){
            valid_betdaq_price = getValidOdds(buffered_odds, RoundingMode.DOWN);
        }
        else{
            valid_betdaq_price = getValidOdds(buffered_odds, RoundingMode.UP);
        }


        if (id == null){
            id = 0;
        }

        PlaceOrdersWithReceiptRequestItem item = new PlaceOrdersWithReceiptRequestItem();
        item.setSelectionId(Long.valueOf(selection_id_string));
        item.setStake(backers_stake);
        item.setPrice(valid_betdaq_price);
        item.setPolarity(betType2Polarity(betOrder.getBet().getType()));
        item.setExpectedSelectionResetCount(Short.valueOf(reset_count_string));
        item.setExpectedWithdrawalSequenceNumber(Short.valueOf(sequence_number_string));
        item.setKillType(KILLTYPE_FILLORKILL);
        item.setFillOrKillThreshold(backers_stake);
        item.setCancelOnInRunning(false);
        item.setCancelIfSelectionReset(true);
        item.setPunterReferenceNumber((long) id);

        return item;
    }


    public PlacedBet betdaqResp2PlacedBet(PlaceOrdersWithReceiptResponseItem resp_item){
        // Set some defaults
        PlacedBet pb = new PlacedBet();
        pb.raw_response = resp_item;
        pb.setSite(this);

        short status = resp_item.getStatus();
        BigDecimal backers_stake;
        BigDecimal layers_stake;
        BetType type = polarity2BetType(resp_item.getPolarity());

        // Betdaq returns 'matched' and 'matchedAgainst' stake parts of bet
        // depending on if this specific bet is BACK or LAY
        if (type.equals(BetType.BACK)){
            backers_stake = resp_item.getMatchedStake();
            layers_stake = resp_item.getMatchedAgainstStake();
        }
        else if (type.equals(BetType.LAY)){
            backers_stake = resp_item.getMatchedAgainstStake();
            layers_stake = resp_item.getMatchedStake();
        }
        else {
            pb.state = PlacedBet.State.FAIL;
            pb.error = String.format("Invalid betdaq polarity %s", resp_item.getPolarity());
            return pb;
        }

        pb.bet_type = type;
        if (status != ORDER_MATCHED){
            pb.state = PlacedBet.State.FAIL;
            pb.error = String.format("Betdaq order not matched - status: %s, matched %s of %s",
                    status, resp_item.getMatchedStake().toPlainString(),
                    resp_item.getUnmatchedStake().toPlainString());
        }
        else{
            pb.set_backersStake_layersProfit(backers_stake);
            pb.set_backersProfit_layersStake(layers_stake);
            pb.avg_odds = resp_item.getMatchedPrice();
            pb.bet_id = String.valueOf(resp_item.getOrderHandle());
        }

        return pb;
    }


    @Override
    public List<PlacedBet> placeBets(List<BetOrder> betOrders, BigDecimal odds_ratio_buffer)
            throws IOException, URISyntaxException {

        List<PlaceOrdersWithReceiptRequestItem> requestItems = new ArrayList<>();
        for (int i=0; i<betOrders.size(); i++){
            BetOrder betOrder = betOrders.get(i);
            PlaceOrdersWithReceiptRequestItem item = betOrder2BetdaqOrder(betOrder, odds_ratio_buffer, i);
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

        List<PlacedBet> placedBets = new ArrayList<>(betOrders.size());
        for (int i=0; i<betOrders.size(); i++){
            PlacedBet pb;

            if (ret_status.getCode() != 0){
                pb = PlacedBet.failedBet(String.format("Betdaq error on bets batch %s: %s - %s",
                        ret_status.getCode(), ret_status.getDescription(), ret_status.getExtraInformation()));
            }
            else{
                List<PlaceOrdersWithReceiptResponseItem> responseItems = resp2.getOrders().getOrder();
                Integer resp_item_index = getRespItemIndexByRef(responseItems, i);
                if (resp_item_index == null){
                    pb = PlacedBet.failedBet(
                            String.format("Could not find betdaq resp item corresponding to betOrder %s", i));
                }
                else{
                    PlaceOrdersWithReceiptResponseItem resp_item = responseItems.get(resp_item_index);
                    pb = betdaqResp2PlacedBet(resp_item);
                    responseItems.remove((int) resp_item_index);
                }
            }

            pb.time_placed = time_placed;
            pb.betOrder = betOrders.get(i);
            placedBets.add(pb);
        }

        return placedBets;
    }


    public static Integer getRespItemIndexByRef(List<PlaceOrdersWithReceiptResponseItem> items, int ref){
        for (int i=0; i<items.size(); i++){
            if (items.get(i).getPunterReferenceNumber() == ref){
                return i;
            }
        }
        return null;
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
        items.add(testBetItem("LAY", 122440972, "2.50", "2.93"));

        PlaceOrdersWithReceiptResponse resp = placeOrders(items);
        ppxs(resp);
    }






    public static void main(String[] args){
        try {
            Betdaq b = new Betdaq();

        }
        catch (Exception e){
            e.printStackTrace();
        }

        print("END");
    }



}
