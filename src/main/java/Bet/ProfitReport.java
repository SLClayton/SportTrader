package Bet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.naming.directory.InvalidAttributesException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import static tools.printer.*;

public class ProfitReport implements Comparable<ProfitReport> {
    /*
    // A collection of Bet Orders, with attributes calculated such as total stake
    // and profit/loss ratio.
     */

    public ArrayList<BetOrder> bet_orders;
    public BigDecimal total_investment;

    public BigDecimal min_return;
    public BigDecimal max_return;
    public BigDecimal largest_min_return;

    public BigDecimal guaranteed_profit;
    public BigDecimal max_profit;

    public  BigDecimal profit_ratio;

    public ProfitReport(ArrayList<BetOrder> BETORDERS) throws InstantiationException {
        bet_orders = BETORDERS;
        bet_orders.trimToSize();

        total_investment = BigDecimal.ZERO;
        for (BetOrder bo: bet_orders){
            print("adding betorder to profitreport");
            total_investment = total_investment.add(bo.investment);
            print("inv: " + bo.investment.toString());
            print("total inv: " + total_investment.toString());

            if (min_return == null || bo.real_return.compareTo(min_return) == -1){
                min_return = bo.real_return;
            }
            if (max_return == null || bo.real_return.compareTo(max_return) == 1){
                max_return = bo.real_return;
            }
            BigDecimal this_largest_min_return = bo.bet_offer.minStakeReturn();
            if (largest_min_return == null || this_largest_min_return.compareTo(largest_min_return) == 1){
                largest_min_return = this_largest_min_return;
            }
        }

        guaranteed_profit = min_return.subtract(total_investment);
        max_profit = max_return.subtract(total_investment);

        print("total inv: " + total_investment.toString());

        if (total_investment.equals(BigDecimal.ZERO)){
            throw new InstantiationException("0 total investment");
        }
        else{
            profit_ratio = guaranteed_profit.divide(total_investment, 20, RoundingMode.HALF_UP);
        }
    }


    public String toString(boolean full){
        return ps(toJSON(full));
    }


    public JSONObject toJSON(boolean full){
        JSONObject m = new JSONObject();
        m.put("total_investment", total_investment.toString());
        m.put("min_return", min_return.toString());
        m.put("max_return", max_return.toString());
        m.put("guaranteed_profit", guaranteed_profit.toString());
        m.put("max_profit", max_profit.toString());
        m.put("profit_ratio", profit_ratio.toString());
        if (full){
            JSONArray orders = new JSONArray();
            for (BetOrder bo: bet_orders){
                orders.add(bo.toJSON());
            }
            m.put("bet_orders", orders);
        }

        return m;
    }


    public ProfitReport newProfitReport(BigDecimal target_return) throws InvalidAttributesException, InstantiationException {
        if (target_return.compareTo(largest_min_return) != -1){
            throw new InvalidAttributesException("Target return is smaller than largest min return");
        }

        ArrayList<BetOrder> new_bet_orders = new ArrayList<BetOrder>();
        for (int i=0; i< bet_orders.size(); i++){
            new_bet_orders.add(new BetOrder(bet_orders.get(i).bet_offer, target_return, true));
        }

        return new ProfitReport(new_bet_orders);
    }


    public static ArrayList<ProfitReport> getTautologyProfitReports(ArrayList<Tautology> tautologies, MarketOddsReport marketOddsReport){
        /*
        // Using a list of tautologies and the market odds report, generate a profit report
        // for each tautology which
         */

        // Calculate profitReport for each tautology using the best ROI for each bet
        ArrayList<ProfitReport> tautologyProfitReports = new ArrayList<ProfitReport>();
        for (Tautology tautology: tautologies){
            print("Trying tautology size " + String.valueOf(tautology.size()) + " " + tautology.toString());

            // Ensure all bets exist before continuing
            boolean skip = false;
            for (Bet bet: tautology.bets){
                if (!marketOddsReport.contains(bet.id()) || marketOddsReport.get(bet.id()).size() <= 0){
                    print("No odds for this bet or odds not present");
                    skip = true;
                    break;
                }
            }
            if (skip){
                continue;
            }
            print("ADDING tautology");


            // Generate a list of ratio profitReport using the best offer for each bet
            ArrayList<BetOrder> betOrders = new ArrayList<BetOrder>();
            for (Bet bet: tautology.bets){
                BetOffer best_offer = marketOddsReport.get(bet.id()).get(0);
                betOrders.add(new BetOrder(best_offer, BigDecimal.ONE, false));
            }

            print(betOrders.size());

            ProfitReport pr;
            try {
                pr = new ProfitReport(betOrders);
            }
            catch (InstantiationException e) {
                print("Instantiation exception!!!!!!!!!!!!!!!!!!!!!!!!!");
                continue;
            }
            print("ADDING tautology TO LIST");
            tautologyProfitReports.add(pr);
            print("PR size");
            print(tautologyProfitReports.size());
        }

        print("Done getting prs");
        print(tautologyProfitReports.size());
        return tautologyProfitReports;
    }

    @Override
    public int compareTo(ProfitReport profitReport) {
        return this.profit_ratio.compareTo(profitReport.profit_ratio);
    }
}
