package Bet;

import SiteConnectors.BettingSite;
import Sport.Match;
import Trader.SportsTrader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.logging.Logger;

import static net.dongliu.commons.Prints.print;

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

    public Map<String, ArrayList<BetOffer>> betOffers;
    public Set<String> sites_used;


    public MarketOddsReport(){
        betOffers = new HashMap<String, ArrayList<BetOffer>>();
        sites_used = new HashSet<>();
    }


    public void addBetOffers(String bet_id, ArrayList<BetOffer> new_betOffers){
        betOffers.put(bet_id, new_betOffers);
        for (BetOffer bo: new_betOffers){
            sites_used.add(bo.site.name);
        }

    }


    public String site_name(){
        if (sites_used.size() == 1){
            return sites_used.iterator().next();
        }
        else if (sites_used.size() > 1){
            log.severe(String.format("Odds report for single site but has this many %s",
                    sites_used.toString()));
            return null;
        }
        return null;
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


    public ArrayList<BetOffer> get(String key){
        return betOffers.get(key);
    }


    public int size(){
        return betOffers.size();
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

            // Combine sites set
            combined.sites_used.addAll(mor.sites_used);

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
                    if (!site_sums.containsKey(offer.site.name)){
                        site_sums.put(offer.site.name, 1);
                    }
                    else{
                        site_sums.put(offer.site.name, ((int) site_sums.get(offer.site.name)) + 1);
                    }
                }
                odds_reports.put(bet_id, site_sums);
            }

        }
        JSONObject j = new JSONObject();
        j.put("bet_offers", odds_reports);
        j.put("match", match().toString());
        return j;
    }


    public JSONObject toJSON(){
        return toJSON(true);
    }


    public Set<String> sitesPresent(){
        Set<String> sites = new HashSet<>();

        for (Map.Entry<String, ArrayList<BetOffer>> entry: betOffers.entrySet()){
            ArrayList<BetOffer> betoffers = entry.getValue();

            for (BetOffer betOffer: betoffers){
                sites.add(betOffer.site.name);
            }
        }

        return sites;

    }

    @Override
    public String toString() {
        return betOffers.toString();
    }
}
