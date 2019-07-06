package Bet;

import SiteConnectors.BettingSite;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;

import static tools.printer.print;

public class PlacedBet {

    public String bet_id;
    public BetOffer bet_offer;
    public BigDecimal stake;
    public BigDecimal ret;
    public boolean matched;
    public Instant time_placed;
    public Instant time_matched;

    public PlacedBet(String BETID, BetOffer BETOFFER, BigDecimal STAKE, BigDecimal RET,
                     boolean MATCHED, Instant TIME_PLACED, Instant TIME_MATCHED){

        bet_id = BETID;
        bet_offer = BETOFFER;
        stake = STAKE;
        ret = RET;
        matched = MATCHED;
        time_placed = TIME_PLACED;
        time_matched = TIME_MATCHED;
    }

    public String toString(){
        HashMap<String, String> m = new HashMap<String, String>();
        m.put("bet_id", bet_id);
        m.put("bet_offer", bet_offer.toString());
        m.put("stake", stake.toString());
        m.put("ret", ret.toString());
        m.put("matched", Boolean.toString(matched));
        m.put("time_placed", time_placed.toString());
        m.put("time_matched", time_matched.toString());
        return m.toString();
    }

    public BigDecimal profit(){
        return ret.subtract(stake);
    }

}
