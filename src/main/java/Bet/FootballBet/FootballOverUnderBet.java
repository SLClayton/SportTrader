package Bet.FootballBet;

import Bet.Bet;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FootballOverUnderBet extends FootballBet{

    public String side;
    public BigDecimal goals;
    public boolean halftime;


    public FootballOverUnderBet(BetType bet_type, String SIDE, BigDecimal GOALS, boolean halftime){
        super(bet_type);

        side = SIDE;
        goals = GOALS.setScale(1, RoundingMode.HALF_UP);
        category = "OVER-UNDER";
        this.halftime = halftime;
    }

    public FootballOverUnderBet(BetType bet_type, String SIDE, BigDecimal GOALS){
        this(bet_type, SIDE, GOALS, false);
    }

    public String id(){
        String HT = "";
        if (halftime){
            HT = "_HT";
        }
        return String.format("%s_%s%s_%s", side, goals.toString(), HT, type);
    }

    @Override
    public JSONObject toJSON(){
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

    public boolean isHalftime(){
        return halftime;
    }

}
