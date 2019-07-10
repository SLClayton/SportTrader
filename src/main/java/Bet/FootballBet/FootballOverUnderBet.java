package Bet.FootballBet;

import org.json.simple.JSONObject;

import java.math.BigDecimal;

public class FootballOverUnderBet extends FootballBet{

    public String side;
    public BigDecimal goals;


    public FootballOverUnderBet(String bet_type, String SIDE, BigDecimal GOALS){
        super(bet_type);

        side = SIDE;
        goals = GOALS;
        category = "OVER-UNDER";
    }

    public String id(){
        return String.format("%s_%s_%s", side, goals.toString(), type);
    }

    public JSONObject json(){
        JSONObject j = new JSONObject();
        j.put("type", type);
        j.put("id", id());
        j.put("side", side);
        j.put("goals", goals.toString());
        j.put("category", category);
        return j;
    }

    public Boolean under(){
        return side == UNDER;
    }

    public Boolean over(){
        return side == OVER;
    }

}
