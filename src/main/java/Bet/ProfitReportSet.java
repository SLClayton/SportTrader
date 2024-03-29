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
        this.profitReports = new ArrayList<>();
    }

    public ProfitReportSet(Collection<ProfitReport> profitReports){
        this.profitReports = new ArrayList<>();
        this.profitReports.addAll(profitReports);
    }



    public List<ProfitReport> profitReports(){
        return profitReports;
    }


    public boolean add(ProfitReport profitReport){
        return profitReports.add(profitReport);
    }

    public boolean isEmpty(){
        return profitReports.isEmpty();
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

    public ProfitReportSet filter_positive(){
        return filter_reports(BigDecimal.ONE);
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
