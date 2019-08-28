package SiteConnectors;

import Bet.BetOffer;
import Bet.FootballBet.FootballBet;
import Bet.FootballBet.FootballOverUnderBet;
import Bet.FootballBet.FootballResultBet;
import Bet.FootballBet.FootballScoreBet;
import Bet.MarketOddsReport;
import Sport.FootballMatch;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.Normalizer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static tools.printer.*;

public class BetfairEventTracker extends SiteEventTracker {

    public Betfair betfair;

    public String event_id;

    public HashMap<String, JSONObject> eventMarketData;
    public Instant lastMarketDataUpdate;
    public HashMap<String, String> market_name_id_map;

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
    public String name() {
        return betfair.name;
    }


    public boolean setupMatchMatch(FootballMatch setup_match) throws Exception {

        log.info(String.format("Attempting to setup match in betfair for %s.", setup_match.toString()));
        Instant start = setup_match.start_time.minus(1, ChronoUnit.SECONDS);
        Instant end = setup_match.start_time.plus(1, ChronoUnit.SECONDS);

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
            log.warning(String.format("Error getting events from betfair while searching for %s.", setup_match));
            throw e;
        }

        // Check each event returned to find matching events
        ArrayList<FootballMatch> matching_events = new ArrayList<>();
        ArrayList<FootballMatch> all_events = new ArrayList<>();
        for (int i=0; i<events.size(); i++){
            JSONObject eventjson = (JSONObject) ((JSONObject) events.get(i)).get("event");

            Instant eventtime = Instant.parse(eventjson.get("openDate").toString());
            String eventname = eventjson.get("name").toString();
            String id = eventjson.get("id").toString();
            String[] teams = eventname.trim().split(" v ");
            if (teams.length != 2){
                continue;
            }
            String team_a = teams[0];
            String team_b = teams[1];

            FootballMatch possible_match;
            try {
                possible_match = new FootballMatch(eventtime, team_a, team_b);
                possible_match.id = id;
            } catch (Exception e) {
                log.warning(String.format("Failed to setup Football match for '%s' at '%s'.", eventname, eventtime));
                continue;
            }

            all_events.add(possible_match);
            if (setup_match.same_match(possible_match)){
                matching_events.add(possible_match);
            }
        }

        // Error if not only one found
        if (matching_events.size() != 1) {
            String fails = "";
            for (FootballMatch m: all_events){
                fails = fails + " " + m.toString();
            }
            log.warning(String.format("No matches found for %s in betfair. Checked %d: %s",
                    setup_match.toString(), all_events.size(), fails));
            return false;
        }

        // Match found
        log.info(String.format("Corresponding match found in Betfair for %s", setup_match));
        match = matching_events.get(0);

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
            throw e;
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

    @Override
    public boolean setupMatch(FootballMatch setup_match) throws Exception {

        log.info(String.format("Attempting to setup match in Betfair Event Tracker for %s.", setup_match.toString()));
        Instant start = setup_match.start_time.minus(1, ChronoUnit.SECONDS);
        Instant end = setup_match.start_time.plus(1, ChronoUnit.SECONDS);

        // Build filter for request to get events
        JSONObject time = new JSONObject();
        time.put("from", start.toString());
        time.put("to", end.toString());
        JSONArray event_types = new JSONArray();
        event_types.add(1);
        JSONObject filter = new JSONObject();
        filter.put("marketStartTime", time);
        filter.put("eventTypeIds", event_types);
        filter.put("textQuery", Normalizer.normalize(setup_match.team_a + " " + setup_match.team_b, Normalizer.Form.NFD));

        JSONArray events = null;
        try {
            events = betfair.getEvents(filter);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            log.warning(String.format("Error getting events from betfair while searching for %s.", setup_match));
            throw e;
        }

        // Error if not only one found
        if (events.size() != 1) {
            String fails = "";
            for (Object event_obj: events){
                JSONObject event = (JSONObject) event_obj;
                fails = fails + " " + event.get("name").toString();
            }
            if (events.size() == 0){
                log.warning(String.format("Zero events returned in betfair when searching for %s.",
                        setup_match));
            }
            else{
                log.warning(String.format("More than 1 event returned in betfair when searching for %s.\n%s",
                        setup_match, ps(events)));
            }

            return false;
        }

        // Match found
        log.info(String.format("Corresponding match found in betfair for %s", setup_match));
        match = setup_match;

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
            throw e;
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


    @Override
    public void updateMarketOddsReport(FootballBet[] bets) throws Exception {
        if (match == null){
            log.severe("Trying to get market odds report on null event.");
            return;
        }
        // Update the raw data before extracting for report.
        updateMarketData();

        HashMap<String, ArrayList<BetOffer>> full_event_market_report = new HashMap<String, ArrayList<BetOffer>>();

        for (FootballBet bet: bets){
            if (bet_blacklist.contains(bet.id())){
                continue;
            }

            // Extract runner from market data depending on category.
            JSONObject runner = null;
            switch (bet.category) {
                case "RESULT":
                    runner = extractRunnerRESULT(bet);
                    break;
                case "CORRECT-SCORE":
                    runner = extractRunnerSCORE(bet);
                    break;
                case "OVER-UNDER":
                    runner = extractRunnerOVERUNDER(bet);
                    break;
                default:
                    // Nothing
            }

            // Ensure runner is valid
            if (runner == null){
                bet_blacklist.add(bet.id());
                log.fine(String.format("No %s bet found in betfair for %s", bet.id(), match));
                continue;
            }
            else if (!(runner.containsKey("ex")) || !(runner.containsKey("status"))
                    || !(runner.get("status").toString().equals("ACTIVE"))){

                log.warning(String.format("Invalid runner found in bet %s for %s in betfair.", bet, match));
                continue;
            }

            // Get the back or lay odds from the runner
            JSONArray betfair_offers = null;
            if (bet.isLay()){
                betfair_offers = (JSONArray) ((JSONObject) runner.get("ex")).get("availableToLay");
            }
            else{
                betfair_offers = (JSONArray) ((JSONObject) runner.get("ex")).get("availableToBack");
            }

            // Create a list of bet offers from those retrieved
            ArrayList<BetOffer> betOffers = new ArrayList<BetOffer>();
            for (int i=0; i<betfair_offers.size(); i++){
                JSONObject bf_offer = (JSONObject) betfair_offers.get(i);

                BigDecimal odds = new BigDecimal(bf_offer.get("price").toString());
                BigDecimal volume = new BigDecimal(bf_offer.get("size").toString());
                HashMap<String, String> metadata = new HashMap<String, String>();
                metadata.put("selection_id", runner.get("selectionId").toString());
                metadata.put("market_id", runner.get("market_id").toString());

                betOffers.add(new BetOffer(match, bet, betfair, odds, volume, metadata));
            }

            full_event_market_report.put(bet.id(), betOffers);
        }

        marketOddsReport = new MarketOddsReport(full_event_market_report);
    }


    private JSONObject extractRunnerOVERUNDER(FootballBet BET) {
        FootballOverUnderBet bet = (FootballOverUnderBet) BET;
        JSONObject runner = null;
        String bf_market_name = String.format("OVER_UNDER_%s", bet.goals.toString().replace(".", ""));

        // Find market id for this market in this event from map
        String market_id = market_name_id_map.get(bf_market_name);
        if (market_id == null){
            log.fine(String.format("%s not found for %s in market id map.", bf_market_name, match));
            return null;
        }

        JSONArray runners = (JSONArray) eventMarketData.get(market_id).get("runners");
        String runner_name = String.format("%s %s goals", bet.side, bet.goals.toString()).toLowerCase();

        for (Object r_obj: runners){
            JSONObject r = (JSONObject) r_obj;
            if (r.get("runnerName").toString().toLowerCase().equals(runner_name)){
                runner = r;
                break;
            }
        }

        if (runner != null){
            runner.put("market_id", market_id);
        }
        return runner;
    }


    private JSONObject extractRunnerSCORE(FootballBet BET) {
        FootballScoreBet bet = (FootballScoreBet) BET;
        JSONObject runner = null;

        // Find market id for this market in this event from map
        String market_id = market_name_id_map.get("CORRECT_SCORE");
        if (market_id == null){
            log.severe(String.format("CORRECT_SCORE not found for %s in market id map when it should be.", match));
            return null;
        }

        JSONArray runners = (JSONArray) eventMarketData.get(market_id).get("runners");
        String runner_name = String.format("%d - %d", bet.score_a, bet.score_b);

        for (Object r_obj: runners){
            JSONObject r = (JSONObject) r_obj;

            if (((String) r.get("runnerName")).equals(runner_name)){
                runner = r;
                break;
            }
        }

        if (runner != null){
            runner.put("market_id", market_id);
        }

        return runner;
    }


    private JSONObject extractRunnerRESULT(FootballBet BET) {
        //log.fine(String.format("Getting runner result from %s for %s.", match, BET.id()));

        FootballResultBet bet = (FootballResultBet) BET;
        JSONObject runner = null;

        // Find market id for this market in this event from map
        String market_id = market_name_id_map.get("MATCH_ODDS");
        if (market_id == null){
            log.severe(String.format("MATCH_ODDS not found for %s in market id map when it should be.", match));
            return null;
        }



        // Check it has 3 runners TEAMA DRAW and TEAMB
        JSONArray runners = (JSONArray) eventMarketData.get(market_id).get("runners");
        if (runners.size() != 3){
            log.severe(String.format("RESULT market for %s has %d runners and not 3.", match, runners.size()));
            return null;
        }

        if (bet.winnerA()){
            runner = (JSONObject) runners.get(0);
            String team_in_runner = runner.get("runnerName").toString();
            if (!(FootballMatch.same_team(match.team_a, team_in_runner))){
                log.severe(String.format("Betfair runner mismatch with team_a '%s' in RESULT_BET.\n%s",
                        match.team_a, ps(runner)));
                return null;
            }
        }
        else if (bet.winnerB()){
            runner = (JSONObject) runners.get(1);
            String team_in_runner = runner.get("runnerName").toString();
            if (!(FootballMatch.same_team(match.team_b, team_in_runner))){
                log.severe(String.format("Betfair runner mismatch with team_b '%s' in RESULT_BET.\n%s",
                        match.team_b, ps(runner)));
                return null;
            }
        }
        else if (bet.isDraw()){
            runner = (JSONObject) runners.get(2);
            String runner_name = runner.get("runnerName").toString().toLowerCase();
            if (!(runner_name.equals("the draw"))){
                log.severe(String.format("Betfair runner mismatch with 'the draw' / '%s' in RESULT_BET.", runner_name));
                return null;
            }
        }
        else{
            log.severe(String.format("Betfair runner invalid for in RESULT_BET for match %s bet %s.", match, bet));
        }

        if (runner != null){
            runner.put("market_id", market_id);
        }

        return runner;
    }


    public void updateMarketData() throws Exception {

        // Get new market data for markets in this event
        JSONArray market_odds = betfair.getMarketOdds(eventMarketData.keySet());

        // Update the runners in the existing market data
        for (Object new_md_obj: market_odds){
            JSONObject new_md = (JSONObject) new_md_obj;

            String market_id = (String) new_md.get("marketId");

            JSONArray new_runners = (JSONArray) new_md.get("runners");
            JSONObject old_market = eventMarketData.get(market_id);
            if (old_market == null){
                continue;
            }
            JSONArray old_runners = (JSONArray) old_market.get("runners");
            if (old_market == null){
                log.warning("No runners found in previous market for %s");
                continue;
            }

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


    public static void main(String[] args){
        try {
            Betfair bf = new Betfair();
            BetfairEventTracker bet = (BetfairEventTracker) bf.getEventTracker();

            FootballMatch fm = new FootballMatch(Instant.parse("2019-08-28T18:45:00Z"), "Lincoln v Everton");

            bet.setupMatch(fm);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
