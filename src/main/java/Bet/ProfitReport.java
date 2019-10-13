package Bet;

import Trader.SportsTrader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.naming.directory.InvalidAttributesException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.logging.Logger;

import static tools.printer.*;

public class ProfitReport implements Comparable<ProfitReport> {
    /*
    // A collection of Bet Orders or placed bets, with attributes calculated such as total stake
    // and profit/loss ratio.
     */

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    public ArrayList<BetOrder> bet_orders;
    public String type;
    public static String BET_ORDER_TYPE = "BET_ORDERS_TYPE";
    public static String PLACED_BET_TYPE = "PLACED_BET_TYPE";

    public BigDecimal total_investment;

    public BigDecimal min_return;
    public BigDecimal max_return;
    public BigDecimal min_profit;
    public BigDecimal max_profit;
    public BigDecimal profit_ratio;

    public BigDecimal ret_from_min_stake;


    public ProfitReport(ArrayList<Object> betOrders_or_placedBets) {

        this.type = BET_ORDER_TYPE;

        // Sum up all investments
        // Find minimum return of all bet orders
        // Find maximum return of all bet orders
        total_investment = BigDecimal.ZERO;
        for (BetOrder bo: bet_orders){
            total_investment = total_investment.add(bo.investment);

            if (min_return == null || bo.actual_return.compareTo(min_return) == -1){
                min_return = bo.actual_return;
            }
            if (max_return == null || bo.actual_return.compareTo(max_return) == 1){
                max_return = bo.actual_return;
            }

            BigDecimal this_largest_min_return = bo.bet_offer.minStakeReturn();
            if (ret_from_min_stake == null || this_largest_min_return.compareTo(ret_from_min_stake) == 1){
                ret_from_min_stake = this_largest_min_return;
            }
        }

        min_profit = min_return.subtract(total_investment);
        max_profit = max_return.subtract(total_investment);

        if (total_investment.equals(BigDecimal.ZERO)){
            profit_ratio = null;
        }
        else{
            profit_ratio = min_profit.divide(total_investment, 20, RoundingMode.HALF_UP);
        }
    }


    public String toString(boolean full){
        return ps(toJSON(full));
    }


    public JSONObject toJSON(boolean full){
        JSONObject j = new JSONObject();
        j.put("total_investment", total_investment.toString());
        j.put("min_return", min_return.toString());
        j.put("max_return", max_return.toString());
        j.put("min_profit", min_profit.toString());
        j.put("max_profit", max_profit.toString());
        j.put("profit_ratio", profit_ratio.toString());
        if (full){
            JSONArray orders = new JSONArray();
            for (BetOrder bo: bet_orders){
                orders.add(bo.toJSON());
            }
            j.put("bet_orders", orders);

        }
        return j;
    }


    public boolean isValid(){
        return profit_ratio != null;
    }


    public ProfitReport newProfitReportReturn(BigDecimal target_return) {
        // Create a new profit report thats the same but with a different target return.

        if (target_return.compareTo(ret_from_min_stake) == -1){
            String msg = String.format("Creating a new profit report failed. " +
                            "Target return set as %s but the return from the min stake is %s.",
                    target_return.toString(), ret_from_min_stake.toString());
            log.warning(msg);
            return null;
        }

        ArrayList<BetOrder> new_bet_orders = new ArrayList<BetOrder>();
        for (int i=0; i< bet_orders.size(); i++){
            new_bet_orders.add(new BetOrder(bet_orders.get(i).bet_offer, target_return, true));
        }

        return new ProfitReport(new_bet_orders);
    }


    public ProfitReport newProfitReportInvestment(BigDecimal new_target_investment) {

        // Find the average target returns of the betOrders
        BigDecimal sum_target_return = BigDecimal.ZERO;
        for (BetOrder betOrder: bet_orders){
            sum_target_return = sum_target_return.add(betOrder.target_return);
        }
        BigDecimal avg_target_return = sum_target_return.divide(
                new BigDecimal(bet_orders.size()), 20, RoundingMode.HALF_UP);

        // Use ratio of this investment and target investment to multiply old target return
        // to new target return
        BigDecimal ratio = new_target_investment.divide(total_investment, 20, RoundingMode.HALF_UP);
        BigDecimal new_target_return = avg_target_return.multiply(ratio);

        return newProfitReportReturn(new_target_return);
    }


    public static ArrayList<ProfitReport> getTautologyProfitReports(ArrayList<BetGroup> tautologies, MarketOddsReport marketOddsReport){
        /*
        // Using a list of tautologies and the market odds report, generate a profit report
        // for each tautology which
         */

        // Calculate profitReport for each tautology using the best ROI for each bet
        ArrayList<ProfitReport> tautologyProfitReports = new ArrayList<ProfitReport>();
        for (BetGroup betGroup : tautologies){

            // Ensure all bets exist before continuing
            boolean skip = false;
            for (Bet bet: betGroup.bets){
                if (!marketOddsReport.contains(bet.id()) || marketOddsReport.get(bet.id()).size() <= 0){
                    skip = true;
                    break;
                }
            }
            if (skip){
                continue;
            }

            // Generate a list of ratio profitReport using the best offer for each bet
            ArrayList<BetOrder> betOrders = new ArrayList<BetOrder>();
            for (Bet bet: betGroup.bets){
                BetOffer best_offer = marketOddsReport.get(bet.id()).get(0);
                betOrders.add(new BetOrder(best_offer, BigDecimal.ONE, false));
            }

            ProfitReport pr = new ProfitReport(betOrders);
            if (pr.isValid()){
                tautologyProfitReports.add(pr);
            }
        }

        return tautologyProfitReports;
    }

    public static JSONArray listToJSON(ArrayList<ProfitReport> profitReports, boolean full){
        JSONArray prs = new JSONArray();
        for (ProfitReport pr: profitReports){
            prs.add(pr.toJSON(full));
        }
        return prs;
    }

    @Override
    public int compareTo(ProfitReport profitReport) {
        return this.profit_ratio.compareTo(profitReport.profit_ratio);
    }
}
