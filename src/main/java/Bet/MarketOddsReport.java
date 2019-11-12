package Bet;

import SiteConnectors.BettingSite;
import Sport.Match;
import Trader.SportsTrader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

import static net.dongliu.commons.Prints.print;
import static tools.printer.toFile;

public class MarketOddsReport {
    /*
        A collection of bet Offers, sorted in separate ordered lists by the bet they represent

        eg.
        SCORE_1-1_BACK
                Betfair: 2.34
                Matchbook: 2.32
                Betfair: 2.3
                Smarkets: 2.28

        RESULT_TEAM-B_BACK
                Matchbook: 1.22
                Betfair: 1.20
     */

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());
    private Map<String, ArrayList<BetOffer>> betOffers;


    public MarketOddsReport(){
        betOffers = new HashMap<String, ArrayList<BetOffer>>();
    }


    public void addBetOffers(String bet_id, ArrayList<BetOffer> new_betOffers){
        betOffers.put(bet_id, new_betOffers);
    }


    public Set<Map.Entry<String, ArrayList<BetOffer>>> entrySet(){
        return betOffers.entrySet();
    }



    public Match match(){
        if (betOffers.size() <= 0){
            return null;
        }
        for (Map.Entry<String, ArrayList<BetOffer>> entry: betOffers.entrySet()){
            for (BetOffer betOffer: entry.getValue()){
                return betOffer.match;
            }
        }
        return null;
    }



    public Set<String> sites_used(){
        Set<String> sites_used = new HashSet<>();
        for (Map.Entry<String, ArrayList<BetOffer>> entry: betOffers.entrySet()){
            for (BetOffer betOffer: entry.getValue()){
                sites_used.add(betOffer.site.getName());
            }
        }
        return sites_used;
    }


    public BettingSite site(){
        if (betOffers.size() <= 0){
            return null;
        }
        for (Map.Entry<String, ArrayList<BetOffer>> entry: betOffers.entrySet()){
            for (BetOffer betOffer: entry.getValue()){
                return betOffer.site;
            }
        }
        return null;
    }


    public ArrayList<BetOffer> get(String key){
        return betOffers.get(key);
    }


    public int size(){
        return betOffers.size();
    }


    public int bets_with_offers(){
        int n = 0;
        for (Map.Entry<String, ArrayList<BetOffer>> entry: betOffers.entrySet()){
            if (entry.getValue().size() > 0){
                n++;
            }
        }
        return n;
    }


    public MarketOddsReport filter(String filter){
        Map<String, ArrayList<BetOffer>> filtered_offers = new HashMap<>();
        for (String bet: betOffers.keySet()){
            if (bet.contains(filter)){
                filtered_offers.put(bet, betOffers.get(bet));
            }
        }

        MarketOddsReport filtered_mor = new MarketOddsReport();
        filtered_mor.betOffers = filtered_offers;

        return filtered_mor;
    }


    public boolean contains(String bet_id){
        return betOffers.containsKey(bet_id);
    }


    public boolean contains_bet(String id){
        return betOffers.containsKey(id);
    }


    public static MarketOddsReport combine(ArrayList<MarketOddsReport> marketOddsReports){
        /*
        Combine all market odds reports into one.
         */
        MarketOddsReport combined = new MarketOddsReport();

        // Add all market odds together
        for (MarketOddsReport mor: marketOddsReports){

            // Add each list of offers into the respective bet input
            for (Map.Entry<String, ArrayList<BetOffer>> entry: mor.betOffers.entrySet()){
                String bet_id = entry.getKey();
                ArrayList<BetOffer> offers = entry.getValue();

                if (!combined.contains_bet(bet_id)){
                    combined.addBetOffers(bet_id, new ArrayList<BetOffer>());
                }
                combined.get(bet_id).addAll(offers);
            }
        }

        // Sort the offers for each bet
        for (Map.Entry<String, ArrayList<BetOffer>> entry: combined.betOffers.entrySet()){
            String bet_id = entry.getKey();
            ArrayList<BetOffer> offers = entry.getValue();

            Collections.sort(offers, Collections.reverseOrder());
        }

        return combined;
}


    public JSONObject toJSON(boolean full_offers){
        JSONObject odds_reports = new JSONObject();
        for (Map.Entry<String, ArrayList<BetOffer>> entry: betOffers.entrySet()){
            String bet_id = entry.getKey();
            ArrayList<BetOffer> bet_offers = entry.getValue();


            if (full_offers){
                JSONArray betoffers = new JSONArray();
                for (BetOffer offer: bet_offers) {
                    betoffers.add(offer.toJSON());
                }
                odds_reports.put(bet_id, betoffers);
            }
            else{
                JSONObject site_sums = new JSONObject();
                for (BetOffer offer: bet_offers) {
                    if (!site_sums.containsKey(offer.site.getName())){
                        site_sums.put(offer.site.getName(), 1);
                    }
                    else{
                        site_sums.put(offer.site.getName(), ((int) site_sums.get(offer.site.getName())) + 1);
                    }
                }
                odds_reports.put(bet_id, site_sums);
            }

        }
        JSONObject j = new JSONObject();
        j.put("bet_offers", odds_reports);
        j.put("match", String.valueOf(match()));
        j.put("bets", betOffers.size());
        j.put("bets_with_offers", bets_with_offers());
        return j;
    }


    public JSONObject toJSON(){
        return toJSON(true);
    }



    @Override
    public String toString() {
        return betOffers.toString();
    }
}
