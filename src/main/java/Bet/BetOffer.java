package Bet;

import SiteConnectors.BettingSite;
import Sport.Match;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;

import static tools.printer.print;

public class BetOffer implements Comparable<BetOffer> {

    public Match match;
    public Bet bet;
    public BettingSite site;
    public BigDecimal odds;
    public BigDecimal volume;
    public HashMap<String, String> metadata;

    public BigDecimal roi_ratio;

    public BetOffer(Match MATCH, Bet BET, BettingSite SITE, BigDecimal ODDS, BigDecimal VOLUME, HashMap METADATA){
        match = MATCH;
        bet = BET;
        site = SITE;
        odds = ODDS;
        volume = VOLUME;
        metadata = METADATA;
        roi_ratio = ROI(BigDecimal.ONE, false);
    }

    public BetOffer(){}

    public BetOffer newOdds(BigDecimal odds){
        // Returns a new betOffer with the same attributes except new odds and volume
        BetOffer newBetOffer = new BetOffer(match, bet, site, odds, volume, metadata);
        return newBetOffer;
    }

    public String toString(){
        return toJSON().toString();
    }

    public JSONObject toJSON(){
        JSONObject m = new JSONObject();
        m.put("match", String.valueOf(match));
        m.put("bet", String.valueOf(bet.id()));
        m.put("odds", String.valueOf(odds));
        m.put("volume", String.valueOf(volume));
        m.put("roi_ratio", String.valueOf(roi_ratio));
        m.put("metadata", String.valueOf(metadata));
        m.put("site", String.valueOf(site.name));
        return m;
    }

    public BigDecimal commission(){
        return site.commission();
    }

    public boolean isBack(){
        return bet.isBack();
    }

    public boolean isLay(){
        return bet.isLay();
    }


    public BigDecimal minStake(){
        if (isLay()){
            BigDecimal min_lay = site.minBet();
            return getStakeFromLay(min_lay, true);
        }
        return site.minBet().setScale(2, RoundingMode.UP);
    }

    public BigDecimal maxStake(){
        if (isLay()){
            BigDecimal max_lay = volume;
            return getStakeFromLay(max_lay, true);
        }
        return volume.setScale(2, RoundingMode.UP);
    }

    public BigDecimal minStakeReturn(){
        return ROI(minStake(), true);
    }

    public BigDecimal maxStakeReturn(){
        return ROI(maxStake(), true);
    }


    public BigDecimal ROI(BigDecimal investment, boolean real){
        return site.ROI(this, investment, real);
    }

    public BigDecimal getLayFromStake(BigDecimal stake, boolean real){
        return BetOffer.getLayFromStake(odds, stake, real);
    }

    public BigDecimal getStakeFromLay(BigDecimal lay, boolean real){
        return BetOffer.getStakeFromLay(odds, lay, real);
    }


    public static BigDecimal getLayFromStake(BigDecimal odds, BigDecimal stake, boolean real){
        BigDecimal lay = stake.divide( odds.subtract(BigDecimal.ONE), 20, RoundingMode.HALF_UP );
        if (real){
            lay = lay.setScale(2, RoundingMode.HALF_UP);
        }
        return lay;
    }

    public static BigDecimal getStakeFromLay(BigDecimal odds, BigDecimal lay, boolean real){
        BigDecimal stake = lay.multiply( odds.subtract(BigDecimal.ONE) );
        if (real){
            stake = stake.setScale(2, RoundingMode.HALF_UP);
        }
        return stake;
    }


    @Override
    public int compareTo(BetOffer betOffer) {
        return this.roi_ratio.compareTo(betOffer.roi_ratio);
    }
}
