package Bet.FootballBet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public  class FootballScoreBet extends FootballBet{

    public int score_a;
    public int score_b;
    public Boolean halftime;

    public FootballScoreBet(String bet_type, int SCORE_A, int SCORE_B, Boolean HALFTIME){
        super(bet_type);
        score_a = SCORE_A;
        score_b = SCORE_B;
        halftime = HALFTIME;

        if (halftime){
            category = "CORRECT-SCORE-HT";
        }
        else{
            category = "CORRECT-SCORE";
        }
    }

    public String id(){
        return String.format("%s_%s-%s_%s",
                             category,
                             String.valueOf(score_a),
                             String.valueOf(score_b),
                             type);
    }

    @Override
    public JSONObject toJSON(){
        JSONObject j = new JSONObject();
        j.put("type", type);
        j.put("id", id());
        j.put("score_a", score_a);
        j.put("score_b", score_b);
        j.put("category", category);
        j.put("halftime", halftime.toString());
        return j;
    }

    public int total_goals(){
        return score_a + score_b;
    }

    public String result(){
        if (score_a > score_b) {return TEAM_A;}
        if (score_a < score_b) {return TEAM_B;}
        return DRAW;
    }

    public Boolean winA(){
        return score_a > score_b;
    }

    public Boolean winB(){
        return score_a < score_b;
    }

    public Boolean isDraw(){
        return score_a == score_b;
    }

    public int goal_difference_A(){
        return score_a - score_b;
    }

    public int goal_difference_B(){
        return score_b - score_a;
    }


}
