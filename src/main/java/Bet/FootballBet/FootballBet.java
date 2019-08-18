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
    public static String[] OVER_UNDER = {OVER, UNDER};

    public static String ANY = "ANY";
    public static String[] RESULTS_HT = {ANY};



    public FootballBet(String bet_type){
        super(bet_type);
    }
}


