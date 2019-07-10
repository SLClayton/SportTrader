package Bet.FootballBet;


import java.math.BigDecimal;
import java.util.ArrayList;

import Bet.Bet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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



    public FootballBet(String bet_type){
        super(bet_type);
    }
}


