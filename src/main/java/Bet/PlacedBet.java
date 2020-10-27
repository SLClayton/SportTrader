package Bet;

import Bet.Bet.BetType;
import SiteConnectors.BettingSite;
import Sport.Event;
import Trader.SportsTrader;
import com.globalbettingexchange.externalapi.PlaceOrdersWithReceiptResponse;
import com.globalbettingexchange.externalapi.PlaceOrdersWithReceiptResponse2;
import com.globalbettingexchange.externalapi.PlaceOrdersWithReceiptResponseItem;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static tools.printer.*;
import static tools.BigDecimalTools.*;

public class PlacedBet implements SiteBet {
    // This should be exact real values taken from a single site of an
    // actual bet that has been placed.

    private static final Logger log = SportsTrader.log;

    public enum State {SUCCESS, FAIL, PARTIAL, INVALID_BETPLAN}

    public BetPlan betPlan;
    private BettingSite site;
    public String bet_id;
    public BetType bet_type;
    public State state;

    private BigDecimal backersStake_layersProfit;
    private BigDecimal backersProfit_layersStake;
    public BigDecimal avg_odds;

    public Instant time_sent;
    public Instant time_created;
    public Instant time_placed;


    public Object raw_response;
    public Object raw_request;
    public String error;



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
        betPlan = null;
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
        return getState().equals(State.SUCCESS);
    }

    public boolean failed(){
        return getState().equals(State.FAIL);
    }

    public State getState(){
        return state;
    }


    public BigDecimal get_backersProfit_layersStake(){
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
        return value;
    }

    public BigDecimal get_backersStake_layersProfit(){
        BigDecimal value;
        if (backersStake_layersProfit != null){
            value = backersStake_layersProfit;
        }
        else if (backersProfit_layersStake != null){
            value = Bet.layStake2BackStake(backersProfit_layersStake, avg_odds);
        }
        else {
            log.severe("Trying to return backersStake_layersProfit for placedBet but both values null.");
            return null;
        }
        return value;
    }


    public void set_backersStake_layersProfit(BigDecimal value){
        this.backersStake_layersProfit = value;
    }

    public void set_backersProfit_layersStake(BigDecimal value){
        this.backersProfit_layersStake = value;
    }


    public BigDecimal stake(){
        if (isBack()){
            return get_backersStake_layersProfit();
        }
        else if (isLay()){
            return get_backersProfit_layersStake();
        }
        return null;
    }


    public BigDecimal getBackersStake(){
        return get_backersStake_layersProfit();
    }

    @Override
    public BigDecimal avgOdds() {
        return avg_odds;
    }


    public BigDecimal potProfitBeforeCom(){
        if (isBack()){
            return get_backersProfit_layersStake();
        }
        else if (isLay()){
            return get_backersStake_layersProfit();
        }
        return null;
    }

    public BigDecimal potProfitAfterCom(){
        try {
            BigDecimal potProfitBeforeCom = potProfitBeforeCom();
            BigDecimal commission = winCommission(potProfitBeforeCom);
            return potProfitBeforeCom.subtract(commission);
        }
        catch (NullPointerException e){
            return null;
        }
    }


    public BigDecimal winCommission(){
        return winCommission(potProfitBeforeCom());
    }

    public BigDecimal winCommission(BigDecimal potProfitBeforeCom){
        try {
            return getSite().winCommissionRate().multiply(potProfitBeforeCom);
        }
        catch (NullPointerException e){
            return null;
        }
    }


    public BigDecimal lossCommission(BigDecimal stake){
        try {
            return getSite().lossCommissionRate().multiply(stake);
        }
        catch (NullPointerException e){
            return null;
        }
    }

    public BigDecimal lossCommission(){
        return lossCommission(stake());
    }


    public BigDecimal getReturn(){
        try {
            return getInvestment().add(potProfitAfterCom());
        }
        catch (NullPointerException e){
            return null;
        }
    }


    public BigDecimal getInvestment() {
        try {
            BigDecimal stake = stake();
            BigDecimal loss_commission = lossCommission(stake);
            return stake.add(loss_commission);
        }
        catch (NullPointerException e){
            return null;
        }
    }


    public BettingSite getSite(){
        return site;
    }

    public Bet getBet() {
        try {
            return betPlan.getBet();
        }
        catch (NullPointerException e){
            return null;
        }
    }

    public Event getEvent() {
        try {
            return betPlan.getEvent();
        }
        catch (NullPointerException e){
            return null;
        }
    }

    public void setSite(BettingSite betting_site){
        this.site = betting_site;

        if (betPlan != null && betPlan.getSite() != this.site){
            log.severe(String.format("PlacedBet SITE (%s) and BetOrder Site (%s) ARE DIFFERENT",
                    this.site.getName(), betPlan.getSite().getName()));
        }
    }



    public String toString(){
        return toJSON().toString();
    }


    public static Object stringObject(Object obj){
        if (obj instanceof JSONObject){
            return (JSONObject) obj;
        }
        else if (obj instanceof JSONArray){
            return (JSONArray) obj;
        }
        else if (obj instanceof PlaceOrdersWithReceiptResponseItem){
            PlaceOrdersWithReceiptResponse powrr = new PlaceOrdersWithReceiptResponse();
            PlaceOrdersWithReceiptResponse2 powrr2 = new PlaceOrdersWithReceiptResponse2();
            PlaceOrdersWithReceiptResponse2.Orders orders = new PlaceOrdersWithReceiptResponse2.Orders();
            orders.getOrder().add((PlaceOrdersWithReceiptResponseItem) obj);
            powrr2.setOrders(orders);
            powrr.setPlaceOrdersWithReceiptResult(powrr2);
            String xml = SOAP2XMLnull(powrr);
            return xml;
        }
        else{
            return obj.toString();
        }
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
        j.put("tot_inv", BDString(getInvestment()));
        j.put("pot_prof_b4_com", BDString(potProfitBeforeCom()));
        j.put("pot_prof", BDString(potProfitAfterCom()));
        j.put("pot_returns", BDString(getReturn()));


        if (getSite() != null){
            BigDecimal loss_com_rate = getSite().lossCommissionRate();
            if (loss_com_rate.signum() != 0){
                com_info.put("loss_com_rate", BDString(loss_com_rate));
                com_info.put("loss_com", BDString(lossCommission()));
            }
            com_info.put("win_com", BDString(winCommission()));
            com_info.put("win_com_rate", BDString(getSite().winCommissionRate()));
        }
        if (betPlan != null){
            j.put("betOrder", betPlan.toJSON());
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
            j.put("raw_response", stringObject(raw_response));
        }
        if (raw_request != null){
            j.put("raw_request", stringObject(raw_request));
        }


        j.put("timings", time_info);
        j.put("comission", com_info);

        return j;
    }


    public static JSONArray list2JSON(List<PlacedBet> placedBets){
        JSONArray j = new JSONArray();
        for (PlacedBet pb: placedBets){
            j.add(pb.toJSON());
        }
        return j;
    }




}
