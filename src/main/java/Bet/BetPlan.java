package Bet;

import Bet.Bet.BetType;
import SiteConnectors.BettingSite;
import Sport.Event;
import Trader.Config;
import Trader.EventTrader;
import Trader.SportsTrader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;

import static tools.printer.*;
import static tools.BigDecimalTools.*;

public class BetPlan implements SiteBet {

    /*
        A BetPlan is a Bet Exchange of a single site, WITH a desired bet amount
        applied which is defined by the BACKERS stake only.

         If odds improve at time of bet, Bets should be placed as to
         RETURN THE SAME and STAKE LESS.
     */

    private static final Config config = SportsTrader.config;

    // Final immutable values for this object
    public final Instant time_created;
    private final String id;
    public final BetExchange betExchange;
    private final BigDecimal backers_stake;

    // Values to be calculated once on first access, and stored.
    private List<BetOfferStake> betOfferStakes;
    private BigDecimal avg_odds;
    private BigDecimal roi_ratio;
    private BigDecimal lay_stake;
    private BigDecimal investment;
    private BigDecimal potential_profit;


    private BetPlan(BetExchange betExchange, BigDecimal backers_stake) {
        time_created = Instant.now();
        id = rndStringNumber(12);
        this.betExchange = betExchange;
        this.backers_stake = backers_stake;

        // Setting null values to fields calculated on first access
        this.betOfferStakes = null;
        this.avg_odds = null;
        this.roi_ratio = null;
        this.lay_stake = null;
        this.investment = null;
        this.potential_profit = null;
    }


    public static BetPlan fromTargetReturn(BetExchange betExchange, BigDecimal target_return){
        return null;
    }

    public static BetPlan fromTargetInvestment(BetExchange betExchange, BigDecimal target_investment){

        BigDecimal stake = Bet.investment2Stake(target_investment, betExchange.site.lossCommissionRate());

        BigDecimal back_stake;
        if (betExchange.isLay()){
            back_stake = betExchange.layStake2BackStake(stake);
        }
        else {
            back_stake = stake;
        }

        return BetPlan.fromBackersStake(betExchange, back_stake);
    }


    public static BetPlan fromBackersStake(BetExchange betExchange, BigDecimal backers_stake){

        // Check stake is smaller than offer volume
        if ((backers_stake.compareTo(betExchange.volume()) > 0)){
            log.warning(String.format("Back stake size %s too large for new betOrder. Total exchange volume: %s",
                    BDString(backers_stake),
                    BDString(betExchange.volume())));

            return null;
        }

        return new BetPlan(betExchange, backers_stake);
    }


    public String getID(){
        return id;
    }

    public Event getEvent(){
        return betExchange.event;
    }

    public Bet getBet(){
        return betExchange.bet;
    }

    public BetType betType(){
        return getBet().getType();
    }

    public boolean isBack(){
        return getBet().isBack();
    }

    public boolean isLay(){
        return getBet().isLay();
    }

    public BettingSite getSite(){
        return betExchange.site;
    }

    public String getSiteName(){
        return getSite().getName();
    }


    public boolean hasMinStake(){
        return backers_stake.compareTo(betExchange.minBackStake()) >= 0;
    }


    public boolean betterROIRatio(BetPlan betPlan){
        return this.ROIRatio().compareTo(betPlan.ROIRatio()) > 0;
    }



    public List<BetOfferStake> getBetOfferStakes(){
        if (betOfferStakes == null){
            betOfferStakes = BetOffer.applyStake(betExchange.getBetOffers(), getBackersStake(), true, true);
        }
        return betOfferStakes;
    }


    public BigDecimal getBackersStake(){
        return backers_stake;
    }


    public BigDecimal getStake(){
        if (isBack()){
            return getBackersStake();
        }
        else if (isLay()){
            return getLayersStake();
        }
        return null;
    }


    public BigDecimal getLayersStake(){
        if (lay_stake == null){
            lay_stake = Bet.backStake2LayStake(backers_stake, avgOdds());
        }
        return lay_stake;
    }


    public BigDecimal getInvestment() {
        if (investment == null){
            investment = Bet.stake2Investment(getStake(), getSite().lossCommissionRate());
        }
        return investment;
    }


    public BigDecimal avgOdds(){
        if (avg_odds == null){
            avg_odds = BetOfferStake.averageOdds(getBetOfferStakes());
        }
        return avg_odds;
    }

    public BigDecimal ROIRatio(){
        if (roi_ratio == null){
            roi_ratio = getReturn().divide(getInvestment(), 12, RoundingMode.HALF_UP);
        }
        return roi_ratio;
    }


    public BigDecimal getProfitB4Comm(){
        // The 'other sides' stake

        if (isBack()){
            return getLayersStake();
        }
        else if (isLay()){
            return getBackersStake();
        }
        return null;
    }


    public BigDecimal getProfit(){
        if (potential_profit == null){
            potential_profit = Bet.profitAfterCommission(getProfitB4Comm(), getSite().winCommissionRate());
        }
        return potential_profit;
    }


    public BigDecimal getReturn(){
        return getInvestment().add(getProfit());
    }


    public BetOffer getWorstValueOffer(){
        // Return the worst offer that's included in this order
        List<BetOfferStake> betOfferStakes = getBetOfferStakes();
        return betOfferStakes.get(betOfferStakes.size()-1).betOffer;
    }


    public BigDecimal getOddsWithBuffer(BigDecimal buffer_ratio){
        // Add a buffer to the odds depending on direction of bet
        BigDecimal multiplier = BigDecimal.ONE;
        if (isBack()){
            multiplier = multiplier.subtract(buffer_ratio);
        }
        else if (isLay()){
            multiplier = multiplier.add(buffer_ratio);
        }
        else{
            return null;
        }

        return getWorstValueOffer().odds
                .subtract(BigDecimal.ONE)
                .multiply(multiplier)
                .add(BigDecimal.ONE);
    }

    public BigDecimal getValidOddsWithBuffer(BigDecimal buffer_ratio){
        BigDecimal buffered_odds = getOddsWithBuffer(buffer_ratio);
        RoundingMode rm;
        if (isBack()){
            rm = RoundingMode.DOWN;
        }
        else if (isLay()){
           rm = RoundingMode.UP;
        }
        else{
            return null;
        }
        return getSite().getValidOdds(buffered_odds, rm);
    }


    @Override
    public String toString(){
        return String.format("[BP for %s on %s with roi %s   %s %s]",
                BDString(backers_stake, 4), getSiteName(), BDString(ROIRatio(), 4), getEvent(), getBet());
    }


    public PlacedBet.State getState(){
        return null;
    }


    public JSONObject toJSON(){
        JSONObject j = new JSONObject();


        j.put("event", stringValue(getEvent()));
        j.put("site", getSite().getName());
        j.put("bet", stringValue(getBet()));


        j.put("back_stake", BDString(getBackersStake()));
        j.put("stake", BDString(getStake()));
        j.put("investment", BDString(getInvestment()));
        j.put("pot_return", BDString(getReturn()));
        j.put("pot_prof_b4_com", BDString(getProfitB4Comm()));
        j.put("pot_profit", BDString(getProfit()));
        j.put("exp_avg_odds", BDString(avgOdds()));


        JSONObject exchange = betExchange.toJSON();
        exchange.remove("event");
        exchange.remove("site");
        exchange.remove("bet");
        j.put("betExchange", exchange);

        return j;
    }



    public static Map<String, List<BetPlan>> splitListBySite(List<BetPlan> betPlans){
        // Sort placed bets into lists depending on their site
        Map<String, List<BetPlan>> site_bets = new HashMap<>();
        for (BetPlan betPlan : betPlans){
            site_bets.computeIfAbsent(betPlan.getSite().getName(), k->new ArrayList<>()).add(betPlan);
        }
        return site_bets;
    }


    public static Map<String, List<BetPlan>> splitListByMetadata(List<BetPlan> betPlans, String metadata_key){
        // Sort placed bets into lists depending on their metadata
        Map<String, List<BetPlan>> site_bets = new HashMap<>();
        for (BetPlan betPlan : betPlans){
            String metadata_value = betPlan.betExchange.getMetadata(metadata_key);
            if (metadata_value == null){
                metadata_value = "null";
            }
            site_bets.computeIfAbsent(metadata_value, k->new ArrayList<>()).add(betPlan);
        }
        return site_bets;
    }


    public static BetPlan find(Collection<BetPlan> betPlans, String id){
        for (BetPlan betPlan : betPlans){
            if (betPlan.getID().equals(id)){
                return betPlan;
            }
        }
        return null;
    }


    public static List<PlacedBet> placeBets(List<BetPlan> betPlans){

        // Sort placed bets into lists depending on the site they're going to.
        Map<String, List<BetPlan>> site_bets = BetPlan.splitListBySite(betPlans);


        // Send list of bets of to their respective Betting site objects to be placed
        ArrayList<PlaceBetsRunnable> placeBetsRunnables = new ArrayList<>();
        for (Map.Entry<String, List<BetPlan>> entry: site_bets.entrySet()){
            List<BetPlan> site_betPlans = entry.getValue();

            PlaceBetsRunnable placeBetsRunnable = new PlaceBetsRunnable(site_betPlans);
            placeBetsRunnable.start();
            placeBetsRunnables.add(placeBetsRunnable);
        }


        // Wait for threads to finish and gather resulting placedBets
        ArrayList<PlacedBet> placedBets = new ArrayList<>();
        for (PlaceBetsRunnable placeBetsRunnable: placeBetsRunnables){
            try {
                placeBetsRunnable.thread.join();
                placedBets.addAll(placeBetsRunnable.placedBets);
            } catch (InterruptedException e) {
                log.severe(String.format("Error with bets sent to %s.", placeBetsRunnable.site.getName()));
                e.printStackTrace();

                ArrayList<PlacedBet> failedbets = new ArrayList<>();

                while (failedbets.size() < placeBetsRunnable.betPlans.size()){
                    BetPlan betPlan = placeBetsRunnable.betPlans.get(failedbets.size());
                    PlacedBet generic_failbet = null;
                    failedbets.add(generic_failbet);
                }
                placedBets.addAll(failedbets);
            }
        }

        return placedBets;
    }

    public static class PlaceBetsRunnable implements Runnable{

        public List<BetPlan> betPlans;
        public List<PlacedBet> placedBets;
        public BettingSite site;
        public Thread thread;

        public PlaceBetsRunnable(List<BetPlan> betPlans){
            this.betPlans = betPlans;
            site = this.betPlans.get(0).getSite();
            thread = new Thread(this);
            thread.setName(String.format("%s-BetPlacer", site.getName()));
        }

        public void start(){
            thread.start();
        }

        @Override
        public void run() {
            try {
                placedBets = site.placeBets(betPlans, config.ODDS_RATIO_BUFFER);
            } catch (IOException | URISyntaxException e) {
                log.severe(String.format("Error while sending bets off to %s", site.getName()));
                e.printStackTrace();
                placedBets = new ArrayList<>();
                while (placedBets.size() < betPlans.size()){
                    placedBets.add(null);
                }
            }
        }
    }



    public static void main(String[] args){


    }


}
