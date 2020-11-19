package Bet;

import Bet.Bet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.xml.bind.DatatypeConverter;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;

public class BetGroup {
    // A grouping of bets. Usually used for tautologies.

    private final Set<Bet> bets;
    public int id;

    // Used to note next time tautology should be used within an event trader
    public Instant next_usage;

    public BetGroup(Collection<Bet> bets){
        this.bets = new HashSet<>();
        if (bets != null) {
            this.bets.addAll(bets);
        }
        this.id = getID();
        next_usage = null;
    }

    public BetGroup(){
        this(null);
    }

    public Set<Bet> getBets(){
        return bets;
    }


    public BetGroup copy(){
        return new BetGroup(bets);
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
        boolean result = bets.add(bet);
        id = getID();
        return result;
    }


    public int getID(){
        ArrayList<String> id_list = new ArrayList<>();
        for (Bet bet: bets){
            id_list.add(bet.id());
        }
        Collections.sort(id_list);
        String ids_string = String.join("", id_list);
        return Math.abs(ids_string.hashCode());
    }

    @Override
    public String toString(){
        String s = "[";
        int count = 0;
        for (Bet bet: bets){
            s += bet.toString();
            count++;

            if (count < bets.size()){
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
                betgroups.add(bg.id);
            }
            j.put("groups", betgroups);
            j.put("size", betgroups.size());
        }
        else{
            JSONObject betgroups = new JSONObject();
            for (BetGroup bg: betGroupList){
                betgroups.put(bg.id, bg.toJSON(false));
            }
            j.put("groups", betgroups);
            j.put("size", betgroups.size());
        }

        return j;
    }

    public static JSONObject list2JSON(List<BetGroup> betGroupList){
        return list2JSON(betGroupList, false);
    }

    public static List<BetGroup> copyList(List<BetGroup> betGroups){
        List<BetGroup> copy = new ArrayList<>();
        for (BetGroup betGroup : betGroups) {
            copy.add(betGroup.copy());
        }
        return copy;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            return ((BetGroup) obj).id == this.id;
        }
        catch (ClassCastException e){
            return false;
        }
    }
}
