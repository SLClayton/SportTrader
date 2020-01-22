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
    public String type;

    public BigDecimal total_investment;

    public BigDecimal min_return;
    public BigDecimal max_return;
    public BigDecimal min_profit;
    public BigDecimal max_profit;
    public BigDecimal profit_ratio;

    public BigDecimal ret_from_min_stake;
    public BigDecimal ret_from_max_stake;


    public BetOrderProfitReport(ArrayList<BetOrder> betOrders) {

        this.betOrders = betOrders;

        // Sum up all investments
        // Find minimum return of all bet orders
        // Find maximum return of all bet orders
        total_investment = BigDecimal.ZERO;
        for (BetOrder bo: betOrders){
            total_investment = total_investment.add(bo.investment);

            if (min_return == null || bo.actual_return.compareTo(min_return) == -1){
                min_return = bo.actual_return;
            }
            if (max_return == null || bo.actual_return.compareTo(max_return) == 1){
                max_return = bo.actual_return;
            }

            if (ret_from_min_stake == null) {
                ret_from_min_stake = bo.bet_offer.returnFromMinStake();
            }
            else{
                ret_from_min_stake = ret_from_min_stake.max(bo.bet_offer.returnFromMinStake());
            }

            if (ret_from_max_stake == null){
                ret_from_max_stake = bo.bet_offer.returnFromMaxStake();
            }
            else{
                ret_from_max_stake = ret_from_max_stake.min(bo.bet_offer.returnFromMaxStake());
            }
        }

        min_profit = min_return.subtract(total_investment);
        max_profit = max_return.subtract(total_investment);

        if (total_investment.compareTo(BigDecimal.ZERO) == 0){
            profit_ratio = null;
        }
        else{
            profit_ratio = min_profit.divide(total_investment, 20, RoundingMode.HALF_UP);
        }
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
                j.put("match", betOrders.get(0).match().toString());
            }
            else{
                j.put("match", null);
            }
        }


        BetGroup tautology = new BetGroup();
        for (BetOrder bo: betOrders){
            tautology.add(bo.bet());
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
            sites_used.add(bo.site());
        }
        return sites_used;
    }


    public BetOrderProfitReport newProfitReportReturn(BigDecimal target_return) {
        // Create a new profit report thats the same but with a different target return.


        ArrayList<BetOrder> new_bet_orders = new ArrayList<BetOrder>();
        for (int i=0; i< betOrders.size(); i++){
            new_bet_orders.add(new BetOrder(betOrders.get(i).bet_offer, target_return, true));
        }

        return new BetOrderProfitReport(new_bet_orders);
    }


    public boolean smallerInvestment(BetOrderProfitReport pr){
        return total_investment.compareTo(pr.total_investment) == -1;
    }


    public boolean biggerInvestment(BetOrderProfitReport pr){
        return total_investment.compareTo(pr.total_investment) == 1;
    }


    public BetOrderProfitReport newProfitReportInvestment(BigDecimal new_target_investment) {

        // Find the average target returns of the betOrders
        BigDecimal sum_target_return = BigDecimal.ZERO;
        for (BetOrder betOrder: betOrders){
            sum_target_return = sum_target_return.add(betOrder.target_return);
        }
        BigDecimal avg_target_return = sum_target_return.divide(
                new BigDecimal(betOrders.size()), 20, RoundingMode.HALF_UP);

        // Use ratio of this investment and target investment to multiply old target return
        // to new target return
        BigDecimal ratio = new_target_investment.divide(total_investment, 20, RoundingMode.HALF_UP);
        BigDecimal new_target_return = avg_target_return.multiply(ratio);

        return newProfitReportReturn(new_target_return);
    }


    public BetGroup getTautology(){
        ArrayList<Bet> bets = new ArrayList<>();
        for (BetOrder betOrder: betOrders){
            bets.add(betOrder.bet());
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
