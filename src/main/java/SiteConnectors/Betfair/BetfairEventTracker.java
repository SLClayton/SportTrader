package SiteConnectors.Betfair;

import Bet.BetOffer;
import Bet.FootballBet.*;
import Bet.MarketOddsReport;
import SiteConnectors.FlashScores;
import SiteConnectors.SiteEventTracker;
import SiteConnectors.SportData;
import Sport.FootballMatch;
import Sport.FootballTeam;
import Sport.Team;
import Trader.EventTrader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.text.Normalizer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static tools.printer.*;

public class BetfairEventTracker extends SiteEventTracker {

    public static String[] football_market_types = new String[] {
            "TEAM_A_1",
            "TEAM_A_2",
            "TEAM_A_3",
            "TEAM_B_1",
            "TEAM_B_2",
            "TEAM_B_3",
            "ASIAN_HANDICAP",
            "OVER_UNDER_05",
            "OVER_UNDER_15",
            "OVER_UNDER_25",
            "OVER_UNDER_35",
            "OVER_UNDER_45",
            "OVER_UNDER_55",
            "OVER_UNDER_65",
            "OVER_UNDER_75",
            "OVER_UNDER_85",
            "MATCH_ODDS",
            "CORRECT_SCORE"};

    public Betfair betfair;

    public String event_id;


    public Instant lastMarketDataUpdate;
    public Map<String, String> marketType_id_map;
    public Map<String, Long> correctScore_selectionId_map;


    public BetfairEventTracker(Betfair betfair, EventTrader eventTrader){
        super(eventTrader);
        this.betfair = betfair;

        match = null;
        lastMarketDataUpdate = null;
        marketType_id_map = new HashMap<>();
        correctScore_selectionId_map = new HashMap<>();
        bet_blacklist = new HashSet<>();
    }


    @Override
    public String name() {
        return betfair.name;
    }


    @Override
    public boolean setupMatch(FootballMatch setup_match) throws IOException,
            URISyntaxException {

        Instant start = setup_match.start_time.minus(1, ChronoUnit.SECONDS);
        Instant end = setup_match.start_time.plus(1, ChronoUnit.SECONDS);

        ArrayList<FootballMatch> potential_matches = betfair.getFootballMatches(start, end);

        // Check each event returned to find matching events
        match = null;
        for (FootballMatch potential_match: potential_matches){
            // Check same match
            if (Boolean.TRUE.equals(potential_match.same_match(setup_match, false))) {
                match = potential_match;
                event_id = potential_match.metadata.get("betfair_id");
                break;
            }
        }

        // Try again but verify if nothing found this time
        if (match == null){
            for (FootballMatch potential_match: potential_matches){
                // Check same match
                if (Boolean.TRUE.equals(potential_match.same_match(setup_match, true))) {
                    match = potential_match;
                    event_id = potential_match.metadata.get("betfair_id");
                    break;
                }
            }
        }

        // Error if not found
        if (match == null) {
            String fails = "";
            for (FootballMatch m: potential_matches){
                fails = fails + " " + m.toString();
            }
            log.warning(String.format("No matches found for %s in betfair. Checked %d: %s",
                    setup_match.toString(), potential_matches.size(), fails));
            return false;
        }


        // Build params for market catalogue request
        JSONObject params = new JSONObject();
        JSONArray marketProjection = new JSONArray();
        params.put("marketProjection", marketProjection);
        marketProjection.add("MARKET_DESCRIPTION");
        marketProjection.add("RUNNER_DESCRIPTION");
        JSONObject filters = new JSONObject();
        params.put("filter", filters);
        JSONArray marketTypeCodes = new JSONArray();
        marketTypeCodes.addAll(Arrays.asList(football_market_types));
        filters.put("marketTypeCodes", marketTypeCodes);
        JSONArray eventIds = new JSONArray();
        eventIds.add(event_id);
        filters.put("eventIds", eventIds);


        // Get market catalogue for this event
        JSONArray markets = null;
        try {
            markets = (JSONArray) betfair.getMarketCatalogue(params);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            throw e;
        }


        toFile(markets);
        System.exit(0);


        // Fill mapping of marketId and market type name
        for (Object market_obj: markets){
            JSONObject market = (JSONObject) market_obj;

            String market_id = (String) market.get("marketId");
            JSONObject desc = (JSONObject) market.get("description");
            String market_type = (String) desc.get("marketType");
            marketType_id_map.put(market_type, market_id);

            if (market_type.equals("CORRECT_SCORE")){
                for (Object item: (JSONArray) market.get("runners")){
                    JSONObject runner = (JSONObject) item;

                    correctScore_selectionId_map.put(
                            (String) runner.get("runnerName"),
                            (long) runner.get("selectionId"));
                }
            }
        }

        match = setup_match;
        return true;
    }


    public HashMap<String, JSONObject> updateMarketData(HashMap<String, JSONObject> eventMarketData) throws Exception {
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
                    throw new Exception(String.format("New runner mismatch old runners.\nold\n%s\nnew\n%s",
                            jstring(old_runners), jstring(new_runners)));
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
        return eventMarketData;
    }


    @Override
    public MarketOddsReport getMarketOddsReport(FootballBet[] bets) throws Exception {

        if (match == null){
            log.severe("Trying to get market odds report on null event.");
            return null;
        }
        // get the raw data market odds
        JSONArray market_odds = betfair.getMarketOdds(marketType_id_map.values());
        MarketOddsReport new_marketOddsReport = new MarketOddsReport();

        for (FootballBet bet: bets){
            if (bet_blacklist.contains(bet.id())){
                continue;
            }

            // Extract runner from market data depending on category.
            JSONObject runner = null;
            switch (bet.category) {
                case FootballBet.RESULT:
                    runner = extractRunnerRESULT(bet, market_odds);
                    break;
                case FootballBet.CORRECT_SCORE:
                    runner = extractRunnerSCORE(bet, market_odds);
                    break;
                case FootballBet.OVER_UNDER:
                    runner = extractRunnerOVERUNDER(bet, market_odds);
                    break;
                case FootballBet.HANDICAP:
                    runner = extractRunnerHANDICAP(bet, market_odds);
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
            else if (!(runner.containsKey("ex")) || !(runner.containsKey("status"))){
                log.warning(String.format("No 'ex' or 'status' fields found in runner for bet %s for %s in betfair.",
                        bet, match));
                continue;
            }
            String runner_status = runner.get("status").toString();
            if (!(runner_status.equals("ACTIVE"))){
                if (!((runner_status.equals("WINNER")
                        || runner_status.equals("LOSER")
                        || runner_status.equals("CLOSED")))){

                    log.warning(String.format("Runner status is %s in bet %s for %s in betfair.",
                            runner_status, bet, match));
                }
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
                metadata.put("selectionId", runner.get("selectionId").toString());
                metadata.put("marketId", runner.get("market_id").toString());

                betOffers.add(new BetOffer(match, bet, betfair, odds, volume, metadata));
            }

            new_marketOddsReport.addBetOffers(bet.id(), betOffers);
        }


        return new_marketOddsReport;
    }



    public JSONObject getMarketFromArray(String id, JSONArray array){
        JSONObject market = null;
        for (Object item: array){
            JSONObject potential_market = (JSONObject) item;
            if (potential_market.get("marketId").equals(id)){
                market = potential_market;
                break;
            }
        }
        return market;
    }


    private JSONObject extractRunnerHANDICAP(FootballBet BET, JSONArray market_odds) {

        FootballHandicapBet bet = (FootballHandicapBet) BET;

        // Different market used if handicap is integer or 0.5 multiple
        if (bet.a_handicap.stripTrailingZeros().scale() <= 0){
            String team = null;

            // Create market type name
            if (bet.a_handicap.compareTo(BigDecimal.ZERO) == 1){
                team = "TEAM_A";
            }
            else if (bet.a_handicap.compareTo(BigDecimal.ZERO) == -1){
                team = "TEAM_B";
            }
            else{
                log.severe("HANDICAP of 0 not allowed in betfair.");
                return null;
            }

            String market_type = String.format("%s_%s",
                    team, bet.a_handicap.abs().setScale(0, RoundingMode.HALF_UP).toString());

            // Get market from mapping
            String market_id = marketType_id_map.get(market_type);
            if (market_id == null){
                return null;
            }

            JSONObject market = getMarketFromArray(market_id, market_odds);
            if (market == null){
                return null;
            }

            Integer sortPriority = null;
            if (bet.winnerA()){ sortPriority = 1;}
            else if (bet.winnerB()){ sortPriority = 2;}
            else if (bet.isDraw()){ sortPriority = 3;}


            JSONArray runners = (JSONArray) market.get("runners");
            JSONObject runner = null;
            for (Object item: runners){
                JSONObject potential_runner = (JSONObject) item;
                if ((Integer) potential_runner.get("sortPriority") == sortPriority){
                    runner = potential_runner;
                    break;
                }
            }

            return runner;
        }
        else{
            // Find market id for this market in this event from map
            String market_id = marketType_id_map.get("ASIAN_HANDICAP");
            if (market_id == null){
                return null;
            }


            //TODO: Selection for handicap bet in betfair.


        }

        // Find market id for this market in this event from map
        String market_id = marketType_id_map.get("ASIAN_HANDICAP");
        if (market_id == null){
            return null;
        }

        JSONObject market = getMarketFromArray(market_id, market_odds);
        if (market == null){
            return null;
        }



        print("Here");
        toFile(market);
        System.exit(0);





        JSONObject runner = null;

        return runner;
    }


    private JSONObject extractRunnerOVERUNDER(FootballBet BET, JSONArray market_odds) {
        FootballOverUnderBet bet = (FootballOverUnderBet) BET;
        String bf_market_name = String.format("OVER_UNDER_%s", bet.goals.toString().replace(".", ""));

        // Find market id for this market in this event from map
        String market_id = marketType_id_map.get(bf_market_name);
        if (market_id == null){
            log.fine(String.format("%s not found for %s in market id map.\n%s",
                    bf_market_name, match, marketType_id_map.toString()));
            return null;
        }

        JSONObject market = getMarketFromArray(market_id, market_odds);
        if (market == null){
            return null;
        }

        JSONArray runners = (JSONArray) market.get("runners");
        String runner_name = String.format("%s %s goals", bet.side, bet.goals.toString()).toLowerCase();


        JSONObject runner = null;
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


    private JSONObject extractRunnerSCORE(FootballBet BET, JSONArray market_odds) {
        FootballScoreBet bet = (FootballScoreBet) BET;

        // Find market id for this market in this event from map
        String market_id = marketType_id_map.get("CORRECT_SCORE");
        if (market_id == null){
            log.fine(String.format("CORRECT_SCORE not found for %s in market id map", match));
            return null;
        }

        JSONObject market = getMarketFromArray(market_id, market_odds);
        if (market == null){
            return null;
        }


        JSONArray runners = (JSONArray) market.get("runners");
        String runner_name = String.format("%d - %d", bet.score_a, bet.score_b);
        Long selectionId = correctScore_selectionId_map.get(runner_name);
        if (selectionId == null){
            return null;
        }

        JSONObject runner = null;
        for (Object r_obj: runners){
            JSONObject r = (JSONObject) r_obj;

            if (selectionId.equals(runner_name)){
                runner = r;
                break;
            }
        }

        if (runner != null){
            runner.put("market_id", market_id);
        }

        return runner;
    }


    private JSONObject extractRunnerRESULT(FootballBet BET, JSONArray market_odds) {
        //log.fine(String.format("Getting runner result from %s for %s.", match, BET.id()));

        FootballResultBet bet = (FootballResultBet) BET;

        // Find market id for this market in this event from map
        String market_id = marketType_id_map.get("MATCH_ODDS");
        if (market_id == null){
            log.fine(String.format("MATCH_ODDS not found for %s in market id map", match));
            return null;
        }


        JSONObject market = getMarketFromArray(market_id, market_odds);
        if (market == null){
            return null;
        }


        // Check it has 3 runners TEAMA DRAW and TEAMB
        JSONArray runners = (JSONArray) market.get("runners");
        if (runners.size() != 3){
            log.severe(String.format("RESULT market for %s has %d runners and not 3.", match, runners.size()));
            return null;
        }


        Integer sortPriority = null;
        if (bet.winnerA()){
            sortPriority = 1;
        }
        else if (bet.winnerB()){
            sortPriority = 2;
        }
        else if (bet.isDraw()){
            sortPriority = 3;
        }
        else{
            log.severe("Result bet wasn't A B or draw.");
            return null;
        }

        JSONObject runner = null;
        for (Object item: runners){
            JSONObject potential_runner = (JSONObject) item;
            if ((int) potential_runner.get("sortPriority") == sortPriority){
                runner = potential_runner;
                break;
            }
        }

        if (runner != null){
            runner.put("market_id", market_id);
        }

        return runner;
    }





    public static void main(String[] args){


    }

}
