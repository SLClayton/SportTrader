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

    public List<MarketOddsReportWorker> marketOddsReportWorkers;
    public BlockingQueue<RequestHandler> marketOddsReportRequestQueue;

    public boolean exit_flag;


    public SportsTrader() throws IOException, ConfigException, org.json.simple.parser.ParseException {
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

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                safe_exit();
            }
        });
    }



    public void newMarketOddsReportWorker(){
        MarketOddsReportWorker morw = new MarketOddsReportWorker(marketOddsReportRequestQueue);

        if (morw == null){
            log.severe("Adding null MORW to list of MORWs???");
        }

        marketOddsReportWorkers.add(morw);
        morw.thread.setName("MORW-" + marketOddsReportWorkers.size());
        morw.start();
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
            log.severe(String.format("%s/%s MarketOddsReportWorkers found to be null.",
                    n_null, marketOddsReportWorkers.size()));

            safe_exit();

            List<String> workernames = new ArrayList<>();
            for (MarketOddsReportWorker morw: marketOddsReportWorkers){
                if (morw == null){
                    workernames.add("null");
                }
                else{
                    workernames.add(morw.thread.getName());
                }
            }
            print(workernames);
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

        // Begin thread to update site details periodically.
        siteAccountInfoUpdater = new SiteAccountInfoUpdater(siteObjects);
        siteAccountInfoUpdater.start();

        // Begin thread to update site sessions periodically
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
        log.info("Master safe exit triggered. Closing down program.");

        exit_flag = true;
        if (sessionsUpdater != null){ sessionsUpdater.exit_flag = true;}
        if (sportDataFileSaver != null){ sportDataFileSaver.exit_flag = true;}
        if (siteAccountInfoUpdater != null){ siteAccountInfoUpdater.exit_flag = true;}

        if (eventTraderSpawns != null) {
            for (EventTraderSpawn ets : eventTraderSpawns) {
                ets.exit_flag = true;
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

    }


    public class SiteAccountInfoUpdater implements Runnable{

        public long seconds_sleep = 10;
        public Thread thread;
        public boolean exit_flag;
        public Map<String, BettingSite> siteObjects;

        public SiteAccountInfoUpdater(Map<String, BettingSite> siteObjects){
            this.siteObjects = siteObjects;
            exit_flag = false;

            thread = new Thread(this);
            thread.setName("SteInfoUpdtr");
        }

        public void start(){
            thread.start();
        }

        @Override
        public void run() {
            mainloop:
            while (!exit_flag){
                try {

                    Instant next_update = Instant.now().plus(seconds_sleep, ChronoUnit.SECONDS);
                    while (Instant.now().isBefore(next_update)){
                        if (exit_flag){
                            break mainloop;
                        }
                        Thread.sleep(1000);
                    }

                    // Update each sites account info
                    for (Map.Entry<String, BettingSite> entry: siteObjects.entrySet()){
                        entry.getValue().updateAccountInfo();
                    }

                } catch (InterruptedException | IOException | URISyntaxException e) {
                    e.printStackTrace();
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

        @Override
        public void run() {

            while (!exit_flag){

                // Get event from queue, break and finish if anything goes wrong.
                FootballMatch footballMatch = null;
                try {
                    footballMatch = match_queue.poll(0, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
                if (footballMatch == null || exit_flag){
                    break;
                }

                log.info(String.format("Attempting to verify and setup %s.", footballMatch));

                // Attempt to setup site event trackers for all sites for this event, try queue again if fails
                EventTrader eventTrader = new EventTrader(sportsTrader, footballMatch, siteObjects, footballBetGenerator);

                int successful_site_connections = eventTrader.setupMatch();
                if (successful_site_connections < config.MIN_SITES_PER_MATCH){
                    log.warning(String.format("%s Only %d/%d sites connected. MIN_SITES_PER_MATCH=%d.",
                            footballMatch, successful_site_connections, siteObjects.size(), config.MIN_SITES_PER_MATCH));
                    continue;
                }

                // Set result and break loop
                log.info(String.format("EventTraderSetup complete for event %s.", footballMatch));
                result = eventTrader;
                break;
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

        public SessionsUpdater(Map<String, BettingSite> siteObjects){
            this.siteObjects = siteObjects;
            exit_flag = false;

            thread = new Thread(this);
            thread.setName("SsionUpdtr");
        }

        public void start(){
            thread.start();
        }

        @Override
        public void run() {
            log.info("Session updater started.");

            mainloop:
            while (!exit_flag){
                try {
                    // Sleep in 1 second intervals until next update time has arrived.
                    Instant sleep_until = Instant.now().plus(session_Update_interval_hours, ChronoUnit.HOURS);
                    while (Instant.now().isBefore(sleep_until) && !exit_flag){
                        Thread.sleep(1000);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (exit_flag){
                    break mainloop;
                }

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

        @Override
        public void run() {

            Instant min_next_save = null;

            mainloop:
            while (!exit_flag){
                try{
                    // Wait for a save request
                    while (sportData.save_requests_queue.poll(1, TimeUnit.SECONDS) == null
                            && !exit_flag){}


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
                    e.printStackTrace();
                }
            }
            log.info("Exiting File saver.");
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
    }

}
