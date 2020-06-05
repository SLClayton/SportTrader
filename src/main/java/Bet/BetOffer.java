package Bet;

import SiteConnectors.BettingSite;
import Sport.Event;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tools.printer.BDString;

public class BetOffer implements Comparable<BetOffer> {

    public BettingSite site;
    public Event event;
    public Bet bet;
    public BigDecimal odds;
    public BigDecimal volume;
    private Map<String, String> metadata;
    public Instant time_betOffer_creation;


    public BetOffer(BettingSite SITE, Event EVENT, Bet BET, BigDecimal ODDS, BigDecimal VOLUME){
        time_betOffer_creation = Instant.now();
        site = SITE;
        event = EVENT;
        bet = BET;
        odds = ODDS;
        volume = VOLUME;
        metadata = new HashMap<>();
    }


    public BetOffer newOdds(BigDecimal odds){
        // Returns a new betOffer with the same attributes except new odds and volume
        BetOffer newBetOffer = new BetOffer(site, event, bet, odds, volume);
        for (Map.Entry<String, String> item: metadata.entrySet()){
            newBetOffer.addMetadata(item.getKey(), item.getValue());
        }
        return newBetOffer;
    }


    public String addMetadata(String key, String value){
        return metadata.put(key, value);
    }

    public String addMetadata(String key, Long value){
        return metadata.put(key, String.valueOf(value));
    }

    public String addMetadata(String key, Integer value){
        return metadata.put(key, String.valueOf(value));
    }

    public String addMetadata(String key, Double value){
        return metadata.put(key, String.valueOf(value));
    }


    public String getMetadata(String key){
        return metadata.get(key);
    }

    public Long getMetadataLong(String key){
        return Long.parseLong(getMetadata(key));
    }

    public Double getMetadataDouble(String key){
        return new BigDecimal(getMetadata(key)).doubleValue();
    }

    public BigDecimal getMetadataBigDecimal(String key){
        return new BigDecimal(getMetadata(key));
    }

    public boolean hasMinVolumeNeeded(){
        return volume.compareTo(minStake()) != -1;
    }


    public BigDecimal backStake2LayStake(BigDecimal back_stake){
        return Bet.backStake2LayStake(back_stake, odds);
    }

    public BigDecimal layStake2BackStake(BigDecimal lay_stake){
        return Bet.layStake2backStake(lay_stake, odds);
    }


    public BigDecimal getOddsWithBuffer(BigDecimal buffer_ratio){
        BigDecimal odds_ratio;
        if (isBack()){
            odds_ratio = BigDecimal.ONE.subtract(buffer_ratio);
        }
        else{
            odds_ratio = BigDecimal.ONE.add(buffer_ratio);
        }
        return Bet.multiplyDecimalOdds(odds, odds_ratio);
    }


    public BigDecimal getValidOddsWithBuffer(BigDecimal buffer_ratio){
        BigDecimal buffered_odds = getOddsWithBuffer(buffer_ratio);
        RoundingMode roundingMode;
        if (isBack()){
            roundingMode = RoundingMode.DOWN;
        }
        else{
            roundingMode = RoundingMode.UP;
        }
        return site.getValidOdds(buffered_odds, roundingMode);
    }


    @Override
    public String toString(){
        return toJSON().toString();
    }



    public JSONObject toJSON(){
        JSONObject m = new JSONObject();
        m.put("created", String.valueOf(time_betOffer_creation));
        m.put("event", String.valueOf(event));
        m.put("bet", String.valueOf(bet.id()));
        m.put("site", String.valueOf(site.getName()));
        m.put("odds", BDString(odds));
        m.put("volume", BDString(volume));
        m.put("roi_ratio", BDString(ROI_ratio()));
        m.put("metadata", String.valueOf(metadata));
        return m;
    }


    public static JSONArray list2JSON(List<BetOffer> betOfferList){
        JSONArray ja = new JSONArray();
        for (BetOffer betOffer: betOfferList){
            ja.add(betOffer.toJSON());
        }
        return ja;
    }



    public boolean isBack(){
        return bet.isBack();
    }


    public boolean isLay(){
        return bet.isLay();
    }


    public BigDecimal minStake(){
        // The minimum stake of money that can be placed in this offer

        BigDecimal min_stake;
        if (isBack()){
            min_stake = site.minBackersStake();
        }
        else if (isLay()){
            min_stake = site.minLayersStake(odds);
        }
        else{
            return null;
        }
        return min_stake.setScale(2, RoundingMode.UP);
    }


    public BigDecimal maxStake(){
        // The maximum stake of money that can be placed in this offer

        BigDecimal max_stake;
        if (isBack()){
            max_stake = volume;
        }
        else {
            max_stake = Bet.backStake2LayStake(volume, odds);
        }
        return max_stake.setScale(2, RoundingMode.DOWN);
    }


    public BigDecimal returnFromMinStake(){
        return ROI(minStake());
    }


    public BigDecimal returnFromMaxStake(){
        return ROI(maxStake());
    }


    public BigDecimal ROI_ratio(){
        return ROI(BigDecimal.ONE);
    }

    public BigDecimal ROI(BigDecimal investment){
        return ROI(investment, null);
    }

    public BigDecimal ROI(BigDecimal investment, Integer scale){
        return site.ROI(bet.getType(), odds, investment, scale);
    }





    public BetOrder betOrderReturn(BigDecimal target_return){
        return BetOrder.fromTargetReturn(this, target_return);
    }

    public BetOrder betOrderInvestment(BigDecimal target_investment){
        return BetOrder.fromTargetInvestment(this, target_investment);
    }

    public BetOrder betOrderFromStake(BigDecimal stake){
        return BetOrder.fromStake(this, stake);
    }


    @Override
    public int compareTo(BetOffer betOffer) {
        return this.ROI_ratio().compareTo(betOffer.ROI_ratio());
    }
}
