package Bet;

import SiteConnectors.BettingSite;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;

import static tools.printer.print;

public class PlacedBet {

    public String state;
    public String bet_id;
    public BetOffer betOffer;
    public BigDecimal investment;
    public BigDecimal returns;
    public Instant time_placed;


    public PlacedBet(String state, String bet_id, BetOffer betOffer, BigDecimal investment,
                     Instant time_placed){

        this.state = state;
        this.bet_id = bet_id;
        this.betOffer = betOffer;
        this.investment = investment;
        this.returns = returns;
        this.time_placed = time_placed;
    }

    public PlacedBet(String state, BetOffer betOffer){
        this.state = state;
        this.betOffer = betOffer;
    }

    public BettingSite site(){
        return betOffer.site;
    }

    public BigDecimal profit(){
        return returns.subtract(investment);
    }

    public String toString(){
        HashMap<String, String> m = new HashMap<String, String>();
        m.put("bet_offer", betOffer.toString());
        m.put("invested", investment.toString());
        m.put("returns", returns.toString());
        m.put("time_placed", time_placed.toString());
        return m.toString();
    }

    public JSONObject toJSON(){
        JSONObject m = new JSONObject();
        m.put("bet_offer", betOffer.toJSON());
        m.put("invested", investment.toString());
        m.put("returns", returns.toString());
        m.put("time_placed", time_placed.toString());
        return m;
    }



}
