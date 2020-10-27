package Bet;

import ch.qos.logback.core.db.BindDataSourceToJNDIAction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tools.printer.*;
import static tools.BigDecimalTools.*;

public class BetOfferStake implements Comparable<BetOfferStake> {

    // An Object to store a betOffer with a back stake to go with it.

    public final BetOffer betOffer;
    private final BigDecimal back_stake;

    public BetOfferStake(BetOffer betOffer, BigDecimal back_stake){
        this.betOffer = betOffer;
        this.back_stake = back_stake;
    }

    @Override
    public String toString() {
        String backstring = padto(BDString(back_stake, 4), 8);
        String invstring = padto(BDString(investment(), 4), 8);
        String retstring = BDString(returns(), 4);


        return String.format("%s for [Back: %s Inv: %s Ret: %s]", betOffer.toString(), backstring, invstring, retstring);
    }

    public BigDecimal backStake(){
        return back_stake;
    }

    public BigDecimal layStake(){
        return betOffer.backStake2LayStake(backStake());
    }

    public BigDecimal stake(){
        if (betOffer.isBack()){
            return backStake();
        }
        else if (betOffer.isLay()){
            return layStake();
        }
        return null;
    }

    public BigDecimal profit_b4_com(){
        if (betOffer.isBack()){
            return layStake();
        }
        else if (betOffer.isLay()){
            return backStake();
        }
        return null;
    }

    public BigDecimal profit_after_com(){
        return betOffer.profitAfterCommission(profit_b4_com());
    }

    public BigDecimal investment() {
        return betOffer.stake2Investment(stake());
    }

    public String site_name(){
        return betOffer.site.getName();
    }


    public BigDecimal returns(){
        return investment().add(profit_after_com());
    }

    public JSONObject toJSON(){
        JSONObject j = new JSONObject();
        j.put("back_stake", BDString(back_stake));
        j.put("bet_offer", betOffer.toJSON());
        return j;
    }

    public static JSONArray list2JSON(List<BetOfferStake> betOfferStakes){
        JSONArray ja = new JSONArray();
        for (BetOfferStake betOfferStake: betOfferStakes){
            ja.add(betOfferStake.toJSON());
        }
        return ja;
    }


    public static void printList(List<BetOfferStake> betOfferStakes){
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<betOfferStakes.size(); i++){
            sb.append(betOfferStakes.get(i).toString());
            if (i < betOfferStakes.size()-1){
                sb.append("\n");
            }
        }
        print(sb.toString());
    }


    public static BigDecimal totalLayStake(List<BetOfferStake> betOfferStakes){
        BigDecimal lay_stake_total = BigDecimal.ZERO;
        for (BetOfferStake betOfferStake: betOfferStakes){
            lay_stake_total = lay_stake_total.add(betOfferStake.layStake());
        }
        return lay_stake_total;
    }

    public static BigDecimal totalBackStake(List<BetOfferStake> betOfferStakes){
        BigDecimal back_stake_total = BigDecimal.ZERO;
        for (BetOfferStake betOfferStake: betOfferStakes){
            back_stake_total = back_stake_total.add(betOfferStake.backStake());
        }
        return back_stake_total;
    }

    public static BigDecimal averageOdds(List<BetOfferStake> betOfferStakes){
        if (betOfferStakes.isEmpty()){
            return null;
        }
        BigDecimal lay_stake_total = BigDecimal.ZERO;
        BigDecimal back_stake_total = BigDecimal.ZERO;
        for (BetOfferStake betOfferStake: betOfferStakes){
            back_stake_total = back_stake_total.add(betOfferStake.backStake());
            lay_stake_total = lay_stake_total.add(betOfferStake.layStake());
        }
        return lay_stake_total.divide(back_stake_total, 12, RoundingMode.HALF_UP).add(BigDecimal.ONE);
    }

    public static BigDecimal averageROIRatio(List<BetOfferStake> betOfferStakes){
        if (betOfferStakes.isEmpty()){
            return null;
        }

        BigDecimal inv_total = BigDecimal.ZERO;
        BigDecimal ret_total = BigDecimal.ZERO;

        for (BetOfferStake betOfferStake: betOfferStakes){
            inv_total = inv_total.add(betOfferStake.investment());
            ret_total = ret_total.add(betOfferStake.returns());
        }
        return ret_total.divide(inv_total, 12, RoundingMode.HALF_UP);
    }

    public static Map<String, BigDecimal> getBackStakePerSite(List<BetOfferStake> betOfferStakes){
        // Returns a map of the total back stake used for each site in list of betofferstakes

        Map<String, BigDecimal> stake_per_site = new HashMap<String, BigDecimal>();
        for (BetOfferStake betOfferStake: betOfferStakes){
            String site_name = betOfferStake.site_name();

            BigDecimal new_value = stake_per_site.computeIfAbsent(site_name, k->BigDecimal.ZERO)
                    .add(betOfferStake.backStake());
            stake_per_site.put(site_name, new_value);
        }
        return stake_per_site;
    }


    public static Map<String, BigDecimal> getInvPerSite(List<BetOfferStake> betOfferStakes){
        // Returns a map of the total back stake used for each site in list of betofferstakes

        Map<String, BigDecimal> inv_per_site = new HashMap<String, BigDecimal>();
        for (BetOfferStake betOfferStake: betOfferStakes){
            String site_name = betOfferStake.site_name();

            BigDecimal new_value = inv_per_site.computeIfAbsent(site_name, k->BigDecimal.ZERO)
                    .add(betOfferStake.investment());
            inv_per_site.put(site_name, new_value);
        }
        return inv_per_site;
    }


    @Override
    public int compareTo(BetOfferStake o) {
        return this.betOffer.compareTo(o.betOffer);
    }
}
