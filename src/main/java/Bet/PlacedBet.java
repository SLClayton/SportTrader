package Bet;

import SiteConnectors.BettingSite;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.lang.management.BufferPoolMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
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
    public String error;
    public JSONObject site_json_response;

    public Instant time_sent;
    public Instant time_created;
    public Instant time_placed;


    public PlacedBet(String state, String bet_id, BetOrder betOrder, BigDecimal back_stake, BigDecimal lay_stake,
                     BigDecimal avg_odds, BigDecimal returns, Instant time_placed, Instant time_sent){

        this.time_created = Instant.now();

        this.state = state;
        this.bet_id = bet_id;
        this.betOrder = betOrder;
        this.backersStake_layersProfit = back_stake;
        this.backersProfit_layersStake = lay_stake;
        this.avg_odds = avg_odds;
        this.returns = returns;

        this.time_placed = time_placed;
        this.time_sent = time_sent;

        // BACK
        if (isBack()){
            investment = site().stake2Investment(backersStake_layersProfit).setScale(2, RoundingMode.HALF_UP);
            profit = backersProfit_layersStake;
        }
        // LAY
        else{
            investment = site().stake2Investment(backersProfit_layersStake).setScale(2, RoundingMode.HALF_UP);
            profit = backersStake_layersProfit;
        }

        profit_commission = profit.multiply(betOrder.commission());
    }

    public PlacedBet(String state, String bet_id, BetOrder betOrder, BigDecimal back_stake,
                     BigDecimal avg_odds, BigDecimal returns, Instant time_placed, Instant time_sent){
        // Constructor without given lay stake, this is calculated using back stake and odds
        // to get the lay stake (backers profit) and then put through the main constructor

        this(state, bet_id, betOrder, back_stake, BetOffer.backStake2LayStake(back_stake, avg_odds),
                avg_odds, returns, time_placed, time_sent);
    }


    public PlacedBet(String state, BetOrder betOrder, String error, Instant time_placed, Instant time_sent){
        // For errors in placed bet

        this.state = state;
        this.betOrder = betOrder;
        this.error = error;
        this.time_placed = time_placed;
        this.time_sent = time_sent;

        backersProfit_layersStake = null;
        backersStake_layersProfit = null;
        returns = null;
        investment = null;
    }

    public PlacedBet(String state, BetOrder betOrder, String error){
        this(state, betOrder, error, null, null);
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


    public BetOffer getBetOffer(){
        return betOrder.bet_offer;
    }


    public JSONObject toJSON(){
        JSONObject m = new JSONObject();

        // Add timings information
        JSONObject timings = new JSONObject();
        timings.put("bet_placed", time_placed.toString());
        timings.put("betoffercreated", getBetOffer().time_betOffer_creation.toString());
        timings.put("bet_sent", time_sent.toString());
        timings.put("placedbet_created", time_created.toString());
        m.put("timings", timings);

        // Add in raw betting site requests and responses
        if (site_json_response != null){
            m.put("site_response", site_json_response);
        }
        if (betOrder.site_json_request != null){
            m.put("site_request", betOrder.site_json_request);
        }

        // Put betOrder this placed bet was based on in json
        JSONObject boj = betOrder.toJSON();
        boj.remove("site");
        boj.remove("event");
        m.put("betOrder", betOrder.toJSON());

        // Put placed bet information in json
        JSONObject pb = new JSONObject();
        pb.put("state", String.valueOf(state));
        pb.put("time_placed", String.valueOf(time_placed));
        pb.put("site", String.valueOf(site().getName()));
        pb.put("event", String.valueOf(betOrder.match()));
        if (successful()){
            pb.put("bet_id", String.valueOf(bet_id));
            pb.put("back_stake", String.valueOf(backersStake_layersProfit));
            pb.put("lay_stake", String.valueOf(backersProfit_layersStake));
            pb.put("invested", String.valueOf(investment));
            pb.put("avg_odds", String.valueOf(avg_odds));
            pb.put("returns", String.valueOf(returns));
            pb.put("profit", String.valueOf(profit));
            pb.put("prof_com", String.valueOf(profit_commission));
        }
        else{
            pb.put("error", String.valueOf(error));
        }
        m.put("placed_bet", pb);

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
