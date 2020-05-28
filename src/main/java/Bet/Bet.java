package Bet;


import Bet.FootballBet.FootballBet;
import org.json.simple.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class Bet {

    public enum BetType {BACK, LAY}

    public Sport sport;
    public String category;
    protected BetType type;

    public enum Sport{FOOTBALL, TENNIS, RUGBY}

    public Bet(BetType bet_type) {
        type = bet_type;
    }


    public Boolean isLay(){
        return type.equals(BetType.LAY);
    }

    public Boolean isBack(){
        return type.equals(BetType.BACK);
    }

    public abstract JSONObject toJSON();

    public abstract String id();

    @Override
    public String toString(){
        return id();
    }


    public BetType getType(){
        return type;
    }


    public static BigDecimal backStake2LayStake(BigDecimal back_stake, BigDecimal odds){
        // AKA
        // Back stake to Back Profit
        // Lay profit to lay stake
        return odds.subtract(BigDecimal.ONE).multiply(back_stake);
    }


    public static BigDecimal layStake2backStake(BigDecimal lay_stake, BigDecimal odds){
        // AKA
        // Lay Stake to Lay Profit
        // Back Profit to Back Stake
        return lay_stake.divide((odds.subtract(BigDecimal.ONE)), 12, RoundingMode.HALF_UP);
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
