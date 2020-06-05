package Bet;

import Bet.Bet.BetType;
import SiteConnectors.BettingSite;
import Sport.Event;
import ch.qos.logback.core.db.BindDataSourceToJNDIAction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

import static tools.printer.*;

public class BetOrder implements ProfitReportItem {

    public Instant time_created;
    private String id;

    private BetOffer bet_offer;
    private BigDecimal backers_stake;

    public Object raw_request;


    private BetOrder(BetOffer bet_offer, BigDecimal backers_stake) {
        time_created = Instant.now();
        id = rndString(12);
        this.bet_offer = bet_offer;
        this.backers_stake = backers_stake;
    }


    public static BetOrder fromTargetReturn(BetOffer bet_offer, BigDecimal target_return){
        BigDecimal target_investment = target_return.divide(bet_offer.ROI_ratio(), 12 , RoundingMode.HALF_UP);
        return BetOrder.fromTargetInvestment(bet_offer, target_investment);
    }

    public static BetOrder fromTargetInvestment(BetOffer bet_offer, BigDecimal target_investment){
        BigDecimal stake = bet_offer.site.stakePartOfInvestment(target_investment);
        return BetOrder.fromStake(bet_offer, stake);
    }

    public static BetOrder fromStake(BetOffer bet_offer, BigDecimal stake){
        if ((stake.compareTo(bet_offer.minStake()) < 0) || (stake.compareTo(bet_offer.maxStake()) > 0)){
            log.severe(String.format("Creating betorder with invalid stake %s for betoffer with min=%s max=%s",
                    BDString(stake), BDString(bet_offer.minStake()), BDString(bet_offer.maxStake())));
        }

        if (bet_offer.isBack()){
            return new BetOrder(bet_offer, stake);
        }
        else if (bet_offer.isLay()){
            return new BetOrder(bet_offer, bet_offer.layStake2BackStake(stake));
        }
        return null;
    }


    public BetOrder newTargetInvestment(BigDecimal new_target_investment){
        return BetOrder.fromTargetInvestment(bet_offer, new_target_investment);
    }

    public BetOrder newTargetReturn(BigDecimal new_target_return){
        return BetOrder.fromTargetReturn(bet_offer, new_target_return);
    }

    public BetOrder newStake(BigDecimal stake){
        return BetOrder.fromStake(bet_offer, stake);
    }

    public String getID(){
        return id;
    }


    public BigDecimal getStake(){
        if (isBack()){
            return backers_stake;
        }
        else if (isLay()){
            return bet_offer.backStake2LayStake(backers_stake);
        }
        return null;
    }


    public BigDecimal getInvestment(){
        return getSite().investmentNeededForStake(getStake());
    }


    public BetOffer getBetOffer(){
        return bet_offer;
    }


    public BigDecimal getReturn(){
        return getBetOffer().ROI(getInvestment());
    }


    public BigDecimal getProfitB4Comm(){
        if (isBack()){
            return Bet.backStake2LayStake(getBackersStake(), getOdds());
        }
        else if (isLay()){
            return getBackersStake();
        }
        return null;
    }


    public BigDecimal getProfit(){
        return getReturn().subtract(getInvestment());
    }


    public BettingSite getSite(){
        return getBetOffer().site;
    }


    public BigDecimal winCommissionRate(){
        return getSite().winCommissionRate();
    }

    public BigDecimal lossCommissionRate(){
        return getSite().lossCommissionRate();
    }


    public BigDecimal getOddsWithBuffer(BigDecimal buffer_ratio){
        return getBetOffer().getOddsWithBuffer(buffer_ratio);
    }

    public BigDecimal getValidOddsWithBuffer(BigDecimal buffer_ratio){
        return getBetOffer().getValidOddsWithBuffer(buffer_ratio);
    }


    public BigDecimal getBackersStake(){
        return backers_stake;
    }


    public Bet getBet(){
        return getBetOffer().bet;
    }


    public String betID(){
        return getBet().id();
    }


    public boolean isBack(){
        return getBet().isBack();
    }


    public boolean isLay(){
        return getBet().isLay();
    }


    public BetType betType(){
        return getBet().getType();
    }


    public Event getEvent(){
        return getBetOffer().event;
    }


    public BigDecimal getOdds(){
        return getBetOffer().odds;
    }


    public String toString(){
        return toJSON().toString();
    }


    public PlacedBet.State getState(){
        return null;
    }


    public JSONObject toJSON(){
        JSONObject j = new JSONObject();

        j.put("bet_offer", getBetOffer().toJSON());
        j.put("back_stake", BDString(getBackersStake()));
        j.put("stake", BDString(getStake()));
        j.put("investment", BDString(getInvestment()));
        j.put("pot_return", BDString(getReturn()));
        j.put("pot_prof_b4_com", BDString(getProfitB4Comm()));
        j.put("pot_profit", BDString(getProfit()));

        return j;
    }

    public static JSONArray list2JSON(List<BetOrder> betOrders){
        JSONArray j = new JSONArray();
        for (BetOrder betOrder: betOrders){
            j.add(betOrder.toJSON());
        }
        return j;
    }

    public static Map<String, List<BetOrder>> splitListBySite(List<BetOrder> betOrders){
        // Sort placed bets into lists depending on their site

        Map<String, List<BetOrder>> site_bets = new HashMap<>();
        for (BetOrder betOrder: betOrders){
            String site_name = betOrder.getSite().getName();

            if (!site_bets.containsKey(site_name)){
                site_bets.put(site_name, new ArrayList<>());
            }

            site_bets.get(site_name).add(betOrder);
        }
        return site_bets;
    }

    public static Map<String, List<BetOrder>> splitListByMetaData(List<BetOrder> betOrders, String key){
        // Sort placed bets into lists depending on their site

        Map<String, List<BetOrder>> key_bet_map = new HashMap<>();
        for (BetOrder betOrder: betOrders){
            String metadata_value = betOrder.getBetOffer().getMetadata(key);

            if (!key_bet_map.containsKey(metadata_value)){
                key_bet_map.put(metadata_value, new ArrayList<>());
            }

            key_bet_map.get(metadata_value).add(betOrder);
        }
        return key_bet_map;
    }

    public static BetOrder find(Collection<BetOrder> betOrders, String id){
        for (BetOrder betOrder: betOrders){
            if (betOrder.getID().equals(id)){
                return betOrder;
            }
        }
        return null;
    }



}
