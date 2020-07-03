package Bet;

import Bet.Bet.BetType;
import SiteConnectors.Betfair.Betfair;
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

    /*
        A Bet Order is a Bet Exchange of a site, WITH a desired bet amount
        applied which is defined by the BACKERS stake only.

         If odds improve at time of bet, Bets should be placed as to
         RETURN THE SAME and STAKE LESS.
     */


    public Instant time_created;
    private String id;

    public final BetExchange betExchange;
    private BigDecimal backers_stake;


    private BetOrder(BetExchange betExchange, BigDecimal backers_stake) {
        time_created = Instant.now();
        id = rndString(12);
        this.betExchange = betExchange;
        this.backers_stake = backers_stake;
    }


    public static BetOrder fromTargetReturn(BetExchange betExchange, BigDecimal target_return){
        return null;
    }


    public static BetOrder fromBackersStake(BetExchange betExchange, BigDecimal backers_stake){
        return BetOrder.fromBackersStake(betExchange, backers_stake, false);
    }

    public static BetOrder fromBackersStake(BetExchange betExchange, BigDecimal backers_stake, boolean disable_checks){

        // Check stake is larger than min site stake and smaller than offer volume
        if (!disable_checks &&
                (backers_stake.compareTo(betExchange.volume()) > 0 ||
                 backers_stake.compareTo(betExchange.minBackStake()) < 0)){

            log.warning(String.format("Invalid back stake size for new betOrder %s. siteMin: %s    volume: %s",
                    BDString(backers_stake), BDString(betExchange.minBackStake()), BDString(betExchange.volume())));
        }

        return new BetOrder(betExchange, backers_stake);
    }


    public String getID(){
        return id;
    }


    public BigDecimal getBackersStake(){
        return backers_stake;
    }


    public BigDecimal getReturn(){
        return null;
    }


    @Override
    public BigDecimal getInvestment() {
        return null;
    }


    public BigDecimal getStake(){
        if (isBack()){
            return getBackersStake();
        }
        else if (isLay()){
            return null;
        }
        return null;
    }


    public BigDecimal avgOdds(){
        return null;
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
        return betExchange.site;
    }


    public BigDecimal winCommissionRate(){
        return getSite().winCommissionRate();
    }

    public BigDecimal lossCommissionRate(){
        return getSite().lossCommissionRate();
    }


    public BigDecimal getOddsWithBuffer(BigDecimal buffer_ratio){
        return null;
    }

    public BigDecimal getValidOddsWithBuffer(BigDecimal buffer_ratio){
        return null;
    }



    public BigDecimal getOdds(){
        return betExchange.avgOddsFromBackStake(backers_stake);
    }



    public Bet getBet(){
        return betExchange.bet;
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
        return betExchange.event;
    }




    public String toString(){
        return toJSON().toString();
    }


    public PlacedBet.State getState(){
        return null;
    }


    public JSONObject toJSON(){
        JSONObject j = new JSONObject();

        j.put("bet_offer", "null");
        j.put("back_stake", BDString(getBackersStake()));
        j.put("stake", BDString(getStake()));
        j.put("investment", BDString(getInvestment()));
        j.put("pot_return", BDString(getReturn()));
        j.put("pot_prof_b4_com", BDString(getProfitB4Comm()));
        j.put("pot_profit", BDString(getProfit()));

        return j;
    }

    @Override
    public Set<String> sites_used() {
        // A set of size 1 of the site used
        Set<String> sites_used = new HashSet<>(1);
        sites_used.add(getSite().getName());
        return sites_used;
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
            site_bets.computeIfAbsent(betOrder.getSite().getName(), k->new ArrayList<>()).add(betOrder);
        }
        return site_bets;
    }

    public static Map<String, List<BetOrder>> splitListByMetadata(List<BetOrder> betOrders, String metadata_key){
        // Sort placed bets into lists depending on their metadata
        Map<String, List<BetOrder>> site_bets = new HashMap<>();
        for (BetOrder betOrder: betOrders){
            String metadata_value = betOrder.betExchange.getMetadata(metadata_key);
            if (metadata_value == null){
                metadata_value = "null";
            }
            site_bets.computeIfAbsent(metadata_value, k->new ArrayList<>()).add(betOrder);
        }
        return site_bets;
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
