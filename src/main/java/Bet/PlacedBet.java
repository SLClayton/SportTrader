package Bet;

import SiteConnectors.BettingSite;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.lang.management.BufferPoolMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

import static tools.printer.print;

public class PlacedBet {
    // This should be exact real values taken from a site of an actual bet that
    // has been placed.

    public static String FAILED_STATE = "FAILED";
    public static String SUCCESS_STATE = "SUCCESS";

    public String state;
    public String bet_id;
    public BetOrder betOrder;
    public BigDecimal backersStake_layersProfit;
    public BigDecimal backersProfit_layersStake;
    public BigDecimal investment;
    public BigDecimal profit;
    public BigDecimal profit_commission;
    public BigDecimal avg_odds;
    public BigDecimal returns;
    public Instant time_placed;
    public String error;
    public JSONObject site_json_response;


    public PlacedBet(String state, String bet_id, BetOrder betOrder, BigDecimal back_stake, BigDecimal lay_stake,
                     BigDecimal avg_odds, BigDecimal returns, Instant time_placed){

        this.state = state;
        this.bet_id = bet_id;
        this.betOrder = betOrder;
        this.backersStake_layersProfit = back_stake;
        this.backersProfit_layersStake = lay_stake;
        this.avg_odds = avg_odds;
        this.returns = returns;
        this.time_placed = time_placed;

        if (isBack()){
            investment = site().stake2Investment(backersStake_layersProfit).setScale(2, RoundingMode.HALF_UP);
            profit = backersProfit_layersStake;
        }
        else{
            investment = site().stake2Investment(backersProfit_layersStake).setScale(2, RoundingMode.HALF_UP);
            profit = backersStake_layersProfit;
        }

        profit_commission = profit.multiply(betOrder.commission());
    }

    public PlacedBet(String state, String bet_id, BetOrder betOrder, BigDecimal back_stake,
                     BigDecimal avg_odds, BigDecimal returns, Instant time_placed){
        // Constructor without given lay stake, so calculated and put into main constructor

        this(state, bet_id, betOrder, back_stake, BetOffer.backStake2LayStake(back_stake, avg_odds),
                avg_odds, returns, time_placed);
    }


    public PlacedBet(String state, BetOrder betOrder, String error){
        this.state = state;
        this.betOrder = betOrder;
        this.error = error;
    }


    public boolean isBack(){
        return betOrder.isBack();
    }


    public boolean isLay(){
        return betOrder.isLay();
    }


    public boolean successful(){
        return (state.toUpperCase().equals(SUCCESS_STATE));
    }


    public BettingSite site(){
        return betOrder.site();
    }


    public BigDecimal profit(){
        return returns.subtract(investment);
    }


    public String toString(){
        return toJSON().toString();
    }

    public JSONObject toJSON(){
        JSONObject m = new JSONObject();
        m.put("state", String.valueOf(state));
        m.put("betOrder", betOrder.toJSON());
        if (successful()){
            m.put("bet_id", String.valueOf(bet_id));
            m.put("back_stake", String.valueOf(backersStake_layersProfit));
            m.put("lay_stake", String.valueOf(backersProfit_layersStake));
            m.put("invested", String.valueOf(investment));
            m.put("avg_odds", String.valueOf(avg_odds));
            m.put("returns", String.valueOf(returns));
            m.put("time_placed", String.valueOf(time_placed));
            m.put("profit", String.valueOf(profit));
            m.put("prof_com", String.valueOf(profit_commission));
        }
        else{
            m.put("error", String.valueOf(error));
        }

        if (site_json_response != null){
            m.put("site_response", site_json_response);
        }
        if (betOrder.site_json_request != null){
            m.put("site_request", betOrder.site_json_request);
        }

        return m;
    }

    public static JSONArray list2JSON(ArrayList<PlacedBet> placedBets){
        JSONArray ja = new JSONArray();
        for (PlacedBet pb: placedBets){
            ja.add(pb.toJSON());
        }
        return ja;
    }



}
