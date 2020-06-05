package Bet;

import SiteConnectors.BettingSite;
import Trader.SportsTrader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.logging.Logger;

import static tools.printer.*;

public class BetOrderProfitReport implements Comparable<BetOrderProfitReport> {
    /*
    // A collection of Bet Orders or placed bets, with attributes calculated such as total stake
    // and profit/loss ratio.
     */

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    public List<BetOrder> betOrders;

    public BigDecimal total_investment;
    public BigDecimal min_return;
    public BigDecimal max_return;
    public BigDecimal min_profit;
    public BigDecimal max_profit;
    public BigDecimal profit_ratio;

    public BigDecimal ret_from_min_stake;
    public BigDecimal ret_from_max_stake;


    public BetOrderProfitReport(ArrayList<BetOrder> betOrders) {


    }


    public String toString(boolean full){
        return jstring(toJSON(full));
    }


    public JSONObject toJSON(boolean use_betorders){
        JSONObject j = new JSONObject();
        j.put("total_investment", total_investment.toString());
        j.put("min_return", min_return.toString());
        j.put("max_return", max_return.toString());
        j.put("min_profit", min_profit.toString());
        j.put("max_profit", max_profit.toString());
        j.put("profit_ratio", profit_ratio.toString());



        if (use_betorders){
            JSONArray orders = new JSONArray();
            for (BetOrder bo: betOrders){
                orders.add(bo.toJSON());
            }
            j.put("bet_orders", orders);

        }
        else{
            if (betOrders.size() > 0){
                j.put("event", betOrders.get(0).getEvent().toString());
            }
            else{
                j.put("event", null);
            }
        }


        BetGroup tautology = new BetGroup();
        for (BetOrder bo: betOrders){
            tautology.add(bo.getBet());
        }
        j.put("tautology_id", tautology.id());
        j.put("bet_ids", tautology.toJSON(false));

        return j;
    }



    public boolean isValid(){
        return profit_ratio != null;
    }


    public Set<BettingSite> sitesUsed(){
        Set<BettingSite> sites_used = new HashSet<>();
        for (BetOrder bo: betOrders){
            sites_used.add(bo.getSite());
        }
        return sites_used;
    }


    public BetOrderProfitReport newProfitReportReturn(BigDecimal new_target_return) {
        // Create a new profit report thats the same but with a different target return.
        ArrayList<BetOrder> new_bet_orders = new ArrayList<BetOrder>();
        for (BetOrder betOrder: betOrders){
            new_bet_orders.add(betOrder.newTargetReturn(new_target_return));
        }
        return new BetOrderProfitReport(new_bet_orders);
    }

    public BetOrderProfitReport newProfitReportInvestment(BigDecimal new_target_investment) {
        // Create a new profit report thats the same but with a different target investment.
        ArrayList<BetOrder> new_bet_orders = new ArrayList<BetOrder>();
        for (BetOrder betOrder: betOrders){
            new_bet_orders.add(betOrder.newTargetInvestment(new_target_investment));
        }
        return new BetOrderProfitReport(new_bet_orders);
    }


    public boolean smallerInvestment(BetOrderProfitReport pr){
        return total_investment.compareTo(pr.total_investment) == -1;
    }


    public boolean biggerInvestment(BetOrderProfitReport pr){
        return total_investment.compareTo(pr.total_investment) == 1;
    }


    public BetGroup getBetGroup(){
        ArrayList<Bet> bets = new ArrayList<>();
        for (BetOrder betOrder: betOrders){
            bets.add(betOrder.getBet());
        }
        return new BetGroup(bets);
    }


    public static JSONArray listToJSON(ArrayList<BetOrderProfitReport> betOrderProfitReports, boolean full){
        JSONArray prs = new JSONArray();
        for (BetOrderProfitReport pr: betOrderProfitReports){
            prs.add(pr.toJSON(full));
        }
        return prs;
    }



    @Override
    public int compareTo(BetOrderProfitReport betOrderProfitReport) {
        return this.profit_ratio.compareTo(betOrderProfitReport.profit_ratio);
    }
}
