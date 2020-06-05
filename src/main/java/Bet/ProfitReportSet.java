package Bet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.util.*;

import static tools.printer.pp;

public class ProfitReportSet {
    // A set of profit reports. Usually to show every tautology.

    public List<BetOrderProfitReport> betOrderProfitReports;


    public ProfitReportSet(){
        betOrderProfitReports = new ArrayList<>();
    }


    public boolean add(BetOrderProfitReport betOrderProfitReport){
        boolean added = betOrderProfitReports.add(betOrderProfitReport);
        return added;
    }



    public BigDecimal best_profit(){
        BigDecimal best_profit = null;
        for (BetOrderProfitReport pr: betOrderProfitReports){
            if (best_profit == null){
                best_profit = pr.profit_ratio;
            }
            else{
                best_profit = best_profit.max(pr.profit_ratio);
            }
        }
        return best_profit;
    }


    public void sort_by_profit(){
        Collections.sort(betOrderProfitReports, Collections.reverseOrder());
    }



    public int size(){
        return betOrderProfitReports.size();
    }


    public BetOrderProfitReport get(int index){
        return betOrderProfitReports.get(index);
    }


    public BetOrderProfitReport remove(int index){
        return betOrderProfitReports.remove(index);
    }


    public ProfitReportSet filter_reports(BigDecimal min_profit_ratio){
        ProfitReportSet filtered = new ProfitReportSet();
        for (BetOrderProfitReport pr: betOrderProfitReports){
            if (pr.profit_ratio.compareTo(min_profit_ratio) != -1){
                filtered.add(pr);
            }
        }
        return filtered;
    }


    public static ProfitReportSet getTautologyProfitReports(Collection<BetGroup> tautologies, MarketOddsReport marketOddsReport){
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
                List<BetOffer> betOffers = marketOddsReport.get(bet.id());
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
                betOrders.add(BetOrder.fromTargetInvestment(best_valid_offer, BigDecimal.ONE));
            }



            BetOrderProfitReport pr = new BetOrderProfitReport(betOrders);
            if (pr.isValid()){
                tautologyProfitReports.add(pr);
            }
        }
        return tautologyProfitReports;
    }


    public JSONObject toJSON(){
        JSONObject j = new JSONObject();
        JSONArray ja = new JSONArray();
        for (BetOrderProfitReport pr: betOrderProfitReports){
            ja.add(pr.toJSON(false));
        }

        j.put("profit_reports", ja);

        return j;
    }
}
