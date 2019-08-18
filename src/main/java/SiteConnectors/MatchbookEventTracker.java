package SiteConnectors;

import Bet.FootballBet.FootballBet;
import Bet.FootballBet.FootballResultBet;
import Bet.MarketOddsReport;
import Sport.FootballMatch;
import Sport.Match;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import static tools.printer.ps;

public class MatchbookEventTracker extends SiteEventTracker {

    public Matchbook matchbook;
    public String event_id;
    public FootballMatch match;
    public JSONObject eventMarketData;
    public Instant lastEventUpdate;
    public HashMap<String, String> market_name_id_map;


    public MatchbookEventTracker(Matchbook matchbook) {
        this.matchbook = matchbook;
    }

    @Override
    public boolean setupMatch(FootballMatch match) throws Exception {

        ArrayList<FootballMatch> events = matchbook.getEvents(match.start_time.minus(1, ChronoUnit.SECONDS),
                                                              match.start_time.plus(1, ChronoUnit.SECONDS),
                                                              matchbook.FOOTBALL_ID);

        // Check each searched for match in matchbook to see if it matches with the desired
        // match we are trying to set up.
        ArrayList<FootballMatch> matching_events = new ArrayList<FootballMatch>();
        for (FootballMatch fm: events){
            if (match.same_match(fm)){
                matching_events.add(fm);
            }
        }

        // Check for no match or >1 match.
        if (matching_events.size() == 0){
            log.warning(String.format("No match for %s found in matchbook. Searched events %s.", match, Match.listtostring(events)));
            return false;
        }
        if (matching_events.size() > 1){
            log.warning(String.format("Multiple matches found for %s in matchbook. Matching events %s.", match, Match.listtostring(matching_events)));
            return false;
        }

        // Match found, set as event for this object
        event_id = matching_events.get(0).metadata.get("matchbook_event_id");
        this.match = match;

        return true;
    }

    @Override
    public void updateMarketOddsReport(FootballBet[] bets) throws Exception {
        if (event_id == null){
            throw new Exception("Matchbook event trader tried to update odds without an event.");
        }

        // Update the raw odds for this event
        updateMarketData();

        MarketOddsReport marketOddsReport = new MarketOddsReport();

        for (FootballBet bet: bets){
            if (bet_blacklist.contains(bet.id())){
                continue;
            }


            // Extract runner based on bet category.
            JSONObject runner = null;
            switch (bet.category){
                case "RESULT":
                    runner = extractRunnerRESULT(bet);
                    break;
                    // TODO: Completed RESULT one above, do the functions for the below two.
                case "CORRECT_SCORE":
                    runner = extractRunnerCORRECTSCORE(bet);
                    break;
                case "GOAL_COUNT":
                    runner = extractRunnerGOALCOUNT(bet);
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





        }
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
                        .format("While confirming RESUlT bet in matchbook for %s, team '%s' does not match '%s'."
                                match, runner_name, match.team_a));
                return null;
            }
        }

        else if (bet.result == FootballBet.TEAM_B){
            runner = (JSONObject) runners.get(1);
            String runner_name = (String) runner.get("name");
            if (!FootballMatch.same_team(runner_name, match.team_b)){
                log.warning(String
                        .format("While confirming RESUlT bet in matchbook for %s, team '%s' does not match '%s'."
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
