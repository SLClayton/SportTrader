package Bet;

import SiteConnectors.BettingSite;
import Sport.Match;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;

public class BetOffer {

    public Match match;
    public Bet bet;
    public BettingSite site;
    public BigDecimal odds;
    public BigDecimal volume;
    public HashMap metadata;

    public BigDecimal roi_ratio;

    public BetOffer(Match MATCH, Bet BET, BettingSite SITE, BigDecimal ODDS, BigDecimal VOLUME, HashMap METADATA){
        match = MATCH;
        bet = BET;
        site = SITE;
        odds = ODDS;
        volume = VOLUME;
        metadata = METADATA;
    }

    public String toString(){
        HashMap<String, String> m = new HashMap<String, String>();
        m.put("match", match.name);
        m.put("bet", bet.id());
        m.put("site", site.name);
        m.put("odds", odds.toString());
        m.put("volume", volume.toString());
        m.put("metadata", metadata.toString());
        return m.toString();
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
        BigDecimal lay = stake.divide( odds.subtract(BigDecimal.ONE) );
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
}
