package Trader;

import Bet.Bet;
import Bet.FootballBet.FootballBetGenerator;
import Bet.FootballBet.FootballHandicapBet;
import SiteConnectors.*;
import SiteConnectors.Betdaq.Betdaq;
import SiteConnectors.Betfair.Betfair;
import SiteConnectors.Matchbook.Matchbook;
import SiteConnectors.Smarkets.Smarkets;
import Sport.FootballMatch;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import tools.MyLogHandler;

import java.awt.*;
import java.io.FileNotFoundException;
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
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;
import static tools.printer.*;

public class SportsTrader {

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());
    public static final SportData sportData = new FlashScores();
    public static final Config config = Config.getConfig("config.json");


    public Lock betlock = new ReentrantLock();

    public ArrayList<Class> siteClasses;
    public Map<String, BettingSite> siteObjects;
    public int session_Update_interval_hours = 4;

    public FootballBetGenerator footballBetGenerator;

    public ArrayList<EventTrader> eventTraders;
    public ArrayList<EventTraderSpawn> eventTraderSpawns;
    public SportDataFileSaver sportDataFileSaver;
    public SessionsUpdater sessionsUpdater;
    public SiteAccountInfoUpdater siteAccountInfoUpdater;
    public HeartBeater heartBeater;

    public List<MarketOddsReportWorker> marketOddsReportWorkers;
    public BlockingQueue<RequestHandler> marketOddsReportRequestQueue;
    public AtomicInteger MORW_id;

    public boolean exit_flag;


    public SportsTrader() throws IOException, ConfigException {
        Thread.currentThread().setName("Main");
        exit_flag = false;

        log.setUseParentHandlers(false);
        log.setLevel(Level.INFO);
        try {
            log.addHandler(new MyLogHandler());
        } catch (IOException e) {
            e.printStackTrace();
            String msg = "Error Setting up logging. Exiting";
            print(msg);
            log.severe(msg);
            throw e;
        }

        if (config == null){
            throw new ConfigException("Config returned null.");
        }

        log.setLevel(Level.parse(config.LOG_LEVEL));

        siteClasses = new ArrayList<Class>();
        siteClasses.add(Betfair.class);
        siteClasses.add(Matchbook.class);
        siteClasses.add(Smarkets.class);
        siteClasses.add(Betdaq.class);

        siteObjects = new HashMap<>();
        eventTraders = new ArrayList<>();

        marketOddsReportWorkers = new ArrayList<>();
        marketOddsReportRequestQueue = new LinkedBlockingQueue<>();
        MORW_id = new AtomicInteger(0);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                safe_exit();
            }
        });
    }



    public void newMarketOddsReportWorker(){
        MarketOddsReportWorker morw =
                new MarketOddsReportWorker(this, MORW_id.addAndGet(1), marketOddsReportRequestQueue);

        if (morw == null){
            log.severe("Adding null MORW to list of MORWs???");
        }


        morw.start();
        marketOddsReportWorkers.add(morw);
        log.info(String.format("Created new MORW '%s'", morw.thread.getName()));
    }


    public void removeMarketOddsReportWorker(){
        MarketOddsReportWorker morw = marketOddsReportWorkers.remove(marketOddsReportWorkers.size() - 1);
        if (morw != null){
            morw.safe_exit(false);
        }
    }


    public int MORWwaiting(){
        int n_waiting = 0;
        int n_null = 0;
        for (int i=0; i< marketOddsReportWorkers.size(); i++){

            MarketOddsReportWorker mor = marketOddsReportWorkers.get(i);
            if (mor == null){
                n_waiting++;
                n_null++;
            }
            else if (mor.isWaiting()){
                n_waiting++;
            }
        }

        if (n_null > 0){

            List<String> workernames = new ArrayList<>();
            for (MarketOddsReportWorker morw: marketOddsReportWorkers){
                workernames.add(stringValue(morw));
            }
            log.severe(String.format("%s/%s MarketOddsReportWorkers found to be null - %s",
                    n_null, marketOddsReportWorkers.size(), workernames));
        }


        return n_waiting;
    }



    public RequestHandler requestMarketOddsReport(SiteEventTracker siteEventTracker, Collection<Bet> bets){
        // Pack up args
        Object[] args = new Object[]{siteEventTracker, bets};
        RequestHandler rh = new RequestHandler(args);

        // Pass into queue and return the handler
        marketOddsReportRequestQueue.add(rh);

        // Add more workers if not enough are waiting
        int waiting_workers = MORWwaiting();
        if (waiting_workers <= 1){
            newMarketOddsReportWorker();
        }
        else if (waiting_workers > 2 * eventTraders.size() * siteObjects.size()){
            removeMarketOddsReportWorker();
        }

        return rh;
    }


    public static SportData getSportData(){
        return sportData;
    }


    public BettingSite getSite(String site_name){
        return siteObjects.get(site_name);
    }


    public class ConfigException extends Exception {
        public ConfigException(List<String> fields){
            super(String.join(", ", fields));
        }

        public ConfigException(String msg){
            super(msg);
        }

        public ConfigException(){
            super();
        }
    }


    public Map<String, BettingSite> getSiteObjects(ArrayList<Class> siteClasses){

        Map<String, BettingSite> new_site_objects = new HashMap<>();

        // Initialize site object for each site class and add to map
        for (Class betting_site_class : siteClasses) {
            Class site_class = betting_site_class;
            String site_name = null;
            try {
                site_name = (String) betting_site_class.getField("name").get(null);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
                log.severe(String.format("Site class '%s' has no static variable 'name' to extract.",
                        betting_site_class.toString()));
                return null;
            }

            // Check site appears in config
            if (!config.ACTIVE_SITES.containsKey(site_name)){
                log.severe("Site %s appears in class list but has no config entry. Skipping.");
                return null;
            }

            // Check config is set as active for this class
            boolean site_active = config.ACTIVE_SITES.get(site_name);
            if (!site_active){
                log.info(String.format("Site %s not activated in config. Skipping.", site_name));
                continue;
            }

            // Initialize Site object
            try {
                //BettingSite site_obj = (BettingSite) site_class.getConstructor().newInstance();
                BettingSite site_obj =  (BettingSite) Class.forName(site_class.getName()).newInstance();
                new_site_objects.put(site_name, site_obj);

            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
                log.severe(String.format("Error instantiating site object for %s", site_name));
                continue;
            }

            log.info(String.format("Successfully setup betting site connector for %s.", site_name));
        }

        return new_site_objects;
    }


    public void run(){
        log.info("Running SportsTrader.");

        // Setup safe exit on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run() {
                safe_exit();
                try {
                    sleep(6000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // Run bet/taut generator and generate the tautologies.
        footballBetGenerator = new FootballBetGenerator();
        footballBetGenerator.getAllTautologies();


        // initialize our site classes to objects
        siteObjects = getSiteObjects(siteClasses);
        if (siteObjects == null){
            log.severe("Error in site classes, exiting.");
            return;
        }
        if (siteObjects.size() <= 0){
            log.severe("No site objects created successfully. Exiting.");
            return;
        }

        // Begin background task threads
        heartBeater = new HeartBeater(this);
        heartBeater.start();

        siteAccountInfoUpdater = new SiteAccountInfoUpdater(siteObjects);
        siteAccountInfoUpdater.start();

        sessionsUpdater = new SessionsUpdater(siteObjects);
        sessionsUpdater.start();



        List<FootballMatch> footballMatches = null;
        try {
            if (config.SINGLE_MATCH_TEST) {
                footballMatches = new ArrayList<>();
                FootballMatch fm = FootballMatch.parse(config.SM_TIME, config.SM_NAME);
                footballMatches.add(fm);
                log.info(String.format("Using %s event for testing.", fm.name));
            }
            else {
                footballMatches = getFootballMatches();
                log.info(String.format("Found %d matches within given time frame.", footballMatches.size()));
            }
        }
        catch (ParseException | IOException | URISyntaxException | InterruptedException e){
            e.printStackTrace();
            footballMatches = null;
        }
        if (footballMatches == null) {
            log.severe("Error getting initial football matches. Exiting...");
            safe_exit();
            return;
        }


        // Create football event job queue and fill
        BlockingQueue<FootballMatch> match_queue = new LinkedBlockingQueue<>();
        for (FootballMatch fm: footballMatches){
            match_queue.add(fm);
        }

        // Create same number of event trader setups as max matches allowed to concurrently
        // setup each event trader
        eventTraderSpawns = new ArrayList<>();
        for (int i=0; i<config.MAX_MATCHES; i++){
            EventTraderSpawn eventTraderSpawn = new EventTraderSpawn(this, match_queue);
            eventTraderSpawn.thread.setName("EvntTdrSpwnr" + String.valueOf(i+1));
            eventTraderSpawn.start();
            eventTraderSpawns.add(eventTraderSpawn);
        }


        // Begin thread to periodically save sports data on requests
        sportDataFileSaver = new SportDataFileSaver(sportData);
        sportDataFileSaver.start();


        // Wait for each event trader setup to finish then add the result to the list of EventTraders
        for (EventTraderSpawn eventTraderSpawn: eventTraderSpawns){
            try {
                eventTraderSpawn.thread.join();
                if (eventTraderSpawn.result != null){
                    eventTraders.add(eventTraderSpawn.result);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        log.info(String.format("%d Event Traders setup successfully with at least %d site connectors.",
                eventTraders.size(), config.MIN_SITES_PER_MATCH));


        // Exit if none have worked.
        if (eventTraders.size() == 0){
            log.severe("0 matches have been setup correctly. Exiting.");
            safe_exit();
            return;
        }


        // End if config says so.
        if (!config.CHECK_MARKETS){
            log.info("CHECK_MARKETS set to false. Ending here.");
            safe_exit();
            return;
        }


        String divider = "##########################################################";
        print(sf("\n\n%s\n# Setup for matches complete, starting up event traders. #\n%s\n\n",
                divider, divider));


        // Run all event traders
        for (EventTrader eventTrader: eventTraders){
            eventTrader.thread = new Thread(eventTrader);
            eventTrader.thread.setName("ET: " + eventTrader.match.name);
            eventTrader.thread.start();
        }
        log.info("All Event Traders started.");


        // Wait until exit flag raised
        while (!exit_flag){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                log.info("Main thread interrupted.");
                break;
            }
        }
    }


    public void safe_exit(){
        // Begins a safe close down of the program. Setting into motion
        // all lower safe_exit routines.

        log.info("Master safe exit triggered. Closing down program.");
        exit_flag = true;
        if (sessionsUpdater != null){
            sessionsUpdater.safe_exit();
        }
        if (sportDataFileSaver != null){
            sportDataFileSaver.safe_exit();
        }
        if (siteAccountInfoUpdater != null){
            siteAccountInfoUpdater.safe_exit();
        }
        if (eventTraderSpawns != null) {
            for (EventTraderSpawn ets : eventTraderSpawns) {
                ets.safe_exit();
            }
        }
        if (siteObjects != null) {
            for (Map.Entry<String, BettingSite> entry : siteObjects.entrySet()) {
                entry.getValue().safe_exit();
            }
        }
        if (eventTraders != null){
            for (EventTrader eventTrader: eventTraders){
                eventTrader.safe_exit();
            }
        }
        if (marketOddsReportWorkers != null){
            for (MarketOddsReportWorker marketOddsReportWorker: marketOddsReportWorkers){
                if (marketOddsReportWorker != null){
                    marketOddsReportWorker.safe_exit();
                }
            }
        }
        if (heartBeater != null){
            heartBeater.safe_exit();
        }
    }


    public class SiteAccountInfoUpdater implements Runnable{

        public long seconds_sleep = 10;
        public Thread thread;
        public boolean exit_flag;
        public Map<String, BettingSite> siteObjects;
        public Instant next_update;

        public SiteAccountInfoUpdater(Map<String, BettingSite> siteObjects){
            this.siteObjects = siteObjects;
            exit_flag = false;

            thread = new Thread(this);
            thread.setName("SteInfoUpdtr");
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

            next_update = Instant.now().plusSeconds(seconds_sleep);

            mainloop:
            while (!exit_flag){
                try {
                    sleepUntil(next_update);

                    // Update each sites account info
                    for (Map.Entry<String, BettingSite> entry: siteObjects.entrySet()){
                        entry.getValue().updateAccountInfo();
                    }
                    next_update = Instant.now().plusSeconds(seconds_sleep);

                }
                catch (InterruptedException e){
                    log.warning("Account updater interrupted.");
                }
                catch (IOException | URISyntaxException e) {
                    log.severe(e.toString());
                }

            }
            log.info("Exiting site account info updater.");
        }
    }


    public class EventTraderSpawn implements Runnable{

        // Should check event queue and attempt to create an event trader fully setup
        // One made per max event traders present and try until complete.

        BlockingQueue<FootballMatch> match_queue;
        EventTrader result;
        SportsTrader sportsTrader;
        Thread thread;
        boolean exit_flag;

        public EventTraderSpawn(SportsTrader sportsTrader, BlockingQueue<FootballMatch> match_queue){
            this.match_queue = match_queue;
            this.sportsTrader = sportsTrader;
            exit_flag = false;

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

            while (!exit_flag) {
                try {

                    // Get event from queue, break and finish if anything goes wrong.
                    FootballMatch footballMatch = null;
                    footballMatch = match_queue.poll(0, TimeUnit.SECONDS);

                    if (footballMatch == null || exit_flag) {
                        break;
                    }

                    log.info(String.format("Attempting to verify and setup %s.", footballMatch));

                    // Attempt to setup site event trackers for all sites for this event, try queue again if fails
                    EventTrader eventTrader = new EventTrader(sportsTrader, footballMatch, siteObjects, footballBetGenerator);

                    int successful_site_connections = eventTrader.setupMatch();
                    if (successful_site_connections < config.MIN_SITES_PER_MATCH) {
                        log.warning(String.format("%s Only %d/%d sites connected. MIN_SITES_PER_MATCH=%d.",
                                footballMatch, successful_site_connections, siteObjects.size(), config.MIN_SITES_PER_MATCH));
                        continue;
                    }

                    // Set result and break loop
                    log.info(String.format("EventTraderSetup complete for event %s.", footballMatch));
                    result = eventTrader;
                    break;
                }
                catch (InterruptedException e){
                    log.warning("Event trader spawner interrupted.");
                }
            }

            if (exit_flag) {
                log.info("Exiting EventTrader setup.");
            }
        }
    }


    public class SessionsUpdater implements Runnable {

        public boolean exit_flag;
        public Thread thread;
        public Map<String, BettingSite> siteObjects;
        public Instant next_update;

        public SessionsUpdater(Map<String, BettingSite> siteObjects){
            this.siteObjects = siteObjects;
            exit_flag = false;

            thread = new Thread(this);
            thread.setName("SsionUpdtr");
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
            log.info("Session updater started.");
            next_update = Instant.now().plus(session_Update_interval_hours, ChronoUnit.HOURS);

            while (!exit_flag){

                // Sleep until next update or when interrupted
                try {
                    sleepUntil(next_update);
                }
                catch (InterruptedException e) {
                    continue;
                }

                // Break if necessary
                if (exit_flag){
                    break;
                }

                // Update sessions
                for (Map.Entry<String, BettingSite> entry: siteObjects.entrySet()){
                    String site_name = entry.getKey();
                    BettingSite bet_site = entry.getValue();

                    try {
                        bet_site.login();
                        log.info(String.format("Successfully refreshed session for %s", site_name));
                    } catch (CertificateException | UnrecoverableKeyException | NoSuchAlgorithmException |
                            KeyStoreException | KeyManagementException | IOException | URISyntaxException |
                            org.json.simple.parser.ParseException | InterruptedException e) {

                        e.printStackTrace();

                        String msg = String.format("Error while trying to refresh session for %s.", site_name);
                        log.severe(msg);
                    }
                }
                next_update = Instant.now().plus(session_Update_interval_hours, ChronoUnit.HOURS);
            }
            log.info("Exiting session updater.");
        }
    }


    public class SportDataFileSaver implements Runnable{

        public long min_save_interval_secs = 20;

        public SportData sportData;
        public Thread thread;
        public boolean exit_flag;

        public SportDataFileSaver(SportData sportData){
            this.sportData = sportData;
            exit_flag = false;

            thread = new Thread(this);
            thread.setName("FS-Filesaver");
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

            Instant min_next_save = null;

            mainloop:
            while (!exit_flag){
                try{
                    sportData.save_requests_queue.take();

                    // Wait until at least the minimum time until next save is reached
                    while (min_next_save != null && Instant.now().isBefore(min_next_save)
                            && !exit_flag){
                        Thread.sleep(1000);
                    }


                    // Save and clear save requests and set next min time to save
                    sportData.save_all();
                    sportData.save_requests_queue.clear();
                    min_next_save = Instant.now().plus(min_save_interval_secs, ChronoUnit.SECONDS);
                }
                catch (InterruptedException e) {
                    continue;
                }
            }
            log.info("Exiting File saver.");
        }
    }


    public class HeartBeater implements Runnable{

        boolean exit_flag;
        Thread thread;
        SportsTrader sportsTrader;

        public HeartBeater(SportsTrader sportsTrader){
            exit_flag = false;
            this.sportsTrader = sportsTrader;
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

            Instant next_heartbeat = Instant.now();

            while (!exit_flag){
                try{

                    // Wait for next heartbeat time, then calculate the NEXT next time.
                    sleepUntil(next_heartbeat);
                    next_heartbeat = next_heartbeat.plusMillis(config.HEARTBEAT_INTERVAL);

                    // Send heartbeat to each site
                    for (BettingSite site: sportsTrader.siteObjects.values()){

                        boolean result = false;
                        try {
                            result = site.sendHeartbeat();
                        } catch (Exception e){
                            log.severe(sf("Exception while sending heartbeat to %s: %s", site.getName(), e.toString()));
                            result = false;
                        }

                        if (!result){
                            log.severe(sf("Unable to send heartbeat for %s", site.getName()));
                        }
                        else{
                            log.info(sf("Heartbeat sent to %s successfully", site.getName()));
                        }
                    }
                }
                catch (InterruptedException e){
                    log.info("HeartBeater interuppted.");
                }
            }
        }

    }


    private List<FootballMatch> getFootballMatches() throws IOException, URISyntaxException, InterruptedException {

        String site_name = config.EVENT_SOURCE;
        BettingSite site = siteObjects.get(site_name);
        if (site == null){
            log.severe(String.format("EVENT_SOURCE '%s' cannot be found. It may not bet set in ACTIVE_SITES.",
                    config.EVENT_SOURCE));
            return null;
        }

        Instant from;
        Instant until;
        Instant now = Instant.now();

        // Find time frame to search in
        if (config.IN_PLAY){
            from = now.minus(5, ChronoUnit.HOURS);
            log.info(String.format("Searching for matches from %s, %d hours ahead and in-play.",
                    site_name, config.HOURS_AHEAD));
        } else{
            from = now;
            log.info(String.format("Searching for matches from %s, %d hours ahead.",
                    site_name, config.HOURS_AHEAD));
        }
        until = now.plus(config.HOURS_AHEAD, ChronoUnit.HOURS);

        // Get all football matches found in time frame.
        List<FootballMatch> fms = site.getFootballMatches(from, until);

        return fms;
    }


    public static void printThreads() {
        try {
            while (true) {
                Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
                String s = "";
                for (Thread thread : threadSet) {
                    s += thread.getName() + "\n";
                }
                print(s);
                sleep(10000);
            }
        }
        catch (Exception e) {
            print(sf("Thread printer ending due to exception %s.", e.toString()));
        }
    }


    public static void main(String[] args){

        SportsTrader st = null;
        try {
            st = new SportsTrader();
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }

        st.run();
        //printThreads();
    }

}
