package Bet;

import Trader.SportsTrader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.naming.directory.InvalidAttributesException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.logging.Logger;

import static tools.printer.*;

public class ProfitReport implements Comparable<ProfitReport> {
    /*
    // A collection of Bet Orders, with attributes calculated such as total stake
    // and profit/loss ratio.
     */

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    public ArrayList<BetOrder> bet_orders;
    public BigDecimal total_investment;

    public BigDecimal min_return;
    public BigDecimal max_return;
    public BigDecimal min_stake_return;
    public BigDecimal min_profit;
    public BigDecimal max_profit;
    public BigDecimal profit_ratio;

    public ArrayList<PlacedBet> placedBets;

    public ProfitReport(ArrayList<BetOrder> betOrders) {

        this.bet_orders = betOrders;

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
            if (min_stake_return == null || this_largest_min_return.compareTo(min_stake_return) == 1){
                min_stake_return = this_largest_min_return;
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
            // PlacedBets include the bet orders so no need to have both
            if (placedBets != null){
                JSONArray placed = new JSONArray();
                for (PlacedBet pb: placedBets){
                    placed.add(pb.toJSON());
                }
                j.put("placed_bets", placed);
            }
            else{
                JSONArray orders = new JSONArray();
                for (BetOrder bo: bet_orders){
                    orders.add(bo.toJSON());
                }
                j.put("bet_orders", orders);
            }

        }
        return j;
    }


    public boolean isValid(){
        return profit_ratio != null;
    }


    public ProfitReport newProfitReport(BigDecimal target_return) throws InvalidAttributesException {
        // Create a new profit report thats the same but with a different target return.

        if (target_return.compareTo(min_stake_return) == -1){
            String msg = String.format("Creating a new profit report failed. " +
                                       "Target return set as %s but the min stake return is %s.",
                    target_return.toString(), min_stake_return.toString());
            log.severe(msg);
            throw new InvalidAttributesException(msg);
        }

        ArrayList<BetOrder> new_bet_orders = new ArrayList<BetOrder>();
        for (int i=0; i< bet_orders.size(); i++){
            new_bet_orders.add(new BetOrder(bet_orders.get(i).bet_offer, target_return, true));
        }

        return new ProfitReport(new_bet_orders);
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
