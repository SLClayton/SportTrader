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

    public ArrayList<BetOrder> betOrders;
    public String type;

    public BigDecimal total_investment;

    public BigDecimal min_return;
    public BigDecimal max_return;
    public BigDecimal min_profit;
    public BigDecimal max_profit;
    public BigDecimal profit_ratio;

    public BigDecimal ret_from_min_stake;
    public BigDecimal ret_from_max_stake;


    public ProfitReport(ArrayList<BetOrder> betOrders) {

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
            for (BetOrder bo: betOrders){
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


        ArrayList<BetOrder> new_bet_orders = new ArrayList<BetOrder>();
        for (int i=0; i< betOrders.size(); i++){
            new_bet_orders.add(new BetOrder(betOrders.get(i).bet_offer, target_return, true));
        }

        return new ProfitReport(new_bet_orders);
    }


    public boolean smallerInvestment(ProfitReport pr){
        return total_investment.compareTo(pr.total_investment) == -1;
    }

    public boolean biggerInvestment(ProfitReport pr){
        return total_investment.compareTo(pr.total_investment) == 1;
    }


    public ProfitReport newProfitReportInvestment(BigDecimal new_target_investment) {

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


    public static ArrayList<ProfitReport> getTautologyProfitReports(ArrayList<BetGroup> tautologies, MarketOddsReport marketOddsReport){
        /*
        // Using a list of tautologies and the market odds report, generate a profit report
        // for each tautology which
         */

        // Calculate profitReport for each tautology using the best ROI for each bet
        ArrayList<ProfitReport> tautologyProfitReports = new ArrayList<ProfitReport>();
        tautologyLoop:
        for (BetGroup betGroup : tautologies){

            // Ensure all bets exist before continuing
            for (Bet bet: betGroup.bets){
                if (!marketOddsReport.contains(bet.id()) || marketOddsReport.get(bet.id()).size() <= 0){
                    continue tautologyLoop;
                }
            }

            // Generate a list of ratio profitReport using the best offer for each bet
            ArrayList<BetOrder> betOrders = new ArrayList<BetOrder>();
            for (Bet bet: betGroup.bets){

                ArrayList<BetOffer> betOffers = marketOddsReport.get(bet.id());

                // Find best valid offer or if none valid skip this tautology
                BetOffer best_valid_offer = null;
                while (best_valid_offer == null || best_valid_offer.minVolumeNeeded()){
                    if (betOffers.size() > 0){
                        best_valid_offer = betOffers.remove(0);
                    }
                    else{
                        continue tautologyLoop;
                    }
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
