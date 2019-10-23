package Bet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static tools.printer.pp;
import static tools.printer.print;

public class ProfitReportSet {
    // A set of profit reports. Usually to show every tautology.

    public ArrayList<ProfitReport> profitReports;


    public ProfitReportSet(){
        profitReports = new ArrayList<>();
    }


    public boolean add(ProfitReport profitReport){
        boolean added = profitReports.add(profitReport);
        return added;
    }


    public void sort_by_profit(){
        Collections.sort(profitReports, Collections.reverseOrder());
    }


    public int size(){
        return profitReports.size();
    }


    public ProfitReport get(int index){
        return profitReports.get(index);
    }


    public ProfitReport remove(int index){
        return profitReports.remove(index);
    }


    public ProfitReportSet filter_reports(BigDecimal min_profit_ratio){
        ProfitReportSet filtered = new ProfitReportSet();
        for (ProfitReport pr: profitReports){
            if (pr.profit_ratio.compareTo(min_profit_ratio) != -1){
                filtered.add(pr);
            }
        }
        return filtered;
    }


    public static ProfitReportSet getTautologyProfitReports(ArrayList<BetGroup> tautologies, MarketOddsReport marketOddsReport){
        /*
        // Using a list of tautologies and the market odds report, generate a profit report
        // for each tautology which
         */

        // Calculate profitReport for each tautology using the best ROI for each bet
        Set<Bet> failed_bets = new HashSet<Bet>();
        ProfitReportSet tautologyProfitReports = new ProfitReportSet();
        tautologyLoop:
        for (BetGroup tautology : tautologies){

            // Ensure all bets exist before continuing
            for (Bet bet: tautology.bets){
                if (!marketOddsReport.contains(bet.id()) || marketOddsReport.get(bet.id()).size() <= 0){
                    failed_bets.add(bet);
                    continue tautologyLoop;
                }
            }


            // Generate a list of ratio profitReport using the best offer for each bet
            ArrayList<BetOrder> betOrders = new ArrayList<BetOrder>();
            for (Bet bet: tautology.bets){

                // Check each offer in sorted list to see if it is valid to bet on
                ArrayList<BetOffer> betOffers = marketOddsReport.get(bet.id());
                BetOffer best_valid_offer = null;
                for (BetOffer betOffer: betOffers){
                    if (betOffer.hasMinVolumeNeeded()){
                        best_valid_offer = betOffer;
                        break;
                    }
                }

                if (best_valid_offer == null){
                    // No valid offer for this bet so tautology is scrapped
                    failed_bets.add(bet);
                    continue tautologyLoop;
                }
                betOrders.add(new BetOrder(best_valid_offer, BigDecimal.ONE, false));
            }



            ProfitReport pr = new ProfitReport(betOrders);
            if (pr.isValid()){
                tautologyProfitReports.add(pr);
            }
        }
        return tautologyProfitReports;
    }


    public JSONObject toJSON(){
        JSONObject j = new JSONObject();
        JSONArray ja = new JSONArray();
        for (ProfitReport pr: profitReports){
            ja.add(pr.toJSON(false, true));
        }

        j.put("profit_reports", ja);

        return j;
    }
}
