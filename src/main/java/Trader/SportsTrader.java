package Trader;

import Bet.Bet;
import Bet.FootballBet.FootballBetGenerator;
import Bet.FootballBet.FootballHandicapBet;
import SiteConnectors.*;
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

    private static final Logger log = Logger.getLogger(SportsTrader.class.getName());
    private static final SportData sportData = new FlashScores();

    public int MAX_MATCHES;
    public int MIN_SITES_PER_MATCH;
    public boolean IN_PLAY;
    public int HOURS_AHEAD;
    public boolean CHECK_MARKETS;
    public boolean PLACE_BETS;
    public long RATE_LIMIT;
    public BigDecimal MIN_ODDS_RATIO;
    public Map<String, Boolean> ACTIVE_SITES;
    public String EVENT_SOURCE;
    public BigDecimal MAX_INVESTMENT;
    public BigDecimal MIN_PROFIT_RATIO;
    public boolean END_ON_BET;
    public BigDecimal TARGET_INVESTMENT;
    public long REQUEST_TIMEOUT;
    public boolean RUN_STATS;
    public boolean SINGLE_MATCH_TEST;
    public String SM_NAME;
    public String SM_TIME;
    public long RATE_LOCKSTEP_INTERVAL;
    public String LOG_LEVEL;


    public Lock betlock = new ReentrantLock();

    public ArrayList<Class> siteClasses;
    public Map<String, BettingSite> siteObjects;
    public int session_Update_interval_hours = 4; // in hours

    public FootballBetGenerator footballBetGenerator;

    public ArrayList<EventTrader> eventTraders;
    public ArrayList<EventTraderSpawn> eventTraderSpawns;
    public SportDataFileSaver sportDataFileSaver;
    public SessionsUpdater sessionsUpdater;
    public SiteAccountInfoUpdater siteAccountInfoUpdater;
    public SportsTraderStats stats;

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

        setupConfig("config.json");
        log.setLevel(Level.parse(LOG_LEVEL));

        siteClasses = new ArrayList<Class>();
        siteClasses.add(Betfair.class);
        siteClasses.add(Matchbook.class);
        siteClasses.add(Smarkets.class);


        siteObjects = new HashMap<>();
        eventTraders = new ArrayList<>();

        marketOddsReportWorkers = new ArrayList<>();
        marketOddsReportRequestQueue = new LinkedBlockingQueue<>();

        psr p = new psr();
        new Thread(p).start();

    }

    public class psr implements Runnable {
        @Override
        public void run() {
            while (true){
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (exit_flag) {
                    break;
                }

                Map<String, Map<String, Integer>> ss =
                        MarketOddsReportWorker.site_sums(marketOddsReportWorkers);

                if (ss.size() > 0){
                    print(ss);
                }
            }
        }
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


    private void setupConfig(String config_filename) throws FileNotFoundException,
            ConfigException, org.json.simple.parser.ParseException {

        JSONObject config = null;
        String config_string = getResourceFileString(config_filename);
        config = (JSONObject) new JSONParser().parse(config_string);

        // Log the config of this execution
        log.info(String.format("Loading config file:\n%s", config_string));

        String[] required = new String[] {"MAX_MATCHES", "IN_PLAY", "HOURS_AHEAD", "CHECK_MARKETS",
                "PLACE_BETS", "RATE_LIMIT", "ACTIVE_SITES", "MIN_ODDS_RATIO", "MIN_SITES_PER_MATCH",
                "EVENT_SOURCE", "MAX_INVESTMENT", "MIN_PROFIT_RATIO", "END_ON_BET", "TARGET_INVESTMENT",
                "REQUEST_TIMEOUT", "RUN_STATS", "SINGLE_MATCH_TEST", "SM_NAME", "SM_TIME", "RATE_LOCKSTEP_INTERVAL",
                "LOG_LEVEL", "SMARKETS_REQ_SIZE", "SMARKETS_RH_WAIT", "BETFAIR_RH_WAIT", "MATCHBOOK_RH_WAIT"};

        List<String> missingFields = new ArrayList<>();
        for (String field: required){
            if (!(config.keySet().contains(field))){
                missingFields.add(field);
            }
        }
        if (missingFields.size() > 0){
            throw new ConfigException(missingFields);
        }

        MAX_MATCHES = ((Long) config.get("MAX_MATCHES")).intValue();
        MIN_SITES_PER_MATCH = ((Long) config.get("MIN_SITES_PER_MATCH")).intValue();
        IN_PLAY = (boolean) config.get("IN_PLAY");
        HOURS_AHEAD = ((Long) config.get("HOURS_AHEAD")).intValue();
        CHECK_MARKETS = (boolean) config.get("CHECK_MARKETS");
        PLACE_BETS = (boolean) config.get("PLACE_BETS");
        ACTIVE_SITES = (Map<String, Boolean>) config.get("ACTIVE_SITES");
        RATE_LIMIT = ((Long) config.get("RATE_LIMIT"));
        MIN_ODDS_RATIO = new BigDecimal(String.valueOf((Double) config.get("MIN_ODDS_RATIO")));
        EVENT_SOURCE = (String) config.get("EVENT_SOURCE");
        MAX_INVESTMENT = new BigDecimal(String.valueOf((Double) config.get("MAX_INVESTMENT")));
        MIN_PROFIT_RATIO = new BigDecimal(String.valueOf((Double) config.get("MIN_PROFIT_RATIO")));
        END_ON_BET = (boolean) config.get("END_ON_BET");
        TARGET_INVESTMENT = new BigDecimal(String.valueOf((Double) config.get("TARGET_INVESTMENT")));
        REQUEST_TIMEOUT = ((Long) config.get("REQUEST_TIMEOUT"));
        RUN_STATS = (boolean) config.get("RUN_STATS");
        SINGLE_MATCH_TEST = (boolean) config.get("SINGLE_MATCH_TEST");
        SM_NAME = (String) config.get("SM_NAME");
        SM_TIME = (String) config.get("SM_TIME");
        RATE_LOCKSTEP_INTERVAL = ((Long) config.get("RATE_LOCKSTEP_INTERVAL"));
        LOG_LEVEL = ((String) config.get("LOG_LEVEL")).toUpperCase();


        // Check target inv per bet is lower than max investment per bet
        if (TARGET_INVESTMENT.compareTo(MAX_INVESTMENT) == 1){
            String msg = String.format("TARGET_INVESTMENT (%s) is higher than MAX_INVESTMENT (%s). Exiting",
                    TARGET_INVESTMENT.toString(), MAX_INVESTMENT.toString());
            log.severe(msg);
            throw new ConfigException(msg);
        }

        // Check number active sites is not lower than min number of sites per match
        int number_active_sites = 0;
        for (Map.Entry<String, Boolean> entry: ACTIVE_SITES.entrySet()){
            if (entry.getValue()){
                number_active_sites += 1;
            }
        }
        if (number_active_sites < MIN_SITES_PER_MATCH){
            String msg = String.format("MIN_SITES_PER_MATCH (%d) is lower than number of active sites (%d). Exiting",
                    MIN_SITES_PER_MATCH, number_active_sites);
            log.severe(msg);
            throw new ConfigException(msg);
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
            if (!ACTIVE_SITES.containsKey(site_name)){
                log.severe("Site %s appears in class list but has no config entry. Skipping.");
                return null;
            }

            // Check config is set as active for this class
            boolean site_active = ACTIVE_SITES.get(site_name);
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


        // Run stats keeper
        if (RUN_STATS){
            stats = new SportsTraderStats("stats.json", footballBetGenerator);
        }

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
            if (SINGLE_MATCH_TEST) {
                footballMatches = new ArrayList<>();
                FootballMatch fm = FootballMatch.parse(SM_TIME, SM_NAME);
                footballMatches.add(fm);
                log.info(String.format("Using %s match for testing.", fm.name));
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
            return;
        }


        // Create football match job queue and fill
        BlockingQueue<FootballMatch> match_queue = new LinkedBlockingQueue<>();
        for (FootballMatch fm: footballMatches){
            match_queue.add(fm);
        }

        // Create same number of event trader setups as max matches allowed to concurrently
        // setup each event trader
        eventTraderSpawns = new ArrayList<>();
        for (int i=0; i<MAX_MATCHES; i++){
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
                eventTraders.size(), MIN_SITES_PER_MATCH));

        // Exit if none have worked.
        if (eventTraders.size() == 0){
            log.severe("0 matches have been setup correctly. Exiting.");
            return;
        }


        // End if config says so.
        if (!CHECK_MARKETS){
            log.info("CHECK_MARKETS set to false. Ending here.");
            return;
        }


        // Start stats keeper
        if (RUN_STATS) {
            stats.start();
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
                Thread.sleep(1000);
            } catch (InterruptedException e) {
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
        if (stats != null){ stats.exit_flag = true;}

        if (eventTraderSpawns != null) {
            for (EventTraderSpawn ets : eventTraderSpawns) {
                ets.exit_flag = true;
            }
        }

        if (siteObjects != null) {
            for (Map.Entry<String, BettingSite> entry : siteObjects.entrySet()) {
                entry.getValue().exit_flag = true;
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

        // Should check match queue and attempt to create an event trader fully setup
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

                // Get match from queue, break and finish if anything goes wrong.
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

                // Attempt to setup site event trackers for all sites for this match, try queue again if fails
                EventTrader eventTrader = new EventTrader(sportsTrader, footballMatch, siteObjects, footballBetGenerator);

                int successful_site_connections = eventTrader.setupMatch();
                if (successful_site_connections < MIN_SITES_PER_MATCH){
                    log.warning(String.format("%s Only %d/%d sites connected. MIN_SITES_PER_MATCH=%d.",
                            footballMatch, successful_site_connections, siteObjects.size(), MIN_SITES_PER_MATCH));
                    continue;
                }

                // Set result and break loop
                log.info(String.format("EventTraderSetup complete for match %s.", footballMatch));
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

        String site_name = EVENT_SOURCE;
        BettingSite site = siteObjects.get(site_name);
        if (site == null){
            log.severe(String.format("EVENT_SOURCE '%s' cannot be found. It may not bet set in ACTIVE_SITES.",
                    EVENT_SOURCE));
            return null;
        }

        Instant from;
        Instant until;
        Instant now = Instant.now();

        // Find time frame to search in
        if (IN_PLAY){
            from = now.minus(5, ChronoUnit.HOURS);
            log.info(String.format("Searching for matches from %s, %d hours ahead and in-play.",
                    site_name, HOURS_AHEAD));
        } else{
            from = now;
            log.info(String.format("Searching for matches from %s, %d hours ahead.",
                    site_name, HOURS_AHEAD));
        }
        until = now.plus(HOURS_AHEAD, ChronoUnit.HOURS);

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
