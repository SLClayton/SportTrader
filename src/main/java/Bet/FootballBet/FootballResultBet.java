package Bet.FootballBet;

import Bet.Bet;
import Bet.FootballBet.FootballBet;
import Bet.FootballBet.FootballOtherScoreBet;
import Bet.FootballBet.FootballScoreBet;
import org.json.simple.JSONObject;

public class FootballResultBet extends FootballBet {

    public String result;
    public Boolean halftime;

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

    public JSONObject json(){
        JSONObject j = new JSONObject();
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

    public Boolean possibleScore(FootballScoreBet scoreBet){
        if (scoreBet.isLay()){
            return true;
        }

        if (isBack()){
            return scoreBet.result() == result;
        }else{
            return scoreBet.result() != result;
        }
    }

}
