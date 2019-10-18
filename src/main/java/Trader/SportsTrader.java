package Trader;

import Bet.FootballBet.FootballBetGenerator;
import SiteConnectors.*;
import SiteConnectors.Betfair.Betfair;
import SiteConnectors.Matchbook.Matchbook;
import SiteConnectors.Smarkets.Smarkets;
import Sport.FootballMatch;
import com.google.gson.JsonSyntaxException;
import org.json.simple.JSONObject;
import tools.MyLogHandler;

import java.io.IOException;
import java.math.BigDecimal;
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

    public Lock betlock = new ReentrantLock();

    public HashMap<String, Class> siteClasses;
    public HashMap<String, BettingSite> siteObjects;
    public int session_Update_interval_hours = 4; // in hours

    public FootballBetGenerator footballBetGenerator;
    public ArrayList<EventTrader> eventTraders;
    SessionsUpdater sessionsUpdater;

    public SportDataFileSaver sportDataFileSaver;

    public boolean exit_all;


    public SportsTrader(){
        Thread.currentThread().setName("Main");
        log.setUseParentHandlers(false);
        log.setLevel(Level.INFO);
        try {
            log.addHandler(new MyLogHandler());
        } catch (Exception e) {
            e.printStackTrace();
            String msg = "Error Setting up logging. Exiting";
            print(msg);
            log.severe(msg);
            System.exit(1);
        }

        try {
            setupConfig("config.json");
        } catch (Exception e) {
            e.printStackTrace();
            log.severe("Invalid config file. Exiting...");
            System.exit(1);
        }

        exit_all = false;

        siteClasses = new HashMap<String, Class>();
        siteClasses.put("betfair", Betfair.class);
        siteClasses.put("matchbook", Matchbook.class);
        siteClasses.put("smarkets", Smarkets.class);

        siteObjects = new HashMap<String, BettingSite>();
        eventTraders = new ArrayList<EventTrader>();

        sportDataFileSaver = new SportDataFileSaver(sportData);
        sportDataFileSaver.start();

    }


    public static SportData getSportData(){
        return sportData;
    }


    private void setupConfig(String config_filename) throws Exception {
        Map config = null;
        try{
            config = getJSONResourceMap(config_filename);
        } catch (JsonSyntaxException e){
            log.severe("Config JSON syntax error.");
            System.exit(0);
        }

        String[] required = new String[] {"MAX_MATCHES", "IN_PLAY", "HOURS_AHEAD", "CHECK_MARKETS",
                "PLACE_BETS", "RATE_LIMIT", "ACTIVE_SITES", "MIN_ODDS_RATIO", "MIN_SITES_PER_MATCH",
                "EVENT_SOURCE", "MAX_INVESTMENT", "MIN_PROFIT_RATIO", "END_ON_BET", "TARGET_INVESTMENT",
                "REQUEST_TIMEOUT"};

        for (String field: required){
            if (!(config.keySet().contains(field))){
                String msg = String.format("Config file does not contain field '%s'", field);
                log.severe(msg);
                throw new Exception(msg);
            }
        }

        MAX_MATCHES = ((Double) config.get("MAX_MATCHES")).intValue();
        MIN_SITES_PER_MATCH = ((Double) config.get("MIN_SITES_PER_MATCH")).intValue();
        IN_PLAY = (boolean) config.get("IN_PLAY");
        HOURS_AHEAD = ((Double) config.get("HOURS_AHEAD")).intValue();
        CHECK_MARKETS = (boolean) config.get("CHECK_MARKETS");
        PLACE_BETS = (boolean) config.get("PLACE_BETS");
        ACTIVE_SITES = (Map<String, Boolean>) config.get("ACTIVE_SITES");
        RATE_LIMIT = (long) ((Double) config.get("RATE_LIMIT")).intValue();
        MIN_ODDS_RATIO = new BigDecimal(String.valueOf((Double) config.get("MIN_ODDS_RATIO")));
        EVENT_SOURCE = (String) config.get("EVENT_SOURCE");
        MAX_INVESTMENT = new BigDecimal(String.valueOf((Double) config.get("MAX_INVESTMENT")));
        MIN_PROFIT_RATIO = new BigDecimal(String.valueOf((Double) config.get("MIN_PROFIT_RATIO")));
        END_ON_BET = (boolean) config.get("END_ON_BET");
        TARGET_INVESTMENT = new BigDecimal(String.valueOf((Double) config.get("TARGET_INVESTMENT")));
        REQUEST_TIMEOUT = (long) ((Double) config.get("REQUEST_TIMEOUT")).intValue();


        if (TARGET_INVESTMENT.compareTo(MAX_INVESTMENT) == 1){
            log.severe(String.format("TARGET_INVESTMENT (%s) is higher than MAX_INVESTMENT (%s). Exiting",
                    TARGET_INVESTMENT.toString(), MAX_INVESTMENT.toString()));
            System.exit(0);
        }


        JSONObject config_json = new JSONObject(config);
        log.info(String.format("Configuration\n%s", ps(config_json)));
    }


    public void run(){
        log.info("Running SportsTrader.");

        // Run bet/taut generator
        footballBetGenerator = new FootballBetGenerator();


        // Initialize site object for each site class and add to map
        for (Map.Entry<String, Class> entry : siteClasses.entrySet() ) {
            String site_name = entry.getKey();
            Class site_class = entry.getValue();


            // Check site appears in config and is set to active
            if (!ACTIVE_SITES.containsKey(site_name)){
                log.severe("Site %s appears in class list but has no config entry. Skipping.");
                continue;
            }
            boolean site_active = ACTIVE_SITES.get(site_name);
            if (!site_active){
                log.info(String.format("Site %s not activated in config. Skipping.", site_name));
                continue;
            }

            // Initialize Site object
            try {
                //BettingSite site_obj = (BettingSite) site_class.getConstructor().newInstance();
                BettingSite site_obj =  (BettingSite) Class.forName(site_class.getName()).newInstance();
                siteObjects.put(site_name, site_obj);

            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
                log.severe(String.format("Error instantiating site object for %s", site_name));
                continue;
            }

            log.info(String.format("Successfully setup betting site connector for %s.", site_name));
        }


        // Exit if no sites have worked.
        if (siteObjects.size() <= 0){
            log.severe("None of the sites could be instantiated, finishing.");
            return;
        }


        // Collect initial football matches
        ArrayList<FootballMatch> footballMatches = null;
        try {
            footballMatches = getFootballMatches();
        } catch (IOException | URISyntaxException | InterruptedException e) {
            e.printStackTrace();
            log.severe("Error getting initial football matches. Exiting...");
            System.exit(1);
        }
        log.info(String.format("Found %d matches within given timeframe.", footballMatches.size()));


        // (For testing) Remove all matches and add single manually inputted match
        if (false) {
            footballMatches.clear();
            FootballMatch fm = new FootballMatch("2019-10-19T14:00:00.000Z", "Tottenham hotspur", "watford");
            footballMatches.add(fm);
        }


        // Create football match job queue and fill
        BlockingQueue<FootballMatch> match_queue = new LinkedBlockingQueue<>();
        for (FootballMatch fm: footballMatches){
            match_queue.add(fm);
        }

        // Create same number of event trader setups as max matches allowed to concurrently
        // setup each event trader
        ArrayList<EventTraderSetup> eventTraderSetups = new ArrayList<>();
        for (int i=0; i<MAX_MATCHES; i++){

            EventTraderSetup eventTraderSetup = new EventTraderSetup(this, match_queue);
            eventTraderSetup.thread = new Thread(eventTraderSetup);
            eventTraderSetup.thread.setName("EvntTderStp" + String.valueOf(i+1));
            eventTraderSetup.thread.start();
            eventTraderSetups.add(eventTraderSetup);
        }

        // Wait for each event trader setup to finish then add the result to the list of EventTraders
        for (EventTraderSetup eventTraderSetup: eventTraderSetups){
            try {
                eventTraderSetup.thread.join();
                if (eventTraderSetup.result != null){
                    eventTraders.add(eventTraderSetup.result);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        log.info(String.format("%d matches setup successfully with at least %d site connectors.",
                eventTraders.size(), MIN_SITES_PER_MATCH));

        // Exit if none have worked.
        if (eventTraders.size() == 0){
            log.severe("0 matches have been setup correctly. Exiting.");
            System.exit(0);
        }


        // End if config says so.
        if (!CHECK_MARKETS){
            log.info("CHECK_MARKETS set to false. Ending here.");
            System.exit(0);
        }


        // Run all event traders
        for (EventTrader eventTrader: eventTraders){
            eventTrader.thread = new Thread(eventTrader);
            eventTrader.thread.setName("ET: " + eventTrader.match.name);
            eventTrader.thread.start();
        }
        log.info("All Event Traders started.");

        // Start the session updater to keep all sites connected.
        sessionsUpdater = new SessionsUpdater();
        Thread sessionUpdaterThread = new Thread(sessionsUpdater);
        sessionUpdaterThread.setName("Session Updater");
        sessionUpdaterThread.start();


    }


    public void safe_exit(){
        exit_all = true;
        sessionsUpdater.exit_flag = true;
    }


    public class EventTraderSetup implements Runnable{

        // Should check match queue and attempt to create an event trader fully setup
        // One made per max event traders present and try until complete.

        BlockingQueue<FootballMatch> match_queue;
        EventTrader result;
        SportsTrader sportsTrader;
        Thread thread;

        public EventTraderSetup(SportsTrader sportsTrader, BlockingQueue<FootballMatch> match_queue){
            this.match_queue = match_queue;
            this.sportsTrader = sportsTrader;
        }

        @Override
        public void run() {
            while (true){

                // Get match from queue, break and finish if anything goes wrong.
                FootballMatch footballMatch = null;
                try {
                    footballMatch = match_queue.poll(0, TimeUnit.MILLISECONDS);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                } if (footballMatch == null){
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
        }
    }


    public class SessionsUpdater implements Runnable {

        public boolean exit_flag;

        public SessionsUpdater(){
            exit_flag = false;
        }

        @Override
        public void run() {
            log.info("Session updater started.");

            while (!exit_flag){
                try {
                    // Sleep in 1 second intervals until next update time has arrived.
                    Instant sleep_until = Instant.now().plus(session_Update_interval_hours, ChronoUnit.HOURS);
                    while (Instant.now().isBefore(sleep_until)){
                        Thread.sleep(1000);
                        if (exit_flag){
                            return;
                        }
                    }


                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                for (Map.Entry<String, BettingSite> entry: siteObjects.entrySet()){
                    String site_name = entry.getKey();
                    BettingSite bet_site = entry.getValue();

                    try {
                        bet_site.login();
                        log.info(String.format("Successfully refreshed session for %s", site_name));
                    } catch (CertificateException | UnrecoverableKeyException | NoSuchAlgorithmException |
                            KeyStoreException | KeyManagementException | IOException | URISyntaxException |
                            InterruptedException e) {

                        e.printStackTrace();

                        String msg = String.format("Error while trying to refresh session for %s.", site_name);
                        log.severe(msg);
                    }

                }
            }

        }
    }


    public class SportDataFileSaver implements Runnable{

        public long min_save_interval_mins = 2;

        public SportData sportData;
        public Thread thread;

        public SportDataFileSaver(SportData sportData){
            this.sportData = sportData;

            thread.setName("FS-Filesaver");
            thread = new Thread(this);
        }

        public void start(){
            thread.start();
        }

        @Override
        public void run() {

            Instant min_next_save = null;

            while (true){
                try{
                    // Wait for a save request
                    sportData.save_requests_queue.take();

                    // Wait until at least the minumym time until next save is reached
                    if (min_next_save != null && Instant.now().isBefore(min_next_save)){
                        long sleeptime = min_next_save.toEpochMilli() - Instant.now().toEpochMilli();
                        Thread.sleep(sleeptime);
                    }

                    // Save and clear save requests and set next min time to save
                    sportData.save_all();
                    sportData.save_requests_queue.clear();
                    min_next_save = Instant.now().plus(min_save_interval_mins, ChronoUnit.MINUTES);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private ArrayList<FootballMatch> getFootballMatches() throws IOException, URISyntaxException,
            InterruptedException {

        String site_name = EVENT_SOURCE;
        BettingSite site = siteObjects.get(site_name);
        if (site == null){
            log.severe(String.format("EVENT_SOURCE '%s' cannot be found. It may not bet set in ACTIVE_SITES.",
                    EVENT_SOURCE));
            System.exit(0);
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
        ArrayList<FootballMatch> fms = site.getFootballMatches(from, until);

        return fms;
     }



    public static void main(String[] args){
        SportsTrader st = new SportsTrader();
        st.run();
    }

}
