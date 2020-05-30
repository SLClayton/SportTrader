package SiteConnectors.Smarkets;

import Bet.Bet;
import Bet.BetOffer;
import Bet.FootballBet.*;
import Bet.MarketOddsReport;
import SiteConnectors.SiteEventTracker;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

import static tools.printer.*;

public class SmarketsEventTracker extends SiteEventTracker {

    public Smarkets smarkets;
    public String event_id;
    public Integer correct_score_max_goals;
    public Integer half_time_correct_score_max_goals;


    public static String[] market_type_names = new String[] {
            "OVER_UNDER",
            "WINNER_3_WAY",
            "HALF_TIME_WINNER_3_WAY",
            "CORRECT_SCORE",
            "HALF_TIME_CORRECT_SCORE",
            "ASIAN_HANDICAP"
    };


    public Map<String, JSONObject> id_market_map;
    public ArrayList<String> market_ids;
    public Map<String, String> fullname_contract_map;
    public Map<String, String> contract_market_map;

    public JSONObject lastPrices;


    public SmarketsEventTracker(Smarkets smarkets){
        super(smarkets);
        this.smarkets = smarkets;
        id_market_map = new HashMap<>();
        fullname_contract_map = new HashMap<>();
        contract_market_map = new HashMap<>();
    }


    @Override
    public String name() {
        return "smarkets";
    }


    @Override
    public boolean siteSpecificSetup() throws IOException, URISyntaxException, InterruptedException {

        event_id = event.metadata.get(Smarkets.SMARKETS_EVENT_ID);

        // Setup market data for this event
        market_ids = new ArrayList<>();
        JSONArray markets = smarkets.getMarkets(event_id);
        for (Object market_obj: markets) {

            // Get data of this market
            JSONObject market = (JSONObject) market_obj;
            String market_type_name = (String) ((JSONObject) market.get("market_type")).get("name");
            String market_id = (String) market.get("id");

            // Add to list if type name appears in our whitelist
            if (Arrays.asList(market_type_names).contains(market_type_name)){
                market_ids.add(market_id);
                id_market_map.put(market_id, market);
            }
        }

        // Build a smarkets 'fullname' for each possible contract and map to its id
        JSONArray contracts = smarkets.getContracts(market_ids);
        String correct_score_market_id = null;
        String half_time_correct_score_market_id = null;

        for (Object market_obj: markets) {
            JSONObject market = (JSONObject) market_obj;

            // Extract names and ids etc
            JSONObject market_type = (JSONObject) market.get("market_type");
            String market_type_name = (String) market_type.get("name");
            String market_id = (String) market.get("id");
            String market_type_param = "";
            if (market_type.containsKey("param")) {
                market_type_param = (String) market_type.get("param");
            }

            if (market_type_name.equals("CORRECT_SCORE")){
                correct_score_market_id = market_id;
            }
            else if (market_type_name.equals("HALF_TIME_CORRECT_SCORE")){
                half_time_correct_score_market_id = market_id;
            }


            for (Object contract_obj : contracts) {
                JSONObject contract = (JSONObject) contract_obj;
                String contract_market_id = (String) contract.get("market_id");

                // If this contracts market id matches this market, then continue
                if (contract_market_id.equals(market_id)) {

                    JSONObject contract_type = (JSONObject) contract.get("contract_type");
                    String contract_id = (String) contract.get("id");

                    String contract_type_name = (String) contract_type.get("name");
                    String contract_type_param = "";
                    if (contract_type.containsKey("param")) {
                        contract_type_param = (String) contract_type.get("param");
                    }

                    // Construct an original 'fullname' for this contract and add its ID to map
                    String fullname = fullname(market_type_name, market_type_param,
                            contract_type_name, contract_type_param);
                    fullname_contract_map.put(fullname, contract_id);
                    contract_market_map.put(contract_id, market_id);
                }
            }
        }


        if (correct_score_market_id != null){
            correct_score_max_goals = max_correct_score_goals(contracts, correct_score_market_id, 3);
            if (correct_score_max_goals == null){
                return false;
            }
        }
        if (half_time_correct_score_market_id != null){
            half_time_correct_score_max_goals = max_correct_score_goals(contracts, half_time_correct_score_market_id, 1);
            if (half_time_correct_score_max_goals == null){
                return false;
            }
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


    public static String fullname(String marketType, String market_type_param,
                                  String contractType, String contractType_param){

        return String.format("%s%s_%s%s",
                marketType, market_type_param, contractType, contractType_param);
    }


    @Override
    public MarketOddsReport _getMarketOddsReport(Collection<Bet> bets) throws InterruptedException {
        lastMarketOddsReport_start_time = Instant.now();

        if (event_id == null){
            return MarketOddsReport.ERROR(String.format("No event id for smarkets event %s metadata %s",
                    event, event.metadata));
        }
        log.fine(String.format("%s Updating market odds report for smarkets.", event));

        JSONObject lastPrices = smarkets.getPrices(market_ids);
        if (lastPrices == null){
            return MarketOddsReport.ERROR("Smarkets last prices returned null.");
        }
        else if (lastPrices.containsKey(Smarkets.RATE_LIMITED)){
            return MarketOddsReport.RATE_LIMITED();
        }

        MarketOddsReport new_marketOddsReport = new MarketOddsReport();

        for (Bet bet: bets){
            if (bet_blacklist.contains(bet.id())){
                continue;
            }

            // Get smarkets marketname depending on bet type
            String contract_fullname = null;
            switch (bet.category){

                case FootballBet.RESULT_HT:
                case FootballBet.RESULT:
                    FootballResultBet rb = (FootballResultBet) bet;

                    String result = "NO-RESULT";
                    if (rb.winnerA()){       result = "HOME"; }
                    else if (rb.winnerB()) { result = "AWAY"; }
                    else if (rb.isDraw()) {  result = "DRAW"; }

                    String halftime = "";
                    if (rb.halftime == true){ halftime = "HALF_TIME_"; }

                    contract_fullname = String.format("%sWINNER_3_WAY_%s", halftime, result);
                    break;

                case FootballBet.CORRECT_SCORE_HT:
                case FootballBet.CORRECT_SCORE:
                    FootballScoreBet sb = (FootballScoreBet) bet;
                    String cs_halftime = "";
                    if (sb.halftime){ cs_halftime = "HALF_TIME_"; }
                    contract_fullname = String.format("%sCORRECT_SCORE_SCORE%d-%d", cs_halftime, sb.score_a, sb.score_b);
                    break;

                case FootballBet.ANY_OVER_HT:
                case FootballBet.ANY_OVER:
                    FootballOtherScoreBet osb = (FootballOtherScoreBet) bet;
                    String osb_halftime;
                    if (osb.halftime && half_time_correct_score_max_goals != null){
                        osb_halftime = "HALF_TIME_";
                        if (osb.over_score != half_time_correct_score_max_goals){ continue; }
                    }
                    else if (correct_score_max_goals != null) {
                        osb_halftime = "";
                        if (osb.over_score != correct_score_max_goals){ continue; }
                    }
                    else{
                        continue;
                    }
                    String osb_result = null;
                    if (osb.winnerA()){ osb_result = "HOME_WIN"; }
                    if (osb.winnerB()){ osb_result = "AWAY_WIN"; }
                    if (osb.isDraw()){ osb_result = "DRAW"; }
                    if (osb.isAnyResult()){ osb_result = "SCORE"; }

                    contract_fullname = String.format("%sCORRECT_SCORE_ANY_OTHER_%s", osb_halftime, osb_result);
                    break;

                case FootballBet.OVER_UNDER:
                    FootballOverUnderBet oub = (FootballOverUnderBet) bet;
                    contract_fullname = String.format("OVER_UNDER%s_%s", oub.goals.toString(), oub.side.toUpperCase());
                    break;

                case FootballBet.HANDICAP:
                    FootballHandicapBet hb = (FootballHandicapBet) bet;
                    String hc_result;
                    if (hb.winnerB()){      hc_result = "AWAY"; }
                    else if (hb.winnerA()){ hc_result = "HOME"; }
                    else{ continue; }
                    contract_fullname = String.format("ASIAN_HANDICAP%s_%s", hb.a_handicap.toString(), hc_result);
                    break;

                default:
                    log.fine(String.format("Bet '%s' not currently valid for smarkets config. Blacklisting.", bet));
                    bet_blacklist.add(bet.id());
                    continue;
            }

            if (contract_fullname == null){
                log.warning(String.format("Failed to create mapping fullname for smarket bet %s", bet));
            }

            String contract_id = fullname_contract_map.get(contract_fullname);
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

            // Convert to our list to betOffer objects list
            ArrayList<BetOffer> new_betOffers = new ArrayList<>();
            for (Object s_offer_obj: offers){
                JSONObject s_offer = (JSONObject) s_offer_obj;

                // Get price and vol as integers and convert them.
                long price = (long) s_offer.get("price");
                long quantity = (long) s_offer.get("quantity");
                BigDecimal decimal_odds = Smarkets.price2DecOdds(price);
                BigDecimal volume = Smarkets.quantity2BackStake(quantity, price);

                BetOffer bo = new BetOffer(lastMarketOddsReport_start_time, event, bet, smarkets, decimal_odds, volume);
                bo.addMetadata(Smarkets.CONTRACT_ID, contract_id);
                bo.addMetadata(Smarkets.MARKET_ID, contract_market_map.get(contract_id));
                bo.addMetadata(Smarkets.SMARKETS_PRICE, String.valueOf(price));
                bo.addMetadata("fullname", contract_fullname);

                new_betOffers.add(bo);
            }

            new_marketOddsReport.addBetOffers(bet.id(), new_betOffers);
        }

        lastMarketOddsReport_end_time = Instant.now();
        return new_marketOddsReport;
    }





    public static void main(String[] args){

    }
}
