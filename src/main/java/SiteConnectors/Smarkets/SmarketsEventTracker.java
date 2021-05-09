package SiteConnectors.Smarkets;

import Bet.*;
import Bet.FootballBet.*;
import Bet.MarketOddsReport;
import SiteConnectors.Betfair.Betfair;
import SiteConnectors.Betfair.BetfairEventTracker;
import SiteConnectors.SiteEventTracker;
import Sport.Event;
import Sport.FootballMatch;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

import static tools.BigDecimalTools.isInteger;
import static tools.printer.*;

public class SmarketsEventTracker extends SiteEventTracker {

    public Smarkets smarkets;
    public String event_id;

    public Integer correct_score_max_goals;
    public Integer correct_score_max_goals_halftime;


    public static String[] market_type_names = new String[] {
            "OVER_UNDER",
            "WINNER_3_WAY",
            "HALF_TIME_WINNER_3_WAY",
            "CORRECT_SCORE",
            "HALF_TIME_CORRECT_SCORE",
            "ASIAN_HANDICAP"
    };


    public Map<String, JSONObject> id_market_map;
    public Map<String, String> betId_contract_map;
    public Map<String, String> contract_market_map;
    public Set<String> bet_blacklist;

    public JSONObject lastPrices;


    public SmarketsEventTracker(Smarkets smarkets){
        super(smarkets);
        this.smarkets = smarkets;
        id_market_map = new HashMap<>();
        contract_market_map = new HashMap<>();
        bet_blacklist = new HashSet<>();
    }


    @Override
    public String name() {
        return "smarkets";
    }


    @Override
    public String toString(){
        return String.format("[Smarktets tracker for %s]", event.toString());
    }



    @Override
    public boolean siteSpecificSetup() throws IOException, URISyntaxException, InterruptedException {

        // Assign event id to this object
        event_id = event.metadata.get(Smarkets.SMARKETS_EVENT_ID);

        // Setup market data for this event
        JSONArray markets = smarkets.getMarkets(event_id);
        for (Object market_obj: markets) {

            // Get data of this market
            JSONObject market = (JSONObject) market_obj;
            String market_type_name = (String) ((JSONObject) market.get("market_type")).get("name");
            String market_id = (String) market.get("id");

            // Add to list if type name appears in our whitelist
            if (Arrays.asList(market_type_names).contains(market_type_name)){
                id_market_map.put(market_id, market);
            }
        }

        // Get contracts for the chosen markets
        JSONArray contracts = smarkets.getContracts(id_market_map.keySet());

        // Create betid -> contract id map
        betId_contract_map = new HashMap<>();
        for (int i=0; i<contracts.size(); i++){

            // Find corresponding market to this contract
            JSONObject contract = (JSONObject) contracts.get(i);
            String contract_id = (String) contract.get("id");
            String market_id = (String) contract.get("market_id");
            JSONObject market = id_market_map.get(market_id);
            contract_market_map.put(contract_id, market_id);

            // Extract names
            JSONObject market_type = (JSONObject) market.get("market_type");
            String market_name = (String) market_type.get("name");
            String market_param = (String) market_type.getOrDefault("param", null);
            JSONObject contract_type = (JSONObject) contract.get("contract_type");
            String contract_name = (String) contract_type.get("name");
            String contract_param = (String) contract_type.getOrDefault("param", null);


            Bet bet = null;
            if (market_name.equals("HALF_TIME_CORRECT_SCORE") || market_name.equals("CORRECT_SCORE")) {
                boolean halftime = market_name.equals("HALF_TIME_CORRECT_SCORE");
                if (halftime && correct_score_max_goals_halftime == null) {
                    correct_score_max_goals_halftime = max_correct_score_goals(contracts, market_id, halftime);
                } else if (correct_score_max_goals == null) {
                    correct_score_max_goals = max_correct_score_goals(contracts, market_id, halftime);
                }
                int max_goals = correct_score_max_goals;
                if (halftime) {
                    max_goals = correct_score_max_goals_halftime;
                }

                if (contract_name.equals("SCORE")) {
                    String[] scores = contract_param.split("-");
                    bet = new FootballScoreBet(Bet.BetType.BACK, Integer.parseInt(scores[0]), Integer.parseInt(scores[1]), halftime);
                } else if (contract_name.equals("ANY_OTHER_HOME_WIN")) {
                    bet = new FootballOtherScoreBet(Bet.BetType.BACK, max_goals, FootballBet.TEAM_A, halftime);
                } else if (contract_name.equals("ANY_OTHER_AWAY_WIN")) {
                    bet = new FootballOtherScoreBet(Bet.BetType.BACK, max_goals, FootballBet.TEAM_B, halftime);
                } else if (contract_name.equals("ANY_OTHER_DRAW")) {
                    bet = new FootballOtherScoreBet(Bet.BetType.BACK, max_goals, FootballBet.DRAW, halftime);
                } else if (contract_name.equals("ANY_OTHER_SCORE")) {
                    bet = new FootballOtherScoreBet(Bet.BetType.BACK, max_goals, FootballBet.ANY, halftime);
                }
            }

            else if (market_name.equals("HALF_TIME_WINNER_3_WAY") || market_name.equals("WINNER_3_WAY")) {
                boolean halftime = market_name.equals("HALF_TIME_WINNER_3_WAY");
                String outcome = null;
                if (contract_name.equals("HOME")) { outcome = FootballBet.TEAM_A;
                } else if (contract_name.equals("AWAY")) { outcome = FootballBet.TEAM_B;
                } else if (contract_name.equals("DRAW")) { outcome = FootballBet.DRAW;
                }
                bet = new FootballResultBet(Bet.BetType.BACK, outcome, halftime);
            }

            else if (market_name.equals("OVER_UNDER") || market_name.equals("FIRST_HALF_OVER_UNDER")){
                boolean halftime = market_name.equals("FIRST_HALF_OVER_UNDER");
                BigDecimal goals = new BigDecimal(market_param);
                bet = new FootballOverUnderBet(Bet.BetType.BACK, contract_name.toUpperCase(), goals, halftime);
            }

            else if (market_name.equals("ASIAN_HANDICAP")){
                BigDecimal a_handicap = new BigDecimal(market_param);
                if (isInteger(a_handicap)){
                    continue;
                }
                String result;
                if (contract_name.equals("HOME")){ result = FootballBet.TEAM_A; }
                else if (contract_name.equals("AWAY")){ result = FootballBet.TEAM_B; }
                else{ continue; }
                bet = new FootballHandicapBet(Bet.BetType.BACK, a_handicap, result);
            }

            betId_contract_map.put(bet.id(), contract_id);
            betId_contract_map.put(bet.altId(), contract_id);
        }

        return true;
    }


    public Integer max_correct_score_goals(JSONArray contracts, String market_id, int other_contracts_size){

        Pattern score_regex = Pattern.compile("\\A\\d - \\d\\z");
        ArrayList<JSONObject> other_contracts = new ArrayList<>();
        int max_score = 0;
        int score_contracts = 0;

        for (Object item: contracts){
            JSONObject contract = (JSONObject) item;
            String contract_market_id = (String) contract.get("market_id");

            if (contract_market_id.equals(market_id)){
                String contract_name = (String) contract.get("name");

                if (score_regex.matcher(contract_name).find()){
                    String[] scores = contract_name.split(" - ");
                    max_score = Integer.max(max_score,
                            Integer.max(Integer.valueOf(scores[0]), Integer.valueOf(scores[1])));
                    score_contracts++;
                }
                else{
                    other_contracts.add(contract);
                }
            }
        }
        int expected_score_runners = (max_score + 1) * (max_score + 1);
        if (score_contracts != expected_score_runners || other_contracts.size() != other_contracts_size){
            log.severe(String.format("smarkets otherscorebet setup error." +
                            "\nmax_score: %d\nexpected score runners: %d\n",
                    max_score, expected_score_runners, jstring(contracts)));
            return null;
        }

        return max_score;
    }

    public Integer max_correct_score_goals(JSONArray all_contracts, String market_id, boolean halftime){

        int total_market_contracts = count_attribute(all_contracts, "market_id", market_id);

        int total_score_contracts = total_market_contracts - 3;
        if (halftime){
            total_score_contracts = total_market_contracts - 1;
        }

        double max_score = Math.sqrt(total_score_contracts);
        if (Math.ceil(max_score) == Math.floor(max_score)){
            return (int) Math.ceil(max_score);
        }
        log.severe(sf("Max score for correct goals returned not int %s", max_score));
        return null;
    }


    public static String fullname(String marketType, String market_type_param,
                                  String contractType, String contractType_param){

        return String.format("%s%s_%s%s",
                marketType, market_type_param, contractType, contractType_param);
    }


    @Override
    public MarketOddsReport _getMarketOddsReport(Collection<Bet> bets) throws InterruptedException {
        lastMarketOddsReport_start_time = Instant.now();

        // Ensure we have the event id
        if (event_id == null){
            return MarketOddsReport.ERROR(String.format("No event id for smarkets event %s", event));
        }


        // Collect which markets are needed for chosen bets
        Set<String> market_ids = new HashSet<>();
        for (Bet bet: bets){
            String contract_id = betId_contract_map.get(bet.id());
            if (contract_id == null){
                continue;
            }
            String market_id = contract_market_map.get(contract_id);
            market_ids.add(market_id);
        }


        // Get most up to date odds for the market ids chosen
        JSONObject lastPrices = smarkets.getPrices(market_ids);
        if (lastPrices == null){
            return MarketOddsReport.ERROR("Smarkets last prices returned null.");
        }
        else if (lastPrices.containsKey(Smarkets.RATE_LIMITED)){
            return MarketOddsReport.RATE_LIMITED();
        }


        MarketOddsReport new_marketOddsReport = new MarketOddsReport(event);
        for (Bet bet: bets){
            if (bet_blacklist.contains(bet.id())){
                continue;
            }


            // Find the contract id from the map
            String contract_id = betId_contract_map.get(bet.id());
            if (contract_id == null){
                bet_blacklist.add(bet.id());
                continue;
            }

            JSONObject prices = (JSONObject) lastPrices.get(contract_id);
            if (prices == null){
                log.severe(String.format("Couldn't find contract_id '%s' for bet %s " +
                                "within last prices (Should be able to).\n%s",
                        contract_id, bet, jstring(lastPrices)));
                continue;
            }

            // Find correct prices depending on if bet is back/lay
            JSONArray offers = null;
            if (bet.isBack()){
                offers = (JSONArray) prices.get("offers");
            }
            else {
                offers = (JSONArray) prices.get("bids");
            }

            // Convert to our list to a betexchange
            BetExchange betExchange = new BetExchange(site, event, bet);
            betExchange.addMetadata(Smarkets.CONTRACT_ID, contract_id);
            betExchange.addMetadata(Smarkets.MARKET_ID, contract_market_map.get(contract_id));

            for (Object s_offer_obj: offers){
                JSONObject s_offer = (JSONObject) s_offer_obj;

                // Get price and vol as integers and convert them.
                int price = ((Long) s_offer.get("price")).intValue();
                long quantity = (long) s_offer.get("quantity");
                BigDecimal decimal_odds = Smarkets.price2DecOdds(price);
                BigDecimal volume = Smarkets.quantity2BackStake(quantity, price);

                BetOffer bo = new BetOffer(smarkets, event, bet, decimal_odds, volume);
                bo.addMetadata(Smarkets.SMARKETS_PRICE, String.valueOf(price));
                betExchange.add(bo);
            }
            new_marketOddsReport.addExchange(betExchange);
        }

        lastMarketOddsReport_end_time = Instant.now();
        return new_marketOddsReport;
    }





    public static void main(String[] args) throws Exception {

        Smarkets b = new Smarkets();
        SmarketsEventTracker set = (SmarketsEventTracker) b.getEventTracker();

        Event fm = FootballMatch.parse("2021-01-26T20:15:00.000Z", "West Bromwich Albion v Manchester City");
        set.setupMatch(fm);



    }
}
