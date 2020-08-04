package Bet;

import Trader.SportsTrader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.logging.Logger;

import static tools.printer.*;
import static tools.BigDecimalTools.*;

public class ProfitReport implements Comparable<ProfitReport> {

    /*
        A list of either BetOrders or PlacedBets representing a whole implemented tautology.
     */

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    private final List<MultiSiteBet> items;

    private BigDecimal total_investment;
    private BigDecimal min_return;
    private BigDecimal max_return;



    public ProfitReport(List<MultiSiteBet> items){

        // Sort all items into a separate ItemSet for each EVENT/BET
        // so that only one set wins in any eventuality.
        this.items = items;

        total_investment = BigDecimal.ZERO;
        for (MultiSiteBet item: items){

            total_investment = total_investment.add(item.getInvestment());
            BigDecimal item_return = item.getReturn();

            // Find smallest and largest returns possible if all items placed
            min_return = BDMin(min_return, item_return);
            max_return = BDMax(max_return, item_return);
        }
    }



    public static ProfitReport fromTautologyTargetReturn(BetGroup tautology, MarketOddsReport marketOddsReport,
                                                         BigDecimal target_return){



        return null;
    }


    @Override
    public int compareTo(ProfitReport profitReport) {
        return this.minProfitRatio().compareTo(profitReport.minProfitRatio());
    }


    public List<MultiSiteBet> getItems(){
        return items;
    }



    public Class<?> getType(){
        Set<Class<?>> item_types = new HashSet<>(2);
        for (MultiSiteBet item: getItems()){
            item_types.add(item.getClass());
        }

        if (item_types.size() == 1){
            return item_types.iterator().next();
        }
        return null;
    }



    public BigDecimal getTotalInvestment(){
        return total_investment;
    }


    public BigDecimal getMinReturn(){
        return min_return;
    }

    public BigDecimal getMaxReturn(){
        return max_return;
    }



    public BigDecimal minProfit(){
        return getMinReturn().subtract(getTotalInvestment());
    }

    public BigDecimal maxProfit(){
        return getMaxReturn().subtract(getTotalInvestment());
    }

    public boolean isValid(){
        return minProfit() != null;
    }


    public Set<String> sites_used() {
        Set<String> sites_used = new HashSet<>();
        for (MultiSiteBet item: items){
            sites_used.addAll(item.sites_used());
        }
        return sites_used;
    }


    public BigDecimal minProfitRatio(){
        BigDecimal total_investment = getTotalInvestment();
        if (total_investment.compareTo(BigDecimal.ZERO) == 0){
            return null;
        }
        return minProfit().divide(total_investment, 12, RoundingMode.HALF_UP);
    }

    public BigDecimal maxProfitRatio(){
        BigDecimal total_investment = getTotalInvestment();
        if (total_investment.compareTo(BigDecimal.ZERO) == 0){
            return null;
        }
        return maxProfit().divide(total_investment, 12, RoundingMode.HALF_UP);
    }


    public JSONObject toJSON(){
        return toJSON(true);
    }

    public JSONObject toJSON(boolean include_items) {
        JSONObject j = new JSONObject();
        j.put("items", items.size());
        j.put("min_return", BDString(min_return));
        j.put("max_return", BDString(max_return));
        j.put("total_investment", BDString(total_investment));
        j.put("prof_ratio", BDString(minProfitRatio()));


        JSONArray bet_ids = new JSONArray();
        for (MultiSiteBet item: items){
            bet_ids.add(item.getBet().id());
        }
        Collections.sort(bet_ids);
        j.put("bets", bet_ids);


        if (include_items){
            JSONArray j_items = new JSONArray();
            for (MultiSiteBet item: items){
                j_items.add(item.toJSON());
            }
            j.put("items", j_items);
        }

        return j;
    }



}
