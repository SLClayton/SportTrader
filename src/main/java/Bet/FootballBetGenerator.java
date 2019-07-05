package Bet;

import java.math.BigDecimal;
import java.util.ArrayList;
import Bet.Bet.*;
import Bet.*;
import tools.printer;

public class FootballBetGenerator {

    static int GOALSCOUNT_MAX = 9;
    static int OTHERGOALS_MIN = 3;
    static int OTHERGOALS_MAX = 5;
    static int OTHERGOALS_HT_MIN = 2;
    static int OTHERGOALS_HT_MAX = 2;
    static int SCORE_MAX = 4;
    static int SCORE_MAX_HT = 2;
    static int HANDICAP_MAX = 2;

    ArrayList<FootballScoreBet> score_bets;
    ArrayList<FootballScoreBet> score_bets_ht;
    ArrayList<FootballOtherScoreBet> other_score_bets;
    ArrayList<FootballOtherScoreBet> other_score_bets_ht;
    ArrayList<FootballResultBet> result_bets;
    ArrayList<FootballResultBet> result_bets_ht;
    ArrayList<FootballHandicapBet> handicap_bets;
    ArrayList<FootballOverUnderBet> over_under_bets;



    public FootballBetGenerator(){
        score_bets = getScoreBets(SCORE_MAX, false);
        score_bets_ht = getScoreBets(SCORE_MAX_HT, true);
        other_score_bets = getOtherScoreBets(OTHERGOALS_MIN, OTHERGOALS_MAX);
        other_score_bets_ht = getOtherScoreBetsHT(OTHERGOALS_HT_MIN, OTHERGOALS_HT_MAX);
        result_bets = getResultBets(false);
        result_bets_ht = getResultBets(true);
        handicap_bets = getHandicapBets(HANDICAP_MAX);
        over_under_bets = getOverUnderBets(GOALSCOUNT_MAX);
    }

    public ArrayList<Bet> getAllBets(){
        ArrayList<Bet> all_bets = new ArrayList<Bet>();
        all_bets.addAll(score_bets);
        all_bets.addAll(score_bets_ht);
        all_bets.addAll(other_score_bets);
        all_bets.addAll(other_score_bets_ht);
        all_bets.addAll(result_bets_ht);
        all_bets.addAll(handicap_bets);
        all_bets.addAll(over_under_bets);
        return all_bets;
    }



    public ArrayList<FootballScoreBet> getScoreBets(int highest, Boolean halftime){
        ArrayList<FootballScoreBet> bets = new ArrayList<FootballScoreBet>();
        for (int a_score=0; a_score<=highest; a_score++){
            for (int b_score=0; b_score<=highest; b_score++){
                for (int i=0; i<Bet.BET_TYPES.length ; i++){
                    String type = Bet.BET_TYPES[i];

                    bets.add(new FootballScoreBet(type, a_score, b_score, halftime));
                }
            }
        }
        return bets;
    }


    public ArrayList<FootballOtherScoreBet> getOtherScoreBets(int lowest, int highest){
        ArrayList<FootballOtherScoreBet> bets = new ArrayList<FootballOtherScoreBet>();
        for (int score=lowest; score<=highest; score++){
            for (int i=0; i<FootballOtherScoreBet.RESULTS.length; i++){
                String result = FootballOtherScoreBet.RESULTS[i];
                for (int j=0; j<Bet.BET_TYPES.length; j++){
                    String type = Bet.BET_TYPES[j];

                    bets.add(new FootballOtherScoreBet(type, score, result, false));
                }
            }
        }
        return bets;
    }


    public ArrayList<FootballOtherScoreBet> getOtherScoreBetsHT(int lowest, int highest){
        ArrayList<FootballOtherScoreBet> bets = new ArrayList<FootballOtherScoreBet>();
        for (int score=lowest; score<=highest; score++){
            for (int j=0; j<Bet.BET_TYPES.length; j++) {
                String type = Bet.BET_TYPES[j];

                bets.add(new FootballOtherScoreBet(type, score, FootballBet.ANY, true));
            }
        }
        return bets;
    }


    public ArrayList<FootballResultBet> getResultBets(Boolean halftime){
        ArrayList<FootballResultBet> bets = new ArrayList<FootballResultBet>();
        for (int i=0; i<FootballResultBet.RESULTS.length; i++){
            String result = FootballResultBet.RESULTS[i];
            for (int j=0; j<Bet.BET_TYPES.length; j++) {
                String type = Bet.BET_TYPES[j];

                bets.add(new FootballResultBet(type, result, halftime));
            }
        }
        return bets;
    }

    public ArrayList<FootballHandicapBet> getHandicapBets(int highest){
        ArrayList<FootballHandicapBet> bets = new ArrayList<FootballHandicapBet>();

        BigDecimal smallest = new BigDecimal(-1*highest);
        BigDecimal largest = new BigDecimal(highest);
        BigDecimal step = new BigDecimal("0.5");

        for (BigDecimal handicap = smallest; handicap.compareTo(largest) != 1; handicap = handicap.add(step)){
            for (int i=0; i<FootballResultBet.RESULTS.length; i++) {
                String result = FootballResultBet.RESULTS[i];
                for (int j = 0; j < Bet.BET_TYPES.length; j++) {
                    String type = Bet.BET_TYPES[j];

                    bets.add(new FootballHandicapBet(type, handicap, result));
                }
            }
        }
        return bets;
    }

    public ArrayList<FootballOverUnderBet> getOverUnderBets(int highest){
        ArrayList<FootballOverUnderBet> bets = new ArrayList<FootballOverUnderBet>();

        BigDecimal smallest = new BigDecimal("0.5");
        BigDecimal largest = (new BigDecimal(highest)).add(new BigDecimal("0.5"));

        for (BigDecimal goals = new BigDecimal("0.5"); goals.compareTo(largest) != 1; goals = goals.add(BigDecimal.ONE)){

            for (int i=0; i<FootballOverUnderBet.OVER_UNDER.length; i++){
                String side = FootballOverUnderBet.OVER_UNDER[i];
                for (int j = 0; j < Bet.BET_TYPES.length; j++) {
                    String type = Bet.BET_TYPES[j];

                    bets.add(new FootballOverUnderBet(type, side, goals));
                }
            }

        }
        return bets;
    }




    public static void main(String[] args){
        FootballBetGenerator g = new FootballBetGenerator();
        ArrayList<Bet> all_bets = g.getAllBets();
        printer.p(Bet.allJSONArray(all_bets));
    }


}
