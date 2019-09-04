package Trader;

import Bet.*;
import Bet.FootballBet.FootballBet;
import Bet.FootballBet.FootballBetGenerator;
import SiteConnectors.*;
import Sport.FootballMatch;
import com.google.gson.internal.LinkedTreeMap;
import org.apache.commons.logging.Log;
import org.json.simple.JSONObject;
import tools.MyLogHandler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;
import static tools.printer.*;

public class SportsTrader {

    private static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    public int MAX_MATCHES;
    public int MIN_SITES_PER_MATCH;
    public boolean IN_PLAY;
    public int HOURS_AHEAD;
    public boolean PLACE_BETS;
    public Map<String, Boolean> ACTIVE_SITES;


    public HashMap<String, Class> siteClasses;
    public HashMap<String, BettingSite> siteObjects;
    public int session_Update_interval = 4; // in hours

    public FootballBetGenerator footballBetGenerator;
    public ArrayList<EventTrader> eventTraders;


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

        siteClasses = new HashMap<String, Class>();
        siteClasses.put("betfair", Betfair.class);
        siteClasses.put("matchbook", Matchbook.class);

        siteObjects = new HashMap<String, BettingSite>();
        eventTraders = new ArrayList<EventTrader>();
    }


    private void setupConfig(String config_filename) throws Exception {
        Map config = getJSONResource(config_filename);
        String[] required = new String[] {"MAX_MATCHES", "IN_PLAY", "HOURS_AHEAD", "PLACE_BETS", "ACTIVE_SITES",
                "MIN_SITES_PER_MATCH"};

        log.info(String.format("Setting up config %s", config.toString()));

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
        PLACE_BETS = (boolean) config.get("PLACE_BETS");
        ACTIVE_SITES = (Map<String, Boolean>) config.get("ACTIVE_SITES");
    }


    public void run(){
        log.info("Running SportsTrader.");

        // Run bet/taut generator
        footballBetGenerator = new FootballBetGenerator();

        // Initialize site object for each site class and add to map
        for (Map.Entry<String, Class> entry : siteClasses.entrySet() ) {
            String site_name = entry.getKey();
            Class site_class = entry.getValue();

            // Check config status of this site
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
            log.severe("None of the sites could be instantiated, exiting");
            return;
        }

        // Collect initial football matches
        ArrayList<FootballMatch> footballMatches = null;
        try {
            footballMatches = getFootballMatches();
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            log.severe("Error getting initial football matches. Exiting...");
            System.exit(1);
        }
        log.info(String.format("Found %d matches within given timeframe.", footballMatches.size()));


        // Create Event Trader for each match found.
        ArrayList<EventTrader.SetupMatchRunner> setupRunners = new ArrayList<>();
        for (FootballMatch match: footballMatches){
            EventTrader eventTrader = new EventTrader(match, siteObjects, footballBetGenerator);

            // Create threads to run the setups of the event traders
            EventTrader.SetupMatchRunner eventTraderSetup = new EventTrader.SetupMatchRunner(eventTrader);
            eventTraderSetup.thread = new Thread(eventTraderSetup);
            eventTraderSetup.thread.setName("SU: " + match.name);
            setupRunners.add(eventTraderSetup);
            eventTraderSetup.thread.start();
        }

        // Wait for setups to complete and add the event trader to the list if the setup
        // successfully connected to enough sites.
        for (EventTrader.SetupMatchRunner setupRunner: setupRunners){
            try {
                setupRunner.thread.join();
            } catch (InterruptedException e) {
                log.severe(String.format("Interrupt while setting up match for %s.",
                        setupRunner.eventTrader.match.toString()));
                continue;
            }

            // When a setuprunner thread is done, add it's eventTrader to the list if it
            // reached the min number of connected sites.
            if (setupRunner.sites_connected != null && setupRunner.sites_connected >= MIN_SITES_PER_MATCH){
                eventTraders.add(setupRunner.eventTrader);
            }
            if (eventTraders.size() >= MAX_MATCHES){
                for (EventTrader.SetupMatchRunner sur: setupRunners){
                    sur.cancel();
                }
                break;
            }
        }
        log.info(String.format("%d matches setup successfully with at least %d site connectors.",
                eventTraders.size(), MIN_SITES_PER_MATCH));

        // Exit if none have worked.
        if (eventTraders.size() == 0){
            log.severe("0 matches have been setup correctly. Exiting.");
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
        SessionsUpdater sessionsUpdater = new SessionsUpdater();
        Thread sessionUpdaterThread = new Thread(sessionsUpdater);
        sessionUpdaterThread.setName("Session Updater");
        sessionUpdaterThread.start();


    }


    public class SessionsUpdater implements Runnable {

        @Override
        public void run() {
            log.info("Session updater started.");

            while (true){
                try {
                    // Sleep for required amount of hours between updates
                    sleep(1000*60*60*session_Update_interval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                for (Map.Entry<String, BettingSite> entry: siteObjects.entrySet()){
                    String site_name = entry.getKey();
                    BettingSite bet_site = entry.getValue();

                    try {
                        bet_site.login();
                        log.info(String.format("Successfully refreshed session for %s", site_name));
                    } catch (CertificateException | UnrecoverableKeyException | NoSuchAlgorithmException
                            | KeyStoreException | KeyManagementException | IOException | URISyntaxException e) {

                        e.printStackTrace();

                        String msg = String.format("Error while trying to refresh session for %s.", site_name);
                        log.severe(msg);
                    }

                }
            }

        }
    }


    private ArrayList<FootballMatch> getFootballMatches() throws IOException, URISyntaxException {
        String site_name = "matchbook";
        BettingSite site = siteObjects.get(site_name);
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
