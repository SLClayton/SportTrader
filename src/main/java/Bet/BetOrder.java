package Bet;

import SiteConnectors.BettingSite;
import Sport.Match;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;

import static tools.printer.ps;

public class BetOrder {

    public boolean real;
    public BetOffer bet_offer;
    public BigDecimal target_return;
    public BigDecimal investment;
    public BigDecimal real_return;
    public BigDecimal lay_amount;

    public BetOrder(BetOffer bet_offer, BigDecimal target_return, boolean real){
        this.real = real;
        this.bet_offer = bet_offer;
        this.target_return = target_return;

        investment = target_return.divide(bet_offer.roi_ratio, 20, RoundingMode.HALF_UP);
        if (real){
            investment = investment.setScale(2, RoundingMode.HALF_UP);
        }

        real_return = bet_offer.ROI(investment, real);
    }

    public BetOrder(){}


    public Bet bet(){
        return bet_offer.bet;
    }


    public BettingSite site(){
        return bet_offer.site;
    }


    public boolean isBack(){
        return bet_offer.bet.isBack();
    }


    public boolean isLay(){
        return bet_offer.bet.isLay();
    }


    public String betType(){
        return bet().type();
    }


    public Match match(){
        return bet_offer.match;
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
        m.put("lay_amount", String.valueOf(lay_amount));
        return m;
    }

    public static JSONArray list2JSON(ArrayList<BetOrder> betOrders){
        JSONArray j = new JSONArray();
        for (BetOrder betOrder: betOrders){
            j.add(betOrder.toJSON());
        }
        return j;
    }



}
