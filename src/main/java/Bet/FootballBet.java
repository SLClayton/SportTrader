package Bet;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;

import java.math.BigDecimal;
import java.util.ArrayList;

import static tools.printer.*;

public abstract class FootballBet extends Bet {

    static String TEAM_A = "TEAM-A";
    static String TEAM_B = "TEAM-B";
    static String DRAW = "DRAW";
    static String[] RESULTS = {TEAM_A, TEAM_B, DRAW};

    static String OVER = "OVER";
    static String UNDER = "UNDER";
    static String[] OVER_UNDER = {OVER, UNDER};

    static String ANY = "ANY";
    static String[] RESULTS_HT = {ANY};

    String category;

    public FootballBet(String bet_type){
        super(bet_type);
    }
}


class FootballResultBet extends FootballBet{

    String result;
    Boolean halftime;

    public FootballResultBet(String bet_type, String match_result, Boolean is_halftime){
        super(bet_type);

        result = match_result;
        halftime = is_halftime;

        if (is_halftime) {
            category = "RESULT-HT";
        }
        else{
            category = "RESULT";
        }
    }

    public String id(){
        return String.format("%s_%s_%s", category, result, type);
    }

    public JsonObject json(){
        JsonObject j = new JsonObject();
        j.put("type", type);
        j.put("result", result);
        j.put("id", id());
        j.put("halftime", halftime.toString());
        return j;
    }

    public Boolean winnerA(){
        return result == TEAM_A;
    }

    public Boolean winnerB(){
        return result == TEAM_B;
    }

    public Boolean isDraw(){
        return result == DRAW;
    }

    public Boolean overlap(FootballOtherScoreBet otherscorebet) throws Exception {
        if (halftime != otherscorebet.halftime){
            throw new Exception("Comparing halftime with non halftime bet.");
        }
        else{
            if (type == otherscorebet.type){
                return result.equals(otherscorebet.result);
            }else{
                return !(result.equals(otherscorebet.result));
            }
        }

    }

}


class FootballScoreBet extends FootballBet{

    int score_a;
    int score_b;
    Boolean halftime;

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

    public JsonObject json(){
        JsonObject j = new JsonObject();
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

    public static JsonArray listJSON(ArrayList<FootballScoreBet> bets){
        JsonArray j = new JsonArray();
        for (int i=0; i<bets.size(); i++){
            j.add(bets.get(i).json());
        }
        return j;
    }
}


class FootballOtherScoreBet extends FootballBet{

    int over_score;
    String result;
    Boolean halftime;
    String category;

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

    public JsonObject json(){
        JsonObject j = new JsonObject();
        j.put("type", type);
        j.put("id", id());
        j.put("over_score", over_score);
        j.put("result", result);
        j.put("category", category);
        j.put("halftime", halftime.toString());
        return j;
    }


}


class FootballOverUnderBet extends FootballBet{

    String side;
    BigDecimal goals;


    public FootballOverUnderBet(String bet_type, String SIDE, BigDecimal GOALS){
        super(bet_type);

        side = SIDE;
        goals = GOALS;
        category = "GOAL_COUNT";
    }

    public String id(){
        return String.format("%s_%s_%s", side, goals.toString(), type);
    }

    public JsonObject json(){
        JsonObject j = new JsonObject();
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


class FootballHandicapBet extends FootballBet{

    BigDecimal a_handicap;
    String result;

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

    public JsonObject json(){
        JsonObject j = new JsonObject();
        j.put("type", type);
        j.put("id", id());
        j.put("a_handicap", a_handicap.toString());
        j.put("category", category);
        return j;
    }


}