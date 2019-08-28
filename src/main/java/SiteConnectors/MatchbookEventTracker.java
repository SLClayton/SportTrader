package SiteConnectors;

import Bet.BetOffer;
import Bet.FootballBet.FootballBet;
import Bet.FootballBet.FootballOverUnderBet;
import Bet.FootballBet.FootballResultBet;
import Bet.FootballBet.FootballScoreBet;
import Bet.MarketOddsReport;
import Sport.FootballMatch;
import Sport.Match;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import static tools.printer.*;

public class MatchbookEventTracker extends SiteEventTracker {

    public Matchbook matchbook;
    public String event_id;
    public FootballMatch match;
    public JSONObject eventMarketData;
    public HashMap<String, String> market_name_id_map;


    public MatchbookEventTracker(Matchbook matchbook) {
        this.matchbook = matchbook;
    }

    @Override
    public String name() {
        return matchbook.name;
    }

    @Override
    public boolean setupMatch(FootballMatch setup_match) throws Exception {

        log.info(String.format("Attempting to setup match in Matchbook Event Tracker for %s.", setup_match.toString()));
        ArrayList<FootballMatch> events = matchbook.getEvents(setup_match.start_time.minus(1, ChronoUnit.SECONDS),
                setup_match.start_time.plus(1, ChronoUnit.SECONDS),
                                                              matchbook.FOOTBALL_ID);

        // Check each searched for match in matchbook to see if it matches with the desired
        // match we are trying to set up.
        ArrayList<FootballMatch> matching_events = new ArrayList<FootballMatch>();
        for (FootballMatch fm: events){
            if (setup_match.same_match(fm)){
                matching_events.add(fm);
            }
        }

        // Check for no match or >1 match.
        if (matching_events.size() == 0){
            log.warning(String.format("No match for %s found in matchbook. Searched events %s.", setup_match, Match.listtostring(events)));
            return false;
        }
        if (matching_events.size() > 1){
            log.warning(String.format("Multiple matches found for %s in matchbook. Matching events %s.", setup_match, Match.listtostring(matching_events)));
            return false;
        }

        // Match found, set as event for this object
        event_id = matching_events.get(0).metadata.get("matchbook_event_id");
        this.match = setup_match;

        return true;
    }

    @Override
    public void updateMarketOddsReport(FootballBet[] bets) throws Exception {
        if (event_id == null){
            throw new Exception("Matchbook event trader tried to update odds without an event.");
        }

        // Update the raw odds for this event
        updateMarketData();

        MarketOddsReport new_marketOddsReport = new MarketOddsReport();

        for (FootballBet bet: bets){
            if (bet_blacklist.contains(bet.id())){
                continue;
            }


            // Extract runner based on bet category.
            JSONObject runner = null;
            switch (bet.category){
                case "RESULT":
                    runner = extractRunnerRESULT((FootballResultBet) bet);
                    break;
                case "CORRECT_SCORE":
                    runner = extractRunnerCORRECTSCORE((FootballScoreBet) bet);
                    break;
                case "GOAL_COUNT":
                    runner = extractRunnerGOALCOUNT((FootballOverUnderBet) bet);
                    break;
                default:
                    bet_blacklist.add(bet.id());
                    log.fine(String.format("No '%s' bet for '%s' found in matchbook. Adding to blacklist.", bet.id(), match));
                    continue;
            }

            // Check runner is valid
            if (runner == null){
                log.fine(String.format("No matchbook runner found for %s in %s", bet.id(), match));
                continue;
            }
            if ((!runner.containsKey("status")) || (!runner.get("status").equals("open"))){
                log.warning(String.format("Matchbook runner is invalid for %s in %s", bet.id(), match));
                continue;
            }

            // From the runner, extract the correct odds depending on the bet.
            ArrayList<JSONObject> matchbookOffers = new ArrayList<>();
            for (Object offer_obj: (JSONArray) runner.get("prices")){
                JSONObject offer = (JSONObject) offer_obj;
                String offer_side = ((String) offer.get("side")).toLowerCase();

                if (offer_side.equals(bet.type.toLowerCase())){
                    matchbookOffers.add(offer);
                }
            }

            // Convert to our list
            ArrayList<BetOffer> new_betOffers = new ArrayList<>();
            for (JSONObject mb_offer: matchbookOffers){

                BigDecimal odds = new BigDecimal(String.valueOf(mb_offer.get("decimal-odds")));
                BigDecimal volume = new BigDecimal(String.valueOf(mb_offer.get("available-amount")));
                HashMap<String, String> metadata = new HashMap<>();
                metadata.put("market_id", String.valueOf(runner.get("market-id")));
                metadata.put("runner_id", String.valueOf(runner.get("id")));

                new_betOffers.add(new BetOffer(match, bet, matchbook, odds, volume, metadata));
            }

            // Add to final report only
            new_marketOddsReport.addBetOffers(bet.id(), new_betOffers);
        }

        // Assign newly created report as the current.
        marketOddsReport = new_marketOddsReport;
    }


    public JSONObject extractRunnerGOALCOUNT(FootballOverUnderBet bet) {
        JSONObject market = null;
        for (Object market_obj: (JSONArray) eventMarketData.get("markets")){
            JSONObject this_market = (JSONObject) market_obj;

            if (this_market.get("market-type").equals("total")         // Correct market type
                    && (!this_market.containsKey("asian-handicap"))    // Isn't asian handicap
                    && (this_market.containsKey("handicap"))           // Has handicap in name
                                                                       // Handicap is amount of over/under goals for bet
                    && (bet.goals.equals(new BigDecimal(String.valueOf(this_market.get("handicap")))))){

                market = (JSONObject) market_obj;
            }
        }

        // Check correct market was found in raw data
        if (market == null){
            return null;
        }

        // Generate what runner name we're searching for.
        String target_name = String.format("%s %s",
                bet.side.toUpperCase(), bet.goals.setScale(1, RoundingMode.HALF_UP));

        // Extract runners
        JSONArray runners = (JSONArray) market.get("runners");
        if (runners == null){
            return null;
        }

        // Search runners for target name and return if match found
        JSONObject runner = null;
        for (Object runner_obj: runners){
            if (((JSONObject) runner_obj).get("name").equals(target_name)){
                runner = (JSONObject) runner_obj;
                break;
            }
        }
        return runner;
    }


    public JSONObject extractRunnerCORRECTSCORE(FootballScoreBet bet) {
        JSONObject market = null;
        for (Object market_obj: (JSONArray) eventMarketData.get("markets")){
            if (((JSONObject) market_obj).get("market-type").equals("correct_score")){
                market = (JSONObject) market_obj;
            }
        }

        // Check correct market was found in raw data
        if (market == null){
            return null;
        }

        // Extract runners
        JSONArray runners = (JSONArray) market.get("runners");
        if (runners == null){
            return null;
        }

        // Create target name
        String target_name = String.format("%s-%s", bet.score_a, bet.score_b);

        // Find matching runner name to target in the runners and return
        JSONObject runner = null;
        for (Object runner_obj: runners){
            if (((JSONObject) runner_obj).get("name").equals(target_name)){
                runner = (JSONObject) runner_obj;
                break;
            }
        }
        return runner;
    }


    public JSONObject extractRunnerRESULT(FootballResultBet bet){
        JSONObject market = null;
        for (Object market_obj: (JSONArray) eventMarketData.get("markets")){
            if (((JSONObject) market_obj).get("market-type").equals("one_x_two")){
                market = (JSONObject) market_obj;
            }
        }

        // Check correct market was found in raw data
        if (market == null){
            return null;
        }

        // Extract runners
        JSONArray runners = (JSONArray) market.get("runners");
        if (runners == null){
            return null;
        }

        // Ensure 3 runners
        if (runners.size() != 3){
            log.severe(String.format("Runner size != 3 for RESULT bet.\n%s", ps(runners)));
            return null;
        }

        JSONObject runner = null;

        if (bet.result == FootballBet.TEAM_A){
            runner = (JSONObject) runners.get(0);
            String runner_name = (String) runner.get("name");
            if (!FootballMatch.same_team(runner_name, match.team_a)){
                log.warning(String
                        .format("While confirming RESUlT bet in matchbook for %s, team '%s' does not match '%s'.",
                                match, runner_name, match.team_a));
                return null;
            }
        }

        else if (bet.result == FootballBet.TEAM_B){
            runner = (JSONObject) runners.get(1);
            String runner_name = (String) runner.get("name");
            if (!FootballMatch.same_team(runner_name, match.team_b)){
                log.warning(String
                        .format("While confirming RESUlT bet in matchbook for %s, team '%s' does not match '%s'.",
                                match, runner_name, match.team_b));
                return null;
            }
        }

        else if (bet.result == FootballBet.DRAW){
            runner = (JSONObject) runners.get(2);
            String runner_name = (String) runner.get("name");
            if (!runner_name.toLowerCase().startsWith("draw")){
                log.warning(String
                        .format("While confirming RESUlT bet in matchbook for %s, draw runner not draw: '%s'.",
                                match, runner_name));
                return null;
            }
        }

        return runner;
    }


    public void updateMarketData() throws InterruptedException {
        eventMarketData = matchbook.getMarketDataFromHandler(event_id);
    }
}
