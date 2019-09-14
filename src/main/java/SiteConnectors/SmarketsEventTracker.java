package SiteConnectors;

import Bet.BetOffer;
import Bet.FootballBet.FootballBet;
import Bet.FootballBet.FootballOverUnderBet;
import Bet.FootballBet.FootballResultBet;
import Bet.FootballBet.FootballScoreBet;
import Bet.MarketOddsReport;
import Sport.FootballMatch;
import Sport.Match;
import Trader.EventTrader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static tools.printer.*;

public class SmarketsEventTracker extends SiteEventTracker {

    public Smarkets smarkets;
    public String event_id;
    public FootballMatch match;

    public static String[] market_names = new String[] {
            "OVER_UNDER",
            "WINNER_3_WAY",
            "CORRECT_SCORE"
    };
    public Map<String, JSONObject> id_market_map;
    public ArrayList<String> market_ids;
    public Map<String, String> fullname_contract_map;

    public JSONObject lastPrices;
    public MarketOddsReport marketOddsReport;


    public SmarketsEventTracker(Smarkets smarkets){
        this.smarkets = smarkets;
        id_market_map = new HashMap<>();
        fullname_contract_map = new HashMap<>();
    }


    @Override
    public String name() {
        return "smarkets";
    }

    @Override
    public boolean setupMatch(FootballMatch setup_match) throws IOException, URISyntaxException, InterruptedException {

        // Get events from smarkets which match the sport and time
        ArrayList<FootballMatch> events = smarkets.getEvents(
                setup_match.start_time.minus(1, ChronoUnit.SECONDS),
                setup_match.start_time.plus(1, ChronoUnit.SECONDS),
                smarkets.FOOTBALL);

        match = null;
        // Verify each match in flashscores and see if it matches
        for (FootballMatch fm: events){

            try{
                fm.verify();
            } catch (InterruptedException | IOException | URISyntaxException | FlashScores.verificationException e){
                log.warning(String.format("Could not verify smarkets match %s in flashscores.", fm));
                continue;
            }

            if (fm.FSID.equals(setup_match.FSID)){
                match = fm;
                event_id = fm.metadata.get("smarkets_event_id");
                break;
            }
        }

        // Check for no match
        if (match == null){
            log.warning(String.format("No match for %s found in smarkets. Searched %d events %s.",
                    setup_match, events.size(), Match.listtostring(events)));
            return false;
        }

        // Assign values to object
        event_id = match.metadata.get("smarkets_event_id");
        this.match = setup_match;



        // Setup market data for this match
        market_ids = new ArrayList<String>();
        JSONArray markets = smarkets.getMarkets(event_id);
        for (Object market_obj: markets) {
            // Get market data
            JSONObject market = (JSONObject) market_obj;
            String market_type_name = (String) ((JSONObject) market.get("market_type")).get("name");
            String market_id = (String) market.get("id");

            // Add to list if type name appears in our whitelist
            if (Arrays.asList(market_names).contains(market_type_name)){
                market_ids.add(market_id);
                id_market_map.put(market_id, market);
            }
        }

        // Build a smarkets 'fullname' for each possible contract and map to its id
        JSONArray contracts = smarkets.getContracts(market_ids);
        p(contracts);
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

            for (Object contract_obj : contracts) {
                JSONObject contract = (JSONObject) contract_obj;

                // Extract contract names and ids etc
                JSONObject contract_type = (JSONObject) contract.get("contract_type");
                String contract_id = (String) contract.get("id");
                String contract_market_id = (String) contract.get("market_id");
                if (!contract_market_id.equals(market_id)){
                    continue;
                }
                String contract_type_name = (String) contract_type.get("name");
                String contract_type_param = "";
                if (contract_type.containsKey("param")) {
                    contract_type_param = (String) contract_type.get("param");
                }

                // Construct an original 'fullname' for this contract and add its ID to map
                String fullname = String.format("%s%s_%s%s",
                        market_type_name, market_type_param, contract_type_name, contract_type_param);
                fullname_contract_map.put(fullname, contract_id);
            }
        }
        return true;
    }

    @Override
    public void updateMarketOddsReport(FootballBet[] bets) throws Exception {
        if (event_id == null){
            throw new Exception("Smarkets event trader tried to update odds without an event.");
        }

        updateMarketData();

        MarketOddsReport new_marketOddsReport = new MarketOddsReport();
        for (FootballBet bet: bets){
            if (bet_blacklist.contains(bet.id())){
                continue;
            }

            // Get smarkets marketname depending on bet type
            String contract_fullname = null;
            switch (bet.category){

                case "RESULT":
                    FootballResultBet rb = (FootballResultBet) bet;
                    if (rb.winnerA()){
                        contract_fullname = "WINNER_3_WAY_HOME";
                    }
                    else if (rb.winnerB()) {
                        contract_fullname = "WINNER_3_WAY_AWAY";
                    }
                    else if (rb.isDraw()) {
                        contract_fullname = "WINNER_3_WAY_DRAW";
                    }
                    break;

                case "CORRECT_SCORE":
                    FootballScoreBet sb = (FootballScoreBet) bet;
                    contract_fullname = String.format("CORRECT_SCORE_SCORE%d-%d", sb.score_a, sb.score_b);
                    break;

                case "GOAL_COUNT":
                    FootballOverUnderBet oub = (FootballOverUnderBet) bet;
                    contract_fullname = String.format("OVER_UNDER%s_%s", oub.goals.toString(), oub.side.toUpperCase());
                    break;

                default:
                    bet_blacklist.add(bet.id());
                    log.fine(String.format("Bet '%s' not valid for smarkets config yet. Blacklisting.", bet));
                    continue;
            }

            if (contract_fullname == null){
                log.warning(String.format("Could not create fullname for smarket bet %s", bet));
            }

            String contract_id = fullname_contract_map.get(contract_fullname);
            JSONObject prices = (JSONObject) lastPrices.get(contract_id);
            if (prices == null){
                log.severe(String.format("Couldn't find contract_id '%s' for bet %s " +
                                "within last prices (Should be able to).\n%s",
                        contract_id, bet, ps(lastPrices)));
                continue;
            }

            // Find correct prices depending on if bet is back/lay
            JSONArray offers = null;
            if (bet.isBack()){
                offers = (JSONArray) prices.get("offers");
            }
            else if (bet.isLay()){
                offers = (JSONArray) prices.get("bids");
            }
            else{
                log.severe(String.format("Bet is not back or lay??? - %s - %s", bet.type, bet));
                continue;
            }

            // Convert to our list
            ArrayList<BetOffer> new_betOffers = new ArrayList<>();
            for (Object s_offer_obj: offers){
                JSONObject s_offer = (JSONObject) s_offer_obj;

                // Get price and vol as integers and convert them.
                long odds_raw = (long) s_offer.get("price");
                long volume_raw = (long) s_offer.get("quantity");
                BigDecimal odds = new BigDecimal(10000).divide(new BigDecimal(odds_raw), 2, RoundingMode.HALF_UP);
                BigDecimal volume = new BigDecimal(volume_raw).divide(new BigDecimal(10000),2, RoundingMode.HALF_UP);

                // Put contract ID in metadata
                HashMap<String, String> metadata = new HashMap<>();
                metadata.put("contract_id", contract_id);

                BetOffer bo = new BetOffer(match, bet, smarkets, odds, volume, metadata);
                new_betOffers.add(bo);
            }

            // Add this bet, and its bet offers for smarkets into the odds report.
            new_marketOddsReport.addBetOffers(bet.id(), new_betOffers);
        }

        // Assign new odds report.
        marketOddsReport = new_marketOddsReport;
        p(marketOddsReport.toJSON());
    }

    public void updateMarketData() throws InterruptedException, IOException, URISyntaxException {
        lastPrices = smarkets.getPrices(market_ids);
    }


    public static void main(String[] args){

        try {
            Smarkets s = new Smarkets();
            SmarketsEventTracker set = (SmarketsEventTracker) s.getEventTracker();

            FootballMatch fm = new FootballMatch("2019-09-14T11:30:00Z",
                    "Liverpool", "Newcastle");

            fm.verify();
            set.setupMatch(fm);
            set.updateMarketData();
            p(set.lastPrices);






        } catch (CertificateException | InterruptedException | FlashScores.verificationException e) {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
