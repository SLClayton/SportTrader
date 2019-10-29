package Bet.FootballBet;

import org.json.simple.JSONObject;

public class FootballOtherScoreBet extends FootballBet{

    public int over_score;
    public String result;
    public Boolean halftime;

    public FootballOtherScoreBet(String bet_type, int OVER_SCORE, String RESULT, Boolean HALFTIME){
        super(bet_type);
        over_score = OVER_SCORE;
        result = RESULT;
        halftime = HALFTIME;

        if (halftime){
            category = "ANY-OVER-HT";
        } else{
            category = "ANY-OVER";
        }
    }

    public String id(){
        return String.format("%s_%s-%s_%s_%s", category, over_score, over_score, result, type);
    }

    @Override
    public JSONObject toJSON(){
        JSONObject j = new JSONObject();
        j.put("type", type);
        j.put("id", id());
        j.put("over_score", over_score);
        j.put("result", result);
        j.put("category", category);
        j.put("halftime", halftime.toString());
        return j;
    }


}
