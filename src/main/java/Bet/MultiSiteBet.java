package Bet;

import SiteConnectors.BettingSite;
import Sport.Event;
import Trader.EventTrader;
import Trader.SportsTrader;
import ch.qos.logback.core.db.BindDataSourceToJNDIAction;
import org.apache.commons.collections.map.HashedMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import tools.printer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.logging.Logger;

import static tools.BigDecimalTools.combine_map;
import static tools.printer.*;

public class MultiSiteBet {

    // A set of bet plans or placedBets of the same event/bet over multiple sites.
    // so they all either fail or succeed together
    // Could be a set of size 1.

    private static final Logger log = SportsTrader.log;

    private final Map<BettingSite, SiteBet> site_bets;

    private Event event;
    private Bet bet;
    private Class item_class;

    private BigDecimal total_back_stake;
    private BigDecimal total_investment;
    private BigDecimal total_return;


    public MultiSiteBet(){
        site_bets = new HashMap<BettingSite, SiteBet>();
        total_investment = BigDecimal.ZERO;
        total_return = BigDecimal.ZERO;
        total_back_stake = BigDecimal.ZERO;
    }

    public void add(SiteBet siteBet){
        if (site_bets.isEmpty()){
            event = siteBet.getEvent();
            bet = siteBet.getBet();
            item_class = siteBet.getClass();
        }
        else if (!event.equals(siteBet.getEvent()) || !bet.equals(siteBet.getBet())){
            log.severe(String.format("Attempted to add betPlan of type %s/%s to MultiSiteBetPlan of %s/%s",
                    siteBet.getEvent(), siteBet.getBet(), event, bet));
            return;
        }

        site_bets.put(siteBet.getSite(), siteBet);
        total_investment = total_investment.add(siteBet.getInvestment());
        total_return = total_return.add(siteBet.getReturn());
        total_back_stake = total_back_stake.add(siteBet.getBackersStake());
    }

    public void addAll(Collection<BetPlan> items){
        for (SiteBet siteBet: items){
            add(siteBet);
        }
    }


    public static MultiSiteBet fromBetPlans(Collection<BetPlan> betPlans){
        MultiSiteBet multiSiteBet = new MultiSiteBet();
        multiSiteBet.addAll(betPlans);
        return multiSiteBet;
    }


    public int total_bets(){
        return site_bets.size();
    }


    public static MultiSiteBet fromSiteInvestments(Map<String, BetExchange> betExchangeMap,
                                                   Map<String, BigDecimal> site_investments){

        return MultiSiteBet.fromSiteInvestments(betExchangeMap.values(), site_investments);
    }


    public static MultiSiteBet fromSiteInvestments(Collection<BetExchange> betExchanges,
                                                   Map<String, BigDecimal> site_investments){

        MultiSiteBet multiSiteBet = new MultiSiteBet();
        for (BetExchange betExchange: betExchanges){
            BigDecimal site_inv = site_investments.get(betExchange.siteName());
            if (site_inv != null){
                BetPlan betPlan = BetPlan.fromTargetInvestment(betExchange, site_inv);

                if (betPlan == null){
                    log.severe(sf("Error creating MultiSiteBet, Betplan %s : %s is invalid - %s",
                            betExchange.siteName(), site_inv, site_investments));

                    print(betExchange);
                    return null;
                }
                multiSiteBet.add(betPlan);
            }
        }
        return multiSiteBet;
    }



    public static MultiSiteBet fromTargetReturn(Collection<BetExchange> betExchanges, BigDecimal target_return,
                                                boolean use_min_bets){

        // Returns the stake per site that would receive the target return with least investment

        Map<String, BetExchange> betExchangeMap = BetExchange.list2Map(betExchanges);

        // Get betOffers from all sites in order best to worst
        List<BetOffer> all_betOffers = BetExchange.getAllBetOffers(betExchanges);
        Collections.sort(all_betOffers, Collections.reverseOrder());


        // Get the bet amounts per site, ignoring any min bet requirements
        Map<String, BigDecimal> site_investments = BetOffer.target_return(all_betOffers, target_return, true);
        if (site_investments == null){
            return null;
        }


        MultiSiteBet multiSiteBet = MultiSiteBet.fromSiteInvestments(betExchangeMap, site_investments);



        // If we don't need min bets, just return this
        if (!use_min_bets){
            return multiSiteBet;
        }


        Map<String, BigDecimal> site_inv_reserved = new HashMap<String, BigDecimal>();
        while (true) {

            // From the current MSB, if there's any invalid betplans, find the worst.
            BetPlan worst_invalid_betPlan = multiSiteBet.worstInvalidBetplan();
            if (worst_invalid_betPlan == null) {
                break;
            }
            String site_name = worst_invalid_betPlan.getSiteName();


            List<BetOffer> betOffers_thisSiteRemoved = BetOffer.removeSite(all_betOffers, site_name);
            BetExchange betExchange = betExchangeMap.get(site_name);



            // Find MSB if that site was removed.
            Map<String, BigDecimal> site_inv_thisSiteRemoved =
                    BetOffer.target_return(betOffers_thisSiteRemoved, target_return, true);
            MultiSiteBet msb_thisSiteRemoved = null;
            if (site_inv_thisSiteRemoved != null){
                msb_thisSiteRemoved = MultiSiteBet.fromSiteInvestments(betExchangeMap, site_inv_thisSiteRemoved);
            }



            // Find MSB if that sites bet was raised to a min bet.
            BigDecimal min_inv = betExchange.minInvestment();
            BigDecimal min_return = betExchange.minReturn();
            BigDecimal reduced_target_return = null;
            if (min_return != null){
                reduced_target_return = target_return.subtract(min_return);
            }
            MultiSiteBet msb_thisSiteEnhanced = null;
            Map<String, BigDecimal> site_inv_thisSiteEnhanced = null;
            if (reduced_target_return != null && reduced_target_return.signum() >= 0){
                site_inv_thisSiteEnhanced = BetOffer.target_return(betOffers_thisSiteRemoved, reduced_target_return, true);
            }
            if (site_inv_thisSiteEnhanced != null){
                site_inv_thisSiteEnhanced.put(site_name, min_inv);
                msb_thisSiteEnhanced = MultiSiteBet.fromSiteInvestments(betExchangeMap, site_inv_thisSiteEnhanced);
            }




            // if both failed, result is impossible so return null
            if (msb_thisSiteRemoved == null && msb_thisSiteEnhanced == null) {
                return null;
            }

            // Site inv enhanced to minimum investment has better return
            else if (msb_thisSiteRemoved == null ||
                    (msb_thisSiteEnhanced != null && msb_thisSiteEnhanced.largerReturn(msb_thisSiteRemoved))) {


                site_inv_reserved.put(site_name, min_inv);
                site_inv_thisSiteEnhanced.remove(site_name);
                site_investments = site_inv_thisSiteEnhanced;

                target_return = target_return.subtract(min_return);
                all_betOffers = BetOffer.remove_stake(all_betOffers, site_name, betExchange.minBackStake());
            }

            // Removing site altogether has better return
            else {
                all_betOffers = betOffers_thisSiteRemoved;
                site_investments = site_inv_thisSiteRemoved;
            }

            // Create new MSB from what we have just computed and the reserved investments
            multiSiteBet = MultiSiteBet.fromSiteInvestments(betExchangeMap, combine_map(site_investments, site_inv_reserved));
        }

        return multiSiteBet;
    }


    public static MultiSiteBet fromTargetInvestment(Collection<BetExchange> betExchanges, BigDecimal target_inv){
        // Returns the investment per site that would receive the best return from the given exchanges

        Map<String, BetExchange> betExchangeMap = BetExchange.list2Map(betExchanges);

        // Get betOffers from all sites in order best to worst
        List<BetOffer> all_betOffers = BetExchange.getAllBetOffers(betExchanges);
        Collections.sort(all_betOffers, Collections.reverseOrder());

        // Get the bet amounts per site, ignoring any min bet requirements
        Map<String, BigDecimal> site_investments = BetOffer.apply_investment(all_betOffers, target_inv, true);
        if (site_investments == null){
            return null;
        }
        MultiSiteBet multiSiteBet = MultiSiteBet.fromSiteInvestments(betExchangeMap, site_investments);


        Map<String, BigDecimal> site_inv_reserved = new HashMap<String, BigDecimal>();
        while (true) {

            // From the current MSB, if there's any invalid betplans, find the worst.
            BetPlan worst_invalid_betPlan = multiSiteBet.worstInvalidBetplan();
            if (worst_invalid_betPlan == null) {
                break;
            }
            String site_name = worst_invalid_betPlan.getSiteName();


            List<BetOffer> betOffers_thisSiteRemoved = BetOffer.removeSite(all_betOffers, site_name);
            BetExchange betExchange = betExchangeMap.get(site_name);


            // Find MSB if that site was removed.
            Map<String, BigDecimal> site_inv_thisSiteRemoved =
                    BetOffer.apply_investment(betOffers_thisSiteRemoved, target_inv, true);
            MultiSiteBet msb_thisSiteRemoved = null;
            if (site_inv_thisSiteRemoved != null){
                msb_thisSiteRemoved = MultiSiteBet.fromSiteInvestments(betExchangeMap, site_inv_thisSiteRemoved);
            }


            // Find MSB if that sites bet was raised to a min bet.
            BigDecimal min_inv = betExchange.minInvestment();
            BigDecimal reduced_target_inv = target_inv.subtract(min_inv);
            Map<String, BigDecimal> site_inv_thisSiteEnhanced =
                    BetOffer.apply_investment(betOffers_thisSiteRemoved, reduced_target_inv, true);
            MultiSiteBet msb_thisSiteEnhanced = null;
            if (site_inv_thisSiteEnhanced != null){
                site_inv_thisSiteEnhanced.put(site_name, min_inv);
                msb_thisSiteEnhanced = MultiSiteBet.fromSiteInvestments(betExchangeMap, site_inv_thisSiteEnhanced);
            }



            // if both failed, result is impossible so return null
            if (msb_thisSiteRemoved == null && msb_thisSiteEnhanced == null) {
                return null;
            }

            // Site inv enhanced to minimum investment has better return
            else if (msb_thisSiteRemoved == null ||
                    (msb_thisSiteEnhanced != null && msb_thisSiteEnhanced.largerReturn(msb_thisSiteRemoved))) {

                site_inv_reserved.put(site_name, min_inv);
                site_inv_thisSiteEnhanced.remove(site_name);
                site_investments = site_inv_thisSiteEnhanced;

                target_inv = target_inv.subtract(min_inv);
                all_betOffers = BetOffer.remove_stake(all_betOffers, site_name, betExchange.minBackStake());
            }

            // Removing site altogether has better return
            else {
                all_betOffers = betOffers_thisSiteRemoved;
                site_investments = site_inv_thisSiteRemoved;
            }

            // Create new MSB from what we have just computed and the reserved investments
            multiSiteBet = MultiSiteBet.fromSiteInvestments(betExchangeMap, combine_map(site_investments, site_inv_reserved));
        }

        return multiSiteBet;
    }



    public BigDecimal avg_odds(){
        if (site_bets.isEmpty()){
            return null;
        }

        BigDecimal avg_odds = BigDecimal.ZERO;
        for (SiteBet siteBet: site_bets.values()){
            BigDecimal part_ratio = siteBet.getBackersStake().divide(total_back_stake, 12, RoundingMode.HALF_UP);
            BigDecimal odds_part = siteBet.avgOdds().multiply(part_ratio);
            avg_odds = avg_odds.add(odds_part);
        }
        return avg_odds;
    }


    public BigDecimal getInvestment() {
        return total_investment;
    }

    public BigDecimal getReturn() {
        return total_return;
    }

    public BigDecimal getROIRatio(){
        return getReturn().divide(getInvestment(), 12, RoundingMode.HALF_UP);
    }

    public Bet getBet() {
        return bet;
    }

    public Event getEvent() {
        return event;
    }

    public JSONObject toJSON() {
        JSONObject j = new JSONObject();
        j.put("event", stringValue(event));
        j.put("bet", bet.id());

        JSONArray siteBets_array = new JSONArray();
        for (SiteBet siteBet: site_bets.values()){
            JSONObject siteBet_json = siteBet.toJSON();
            siteBet_json.remove("event");
            siteBets_array.add(siteBet_json);
        }

        j.put("site_bets", siteBets_array);
        return j;
    }

    public Set<BettingSite> sites_used() {
        return site_bets.keySet();
    }

    public List<BetPlan> getBetPlans(){
        try{
            List<BetPlan> betPlans = new ArrayList<BetPlan>();
            for (SiteBet item: site_bets.values()){
                betPlans.add((BetPlan) item);
            }
            return betPlans;
        }
        catch (ClassCastException e){
            return null;
        }
    }

    public Map<String, BigDecimal> inv_per_site(){
        Map<String, BigDecimal> site_invs = new HashMap<>();
        for (SiteBet siteBet: site_bets.values()){
            site_invs.put(siteBet.getSite().getName(), siteBet.getInvestment());
        }
        return site_invs;
    }

    public List<BetOfferStake> getBetOfferStakes(){
        List<BetOfferStake> betOfferStakes = new ArrayList<BetOfferStake>();
        for (BetPlan betPlan: getBetPlans()){
            betOfferStakes.addAll(betPlan.getBetOfferStakes());
        }
        Collections.sort(betOfferStakes, Collections.reverseOrder());
        return betOfferStakes;
    }

    public void printBetOfferStakes(){
        for (BetOfferStake betOfferStake: getBetOfferStakes()){
            print(betOfferStake.toString());
        }
    }

    public List<PlacedBet> getPlacedBets(){
        try{
            List<PlacedBet> placedBets = new ArrayList<PlacedBet>();
            for (SiteBet item: site_bets.values()){
                placedBets.add((PlacedBet) item);
            }
            return placedBets;
        }
        catch (ClassCastException e){
            return null;
        }
    }


    public BetPlan worstInvalidBetplan(){
        try {
            BetPlan worst_betplan = null;
            for (SiteBet siteBet : site_bets.values()) {
                BetPlan betPlan = (BetPlan) siteBet;

                if (!betPlan.hasMinStake() &&
                        (worst_betplan == null || worst_betplan.betterROIRatio(betPlan))) {

                    worst_betplan = betPlan;
                }
            }
            return worst_betplan;
        }
        catch (ClassCastException e){
            log.severe("Attempted to run worstInvalidBetplan on a multisitebet of non betplans.");
            return null;
        }
    }


    public boolean largerReturn(MultiSiteBet multiSiteBet){
        return this.getReturn().compareTo(multiSiteBet.getReturn()) > 0;
    }




}
