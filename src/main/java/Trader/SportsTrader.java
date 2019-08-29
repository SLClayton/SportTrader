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

    public int MAX_MATCHES;
    public boolean IN_PLAY;
    public int HOURS_AHEAD;
    public boolean PLACE_BETS;
    public Map<String, Boolean> ACTIVE_SITES;

    private static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    public HashMap<String, Class> siteClasses;
    public HashMap<String, BettingSite> siteObjects;

    public FootballBetGenerator footballBetGenerator;
    public ArrayList<EventTrader> eventTraders;


    public SportsTrader(){
        log.setUseParentHandlers(false);
        log.setLevel(Level.INFO);
        log.addHandler(new MyLogHandler());


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
        String[] required = new String[] {"MAX_MATCHES", "IN_PLAY", "HOURS_AHEAD", "PLACE_BETS", "ACTIVE_SITES"};

        log.info(String.format("Setting up config %s", config.toString()));

        for (String field: required){
            if (!(config.keySet().contains(field))){
                String msg = String.format("Config file does not contain field '%s'", field);
                log.severe(msg);
                throw new Exception(msg);
            }
        }

        MAX_MATCHES = ((Double) config.get("MAX_MATCHES")).intValue();
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
        int total_matches_found = footballMatches.size();
        if (MAX_MATCHES > 0 && MAX_MATCHES < total_matches_found){
            footballMatches = new ArrayList<FootballMatch>(footballMatches.subList(0, MAX_MATCHES));
        }
        footballMatches.trimToSize();
        log.info(String.format("Using %d football matches.", footballMatches.size()));


        // Launch new Event Trader for each match found.
        for (FootballMatch match: footballMatches){
            EventTrader eventTrader = new EventTrader(match, siteObjects, footballBetGenerator);
            Thread evenTraderThread = new Thread(eventTrader);
            eventTrader.thread = evenTraderThread;
            eventTraders.add(eventTrader);
            evenTraderThread.setName("ET - " + match.name);
            evenTraderThread.start();
        }

        log.info("All Event Traders spawned.");
    }


    private ArrayList<FootballMatch> getFootballMatches() throws IOException, URISyntaxException {
        BettingSite site = siteObjects.get("matchbook");
        Instant from;
        Instant until;
        Instant now = Instant.now();


        if (IN_PLAY){
            from = now.minus(3, ChronoUnit.HOURS);
            log.info(String.format("Searching for matches %d hours ahead and in-play.", HOURS_AHEAD));
        } else{
            from = now;
            log.info(String.format("Searching for matches %d hours ahead.", HOURS_AHEAD));
        }
        until = now.plus(HOURS_AHEAD, ChronoUnit.HOURS);

        ArrayList<FootballMatch> fms = site.getFootballMatches(from, until);
        ArrayList<FootballMatch> final_fms = new ArrayList<>();

        log.info(String.format("Initially found %d matches in time frame.", fms.size()));

        // Make sure each fm has a betfair ID associated. Skip if not and can't find one.
        for (FootballMatch fm: fms){
            if (fm.betfairEventId == null){
                log.info(String.format("Attempting to match betfair ID to %s", fm));
                fm.betfairEventId = Betfair.getEventFromSearch(fm.name, (Betfair) siteObjects.get("betfair"));
                if (fm.betfairEventId == null){
                    continue;
                }
            }
            final_fms.add(fm);
            if (final_fms.size() >= MAX_MATCHES){
                break;
            }
        }

        return final_fms;
     }


    public static void main(String[] args){
        SportsTrader st = new SportsTrader();
        st.run();
    }

}
