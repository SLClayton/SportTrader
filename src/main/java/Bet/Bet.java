package Bet;


import org.json.simple.*;

import java.util.ArrayList;

public abstract class Bet {

    static String BACK = "BACK";
    static String LAY = "LAY";
    static String[] BET_TYPES = {BACK, LAY};

    String type;

    public Bet(String bet_type) {
        type = bet_type;
    }

    public Boolean isLay(){
        return (type == LAY);
    }

    public Boolean isBack(){
        return type == BACK;
    }

    public abstract JSONObject json();

    public abstract String id();

    public Boolean equals(Bet bet){
        return id().equals(bet.id());
    }

    public static JSONArray allJSONArray(ArrayList<Bet> bets){
        JSONArray j = new JSONArray();
        for (int i=0; i<bets.size(); i++){
            j.add(bets.get(i).json());
        }
        return j;
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
