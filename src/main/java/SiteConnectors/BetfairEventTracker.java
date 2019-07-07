package SiteConnectors;

import Bet.Bet;
import Sport.FootballMatch;
import Sport.Match;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;

import static tools.printer.p;
import static tools.printer.print;

public class BetfairEventTracker extends SiteEventTracker {

    private static final Logger log = Logger.getLogger(BetfairEventTracker.class.getName());

    public Betfair betfair;

    public String event_id;
    public FootballMatch match;
    public JSONObject eventMarketData;
    public Instant lastMarketDataUpdate;
    public HashMap<String, String> market_name_id_map;
    public Set<String> bet_blacklist;

    public BetfairEventTracker(Betfair BETFAIR){
        betfair = BETFAIR;

        match = null;
        eventMarketData = null;
        lastMarketDataUpdate = null;
        market_name_id_map = new HashMap<String, String>();
        bet_blacklist = new HashSet<String>();

    }


    @Override
    public boolean setupMatch(FootballMatch match) {
        log.info(String.format("Attempting to setup match for %s in betfair.", match.toString()));
        print("Got here");
        Instant start = match.start_time.minus(1, ChronoUnit.SECONDS);
        Instant end = match.start_time.plus(1, ChronoUnit.SECONDS);

        // Build filter for request to get events
        JSONObject time = new JSONObject();
        time.put("from", start.toString());
        time.put("to", end.toString());
        JSONArray event_types = new JSONArray();
        event_types.add(1);
        JSONObject filter = new JSONObject();
        filter.put("marketStartTime", time);
        filter.put("eventTypeIds", event_types);

        JSONArray events = null;
        try {
            events = betfair.getEvents(filter);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            log.warning("Error getting events from betfair.");
            return false;
        }


        // Check each event returned to find matching events
        ArrayList<FootballMatch> matching_events = new ArrayList<>();
        ArrayList<FootballMatch> all_events = new ArrayList<>();
        for (int i=0; i<events.size(); i++){
            JSONObject eventjson = (JSONObject) ((JSONObject) events.get(i)).get("event");

            Instant eventtime = Instant.parse(eventjson.get("openDate").toString());
            String eventname = eventjson.get("name").toString();
            String id = eventjson.get("id").toString();

            FootballMatch possible_match;
            try {
                possible_match = new FootballMatch(eventtime, eventname);
                match.id = id;
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            all_events.add(possible_match);
            if (match.same_match(possible_match)){
                matching_events.add(possible_match);
            }
        }

        // TODO event not matching

        // Error if not only one found
        if (matching_events.size() != 1) {
            log.warning(String.format("No matches found for %s in betfair. Checked %d: %s",
                    match.toString(), all_events.size(), all_events.toString()));
            return false;
        }

        this.match = match;


        JSONArray marketTypeCodes = new JSONArray();
        for (String marketType: betfair.football_market_types){
            marketTypeCodes.add(marketType);
        }

        JSONArray eventIds = new JSONArray();
        eventIds.add(match.id);
        JSONArray marketProjection = new JSONArray();
        marketProjection.add("MARKET_DESCRIPTION");
        marketProjection.add("RUNNER_DESCRIPTION");
        JSONObject filters = new JSONObject();
        filters.put("eventIds", eventIds);
        filters.put("marketTypeCodes", marketTypeCodes);
        JSONObject params = new JSONObject();
        params.put("filters", filters);
        params.put("marketProjection", marketProjection);

        JSONArray markets = null;
        try {
            markets = (JSONArray) betfair.getMarketCatalogue(params);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            log.severe(String.format("Error getting initial catalogue market data for %s in betfair.", match.toString()));
            return false;
        }

        p(markets);

        return true;
    }


    public static void main(String[] args){
        try {
            Betfair b = new Betfair();

            BetfairEventTracker be = (BetfairEventTracker) b.getEventTracker();
            FootballMatch fm = new FootballMatch(Instant.parse("2019-07-08T02:00:00.000Z"), "Mexico", "USA");
            be.setupMatch(fm);








        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }
}
