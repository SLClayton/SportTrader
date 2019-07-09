package SiteConnectors;

import Sport.FootballMatch;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static tools.printer.*;

public class BetfairEventTracker extends SiteEventTracker {

    public Betfair betfair;

    public String event_id;
    public FootballMatch match;
    public HashMap<String, JSONObject> eventMarketData;
    public Instant lastMarketDataUpdate;
    public HashMap<String, String> market_name_id_map;
    public Set<String> bet_blacklist;

    public BetfairEventTracker(Betfair BETFAIR){
        super();

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

        // Error if not only one found
        if (matching_events.size() != 1) {
            log.warning(String.format("No matches found for %s in betfair. Checked %d: %s",
                    match.toString(), all_events.size(), all_events.toString()));
            return false;
        }

        // Match found
        log.info(String.format("Corresponding match found in Betfair for %s", match));
        this.match = match;


        // Build params for market catalogue request
        JSONObject params = new JSONObject();
        JSONArray marketProjection = new JSONArray();
        params.put("marketProjection", marketProjection);
        marketProjection.add("MARKET_DESCRIPTION");
        marketProjection.add("RUNNER_DESCRIPTION");
        JSONObject filters = new JSONObject();
        params.put("filter", filters);
        JSONArray marketTypeCodes = new JSONArray();
        marketTypeCodes.addAll(Arrays.asList(betfair.football_market_types));
        filters.put("marketTypeCodes", marketTypeCodes);
        JSONArray eventIds = new JSONArray();
        eventIds.add(match.id);
        filters.put("eventIds", eventIds);


        // Get market catalogue for this event
        JSONArray markets = null;
        log.info(String.format("Attempting to collect initial market data for %s in Betfair.", match));
        try {
            markets = (JSONArray) betfair.getMarketCatalogue(params);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


        // Initially fill in new market data
        eventMarketData = new HashMap<>();
        for (Object market_obj: markets){
            JSONObject market = (JSONObject) market_obj;
            String market_id = (String) market.get("marketId");
            JSONObject desc = (JSONObject) market.get("description");
            String market_type = desc.get("marketType").toString();

            eventMarketData.put(market_id, market);
            market_name_id_map.put(market_type, market_id);
        }

        return true;
    }


    public void updateMarketData() throws Exception {

        JSONObject OLD = (JSONObject) new JSONObject(eventMarketData).clone();
        p(OLD, "outputold.json");

        // Get new market data for markets in this event
        JSONArray market_odds = betfair.getMarketOdds(eventMarketData.keySet());

        // Update the runners in the existing market data
        for (Object new_md_obj: market_odds){
            JSONObject new_md = (JSONObject) new_md_obj;

            String market_id = (String) new_md.get("marketId");

            JSONArray new_runners = (JSONArray) new_md.get("runners");
            JSONArray old_runners = (JSONArray) eventMarketData.get(market_id).get("runners");

            for (Object this_runner_obj: new_runners){
                JSONObject this_runner = (JSONObject) this_runner_obj;
                Long runner_id = (Long) this_runner.get("selectionId");

                // Find matching old runner from the new
                JSONObject old_runner = null;
                for (Object r_ojb: old_runners){
                    JSONObject r = (JSONObject) r_ojb;

                    if (((Long) r.get("selectionId")).equals(runner_id)){
                        old_runner = r;
                        break;
                    }
                }
                if (old_runner == null){
                    throw new Exception(String.format("New runner mismatch old runners.\nold\n%s\nnew\n%s", ps(old_runners), ps(new_runners)));
                }

                // Overwrite new values in the original runner
                Set<String> keys = this_runner.keySet();
                for (String key: keys){
                    Object new_value = this_runner.get(key);
                    old_runner.put(key, new_value);
                }

            }

        }
        lastMarketDataUpdate = Instant.now();
        // Market is updated during the code so no need to set anything at the end.
    }


}
