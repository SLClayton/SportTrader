package Bet.FootballBet;

import org.json.simple.JSONObject;

import java.math.BigDecimal;

public class FootballHandicapBet extends FootballBet{

    public BigDecimal a_handicap;
    public String result;

    public FootballHandicapBet(String bet_type, BigDecimal A_HANDICAP, String RESULT){
        super(bet_type);
        category = "HANDICAP";
        a_handicap = A_HANDICAP;
        result = RESULT;
    }

    public String id(){
        String addition = "";
        if (a_handicap.compareTo(BigDecimal.ZERO) == 1){ // Bigger than 0
            addition = "+";
        }

        return String.format("HCP-A%s%s_%s_%s", addition, a_handicap.toString(), result, type);
    }

    public JSONObject json(){
        JSONObject j = new JSONObject();
        j.put("type", type);
        j.put("id", id());
        j.put("a_handicap", a_handicap.toString());
        j.put("category", category);
        return j;
    }


}
