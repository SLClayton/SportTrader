package Bet;

import Bet.Bet.BetType;
import SiteConnectors.BettingSite;
import Trader.SportsTrader;
import com.globalbettingexchange.externalapi.PlaceOrdersWithReceipt;
import com.globalbettingexchange.externalapi.PlaceOrdersWithReceiptResponse;
import com.globalbettingexchange.externalapi.PlaceOrdersWithReceiptResponse2;
import com.globalbettingexchange.externalapi.PlaceOrdersWithReceiptResponseItem;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.lang.management.BufferPoolMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import static tools.Requester.SOAP2XMLnull;
import static tools.printer.*;

public class PlacedBet {
    // This should be exact real values taken from a site of an actual bet that
    // has been placed.

    private static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    public enum State {SUCCESS, FAIL, PARTIAL, INVALID_BETORDER}



    public State state;
    public String bet_id;
    public BetType bet_type;

    private BigDecimal backersStake_layersProfit;
    private BigDecimal backersProfit_layersStake;
    public BigDecimal avg_odds;
    public String error;

    public Instant time_sent;
    public Instant time_created;
    public Instant time_placed;

    public Object raw_response;
    public BetOrder betOrder;
    private BettingSite site;


    public PlacedBet(){
        this.time_created = Instant.now();

        state = null;
        bet_id = null;
        bet_type = null;

        backersStake_layersProfit = null;
        backersProfit_layersStake = null;
        avg_odds = null;
        error = null;

        time_sent = null;
        time_created = null;
        time_placed = null;

        raw_response = null;
        betOrder = null;
        site = null;
    }

    public static PlacedBet failedBet(String error){
        PlacedBet pb = new PlacedBet();
        pb.state = State.FAIL;
        pb.error = error;
        return pb;
    }

    public void setFail(String error_msg){
        state = State.FAIL;
        error = error_msg;
    }


    public void setSuccess(){
        state = State.SUCCESS;
    }


    public boolean isBack(){
        return bet_type == BetType.BACK;
    }


    public boolean isLay(){
        return bet_type == BetType.LAY;
    }


    public boolean successful(){
        return state.equals(State.SUCCESS);
    }


    public boolean failed(){
        return state.equals(State.FAIL);
    }


    public BigDecimal get_backersProfit_layersStake(Integer scale){
        BigDecimal value;
        if (backersProfit_layersStake != null){
            value = backersProfit_layersStake;
        }
        else if (backersStake_layersProfit != null){
            value = Bet.backStake2LayStake(backersStake_layersProfit, avg_odds);
        }
        else {
            log.severe("Trying to return backersprofit_layerssStake for placedBet but both values null.");
            return null;
        }

        if (scale != null) {
            value = value.setScale(2, RoundingMode.HALF_UP);
        }
        return value;
    }

    public BigDecimal get_backersProfit_layersStake(){
        return get_backersProfit_layersStake(null);
    }


    public BigDecimal get_backersStake_layersProfit(Integer scale){
        BigDecimal value;
        if (backersStake_layersProfit != null){
            value = backersStake_layersProfit;
        }
        else if (backersProfit_layersStake != null){
            value = Bet.layStake2backStake(backersProfit_layersStake, avg_odds);
        }
        else {
            log.severe("Trying to return backersStake_layersProfit for placedBet but both values null.");
            return null;
        }

        if (scale != null) {
            value = value.setScale(2, RoundingMode.HALF_UP);
        }
        return value;
    }

    public BigDecimal get_backersStake_layersProfit(){
        return get_backersStake_layersProfit(null);
    }


    public void set_backersStake_layersProfit(BigDecimal value){
        this.backersStake_layersProfit = value;
    }

    public void set_backersProfit_layersStake(BigDecimal value){
        this.backersProfit_layersStake = value;
    }


    public BigDecimal stake(Integer scale){
        if (isBack()){
            return get_backersStake_layersProfit(scale);
        }
        else{
            return get_backersProfit_layersStake(scale);
        }
    }


    public BigDecimal stake(){
        return stake(null);
    }


    public BigDecimal potProfitBeforeCom(){
        if (isBack()){
            return get_backersProfit_layersStake();
        }
        else{
            return get_backersStake_layersProfit();
        }
    }

    public BigDecimal potProfitAfterCom(){

        BigDecimal potProfitBeforeCom = potProfitBeforeCom();
        if (potProfitBeforeCom == null){
            return null;
        }

        BigDecimal commission = winCommission(potProfitBeforeCom);
        if (commission == null){
            return null;
        }

        return potProfitBeforeCom.subtract(commission);
    }


    public BigDecimal winCommission(){
        return winCommission(potProfitBeforeCom());
    }

    public BigDecimal winCommission(BigDecimal potProfitBeforeCom){
        if (getSite() == null || potProfitBeforeCom == null){
            return null;
        }
        return getSite().winCommissionRate().multiply(potProfitBeforeCom);
    }

    public BigDecimal lossCommission(BigDecimal stake){
        if (getSite() == null || stake == null){
            return null;
        }
        return getSite().lossCommissionRate().multiply(stake);
    }

    public BigDecimal lossCommission(){
        return lossCommission(stake());
    }


    public BigDecimal total_investment(Integer scale){
        BigDecimal stake = stake();
        if (stake == null){
            return null;
        }

        BigDecimal loss_commission = lossCommission(stake);
        if (loss_commission == null){
            return null;
        }

        BigDecimal investment = stake.add(loss_commission);

        if (scale != null){
            investment = investment.setScale(scale, RoundingMode.HALF_UP);
        }
        return investment;
    }

    public BigDecimal total_investment(){
        return total_investment(null);
    }


    public BigDecimal potReturns(){
        BigDecimal profit_before_com = potProfitBeforeCom();
        BigDecimal total_investment = total_investment();

        if (profit_before_com == null || total_investment == null){
            return null;
        }

        return total_investment
                .add(profit_before_com)
                .subtract(winCommission(profit_before_com));
    }


    public BettingSite getSite(){
        if (site != null){
            return site;
        }
        if (betOrder != null){
            return betOrder.site();
        }
        return null;
    }

    public void setSite(BettingSite betting_site){
        this.site = betting_site;

        if (betOrder != null && betOrder.site() != this.site){
            log.severe(String.format("PlacedBet SITE (%s) and BetOrder Site (%s) ARE DIFFERENT",
                    this.site.getName(), betOrder.site().getName()));
        }
    }



    public String toString(){
        return toJSON().toString();
    }


    public JSONObject toJSON(){
        JSONObject j = new JSONObject();

        JSONObject time_info = new JSONObject();
        JSONObject com_info = new JSONObject();


        j.put("backersStake_layersProfit", BDString(backersStake_layersProfit));
        j.put("backersProfit_layersStake", BDString(backersProfit_layersStake));
        j.put("state", stringValue(state));
        j.put("bet_id", stringValue(bet_id));
        j.put("avg_odds", BDString(avg_odds));
        j.put("type", stringValue(bet_type));
        j.put("site", stringValue(getSite()));


        j.put("tot_inv", BDString(total_investment()));
        j.put("pot_prof_b4_com", BDString(potProfitBeforeCom()));
        j.put("pot_prof", BDString(potProfitAfterCom()));

        j.put("pot_returns", BDString(potReturns()));



        if (getSite() != null){
            com_info.put("win_com", BDString(winCommission()));
            com_info.put("loss_com", BDString(lossCommission()));
            com_info.put("win_com_rate", BDString(getSite().winCommissionRate()));
            com_info.put("loss_com_rate", BDString(getSite().lossCommissionRate()));
        }
        if (betOrder != null){
            j.put("betOrder", betOrder.toJSON());
        }
        if (betOrder != null && time_placed != null){
            time_info.put("offer_to_placed",
                    time_placed.toEpochMilli() - betOrder.bet_offer.time_betOffer_creation.toEpochMilli());
        }
        if (betOrder != null && time_sent != null){
            time_info.put("offer_to_sent",
                    time_sent.toEpochMilli() - betOrder.bet_offer.time_betOffer_creation.toEpochMilli());
        }
        if (time_placed != null){
            time_info.put("time_placed", time_placed.toString());
        }
        if (time_sent != null){
            time_info.put("time_sent", time_sent.toString());
        }
        if (time_placed != null && time_sent != null){
            time_info.put("sent_to_placed", time_placed.toEpochMilli() - time_sent.toEpochMilli());
        }

        if (error != null){
            j.put("error", error);
        }

        if (raw_response != null){
            if (raw_response instanceof JSONObject){
                j.put("raw_resp", (JSONObject) raw_response);
            }
            else if (raw_response instanceof PlaceOrdersWithReceiptResponseItem){
                PlaceOrdersWithReceiptResponse powrr = new PlaceOrdersWithReceiptResponse();
                PlaceOrdersWithReceiptResponse2 powrr2 = new PlaceOrdersWithReceiptResponse2();
                PlaceOrdersWithReceiptResponse2.Orders orders = new PlaceOrdersWithReceiptResponse2.Orders();
                orders.getOrder().add((PlaceOrdersWithReceiptResponseItem) raw_response);
                powrr2.setOrders(orders);
                powrr.setPlaceOrdersWithReceiptResult(powrr2);
                String xml = SOAP2XMLnull(powrr);
                j.put("raw_resp", xml);
            }
            else{
                j.put("raw_resp", raw_response.toString());
            }
        }


        j.put("timings", time_info);
        j.put("comission", com_info);

        return j;
    }

    public static JSONArray list2JSON(ArrayList<PlacedBet> placedBets){
        JSONArray ja = new JSONArray();
        for (PlacedBet pb: placedBets){
            ja.add(pb.toJSON());
        }
        return ja;
    }


    public static void main(String[] args){
        BigDecimal bd = new BigDecimal("6000.004000");
        print(BDString(bd));
    }

}
