package Bet;

import Bet.*;
import Bet.Bet.BetType;
import Bet.FootballBet.FootballBet;
import Bet.FootballBet.FootballResultBet;
import SiteConnectors.Betfair.Betfair;
import SiteConnectors.BettingSite;
import SiteConnectors.Smarkets.Smarkets;
import Sport.Event;
import org.apache.commons.collections.map.LinkedMap;
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
import static tools.BigDecimalTools.*;

public class BetOffer implements Comparable<BetOffer> {

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


    public BetOffer removeVolume(BigDecimal volume_reduction){
        // Returns and identical offer but with a certain amount of its volume removed
        BigDecimal new_vol = volume.subtract(volume_reduction);
        if (new_vol.signum() >= 0){
            BetOffer betOffer = new BetOffer(site, event, bet, odds, new_vol);
            betOffer.updateMetadata(metadata);
            return betOffer;
        }
        return null;
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

        String oddsString = padto(BDString(odds, 4), 10);
        String volString = padto(BDString(volume, 4), 10);
        String roiString = padto(BDString(ROI_ratio(), 4), 10);

        return String.format("[%s: odds: %s vol: %s roi: %s  %s %s]",
                site.getID(), oddsString, volString, roiString, event.toString(), bet.id());
    }


    public String getSiteName(){
        return site.getName();
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



    public BigDecimal profitAfterCommission(BigDecimal profit_b4_com){
        return Bet.profitAfterCommission(profit_b4_com, site.winCommissionRate());
    }


    public BigDecimal return2Investment(BigDecimal target_return){
        return site.return2Investment(bet.type, odds, target_return);
    }

    public BigDecimal return2Stake(BigDecimal target_return){
        return site.return2Stake(bet.type, odds, target_return);
    }

    public BigDecimal return2BackStake(BigDecimal target_return){
        return site.return2BackStake(bet.type, odds, target_return);
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


    public BetOfferStake getFullOfferStake(){
        return getOfferStake(volume);
    }

    public BetOfferStake getOfferStake(BigDecimal back_stake){
        return new BetOfferStake(this, back_stake);
    }


    public boolean largerROI(BetOffer betOffer){
        return this.ROI_ratio().compareTo(betOffer.ROI_ratio()) > 0;
    }


    @Override
    public int compareTo(BetOffer betOffer) {
        return this.ROI_ratio().compareTo(betOffer.ROI_ratio());
    }



    public static String list2String(List<BetOffer> betOffers){
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<betOffers.size(); i++){
            sb.append(betOffers.get(i).toString());
            if (i < betOffers.size()-1){
                sb.append("\n");
            }
        }
        return sb.toString();
    }


    public static void printList(List<BetOffer> betOffers){
        print(list2String(betOffers));
    }



    public static List<BetOffer> removeSite(List<BetOffer> betOffers, String site_name){
        List<BetOffer> new_betOffers = new ArrayList<BetOffer>();
        for (BetOffer betOffer: betOffers){
            if (!betOffer.getSiteName().equals(site_name)){
                new_betOffers.add(betOffer);
            }
        }
        return new_betOffers;
    }


    public static List<BetOfferStake> applyStake(List<BetOffer> betOffers, BigDecimal target_stake, boolean back_stake,
                                                 boolean is_list_sorted){

        // Applies a certain back stake to a list of offers to find what amount to place
        // to fill each, one after the next, until all stake gone.

        if (!is_list_sorted) {
            Collections.sort(betOffers, Collections.reverseOrder());
        }

        BigDecimal stake_remainder = target_stake;
        List<BetOfferStake> betOfferStakes = new ArrayList<BetOfferStake>();

        for (BetOffer betOffer: betOffers){

            BigDecimal back_stake_remainder;
            if (back_stake){
                back_stake_remainder = stake_remainder;
            }
            else{
                back_stake_remainder = betOffer.layStake2BackStake(stake_remainder);
            }

            // Back stake is full amount or the filled offer
            BigDecimal back_stake_this_offer = BDMin(back_stake_remainder, betOffer.volume);
            betOfferStakes.add(new BetOfferStake(betOffer, back_stake_this_offer));

            // Remove the back stake we just made from target back stake
            back_stake_remainder = back_stake_remainder.subtract(back_stake_this_offer);

            if (back_stake_remainder.signum() <= 0){
                return betOfferStakes;
            }
        }
        return null;
    }


    public static BigDecimal backStake2LayStake(List<BetOffer> betOffers, BigDecimal back_stake,
                                                boolean is_list_sorted){

        List<BetOfferStake> betOfferStakes = applyStake(betOffers, back_stake, true, is_list_sorted);
        if (betOfferStakes == null){
            return null;
        }
        return BetOfferStake.totalLayStake(betOfferStakes);
    }

    public static BigDecimal layStake2BackStake(List<BetOffer> betOffers, BigDecimal lay_stake,
                                                boolean is_list_sorted){

        List<BetOfferStake> betOfferStakes = applyStake(betOffers, lay_stake, false, is_list_sorted);
        if (betOfferStakes == null){
            return null;
        }
        return BetOfferStake.totalBackStake(betOfferStakes);
    }



    public static Map<String, BigDecimal> apply_investment(List<BetOffer> betOffers, BigDecimal target_investment,
                                                           boolean is_list_sorted){

        // Takes betOffers from multiple sites and shows the best amount to placed on each
        // site to get best return from given target investment

        if (target_investment.signum() < 0){
            return null;
        }

        if (!is_list_sorted) {
            Collections.sort(betOffers, Collections.reverseOrder());
        }

        BigDecimal inv_remainder = target_investment;
        Map<String, BigDecimal> site_investments = new HashMap<String, BigDecimal>();
        List<BetOfferStake> betOfferStakes = new ArrayList<BetOfferStake>();

        if (inv_remainder.compareTo(penny) <= 0){
            return site_investments;
        }

        for (BetOffer betOffer: betOffers){

            // Use either the back stake required to finish off remainder, or volume if that's smaller.
            BigDecimal back_stake_for_remainder = betOffer.investment2BackStake(inv_remainder);
            BigDecimal back_stake_to_use = BDMin(back_stake_for_remainder, betOffer.volume);
            BetOfferStake betOfferStake = new BetOfferStake(betOffer, back_stake_to_use);
            betOfferStakes.add(betOfferStake);


            BigDecimal investment_used = betOfferStake.investment();
            site_investments.put(betOffer.getSiteName(),
                    site_investments.getOrDefault(betOffer.getSiteName(), BigDecimal.ZERO).add(investment_used));
            inv_remainder = inv_remainder.subtract(investment_used);


            if (inv_remainder.compareTo(penny) <= 0){
                return site_investments;
            }
        }
        return null;
    }

    public static Map<String, BigDecimal> apply_investment(List<BetOffer> betOffers, BigDecimal target_investment){
        return apply_investment(betOffers, target_investment, false);
    }



    public static Map<String, BigDecimal> target_return(List<BetOffer> betOffers, BigDecimal target_return,
                                                  boolean is_list_sorted){

        // Takes betOffers from multiple sites and shows the best amount to placed on each
        // site to get the least inv from a given return

        if (!is_list_sorted) {
            Collections.sort(betOffers, Collections.reverseOrder());
        }


        BigDecimal return_remainder = target_return;
        BigDecimal inv_so_far = BigDecimal.ZERO;
        List<BetOfferStake> betOfferStakes = new ArrayList<BetOfferStake>();
        Map<String, BigDecimal> site_investments = new HashMap<String, BigDecimal>();

        for (BetOffer betOffer: betOffers){

            // How much back stake would be needed to satisfy remaining return target
            BigDecimal back_stake_remainder = betOffer.return2BackStake(return_remainder);

            // Just use max volume of this offer if it is smaller than required amount
            BigDecimal back_stake_this_offer = BDMin(back_stake_remainder, betOffer.volume);
            BetOfferStake betOfferStake = new BetOfferStake(betOffer, back_stake_this_offer);


            // Remove the back stake we just made from target back stake
            return_remainder = return_remainder.subtract(betOfferStake.returns());
            inv_so_far = inv_so_far.add(betOfferStake.investment());
            betOfferStakes.add(betOfferStake);


            site_investments.put(betOffer.getSiteName(),
                    site_investments.getOrDefault(betOffer.getSiteName(), BigDecimal.ZERO).add(betOfferStake.investment()));

            if (return_remainder.compareTo(penny) <= 0){
                return site_investments;
            }
        }
        return null;
    }

    public static Map<String, BigDecimal> target_return(List<BetOffer> betOffers, BigDecimal target_return){
        return target_return(betOffers, target_return, false);
    }


    public static BigDecimal max_stake_for_specific_odds(List<BetOffer> betOffers, BigDecimal target_odds,
                                                         boolean is_list_sorted){

        // Given a list of betOffers, find what the max stake you could place to receive the target odds.

        if (!is_list_sorted) {
            Collections.sort(betOffers, Collections.reverseOrder());
        }

        BigDecimal stake_total = null;
        BigDecimal current_odds = null;

        for (BetOffer betOffer: betOffers) {

            // If odds in this offer are worse than target, it might end here
            if (betOffer.odds.compareTo(target_odds) < 0) {

                // If first offer is below target odds, then we can't make the target work
                if (stake_total == null){
                    return null;
                }

                BigDecimal stake_needed_for_max_odds = Bet.new_stake_needed_for_odds(
                        stake_total, current_odds, betOffer.odds, target_odds);

                if (stake_needed_for_max_odds.compareTo(betOffer.volume) <= 0){
                    stake_total = stake_total.add(stake_needed_for_max_odds);
                    return stake_total;
                }
            }

            // Apply the full volume of this offer to the running values
            if (stake_total == null){
                current_odds = betOffer.odds;
                stake_total = betOffer.volume;
            }
            else{
                current_odds = Bet.combine_odds(stake_total, current_odds, betOffer.volume, betOffer.odds);
                stake_total = stake_total.add(betOffer.volume);
            }
        }

        return null;
    }



    public static List<BetOffer> remove_stake(List<BetOffer> betOffers, Map<String, BigDecimal> site_stakes){
        // Gives a new list of betOffers which represents what it would look like if
        // the stakes given were removed from it.

        List<BetOffer> new_betOffers = new ArrayList<BetOffer>(betOffers.size());
        Map<String, BigDecimal> stakes = new HashMap<String, BigDecimal>(site_stakes.size());
        stakes.putAll(site_stakes);

        for (BetOffer betOffer: betOffers){

            BigDecimal stake_rem = stakes.getOrDefault(betOffer.getSiteName(), BigDecimal.ZERO);

            // No stake rem for this site, pass whole offer, no change stake remaining
            if (stake_rem.signum() == 0){
                new_betOffers.add(betOffer);
            }

            // Stake rem < vol of offer, pass reduced offer, change rem to 0
            else if (stake_rem.compareTo(betOffer.volume) < 0){
                new_betOffers.add(betOffer.removeVolume(stake_rem));
                stakes.put(betOffer.getSiteName(), BigDecimal.ZERO);
            }

            // Stake rem >= vol of offer, pass no offer, reduce change rem by vol amount
            else {
                stakes.put(betOffer.getSiteName(), stake_rem.subtract(betOffer.volume));
            }
        }

        // Ensure all stakes are completed in full
        for (BigDecimal stake_rem: stakes.values()){
            if (stake_rem.signum() != 0){
                return null;
            }
        }

        return new_betOffers;
    }

    public static List<BetOffer> remove_stake(List<BetOffer> betOffers, String site, BigDecimal back_stake){
        Map<String , BigDecimal> site_stake = new HashMap<String , BigDecimal>(1);
        site_stake.put(site, back_stake);
        return remove_stake(betOffers, site_stake);
    }

}
