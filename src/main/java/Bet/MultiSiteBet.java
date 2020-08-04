package Bet;

import Sport.Event;
import Trader.SportsTrader;
import ch.qos.logback.core.db.BindDataSourceToJNDIAction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import tools.printer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.logging.Logger;

import static tools.printer.print;

public class MultiSiteBet {

    // A set of bet plans or placedBets of the same event/bet over multiple sites.
    // so they all either fail or succeed together
    // Could be a set of size 1.

    private static final Logger log = SportsTrader.log;

    private final Map<String, SiteBet> site_bets;

    private Event event;
    private Bet bet;
    private Class item_class;

    private BigDecimal total_back_stake;
    private BigDecimal total_investment;
    private BigDecimal total_return;


    public MultiSiteBet(){
        site_bets = new HashMap<String, SiteBet>();
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

        site_bets.put(siteBet.getSite().getName(), siteBet);
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


    public static MultiSiteBet fromBetExchanges(Collection<BetExchange> betExchanges,
                                                Map<String, BigDecimal> site_investments){

        MultiSiteBet multiSiteBet = new MultiSiteBet();
        for (BetExchange betExchange: betExchanges){
            BigDecimal site_inv = site_investments.get(betExchange.siteName());
            if (site_inv != null){
                multiSiteBet.add(BetPlan.fromTargetInvestment(betExchange, site_inv));
            }
        }
        return multiSiteBet;
    }

    public static MultiSiteBet fromBetExchanges(Map<String, BetExchange> betExchangeMap,
                                                Map<String, BigDecimal> site_investments){

        MultiSiteBet multiSiteBet = new MultiSiteBet();
        for (String site_name: site_investments.keySet()){
            multiSiteBet.add(BetPlan.fromTargetInvestment(
                    betExchangeMap.get(site_name), site_investments.get(site_name)));
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
        JSONArray siteBets_array = new JSONArray();
        for (SiteBet siteBet: site_bets.values()){
            siteBets_array.add(siteBet.toJSON());
        }

        JSONObject j = new JSONObject();
        j.put("event", event.toString());
        j.put("bet", bet.id());
        j.put("bet_plans", siteBets_array);
        return j;
    }

    public Set<String> sites_used() {
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
