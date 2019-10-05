package Bet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

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

    

    public HashMap<String, ArrayList<BetOffer>> betOffers;

    public MarketOddsReport(HashMap<String, ArrayList<BetOffer>> betOffers){
        this.betOffers = betOffers;
    }

    public MarketOddsReport(){
        betOffers = new HashMap<String, ArrayList<BetOffer>>();
    }

    public void addBetOffers(String bet_id, ArrayList<BetOffer> new_betOffers){
        betOffers.put(bet_id, new_betOffers);
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

    public static MarketOddsReport combine(ArrayList<MarketOddsReport> marketOddsReports){
        /*
        Combine all market odds reports into one.
         */
        HashMap<String, ArrayList<BetOffer>> combined = new HashMap<String, ArrayList<BetOffer>>();

        // Add all market odds together
        for (MarketOddsReport mor: marketOddsReports){
            for (Map.Entry<String, ArrayList<BetOffer>> entry: mor.betOffers.entrySet()){
                String bet_id = entry.getKey();
                ArrayList<BetOffer> offers = entry.getValue();

                if (!combined.containsKey(bet_id)){
                    combined.put(bet_id, new ArrayList<BetOffer>());
                }
                combined.get(bet_id).addAll(offers);
            }
        }

        // Sort the offers for each bet
        for (Map.Entry<String, ArrayList<BetOffer>> entry: combined.entrySet()){
            String bet_id = entry.getKey();
            ArrayList<BetOffer> offers = entry.getValue();

            Collections.sort(offers, Collections.reverseOrder());
        }

        return new MarketOddsReport(combined);
    }

    public JSONObject toJSON(){
        JSONObject j = new JSONObject();
        for (Map.Entry<String, ArrayList<BetOffer>> entry: betOffers.entrySet()){
            String bet_id = entry.getKey();
            ArrayList<BetOffer> bet_offers = entry.getValue();

            JSONArray ja = new JSONArray();
            for (BetOffer offer: bet_offers){
                ja.add(offer.toJSON());
            }

            j.put(bet_id, ja);
        }
        return j;
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
