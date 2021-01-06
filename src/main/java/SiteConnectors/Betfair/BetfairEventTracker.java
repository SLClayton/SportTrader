package SiteConnectors.Betfair;

import Bet.*;
import Bet.BetOffer;
import Bet.FootballBet.*;
import Bet.MarketOddsReport;
import SiteConnectors.SiteEventTracker;
import Sport.FootballMatch;
import Sport.Team;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

import static java.lang.System.exit;
import static java.lang.System.out;
import static tools.BigDecimalTools.BDInteger;
import static tools.BigDecimalTools.half;
import static tools.printer.*;

public class BetfairEventTracker extends SiteEventTracker {

    public static List<String> football_market_types = Arrays.asList(
            "ASIAN_HANDICAP",
            "MATCH_ODDS",
            "HALF_TIME",
            "CORRECT_SCORE",
            "HALF_TIME_SCORE",
            "FIRST_HALF_GOALS_05",
            "FIRST_HALF_GOALS_15",
            "FIRST_HALF_GOALS_25",
            "OVER_UNDER_05",
            "OVER_UNDER_15",
            "OVER_UNDER_25",
            "OVER_UNDER_35",
            "OVER_UNDER_45",
            "OVER_UNDER_55",
            "OVER_UNDER_65",
            "OVER_UNDER_75",
            "OVER_UNDER_85",
            "TEAM_A_1",
            "TEAM_A_2",
            "TEAM_A_3",
            "TEAM_B_1",
            "TEAM_B_2",
            "TEAM_B_3");

    public Pattern OVERUNDER_marketTypePattern = Pattern.compile("\\A((OVER_UNDER)|(FIRST_HALF_GOALS))_\\d\\d\\z");
    public Pattern HANDICAP_marketTypePattern = Pattern.compile("\\ATEAM_[AB]_\\d\\z");
    public Pattern scoreline_pattern = Pattern.compile("\\A\\d - \\d\\z");

    public Betfair betfair;
    public String event_id;


    public Instant lastMarketDataUpdate;
    public Map<String, String> marketType_id_map;
    public Map<String, String> betId_marketSelId_map;



    public BetfairEventTracker(Betfair betfair){
        super(betfair);
        this.betfair = betfair;

        event = null;
        lastMarketDataUpdate = null;
        marketType_id_map = new HashMap<>();
        bet_blacklist = new HashSet<>();
        betId_marketSelId_map = new HashMap<>();
    }


    @Override
    public String name() {
        return Betfair.name;
    }



    @Override
    public boolean siteSpecificSetup() throws IOException, URISyntaxException {

        event_id = event.metadata.get(Betfair.BETFAIR_EVENT_ID);
        if (event_id == null){
            log.severe(String.format("Could not find betfair event id in event metadata. %s md:%s",
                    event.toString(), event.metadata.toString()));
            return false;
        }


        // Get market catalogue for this event (metadata about markets and runners
        JSONArray markets = null;
        markets = betfair.getMarketCatalogue(Arrays.asList(event_id), football_market_types);


        // Get initial data from markets of this event
        for (Object market_obj: markets){
            JSONObject market = (JSONObject) market_obj;

            String market_id = (String) market.get("marketId");
            String market_type = (String) ((JSONObject) market.get("description")).get("marketType");
            marketType_id_map.put(market_type, market_id);


            // Result bets
            if (market_type.equals("MATCH_ODDS") || market_type.equals("HALF_TIME")){
                setup_runners_matchOdds(market);
            }
            // Scoreline bets
            else if (market_type.equals("CORRECT_SCORE") || market_type.equals("HALF_TIME_SCORE")){
                setup_runners_correctScore(market);
            }
            // Asian Handicap
            else if (market_type.equals("ASIAN_HANDICAP")){
                setup_runners_asianHandicap(market);
            }
            // Over under
            else if (OVERUNDER_marketTypePattern.matcher(market_type).matches()){
                setup_runners_overUnder(market);
            }
            // Handicap
            else if (HANDICAP_marketTypePattern.matcher(market_type).matches()){
                setup_runners_handicap(market);
            }

        }

        return true;
    }


    public void setup_runners_matchOdds(JSONObject market){

        JSONArray runners = (JSONArray) market.get("runners");
        String market_type = (String) ((JSONObject) market.get("description")).get("marketType");
        String market_id = (String) market.get("marketId");


        Boolean halftime = null;
        if      (market_type.equals("HALF_TIME")){
            halftime = true;

        }
        else if (market_type.equals("MATCH_ODDS")){ halftime = false; }
        else {
            log.severe(sf("Invalid market type %s", market_type));
            return;
        }


        for (Object runner_obj: runners){
            JSONObject runner = (JSONObject) runner_obj;
            int sort_priority = ((Long) runner.get("sortPriority")).intValue();
            int selectionId = ((Long) runner.get("selectionId")).intValue();


            String outcome = null;
            if      (sort_priority == 1){ outcome = FootballBet.TEAM_A; }
            else if (sort_priority == 2){ outcome = FootballBet.TEAM_B; }
            else if (sort_priority == 3){ outcome = FootballBet.DRAW; }
            else{
                log.severe(sf("Match odds runner has sort priority %s which isnt 1-3 as expected.", sort_priority));
                continue;
            }

            FootballResultBet back_bet = new FootballResultBet(Bet.BetType.BACK, outcome, halftime);
            FootballResultBet lay_bet = new FootballResultBet(Bet.BetType.LAY, outcome, halftime);

            String market_selection_id = market_sel_id(market_id, selectionId);
            betId_marketSelId_map.put(back_bet.id(), market_selection_id);
            betId_marketSelId_map.put(lay_bet.id(), market_selection_id);
        }
    }


    public void setup_runners_correctScore(JSONObject market){

        JSONArray runners = (JSONArray) market.get("runners");
        String market_type = (String) ((JSONObject) market.get("description")).get("marketType");
        String market_id = (String) market.get("marketId");

        Boolean halftime = null;
        if      (market_type.equals("HALF_TIME_SCORE")){ halftime = true; }
        else if (market_type.equals("CORRECT_SCORE")){ halftime = false; }
        else {
            log.severe(sf("Invalid market type %s", market_type));
            return;
        }

        int max_score = 0;

        for (Object runner_obj: runners) {
            JSONObject runner = (JSONObject) runner_obj;

            String runnerName = (String) runner.get("runnerName");
            if (scoreline_pattern.matcher(runnerName).matches()){
                int selectionId = ((Long) runner.get("selectionId")).intValue();

                String[] score_strings = runnerName.split("-");
                int score_a = Integer.parseInt(score_strings[0].trim());
                int score_b = Integer.parseInt(score_strings[1].trim());

                max_score = Integer.max(Integer.max(score_a, score_b), max_score);

                FootballScoreBet back_bet = new FootballScoreBet(Bet.BetType.BACK, score_a, score_b, halftime);
                FootballScoreBet lay_bet = new FootballScoreBet(Bet.BetType.LAY, score_a, score_b, halftime);

                String mark_sel_id = market_sel_id(market_id, selectionId);
                betId_marketSelId_map.put(back_bet.id(), mark_sel_id);
                betId_marketSelId_map.put(lay_bet.id(), mark_sel_id);
            }
        }

        // Now we have the max score, we can go over again and do the 'other score' bets
        for (Object runner_obj: runners) {
            JSONObject runner = (JSONObject) runner_obj;

            String runnerName = (String) runner.get("runnerName");
            if (!scoreline_pattern.matcher(runnerName).matches()){
                int selectionId = ((Long) runner.get("selectionId")).intValue();


                String outcome = null;
                if      (runnerName.equals("Any Other Home Win")){ outcome = FootballBet.TEAM_A; }
                else if (runnerName.equals("Any Other Away Win")){ outcome = FootballBet.TEAM_B; }
                else if (runnerName.equals("Any Other Draw"))    { outcome = FootballBet.DRAW; }
                else if (runnerName.trim().toLowerCase().equals("any unquoted"))    { outcome = FootballBet.ANY; }
                else{
                    log.severe(sf("invalid runnername '%s'", runnerName));
                    continue;
                }

                FootballOtherScoreBet back_bet = new FootballOtherScoreBet(Bet.BetType.BACK, max_score, outcome, halftime);
                FootballOtherScoreBet lay_bet = new FootballOtherScoreBet(Bet.BetType.BACK, max_score, outcome, halftime);

                String mark_sel_id = market_sel_id(market_id, selectionId);
                betId_marketSelId_map.put(back_bet.id(), mark_sel_id);
                betId_marketSelId_map.put(lay_bet.id(), mark_sel_id);
            }
        }
    }


    public void setup_runners_overUnder(JSONObject market) {

        JSONArray runners = (JSONArray) market.get("runners");
        String market_type = (String) ((JSONObject) market.get("description")).get("marketType");
        String market_id = (String) market.get("marketId");

        // Find whether halftime or not
        Boolean halftime = null;
        if      (market_type.contains("FIRST_HALF_GOALS")) { halftime = true; }
        else if (market_type.contains("OVER_UNDER")) { halftime = false; }
        else {
            log.severe(sf("Invalid market type %s", market_type));
            return;
        }

        // Find what score the 'over/under' refers to from market type name
        String[] type_parts = market_type.split("_");
        String goals_string = type_parts[type_parts.length-1];
        BigDecimal goals = new BigDecimal(String.format("%s.%s", goals_string.charAt(0), goals_string.charAt(1)));


        for (Object runner_obj: runners) {
            JSONObject runner = (JSONObject) runner_obj;
            int sort_priority = ((Long) runner.get("sortPriority")).intValue();
            int selectionId = ((Long) runner.get("selectionId")).intValue();

            String over_under = null;
            if      (sort_priority == 1){ over_under = FootballOverUnderBet.UNDER; }
            else if (sort_priority == 2){ over_under = FootballOverUnderBet.OVER; }
            else{
                log.severe(sf("Invalid over/under sort Priority %s", sort_priority));
                continue;
            }

            FootballOverUnderBet back_bet = new FootballOverUnderBet(Bet.BetType.BACK, over_under, goals, halftime);
            FootballOverUnderBet lay_bet = new FootballOverUnderBet(Bet.BetType.LAY, over_under, goals, halftime);

            String mark_sel_id = market_sel_id(market_id, selectionId);
            betId_marketSelId_map.put(back_bet.id(), mark_sel_id);
            betId_marketSelId_map.put(lay_bet.id(), mark_sel_id);
        }
    }


    public void setup_runners_handicap(JSONObject market) {

        JSONArray runners = (JSONArray) market.get("runners");
        String market_type = (String) ((JSONObject) market.get("description")).get("marketType");
        String market_id = (String) market.get("marketId");

        String[] type_parts = market_type.split("_");
        int handicap = Integer.parseInt(type_parts[type_parts.length-1]);
        String team = type_parts[type_parts.length-2];

        // Write handicap in terms of team A, so negative if team B
        BigDecimal a_handicap = null;
        if      (team.equals("A")){ a_handicap = new BigDecimal(handicap); }
        else if (team.equals("B")){ a_handicap = new BigDecimal(-handicap); }
        else{
            log.severe(sf("Invalid handicap team %s.", team));
            return;
        }


        for (Object runner_obj: runners) {
            JSONObject runner = (JSONObject) runner_obj;
            int sort_priority = ((Long) runner.get("sortPriority")).intValue();
            int selectionId = ((Long) runner.get("selectionId")).intValue();

            String outcome = null;
            if      (sort_priority == 1){ outcome = FootballBet.TEAM_A; }
            else if (sort_priority == 2){ outcome = FootballBet.TEAM_B; }
            else if (sort_priority == 3){ outcome = FootballBet.DRAW; }
            else{
                log.severe(sf("handicap outcome runner has sort priority %s which isnt 1-3 as expected.",
                        sort_priority));
                continue;
            }

            FootballHandicapBet back_bet = new FootballHandicapBet(Bet.BetType.BACK, a_handicap, outcome);
            FootballHandicapBet lay_bet = new FootballHandicapBet(Bet.BetType.LAY, a_handicap, outcome);

            String mark_sel_id = market_sel_id(market_id, selectionId);
            betId_marketSelId_map.put(back_bet.id(), mark_sel_id);
            betId_marketSelId_map.put(lay_bet.id(), mark_sel_id);
        }
    }


    public void setup_runners_asianHandicap(JSONObject market){

        JSONArray runners = (JSONArray) market.get("runners");
        String market_id = (String) market.get("marketId");

        for (Object runner_obj: runners) {
            JSONObject runner = (JSONObject) runner_obj;
            BigDecimal handicap = new BigDecimal(runner.get("handicap").toString());

            // Ensure handicap is a half (0.5, 1.5, 2.5)
            if (BDInteger(handicap.add(half))){
                int sort_priority = ((Long) runner.get("sortPriority")).intValue();
                int selectionId = ((Long) runner.get("selectionId")).intValue();

                String outcome;
                BigDecimal a_handicap;
                // A Wins
                if (sort_priority % 2 == 0){
                    outcome = FootballBet.TEAM_A;
                    a_handicap = handicap;
                }
                // B Wins
                else {
                    outcome = FootballBet.TEAM_B;
                    a_handicap = handicap.negate();
                }


                FootballHandicapBet back_bet = new FootballHandicapBet(Bet.BetType.BACK, a_handicap, outcome);
                FootballHandicapBet lay_bet = new FootballHandicapBet(Bet.BetType.BACK, a_handicap, outcome);

                String mark_sel_id = market_sel_id(market_id, selectionId);
                betId_marketSelId_map.put(back_bet.id(), mark_sel_id);
                betId_marketSelId_map.put(lay_bet.id(), mark_sel_id);
            }
        }
    }



    public MarketOddsReport _getMarketOddsReport(Collection<Bet> bets) throws InterruptedException {

        lastMarketOddsReport_start_time = Instant.now();

        if (event == null){
            log.severe("Trying to get market odds report on null event.");
            return MarketOddsReport.ERROR("NULL event in betfair event tracker");
        }


        // Establish from the given bets, which markets to ask for prices in
        Set<String> market_ids = new HashSet<>();
        for (Bet bet: bets){
            String market_sel_id = betId_marketSelId_map.get(bet.id());
            if (market_sel_id == null){
                continue;
            }
            String market_id = extractMarketId(market_sel_id);
            market_ids.add(market_id);
        }


        // Get the raw data market odds
        JSONArray market_odds = betfair.getMarketOdds(market_ids);
        if (market_odds == null){
            return MarketOddsReport.ERROR("Betfair market odds returned null.");
        }


        // Create map of market_sel_Ids -> Runner JSONs
        Map<String, JSONObject> markSelId_runner_map = new HashMap<>();
        for (JSONObject market: (List<JSONObject>) market_odds){
            String market_id = (String) market.get("marketId");

            for (Object runner_obj: (JSONArray) market.get("runners")){
                JSONObject runner = (JSONObject) runner_obj;
                int selectionId = ((Long) runner.get("selectionId")).intValue();
                String mark_sel_id = market_sel_id(market_id, selectionId);

                markSelId_runner_map.put(mark_sel_id, runner);
            }
        }

        MarketOddsReport new_marketOddsReport = new MarketOddsReport(event);
        for (Bet abstract_bet: bets){
            if (bet_blacklist.contains(abstract_bet.id())){
                continue;
            }

            FootballBet bet = (FootballBet) abstract_bet;

            //Get runner
            String markSelId = betId_marketSelId_map.get(bet.id());
            String marketId = null;
            JSONObject runner = null;
            if (markSelId != null){
                runner = markSelId_runner_map.get(markSelId);
                marketId = extractMarketId(markSelId);
            }

            // Ensure runner is valid
            if (runner == null){
                bet_blacklist.add(bet.id());
                log.fine(String.format("No %s bet found in betfair.", bet.id()));
                continue;
            }
            else if (!(runner.containsKey("ex")) || !(runner.containsKey("status"))){
                log.warning(String.format("No 'ex' or 'status' fields found in runner for bet %s in betfair.", bet));
                continue;
            }
            String runner_status = runner.get("status").toString();
            if (!(runner_status.equals("ACTIVE"))){
                if (!((runner_status.equals("WINNER")
                        || runner_status.equals("LOSER")
                        || runner_status.equals("CLOSED")
                        || runner_status.equals("REMOVED")))){

                    log.warning(String.format("Runner status is %s in bet %s for %s in betfair.",
                            runner_status, bet, event));
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



            // Create a new betExchange from the offers
            BetExchange betExchange = new BetExchange(site, event, bet);
            betExchange.addMetadata(Betfair.BETFAIR_SELECTION_ID, runner.get("selectionId"));
            betExchange.addMetadata(Betfair.BETFAIR_MARKET_ID, marketId);
            betExchange.addMetadata(Betfair.BETFAIR_HANDICAP, runner.get("handicap"));


            for (int i=0; i<betfair_offers.size(); i++){
                JSONObject bf_offer = (JSONObject) betfair_offers.get(i);

                BigDecimal odds = new BigDecimal(bf_offer.get("price").toString());
                BigDecimal volume = new BigDecimal(bf_offer.get("size").toString());
                betExchange.add(new BetOffer(betfair, event, bet, odds, volume));
            }
            new_marketOddsReport.addExchange(betExchange);
        }



        lastMarketOddsReport_end_time = Instant.now();
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


    public static String market_sel_id(String market_id, int sel_id){
        return String.format("%s_%s", market_id, sel_id);
    }

    public static String extractMarketId(String markSelId){
        return markSelId.substring(0, markSelId.indexOf("_"));
    }


    public static void main(String[] args){


    }

}
