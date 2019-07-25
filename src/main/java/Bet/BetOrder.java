package Bet;

import SiteConnectors.BettingSite;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

import static tools.printer.ps;

public class BetOrder {

    public boolean real;
    public BetOffer bet_offer;
    public BigDecimal target_return;
    public BigDecimal investment;
    public BigDecimal real_return;

    public BetOrder(BetOffer BET_OFFER, BigDecimal TARGET_RETURN, boolean REAL){
        real = REAL;
        bet_offer = BET_OFFER;
        target_return = TARGET_RETURN;

        investment = target_return.divide(bet_offer.roi_ratio, 20, RoundingMode.HALF_UP);
        if (real){
            investment = investment.setScale(2, RoundingMode.HALF_UP);
        }

        real_return = bet_offer.ROI(investment, real);
    }

    public String toString(){
        return toJSON().toString();
    }

    public JSONObject toJSON(){
        JSONObject m = new JSONObject();
        m.put("bet_offer", bet_offer.toJSON());
        m.put("target_return", target_return.toString());
        m.put("investment", investment.toString());
        m.put("real_return", real_return.toString());
        m.put("real", Boolean.toString(real));
        return m;
    }

    public BettingSite site(){
        return bet_offer.site;
    }


}
