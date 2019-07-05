package Bet;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;

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

    public abstract JsonObject json();

    public abstract String id();

    public static JsonArray allJSONArray(ArrayList<Bet> bets){
        JsonArray j = new JsonArray();
        for (int i=0; i<bets.size(); i++){
            j.add(bets.get(i).json());
        }
        return j;
    }

}
