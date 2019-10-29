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

    public ArrayList<Bet> bets;

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


    public String id(){
        ArrayList<String> betids = new ArrayList<>();
        for (Bet bet: bets){
            betids.add(bet.id());
        }
        Collections.sort(betids);
        String joinedbetids = String.join("", betids);

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        byte[] digest = md.digest(joinedbetids.getBytes(StandardCharsets.UTF_8));
        String sha256 = DatatypeConverter.printHexBinary(digest).toLowerCase();
        return sha256.substring(0, 10);
    }


    public String toString(){
        return toJSON(false).toString();
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
