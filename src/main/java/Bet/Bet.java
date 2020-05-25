package Bet;


import Bet.FootballBet.FootballBet;
import org.json.simple.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class Bet {

    public final static String BACK = "BACK";
    public final static String LAY = "LAY";
    public static String[] BET_TYPES = {BACK, LAY};

    public Sport sport;
    public String category;
    protected String type;

    public enum Sport{FOOTBALL, TENNIS, RUGBY}

    public Bet(String bet_type) {
        if (!bet_type.equals(BACK) && !bet_type.equals(LAY)){
            throw new ExceptionInInitializerError("Bet type must be either BACK or LAY");
        }
        type = bet_type;
    }


    public String type(){
        return type;
    }

    public Boolean isLay(){
        return (type == LAY);
    }

    public Boolean isBack(){
        return type == BACK;
    }

    public abstract JSONObject toJSON();

    public abstract String id();

    @Override
    public String toString(){
        return id();
    }


    public Boolean equals(Bet bet){
        return id().equals(bet.id());
    }

    public Map<String, BetGroup> sortByCategory(Collection<Bet> bets){
        Map<String, BetGroup> map = new HashMap<>();
        for (Bet bet: bets){
            BetGroup current = map.get(bet.category);
            if (current == null){
                current = new BetGroup();
                map.put(bet.category, current);
            }
            current.add(bet);
        }
        return map;
    }

    public static JSONArray getTautIds(Bet[][] tauts) {
        JSONArray taut_list = new JSONArray();
        for (int i = 0; i < tauts.length; i++) {
            JSONArray taut_ids = new JSONArray();

            for (int j = 0; j < tauts[i].length; j++) {
                taut_ids.add(tauts[i][j].id());
            }

            taut_list.add(taut_ids);
        }
        return taut_list;
    }

}
