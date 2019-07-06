package Bet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

public class ProfitReport {

    public BetOrder[] bet_orders;
    public BigDecimal total_investment;

    public BigDecimal min_return;
    public BigDecimal max_return;
    public BigDecimal largest_min_return;

    public BigDecimal guaranteed_profit;
    public BigDecimal max_profit;

    public  BigDecimal profit_ratio;

    public ProfitReport(BetOrder[] BETORDERS){
        bet_orders = BETORDERS;

        total_investment = BigDecimal.ZERO;
        for (BetOrder bo: bet_orders){
            total_investment.add(bo.investment);

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
        profit_ratio = guaranteed_profit.divide(total_investment);
    }


    public String toString(boolean full){
        HashMap<String, String> m = new HashMap<String, String>();
        m.put("total_investment", total_investment.toString());
        m.put("min_return", min_return.toString());
        m.put("max_return", max_return.toString());
        m.put("guaranteed_profit", guaranteed_profit.toString());
        m.put("max_profit", max_profit.toString());
        m.put("profit_ratio", profit_ratio.toString());
        if (full){
            ArrayList<String> orders = new ArrayList<String>();
            for (BetOrder bo: bet_orders){
                orders.add(bo.toString());
            }
            m.put("bet_orders", orders.toString());
        }

        return m.toString();
    }

    public ProfitReport newProfitReport(BigDecimal target_return) throws Exception {
        if (target_return.compareTo(largest_min_return) != -1){
            throw new Exception("Target return not possible within possible bet ranges.");
        }

        BetOrder[] new_bet_orders = new BetOrder[bet_orders.length];
        for (int i=0; i< bet_orders.length; i++){
            new_bet_orders[i] = new BetOrder(bet_orders[i].bet_offer, target_return, true);
        }

        return new ProfitReport(new_bet_orders);
    }
}
