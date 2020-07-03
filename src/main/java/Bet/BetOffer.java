package Bet;

import Bet.*;
import Bet.Bet.BetType;
import Bet.FootballBet.FootballBet;
import Bet.FootballBet.FootballResultBet;
import SiteConnectors.Betfair.Betfair;
import SiteConnectors.BettingSite;
import SiteConnectors.Smarkets.Smarkets;
import Sport.Event;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.lang.management.BufferPoolMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

import static java.lang.System.exit;
import static java.lang.System.in;
import static tools.printer.*;

public class BetOffer implements Comparable<BetOffer> {

    static final BigDecimal penny = new BigDecimal("0.01");

    public final BettingSite site;
    public final Event event;
    public final Bet bet;
    public final BigDecimal odds;
    public final BigDecimal volume;
    private Map<String, String> metadata;
    public final Instant time_betOffer_creation;

    // Computed once and stored as values are final
    private BigDecimal roi_ratio;


    public BetOffer(BettingSite SITE, Event EVENT, Bet BET, BigDecimal ODDS, BigDecimal VOLUME){
        time_betOffer_creation = Instant.now();
        site = SITE;
        event = EVENT;
        bet = BET;
        odds = ODDS;
        volume = VOLUME;
        metadata = new HashMap<String, String>();
        roi_ratio = null;
    }

    public void updateMetadata(Map<String, String> metadata){
        for (Map.Entry<String, String> entry: metadata.entrySet()){
            this.metadata.put(entry.getKey(), entry.getValue());
        }
    }

    public void addMetadata(String key, String value){
        metadata.put(key, value);
    }

    public boolean hasMinVolumeNeeded(){
        return volume.compareTo(minStake()) >= 0;
    }


    public BigDecimal backStake2LayStake(BigDecimal back_stake){
        return Bet.backStake2LayStake(back_stake, odds);
    }

    public BigDecimal layStake2BackStake(BigDecimal lay_stake){
        return Bet.layStake2BackStake(lay_stake, odds);
    }


    public BigDecimal getOddsWithBuffer(BigDecimal buffer_ratio){
        BigDecimal odds_ratio;
        if (isBack()){
            odds_ratio = BigDecimal.ONE.subtract(buffer_ratio);
        }
        else{
            odds_ratio = BigDecimal.ONE.add(buffer_ratio);
        }
        return Bet.multiplyDecimalOdds(odds, odds_ratio);
    }


    public BigDecimal getValidOddsWithBuffer(BigDecimal buffer_ratio) {
        // Check originla odds are valid
        if (odds.compareTo(site.minValidOdds()) < 0 || odds.compareTo(site.maxValidOdds()) > 0) {
            log.severe("BetOffer offer odds not within site odds range.");
            return null;
        }

        BigDecimal buffered_odds = getOddsWithBuffer(buffer_ratio);


        RoundingMode roundingMode;
        if (isBack()){
            buffered_odds = BDMax(buffered_odds, site.minValidOdds());
            roundingMode = RoundingMode.DOWN;
        }
        else {
            buffered_odds = BDMin(buffered_odds, site.maxValidOdds());
            roundingMode = RoundingMode.UP;
        }

        return site.getValidOdds(buffered_odds, roundingMode);
    }


    @Override
    public String toString(){
        return toJSON().toString();
    }


    public JSONObject toJSON(){
        JSONObject m = new JSONObject();
        m.put("created", String.valueOf(time_betOffer_creation));
        m.put("event", String.valueOf(event));
        m.put("bet", String.valueOf(bet.id()));
        m.put("site", String.valueOf(site.getName()));
        m.put("odds", BDString(odds));
        m.put("volume", BDString(volume));
        m.put("roi_ratio", BDString(ROI_ratio()));
        m.put("metadata", String.valueOf(metadata));
        return m;
    }


    public static JSONArray list2JSON(List<BetOffer> betOfferList){
        JSONArray ja = new JSONArray();
        for (BetOffer betOffer: betOfferList){
            ja.add(betOffer.toJSON());
        }
        return ja;
    }


    public boolean isBack(){
        return bet.isBack();
    }


    public boolean isLay(){
        return bet.isLay();
    }


    public BigDecimal minBackersStake(){
        return site.minBackersStake();
    }


    public BigDecimal minStake(){
        // The minimum stake of money that can be placed in this offer

        BigDecimal min_stake;
        if (isBack()){
            min_stake = site.minBackersStake();
        }
        else if (isLay()){
            min_stake = site.minLayersStake(odds);
        }
        else{
            return null;
        }
        return min_stake.setScale(2, RoundingMode.UP);
    }


    public BigDecimal maxStake(){
        // The maximum stake of money that can be placed in this offer

        BigDecimal max_stake;
        if (isBack()){
            max_stake = volume;
        }
        else {
            max_stake = Bet.backStake2LayStake(volume, odds);
        }
        return max_stake.setScale(2, RoundingMode.DOWN);
    }


    public BigDecimal returnFromMinStake(){
        return ROI(minStake());
    }


    public BigDecimal returnFromMaxStake(){
        return ROI(maxStake());
    }


    public BigDecimal investment2Stake(BigDecimal investment){
        return Bet.investment2Stake(investment, site.lossCommissionRate());
    }


    public BigDecimal investment2BackStake(BigDecimal investment){
        if (isBack()){
            return investment2Stake(investment);
        }
        else if (isLay()){
            return layStake2BackStake(investment2Stake(investment));
        }
        return null;
    }


    public BigDecimal stake2Investment(BigDecimal stake){
        return Bet.stake2Investment(stake, site.lossCommissionRate());
    }


    public BigDecimal backStake2Investment(BigDecimal back_stake){
        if (isBack()){
            return stake2Investment(back_stake);
        }
        else if (isLay()){
            return stake2Investment(backStake2LayStake(back_stake));
        }
        return null;
    }


    public BigDecimal return2Investment(BigDecimal target_return){
        return target_return.divide(ROI_ratio(), 12, RoundingMode.HALF_UP);
    }


    public BigDecimal return2Stake(BigDecimal target_return){
        BigDecimal inv = return2Investment(target_return);
        return investment2Stake(inv);
    }

    public BigDecimal return2BackStake(BigDecimal target_return){
        if (isBack()){
            return return2Stake(target_return);
        }
        else if (isLay()){
            return layStake2BackStake(return2Stake(target_return));
        }
        return null;
    }



    public BigDecimal ROI_ratio(){
        // All values are final, so compute when needed once, and store value.
        if (roi_ratio == null){
            roi_ratio = site.ROI(bet.type, odds, BigDecimal.ONE);
        }
        return roi_ratio;
    }

    public BigDecimal ROI(BigDecimal investment){
        return ROI_ratio().multiply(investment);
    }


    public boolean largerROI(BetOffer betOffer){
        return this.ROI_ratio().compareTo(betOffer.ROI_ratio()) > 0;
    }


    @Override
    public int compareTo(BetOffer betOffer) {
        return this.ROI_ratio().compareTo(betOffer.ROI_ratio());
    }



    public static BetOffer getBestValidOffer_old(List<BetOffer> betOffers, String site_name){
        // Finds the best offer from a list, from a specific site if asked.

        BetOffer best_offer = null;
        for (BetOffer betOffer: betOffers){
            if (betOffer.hasMinVolumeNeeded() &&
                    (site_name == null || site_name.equals(betOffer.site.getName())) &&
                    (best_offer == null || betOffer.largerROI(best_offer))){

                best_offer = betOffer;
            }
        }
        return best_offer;
    }


    public static BigDecimal ROI(List<BetOffer> betOffers, BigDecimal target_investment){
        return ROI(betOffers, target_investment, false);
    }

    public static BigDecimal ROI(List<BetOffer> betOffers, BigDecimal target_investment, boolean is_list_sorted){

        // Finds the Return, if the target investment was placed on the series of betOffers filled as far as possible
        // NO MIN STAKE

        // Sort list by best offer to worst
        if (!is_list_sorted) {
            Collections.sort(betOffers, Collections.reverseOrder());
        }

        // Count down from target and up to return
        BigDecimal inv_remainder = target_investment;
        BigDecimal total_return = BigDecimal.ZERO;

        for (BetOffer betOffer: betOffers){

            // Find what back stake would be needed for the remaining investment
            BigDecimal back_stake_for_remainder = betOffer.investment2BackStake(inv_remainder);

            // Choose what back stake to use for this offer, either remaining amount, or the max volume of this offer
            BigDecimal back_stake_this_offer = BDMin(
                    back_stake_for_remainder.setScale(2, RoundingMode.HALF_UP),
                    betOffer.volume.setScale(2, RoundingMode.DOWN));

            // Calculate the investment and return resulting from this offer using the chosen back stake
            BigDecimal this_investment = betOffer.backStake2Investment(back_stake_this_offer);
            BigDecimal this_return = betOffer.ROI(this_investment);

            // Remove the inv we just made from target investment
            total_return = total_return.add(this_return);
            inv_remainder = inv_remainder.subtract(this_investment);

            // If less than 0.01 is remaining then complete
            if (inv_remainder.compareTo(penny) < 0){
                return total_return;
            }
        }
        // Unable to complete target investment with given offers
        return null;
    }


    public static BigDecimal getAvgROIRatio(List<BetOffer> betOffers, BigDecimal target_investment) {
        return getAvgROIRatio(betOffers, target_investment, false);
    }

    public static BigDecimal getAvgROIRatio(List<BetOffer> betOffers, BigDecimal target_investment,
                                             boolean is_list_sorted){

        BigDecimal returns = ROI(betOffers, target_investment, is_list_sorted);
        return returns.divide(target_investment, 12, RoundingMode.HALF_UP);
    }


    public static BigDecimal investmentForReturn(List<BetOffer> betOffers, BigDecimal target_return,
                                                 boolean is_list_sorted){

        // Finds what investment is needed to get a specific return on the series of betOffers filled as far as possible
        // NO MIN STAKE

        // Sort list by best offer to worst
        if (!is_list_sorted) {
            Collections.sort(betOffers, Collections.reverseOrder());
        }

        // Count down from remainder and sum up investment as we go
        BigDecimal return_remainder = target_return;
        BigDecimal total_investment = BigDecimal.ZERO;

        for (BetOffer betOffer: betOffers){

            // Find what back stake would be needed for the remaining return
            BigDecimal back_stake_for_remainder = betOffer.return2BackStake(return_remainder);

            // Choose what back stake to use for this offer, either remaining amount, or the max volume of this offer
            BigDecimal back_stake_this_offer = BDMin(
                    back_stake_for_remainder.setScale(2, RoundingMode.HALF_UP),
                    betOffer.volume.setScale(2, RoundingMode.DOWN));

            // Calculate the investment and return resulting from this offer using the chosen back stake
            BigDecimal this_investment = betOffer.backStake2Investment(back_stake_this_offer);
            BigDecimal this_return = betOffer.ROI(this_investment);

            // Remove the inv we just made from target investment
            total_investment = total_investment.add(this_investment);
            return_remainder = return_remainder.subtract(this_return);

            // If less than 0.01 is remaining then complete
            if (return_remainder.compareTo(penny) < 0){
                return total_investment;
            }
        }
        // Unable to complete target investment with given offers
        return null;
    }

    public static BigDecimal investmentForReturn(List<BetOffer> betOffers, BigDecimal target_return){
        return investmentForReturn(betOffers, target_return, false);
    }



    public static BigDecimal backStake2LayStake(List<BetOffer> betOffers, BigDecimal target_backers_stake, boolean is_list_sorted){
        // Finds the backersProfitLayersStake from the backersStakeLayersProfit
        if (!is_list_sorted) {
            Collections.sort(betOffers, Collections.reverseOrder());
        }
        BigDecimal back_stake_remainder = target_backers_stake;
        BigDecimal lay_stake_total = BigDecimal.ZERO;

        for (BetOffer betOffer: betOffers){
            // Back stake is full amount or the filled offer
            BigDecimal back_stake_this_offer = BDMin(back_stake_remainder, betOffer.volume);
            BigDecimal lay_stake_this_offer = betOffer.backStake2LayStake(back_stake_this_offer);

            // Remove the inv we just made from target investment
            lay_stake_total = lay_stake_total.add(lay_stake_this_offer);
            back_stake_remainder = back_stake_remainder.subtract(back_stake_this_offer);

            if (back_stake_remainder.signum() <= 0){
                return lay_stake_total;
            }
        }
        return null;
    }

    public static BigDecimal backStake2LayStake(List<BetOffer> betOffers, BigDecimal target_backers_stake) {
        return backStake2LayStake(betOffers, target_backers_stake, false);
    }



    public static void main(String[] args){
        try {

            Smarkets bf = new Smarkets();

            Bet bet = new FootballResultBet(BetType.BACK, FootballBet.TEAM_A, false);

            List<BetOffer> betOffers = new ArrayList<>();
            betOffers.add(new BetOffer(bf, null, bet, new BigDecimal("3.50"), new BigDecimal("11")));
            betOffers.add(new BetOffer(bf, null, bet, new BigDecimal("3.40"), new BigDecimal("11")));
            betOffers.add(new BetOffer(bf, null, bet, new BigDecimal("3.30"), new BigDecimal("11")));
            betOffers.add(new BetOffer(bf, null, bet, new BigDecimal("3.25"), new BigDecimal("11")));
            betOffers.add(new BetOffer(bf, null, bet, new BigDecimal("3.1"), new BigDecimal("10.00")));

            Collections.sort(betOffers, Collections.reverseOrder());
            for (BetOffer bo: betOffers){
                print(String.format("%s   -   %s   -   %s",
                        BDString(bo.odds), BDString(bo.ROI_ratio()), bo.volume));
            }


            BigDecimal input = new BigDecimal("44");
            BigDecimal ret = backStake2LayStake(betOffers, input);
            BigDecimal avg_odds = ret.divide(input, 12, RoundingMode.HALF_UP).add(BigDecimal.ONE);


            print(String.format("\nReturned %s", BDString(ret, 12)));
            print(String.format("\navg_odds %s", BDString(avg_odds, 12)));


        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
