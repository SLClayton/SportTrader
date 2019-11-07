package SiteConnectors.Matchbook;

import Bet.BetOffer;
import Bet.FootballBet.*;
import Bet.MarketOddsReport;
import SiteConnectors.FlashScores;
import SiteConnectors.SiteEventTracker;
import SiteConnectors.Smarkets.Smarkets;
import SiteConnectors.Smarkets.SmarketsEventTracker;
import Sport.FootballMatch;
import Sport.Match;
import Trader.EventTrader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static tools.printer.*;

public class MatchbookEventTracker extends SiteEventTracker {

    public Matchbook matchbook;
    public String event_id;
    public FootballMatch match;
    public JSONObject eventMarketData;
    public Map<String, String> market_name_id_map;


    public MatchbookEventTracker(Matchbook matchbook, EventTrader eventTrader) {
        super(eventTrader);
        this.matchbook = matchbook;
        market_name_id_map = new HashMap<>();
    }

    @Override
    public String name() {
        return matchbook.name;
    }





    @Override
    public boolean setupMatch(FootballMatch setup_match) throws IOException, URISyntaxException, InterruptedException {

        ArrayList<FootballMatch> events = matchbook
                .getFootballMatches(setup_match.start_time.minus(1, ChronoUnit.SECONDS),
                                    setup_match.start_time.plus(1, ChronoUnit.SECONDS));

        match = null;
        // Verify each match in flashscores and see if it matches
        for (FootballMatch fm: events){
            if (Boolean.TRUE.equals(fm.same_match(setup_match, false))){
                match = fm;
                event_id = fm.metadata.get("matchbook_event_id");
                break;
            }
        }
        if (match == null){
            for (FootballMatch fm: events){
                if (Boolean.TRUE.equals(fm.same_match(setup_match, true))){
                    match = fm;
                    event_id = fm.metadata.get("matchbook_event_id");
                    break;
                }
            }
        }

        // Check for no match
        if (match == null){
            log.warning(String.format("%s No match found in matchbook. Searched %d events %s.",
                    setup_match, events.size(), Match.listtostring(events)));
            return false;
        }


        this.match = setup_match;
        return true;
    }


    public JSONObject getMarketData() throws InterruptedException {
        return matchbook.getMarketDataFromHandler(event_id);
    }


    @Override
    public MarketOddsReport getMarketOddsReport(FootballBet[] bets) throws Exception {

        if (event_id == null){
            return null;
        }

        // Update the raw odds for this event
        eventMarketData = getMarketData();
        MarketOddsReport new_marketOddsReport = new MarketOddsReport();
        JSONObject eventMarketData = (JSONObject) this.eventMarketData.clone();

        toFile(eventMarketData);
        eventTrader.sportsTrader.safe_exit();


        for (FootballBet bet: bets){
            if (bet_blacklist.contains(bet.id())){
                continue;
            }

            // Extract runner based on bet category.
            JSONObject runner = null;
            switch (bet.category){
                case FootballBet.RESULT:
                    runner = extractRunnerRESULT((FootballResultBet) bet, eventMarketData);
                    break;
                case FootballBet.CORRECT_SCORE:
                    runner = extractRunnerCORRECTSCORE((FootballScoreBet) bet, eventMarketData);
                    break;
                case FootballBet.OVER_UNDER:
                    runner = extractRunnerGOALCOUNT((FootballOverUnderBet) bet, eventMarketData);
                    break;
                case FootballBet.HANDICAP:
                    runner = extractRunnerHandicap((FootballHandicapBet) bet, eventMarketData);
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

                if (offer_side.equals(bet.type().toLowerCase())){
                    matchbookOffers.add(offer);
                }
            }

            // Convert to our list
            ArrayList<BetOffer> new_betOffers = new ArrayList<>();
            for (JSONObject mb_offer: matchbookOffers){

                BigDecimal odds = new BigDecimal(String.valueOf(mb_offer.get("decimal-odds")));
                BigDecimal volume = new BigDecimal(String.valueOf(mb_offer.get("available-amount")));
                HashMap<String, String> metadata = new HashMap<>();
                metadata.put(Matchbook.MARKET_ID, String.valueOf(runner.get("market-id")));
                metadata.put(Matchbook.RUNNER_ID, String.valueOf(runner.get("id")));

                new_betOffers.add(new BetOffer(match, bet, matchbook, odds, volume, metadata));
            }

            // Add to final report only
            new_marketOddsReport.addBetOffers(bet.id(), new_betOffers);
        }

        return new_marketOddsReport;
    }


    public JSONObject extractRunnerGOALCOUNT(FootballOverUnderBet bet, JSONObject eventMarketData) {
        JSONObject market = null;
        for (Object market_obj: (JSONArray) eventMarketData.get("markets")){
            JSONObject this_market = (JSONObject) market_obj;

            if (this_market.get("market-type").equals("total")         // Correct market type
                    && (!this_market.containsKey("asian-handicap"))    // Isn't asian handicap
                    && (this_market.containsKey("handicap"))           // Has handicap in name
                                                                       // Handicap is amount of over/under goals for bet
                    && (bet.goals.compareTo(new BigDecimal(String.valueOf(this_market.get("handicap")))) == 0)){

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


    public JSONObject extractRunnerCORRECTSCORE(FootballScoreBet bet, JSONObject eventMarketData) {
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


    public JSONObject extractRunnerHandicap(FootballHandicapBet bet, JSONObject eventMarketData){
        if (bet.isDraw()){
            return null;
        }

        JSONObject market = null;
        for (Object item: (JSONArray) eventMarketData.get("markets")){
            JSONObject potential_market = (JSONObject) item;
            String market_type = (String) potential_market.get("market-type");

            if (market_type.equals("handicap") && potential_market.containsKey("handicap")){
                BigDecimal handicap = new BigDecimal(String.valueOf(potential_market.get("handicap")));
                if (bet.a_handicap.compareTo(handicap) == 0){
                    market = potential_market;
                    break;
                }
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

        if (runners.size() != 2){
            log.severe(String.format("There should be exactly 2 handicap runners in matchbook " +
                            "market but there are %s\n%s",
                    String.valueOf(runners.size()), jstring(runners)));
            return null;
        }

        JSONObject runner = null;
        if (bet.winnerA()){
            runner = (JSONObject) runners.get(0);
        }
        else if (bet.winnerB()){
            runner = (JSONObject) runners.get(1);
        }


        return runner;
    }


    public JSONObject extractRunnerRESULT(FootballResultBet bet, JSONObject eventMarketData){

        JSONObject market = null;
        for (Object market_obj: (JSONArray) eventMarketData.get("markets")){
            JSONObject market_json = (JSONObject) market_obj;
            String market_type = (String) market_json.get("market-type");
            String market_name = (String) market_json.get("name");

            if (market_type.toLowerCase().equals("one_x_two")
                    && market_name.toLowerCase().equals("match odds")){

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
            log.severe(String.format("Runner size != 3 for RESULT bet.\n%s", jstring(runners)));
            return null;
        }

        JSONObject runner = null;

        if (bet.result == FootballBet.TEAM_A){
            runner = (JSONObject) runners.get(0);
        }

        else if (bet.result == FootballBet.TEAM_B){
            runner = (JSONObject) runners.get(1);
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




    public static void main(String[] args){

        try {



        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
