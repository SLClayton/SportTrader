package Bet;

import SiteConnectors.BettingSite;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

import static tools.printer.print;

public class PlacedBet {

    public String state;
    public String bet_id;
    public BetOffer betOffer;
    public BigDecimal investment;
    public BigDecimal returns;
    public Instant time_placed;
    public String error;


    public PlacedBet(String state, String bet_id, BetOffer betOffer, BigDecimal investment,
                     Instant time_placed){

        this.state = state;
        this.bet_id = bet_id;
        this.betOffer = betOffer;
        this.investment = investment;
        this.returns = returns;
        this.time_placed = time_placed;
    }

    public PlacedBet(String state, BetOffer betOffer, String error){
        this.state = state;
        this.betOffer = betOffer;
        this.error = error;
    }

    public boolean successful(){
        return (state.toUpperCase().equals("SUCCESS"));
    }

    public BettingSite site(){
        return betOffer.site;
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
        m.put("bet_id", String.valueOf(bet_id));
        m.put("bet_offer", betOffer.toJSON());
        m.put("invested", String.valueOf(investment));
        m.put("returns", String.valueOf(returns));
        m.put("time_placed", String.valueOf(time_placed));
        m.put("error", String.valueOf(error));
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
