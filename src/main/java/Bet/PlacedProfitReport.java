package Bet;

import Trader.SportsTrader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.logging.Logger;

public class PlacedProfitReport {

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    public ArrayList<PlacedBet> placedBets;

    public BigDecimal total_investment;
    public BigDecimal min_return;
    public BigDecimal max_return;
    public BigDecimal min_profit;
    public BigDecimal max_profit;
    public BigDecimal profit_ratio;

    public String state;
    public ProfitReport blueprint;


    public PlacedProfitReport(ArrayList<PlacedBet> placedBets, ProfitReport blueprint) {

        this.placedBets = placedBets;
        this.blueprint = blueprint;
        this.state = "SUCCESS";

        // Sum up all investments
        // Find minimum return of all placed bets
        // Find maximum return of all placed bets
        total_investment = BigDecimal.ZERO;
        for (PlacedBet pb: placedBets){

            if (!pb.state.equals(PlacedBet.SUCCESS_STATE)){
                this.state = "FAIL";
                continue;
            }

            total_investment = total_investment.add(pb.investment);

            if (min_return == null || pb.returns.compareTo(min_return) == -1){
                min_return = pb.returns;
            }
            if (max_return == null || pb.returns.compareTo(max_return) == 1){
                max_return = pb.returns;
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


    public JSONObject toJSON(boolean full){
        JSONObject j = new JSONObject();
        j.put("total_investment", total_investment.toString());
        j.put("min_return", min_return.toString());
        j.put("max_return", max_return.toString());
        j.put("min_profit", min_profit.toString());
        j.put("max_profit", max_profit.toString());
        j.put("profit_ratio", profit_ratio.toString());
        j.put("pre_profit_report", blueprint.toJSON(false));
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
