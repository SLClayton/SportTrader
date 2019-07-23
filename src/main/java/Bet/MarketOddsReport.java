package Bet;

import java.util.*;

public class MarketOddsReport {

    public HashMap<String, ArrayList<BetOffer>> betOffers;

    public MarketOddsReport(HashMap<String, ArrayList<BetOffer>> betOffers){
        this.betOffers = betOffers;
    }

    public ArrayList<BetOffer> get(String key){
        return betOffers.get(key);
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

            Collections.sort(offers);
        }

        // TODO continue here, odds should be combined and sorted so check


    }
}
