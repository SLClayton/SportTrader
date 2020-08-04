package Bet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.util.*;

import static tools.BigDecimalTools.*;
import static tools.printer.pp;

public class ProfitReportSet {
    /*
        A set of Profit Reports.
     */

    private List<ProfitReport> profitReports;


    public ProfitReportSet(){
        profitReports = new ArrayList<>();
    }


    public List<ProfitReport> profitReports(){
        return profitReports;
    }


    public boolean add(ProfitReport profitReport){
        return profitReports.add(profitReport);
    }


    public BigDecimal best_profit(){
        BigDecimal best_profit = null;
        for (ProfitReport pr: profitReports){
            best_profit = BDMax(best_profit, pr.minProfitRatio());
        }
        return best_profit;
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
            if (pr.minProfitRatio().compareTo(min_profit_ratio) >= 0){
                filtered.add(pr);
            }
        }
        return filtered;
    }



    public static ProfitReportSet fromTautologies(Collection<BetGroup> tautologies, MarketOddsReport marketOddsReport,
                                                  BigDecimal returns){
        /*
        // Using a list of tautologies and the market odds report, generate a profit report
        // for each tautology which
         */

        // Calculate profitReport for each tautology using the best ROI for each bet
        Set<String> invalid_bets = new HashSet<String>();
        ProfitReportSet tautologyProfitReports = new ProfitReportSet();
        tautologyLoop:
        for (BetGroup tautology : tautologies){

            ProfitReport pr = ProfitReport.fromTautologyTargetReturn(tautology, marketOddsReport, returns);
            if (pr != null && pr.isValid()){
                tautologyProfitReports.add(pr);
            }
        }
        return tautologyProfitReports;
    }


    public JSONObject toJSON(boolean include_items){
        JSONObject j = new JSONObject();

        JSONArray report_jsons = new JSONArray();
        for (ProfitReport pr: profitReports){
            report_jsons.add(pr.toJSON(include_items));
        }
        j.put("profit_reports", report_jsons);


        return j;
    }
}
