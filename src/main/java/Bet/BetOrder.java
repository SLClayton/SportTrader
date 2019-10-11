package Bet;

import SiteConnectors.BettingSite;
import Sport.Match;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.lang.management.BufferPoolMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

import static tools.printer.print;
import static tools.printer.ps;

public class BetOrder {

    public boolean real;
    public BetOffer bet_offer;
    public BigDecimal target_return;

    public BigDecimal backersStake_layersProfit;
    public BigDecimal backersProfit_layersStake;

    public BigDecimal investment;
    public BigDecimal stake;
    public BigDecimal potential_profit;
    public BigDecimal potential_commission;
    public BigDecimal actual_return;

    public JSONObject site_json_request;


    public BetOrder(BetOffer bet_offer, BigDecimal target_return, boolean real) {

        this.bet_offer = bet_offer;
        this.target_return = target_return;
        this.real = real;

        // Calculate investment needed to achieve the target return, using the ROI ratio
        investment = target_return.divide(bet_offer.roi_ratio, 20, RoundingMode.HALF_UP);

        // BACK
        if (isBack()) {

            // Get what stake this site would need with this investment
            backersStake_layersProfit = bet_offer.site.investment2Stake(investment);

            // If real, then round backers stake and re-calculate investment.
            if (real) {
                backersStake_layersProfit = backersStake_layersProfit
                        .setScale(2, RoundingMode.HALF_UP);
                investment = bet_offer.site.stake2Investment(backersStake_layersProfit);
            }

            // Calculate the profit and return generated with this stake
            backersProfit_layersStake = BetOffer.backStake2LayStake(backersStake_layersProfit, bet_offer.odds);
            potential_profit = backersProfit_layersStake;
            stake = backersStake_layersProfit;
        }

        // LAY
        else {

            // Get what stake this site would need with this investment, then calculate what
            // the backers stake would be as this is what a bet is placed on.
            backersProfit_layersStake = bet_offer.site.investment2Stake(investment);
            backersStake_layersProfit = BetOffer.layStake2backStake(backersProfit_layersStake, bet_offer.odds);

            // If real, then round backers stake and re-calculate layers stake and investment.
            if (real) {
                backersStake_layersProfit = backersStake_layersProfit
                        .setScale(2, RoundingMode.HALF_UP);

                backersProfit_layersStake = BetOffer.backStake2LayStake(backersStake_layersProfit, bet_offer.odds);
                investment = bet_offer.site.stake2Investment(backersProfit_layersStake);
            }

            // Calculate the return generated with this stake
            potential_profit = backersStake_layersProfit;
            stake = backersProfit_layersStake;
        }


        // Calculate the potential commission given the potential profit
        potential_commission = potential_profit.multiply(site().commission());
        actual_return = investment.add(potential_profit).subtract(potential_commission);
    }


    public BetOrder(){}


    public BigDecimal commission(){
        return bet_offer.commission();
    }


    public BigDecimal getBackersStake(){
        return backersStake_layersProfit;
    }


    public Bet bet(){
        return bet_offer.bet;
    }


    public String betID(){
        return bet().id();
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


    public BigDecimal odds(){
        return bet_offer.odds;
    }


    public String toString(){
        return toJSON().toString();
    }


    public JSONObject toJSON(){
        JSONObject m = new JSONObject();

        m.put("offer_created", String.valueOf(bet_offer.time_created));
        m.put("match", String.valueOf(bet_offer.match));
        m.put("bet", String.valueOf(bet_offer.bet.id()));
        m.put("site", String.valueOf(bet_offer.site.name));
        m.put("odds", String.valueOf(bet_offer.odds));
        m.put("offer_roi", String.valueOf(bet_offer.roi_ratio));
        m.put("offer_volume", String.valueOf(bet_offer.volume));
        m.put("metadata", String.valueOf(bet_offer.metadata));

        m.put("target_return", String.valueOf(target_return));
        m.put("investment", String.valueOf(investment));
        m.put("real", String.valueOf(real));
        if (bet_offer.isLay()){
            m.put("layers_stake", String.valueOf(backersProfit_layersStake));
        }
        m.put("backers_stake", String.valueOf(backersStake_layersProfit));
        m.put("profit", String.valueOf(potential_profit));
        m.put("commission", String.valueOf(potential_commission));
        m.put("actual_return", String.valueOf(actual_return));

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
