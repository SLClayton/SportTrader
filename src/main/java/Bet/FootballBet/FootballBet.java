package Bet.FootballBet;


import java.math.BigDecimal;
import java.util.ArrayList;

import Bet.Bet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public abstract class FootballBet extends Bet {

    public static String TEAM_A = "TEAM-A";
    public static String TEAM_B = "TEAM-B";
    public static String DRAW = "DRAW";
    public  static String[] RESULTS = {TEAM_A, TEAM_B, DRAW};

    public static String OVER = "OVER";
    public static String UNDER = "UNDER";
    public static String[] OVER_UNDER_ENUM = {OVER, UNDER};

    public static String ANY = "ANY";
    public static String[] RESULTS_HT = {ANY};

    public final static String RESULT = "RESULT";
    public final static String RESULT_HT = "RESULT-HT";
    public final static String HANDICAP = "HANDICAP";
    public final static String ANY_OVER = "ANY-OVER";
    public final static String ANY_OVER_HT = "ANY-OVER-HT";
    public final static String OVER_UNDER = "OVER-UNDER";
    public final static String CORRECT_SCORE = "CORRECT-SCORE";
    public final static String CORRECT_SCORE_HT = "CORRECT-SCORE-HT";


    public FootballBet(String bet_type){
        super(bet_type);
    }
}


