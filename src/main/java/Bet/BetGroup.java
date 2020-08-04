package Bet;

import Bet.Bet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.xml.bind.DatatypeConverter;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class BetGroup {
    // A grouping of bets. Usually used for tautologies.

    public final ArrayList<Bet> bets;

    public BetGroup(ArrayList<Bet> bets){
        this.bets = bets;
        this.bets.trimToSize();
    }

    public BetGroup(){
        bets = new ArrayList<>();
    }


    public int size(){
        return bets.size();
    }


    public JSONArray toJSON(boolean full){
        JSONArray ja = new JSONArray();
        for (Bet bet: bets){
            if (full){
                ja.add(bet.toJSON());
            }
            else{
                ja.add(bet.id());
            }
        }
        return ja;
    }


    public boolean add(Bet bet){
        return bets.add(bet);
    }


    public int id(){
        ArrayList<String> id_list = new ArrayList<>();
        for (Bet bet: bets){
            id_list.add(bet.id());
        }
        Collections.sort(id_list);
        String ids_string = String.join("", id_list);
        return Math.abs(ids_string.hashCode());
    }


    public String toString(){
        String s = "[";
        for (int i=0; i<bets.size(); i++){
            s += bets.get(i).toString();
            if (i != bets.size()-1){
                s += ", ";
            }
        }
        s += "]";
        return s;
    }


    public static JSONObject list2JSON(List<BetGroup> betGroupList, boolean just_ids){
        JSONObject j = new JSONObject();
        if (just_ids){
            JSONArray betgroups = new JSONArray();
            for (BetGroup bg: betGroupList){
                betgroups.add(bg.id());
            }
            j.put("groups", betgroups);
            j.put("size", betgroups.size());
        }
        else{
            JSONObject betgroups = new JSONObject();
            for (BetGroup bg: betGroupList){
                betgroups.put(bg.id(), bg.toJSON(false));
            }
            j.put("groups", betgroups);
            j.put("size", betgroups.size());
        }

        return j;
    }

    public static JSONObject list2JSON(List<BetGroup> betGroupList){
        return list2JSON(betGroupList, false);
    }


}
