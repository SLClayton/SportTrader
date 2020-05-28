package Bet;

import SiteConnectors.BettingSite;
import Trader.SportsTrader;
import com.globalbettingexchange.externalapi.PlaceOrdersWithReceipt;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.lang.management.BufferPoolMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import static tools.printer.print;

public class PlacedBet {
    // This should be exact real values taken from a site of an actual bet that
    // has been placed.

    public enum State {SUCCESS, FAIL, PARTIAL}

    public BetOrder betOrder;
    public State state;
    public String bet_id;

    public BigDecimal backersStake_layersProfit;
    public BigDecimal investment;
    public BigDecimal avg_odds;
    public String error;

    public Instant time_sent;
    public Instant time_created;
    public Instant time_placed;

    public JSONObject site_json_response;


    public PlacedBet(State state, BetOrder betOrder, String bet_id, BigDecimal back_stake,
                     BigDecimal avg_odds, Instant time_sent, Instant time_placed, String error){


        //TODO: Use examples found to re-create this

        this.time_created = Instant.now();

        this.state = state;
        this.bet_id = bet_id;
        this.betOrder = betOrder;
        this.backersStake_layersProfit = back_stake;
        this.avg_odds = avg_odds;

        this.time_placed = time_placed;
        this.time_sent = time_sent;

        this.error = error;
    }


    public static PlacedBet FullPlacedBet(BetOrder betOrder, String bet_id, BigDecimal backers_stake,
                                          BigDecimal avg_odds, Instant time_sent, Instant time_placed){

        return new PlacedBet(State.SUCCESS, betOrder, bet_id, backers_stake, avg_odds,
                time_sent, time_placed, null);
    }


    public static PlacedBet FailedPlacedBet(BetOrder betOrder, String bet_id, Instant time_sent, Instant time_placed,
                                            String error){

        return new PlacedBet(State.FAIL, betOrder, null, null, null,
                time_sent, time_placed, error);
    }


    public BigDecimal getBackersProfit_layersStake(){
        return Bet.backStake2LayStake(backersStake_layersProfit, avg_odds);
    }

    public boolean isBack(){
        return betOrder.isBack();
    }


    public boolean isLay(){
        return betOrder.isLay();
    }


    public boolean successful(){
        return state.equals(State.SUCCESS);
    }


    public boolean failed(){
        return state.equals(State.FAIL);
    }


    public BigDecimal stake(){
        if (isBack()){
            return backersStake_layersProfit;
        }
        else{
            return getBackersProfit_layersStake();
        }
    }


    public BigDecimal potProfitBeforeCom(){
        if (isBack()){
            return getBackersProfit_layersStake();
        }
        else{
            return backersStake_layersProfit;
        }
    }


    public BigDecimal winCommission(){
        return winCommission(potProfitBeforeCom());
    }


    public BigDecimal winCommission(BigDecimal potProfitBeforeCom){
        return site().winCommissionRate().multiply(potProfitBeforeCom);
    }


    public BigDecimal total_investment(){
        return site().investmentNeededForStake(stake());
    }


    public BigDecimal potReturns(){
        BigDecimal profit_before_com = potProfitBeforeCom();
        return total_investment()
                .add(profit_before_com)
                .subtract(winCommission(profit_before_com));
    }




    public BettingSite site(){
        return betOrder.site();
    }


    public BetOffer getBetOffer(){
        return betOrder.bet_offer;
    }


    public String toString(){
        return toJSON().toString();
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
            pb.put("lay_stake", String.valueOf(getBackersProfit_layersStake()));
            pb.put("invested", String.valueOf(investment));
            pb.put("avg_odds", String.valueOf(avg_odds));
            pb.put("returns", String.valueOf(potReturns()));
            pb.put("profit", String.valueOf(potProfitBeforeCom()));
            pb.put("prof_com", String.valueOf(winCommission()));
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
