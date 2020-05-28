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

public class BetOffer implements Comparable<BetOffer> {

    public Event event;
    public Bet bet;
    public BettingSite site;
    public BigDecimal odds;
    public BigDecimal volume;
    public Map<String, String> metadata;

    public BigDecimal roi_ratio;

    public Instant time_start_getMarketOddsReport;
    public Instant time_betOffer_creation;


    public BetOffer(Instant time_getMarketOddsReport, Event Event, Bet BET, BettingSite SITE,
                    BigDecimal ODDS, BigDecimal VOLUME){

        time_start_getMarketOddsReport = time_getMarketOddsReport;
        time_betOffer_creation = Instant.now();

        event = Event;
        bet = BET;
        site = SITE;
        odds = ODDS;
        volume = VOLUME;
        metadata = new HashMap<>();
        roi_ratio = ROI_ratio();
    }




    public BetOffer newOdds(BigDecimal odds){
        // Returns a new betOffer with the same attributes except new odds and volume
        BetOffer newBetOffer = new BetOffer(time_start_getMarketOddsReport, event, bet, site, odds, volume);
        for (Map.Entry<String, String> item: metadata.entrySet()){
            newBetOffer.metadata.put(item.getKey(), item.getValue());
        }
        return newBetOffer;
    }


    public String addMetadata(String key, String value){
        return metadata.put(key, value);
    }

    public String getMetadata(String key){
        return metadata.get(key);
    }


    public boolean hasMinVolumeNeeded(){
        boolean r =  volume.compareTo(minStake()) != -1;
        return r;
    }


    public String toString(){
        return toJSON().toString();
    }


    public JSONObject toJSON(){
        JSONObject m = new JSONObject();
        m.put("created", String.valueOf(time_betOffer_creation));
        m.put("event", String.valueOf(event));
        m.put("bet", String.valueOf(bet.id()));
        m.put("site", String.valueOf(site.getName()));
        m.put("odds", String.valueOf(odds));
        m.put("volume", String.valueOf(volume));
        m.put("roi_ratio", String.valueOf(roi_ratio));
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
        BigDecimal min_backers_stake = site.minBackersStake();
        BigDecimal min_stake;
        if (isBack()){
            min_stake = min_backers_stake;
        }
        else {
            min_stake =  Bet.backStake2LayStake(min_backers_stake, odds);
        }
        return min_stake.setScale(2, RoundingMode.UP);
    }


    public BigDecimal maxStake(){
        BigDecimal max_backers_stake = volume;
        BigDecimal max_stake;
        if (isBack()){
            max_stake = max_backers_stake;
        }
        else {
            max_stake = Bet.backStake2LayStake(max_backers_stake, odds);
        }
        return max_stake.setScale(2, RoundingMode.DOWN);
    }


    public BigDecimal returnFromMinStake(){
        return ROI(minStake(), true);
    }


    public BigDecimal returnFromMaxStake(){
        return ROI(maxStake(), true);
    }


    public BigDecimal ROI_ratio(){
        return site.ROI(this, BigDecimal.ONE, false);
    }


    public BigDecimal ROI(BigDecimal investment, boolean real){
        return site.ROI(this, investment, real);
    }





    public static BigDecimal dec2americ(BigDecimal decimal_odds){

        BigDecimal american_odds;

        if (decimal_odds.compareTo(new BigDecimal(2)) == -1){
            american_odds = new BigDecimal(-100)
                    .divide(decimal_odds.subtract(BigDecimal.ONE), 20, RoundingMode.HALF_UP);
        }
        else{
            american_odds = decimal_odds.subtract(BigDecimal.ONE).multiply(new BigDecimal(100));
        }

        return american_odds;
    }


    public static BigDecimal americ2dec(BigDecimal american_odds){
        BigDecimal decimal_odds;

        if (american_odds.compareTo(new BigDecimal(100)) != -1){
            decimal_odds = american_odds
                    .divide(new BigDecimal(100), 20, RoundingMode.HALF_UP).add(BigDecimal.ONE);
        }
        else if (american_odds.compareTo(new BigDecimal(-100)) != 1){
            decimal_odds = new BigDecimal(-100)
                    .divide(american_odds, 20, RoundingMode.HALF_UP).add(BigDecimal.ONE);
        }
        else{
            return null;
        }

        return decimal_odds;
    }


    @Override
    public int compareTo(BetOffer betOffer) {
        return this.roi_ratio.compareTo(betOffer.roi_ratio);
    }
}
