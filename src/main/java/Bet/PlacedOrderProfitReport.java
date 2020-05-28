package Bet;

import Trader.SportsTrader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.logging.Logger;

public class PlacedOrderProfitReport {

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    public List<PlacedBet> placedBets;

    public BigDecimal total_investment;
    public BigDecimal min_return;
    public BigDecimal max_return;
    public BigDecimal min_profit;
    public BigDecimal max_profit;
    public BigDecimal profit_ratio;
    public BetOrderProfitReport betOrderProfitReport;

    public State state;

    public enum State {ALL_SUCCESS, ALL_FAILURES, MIX_STATES}


    public PlacedOrderProfitReport(List<PlacedBet> placedBets, BetOrderProfitReport betOrderProfitReport) {

        this.placedBets = placedBets;
        this.betOrderProfitReport = betOrderProfitReport;

        // Sum up all investments
        // Find minimum return of all placed bets
        // Find maximum return of all placed bets
        total_investment = BigDecimal.ZERO;
        boolean any_success = false;
        boolean any_failures = false;
        for (PlacedBet pb: placedBets){

            if (!pb.state.equals(PlacedBet.State.SUCCESS)){
                any_failures = true;
                continue;
            }
            any_success = true;


            total_investment = total_investment.add(pb.investment);

            if (min_return == null || pb.potReturns().compareTo(min_return) == -1){
                min_return = pb.potReturns();
            }
            if (max_return == null || pb.potReturns().compareTo(max_return) == 1){
                max_return = pb.potReturns();
            }
        }
        if      (any_success && any_failures){  state = State.MIX_STATES; }
        else if (any_success && !any_failures){ state = State.ALL_SUCCESS; }
        else {                                  state = State.ALL_FAILURES; }

        min_profit = min_return.subtract(total_investment);
        max_profit = max_return.subtract(total_investment);

        if (total_investment.compareTo(BigDecimal.ZERO) == 0){
            profit_ratio = null;
        }
        else{
            profit_ratio = min_profit.divide(total_investment, 20, RoundingMode.HALF_UP);
        }
    }


    public JSONObject toJSON(boolean full){
        JSONObject j = new JSONObject();

        JSONObject ppr = new JSONObject();
        ppr.put("total_investment", total_investment.toString());
        ppr.put("min_return", min_return.toString());
        ppr.put("max_return", max_return.toString());
        ppr.put("min_profit", min_profit.toString());
        ppr.put("max_profit", max_profit.toString());
        ppr.put("profit_ratio", profit_ratio.toString());

        j.put("placed_profit_report", ppr);
        j.put("betOrder_profit_report", betOrderProfitReport.toJSON(false));
        if (full){
            JSONArray orders = new JSONArray();
            for (PlacedBet pb: placedBets){
                orders.add(pb.toJSON());
            }
            j.put("placed_bets", orders);
        }
        return j;
    }


}
