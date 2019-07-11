package Trader;

import Bet.*;
import Bet.FootballBet.FootballBet;
import Bet.FootballBet.FootballBetGenerator;
import SiteConnectors.Betfair;
import SiteConnectors.BetfairEventTracker;
import SiteConnectors.BettingSite;
import SiteConnectors.SiteEventTracker;
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

    public static HashMap<String, Class> siteClasses = new HashMap<String, Class>();
    public HashMap<String, BettingSite> siteObjects = new HashMap<String, BettingSite>();

    public FootballBetGenerator footballBetGenerator;
    public ArrayList<EventTrader> eventTraders;


    public SportsTrader(){
        log.setUseParentHandlers(false);
        log.setLevel(Level.ALL);
        log.addHandler(new MyLogHandler());


        try {
            setupConfig("config.json");
        } catch (Exception e) {
            e.printStackTrace();
            log.severe("Invalid config file. Exiting...");
            System.exit(1);
        }

        siteClasses.put("betfair", Betfair.class);
    }

    private void setupConfig(String config_filename) throws Exception {
        Map config = getJSONResource(config_filename);
        String[] required = new String[] {"MAX_MATCHES", "IN_PLAY", "HOURS_AHEAD", "PLACE_BETS", "ACTIVE_SITES"};

        print(config.toString());

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

            try {
                BettingSite site_obj = (BettingSite) site_class.getConstructor().newInstance();
                siteObjects.put(site_name, site_obj);

            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
                log.severe(String.format("Error instanciating site object for %s", site_name));
                continue;
            }

            log.info(String.format("Successfully setup betting site connector for %s.", site_name));
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
        if (MAX_MATCHES > 0){
            footballMatches = (ArrayList<FootballMatch>) footballMatches.subList(0, MAX_MATCHES);
        }
        footballMatches.trimToSize();
        log.info(String.format("%d Football matches found. Using %d.", total_matches_found, footballMatches.size()));


        // Launch new Event Trader for each match found.
        for (FootballMatch match: footballMatches){
            EventTrader eventTrader = new EventTrader(match, siteObjects, footballBetGenerator);
            Thread evenTraderThread = new Thread(eventTrader);
            eventTrader.thread = evenTraderThread;
            eventTraders.add(eventTrader);
            evenTraderThread.start();
        }

        log.info("All Event Traders spawned.");
    }

    private ArrayList<FootballMatch> getFootballMatches() throws IOException, URISyntaxException {
        Betfair bf = (Betfair) siteObjects.get("betfair");
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

        return bf.getFootballMatches(from, until);
     }


    public static void main(String[] args){
        SportsTrader st = new SportsTrader();
        st.run();
    }

}
