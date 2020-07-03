package Bet;

import SiteConnectors.BettingSite;
import Trader.SportsTrader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import tools.printer.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.logging.Logger;

import static tools.printer.*;

public class ProfitReport implements Comparable<ProfitReport> {

    /*
        A list of either BetOrders or PlacedBets representing a whole implemented tautology.
     */

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    private List<ProfitReportItem> items;

    private BigDecimal total_investment;
    private BigDecimal min_return;
    private BigDecimal max_return;

    public enum OutcomeState {ALL_SUCCESSFUL, NONE_SUCCESSFUL, MIX_STATES}

    public ProfitReport(List<ProfitReportItem> items){

        // All items must add up to a tautology for profit report to make sense
        this.items = items;

        total_investment = BigDecimal.ZERO;
        for (ProfitReportItem item: items){

            total_investment = total_investment.add(item.getInvestment());
            BigDecimal item_return = item.getReturn();

            // Find smallest and largest returns possible if all items placed
            min_return = BDMin(min_return, item_return);
            max_return = BDMax(max_return, item_return);
        }
    }



    public static ProfitReport fromTautology(BetGroup tautology, MarketOddsReport marketOddsReport, BigDecimal returns){

        return null;
    }


    @Override
    public int compareTo(ProfitReport profitReport) {
        return this.minProfitRatio().compareTo(profitReport.minProfitRatio());
    }


    public List<ProfitReportItem> getItems(){
        return items;
    }


    public List<BetOrder> getBetOrders(){
        List<BetOrder> betOrders = new ArrayList<BetOrder>(items.size());
        for (ProfitReportItem item: items){
            betOrders.add((BetOrder) item);
        }
        return betOrders;
    }


    public Class<?> getType(){
        Set<Class<?>> item_types = new HashSet<>(1);
        for (ProfitReportItem item: getItems()){
            item_types.add(item.getClass());
        }

        if (item_types.size() == 1){
            return item_types.iterator().next();
        }
        return null;
    }


    public OutcomeState getPlacedBetsState(){
        int successes = 0;

        for (ProfitReportItem item: items){
            try{
                PlacedBet.State item_state  =  ((PlacedBet) item).getState();
                if (item_state == PlacedBet.State.SUCCESS){
                    successes += 1;
                }
            }
            catch (ClassCastException e){
                return null;
            }
        }

        if (successes == items.size()){
            return OutcomeState.ALL_SUCCESSFUL;
        }
        else if (successes > 0){
            return OutcomeState.MIX_STATES;
        }
        return OutcomeState.NONE_SUCCESSFUL;
    }


    public boolean isValid(){
        return minProfit() != null;
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


    public Set<String> sites_used() {
        Set<String> sites_used = new HashSet<>();
        for (ProfitReportItem item: items){
            sites_used.addAll(item.sites_used());
        }
        return sites_used;
    }

    public Set<Bet> bets_used(){
        Set<Bet> bets_used = new HashSet<>();
        for (ProfitReportItem item: items){
            bets_used.add(item.getBet());
        }
        return bets_used;
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



    public BetGroup getBetGroup(){
        ArrayList<Bet> bets = new ArrayList<>();
        for (ProfitReportItem item: items){
            bets.add(item.getBet());
        }
        return new BetGroup(bets);
    }


    public JSONObject toJSON(boolean include_items) {
        JSONObject j = new JSONObject();
        j.put("items", items.size());
        j.put("min_return", BDString(min_return));
        j.put("max_return", BDString(max_return));
        j.put("total_investment", BDString(total_investment));
        j.put("prof_ratio", BDString(minProfitRatio()));


        JSONArray bet_ids = new JSONArray();
        for (ProfitReportItem item: items){
            bet_ids.add(item.getBet().id());
        }
        Collections.sort(bet_ids);
        j.put("bets", bet_ids);


        if (include_items){
            JSONArray j_items = new JSONArray();
            for (ProfitReportItem item: items){
                j_items.add(item.toJSON());
            }
            j.put("items", j_items);
        }

        return j;
    }



}
