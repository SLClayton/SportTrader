package Bet;

import Bet.FootballBet.FootballBet;
import Bet.FootballBet.FootballBetGenerator;
import SiteConnectors.Betfair.Betfair;
import SiteConnectors.BettingSite;
import SiteConnectors.SiteEventTracker;
import SiteConnectors.Smarkets.Smarkets;
import Sport.Event;
import Sport.FootballMatch;
import Trader.SportsTrader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.ProviderNotFoundException;
import java.util.*;
import java.util.logging.Logger;

import static java.lang.System.exit;
import static tools.printer.*;
import static tools.BigDecimalTools.*;

public class MarketOddsReport {
    /*
        BetExchanges, made up of BetExchanges, sorted by Bet->Site->Exchanges in map

        eg.
        SCORE_1-1_BACK
                Betfair
                    odds: 2.34   vol: 10.55
                    odds: 2.32   vol  15.22
                    odds: 2.28   vol: 7.36
                Matchbook
                    odds: 2.32   vol: 20.73
                    odds: 2.30   vol: 34.12
                Smarkets
                    odds: 2.33   vol: 23.13

        RESULT_TEAM-B_BACK
                Betfair
                    odds: 2.34   vol: 10.55
                    odds: 2.32   vol  15.22
                    odds: 2.28   vol: 7.36
                Smarkets
                    odds: 2.33   vol: 23.13
                    odds: 2.28   vol: 7.36
     */

    public static final Logger log = SportsTrader.log;

    public final Event event;
    private Map<String, Map<String, BetExchange>> bet_site_exchanges;
    private final Set<String> sites_used;

    public ErrorType errorType;
    public String errorMessage;


    public enum ErrorType {NONE, TIMED_OUT, RATE_LIMITED, GENERIC}


    public MarketOddsReport(Event event, ErrorType errorType){
        this.event = event;
        this.errorType = errorType;
        sites_used = new HashSet<>(5);
        if (noError()){
            bet_site_exchanges = new HashMap<String, Map<String, BetExchange>>();
        }
    }

    public MarketOddsReport(Event event){
        this(event, ErrorType.NONE);
    }

    public MarketOddsReport(ErrorType errorType){
        this(null, errorType);
    }



    public boolean timed_out(){
        return errorType.equals(ErrorType.TIMED_OUT);
    }


    public boolean unknown_error(){
        return errorType.equals(ErrorType.GENERIC);
    }


    public boolean rate_limited(){
        return errorType.equals(ErrorType.RATE_LIMITED);
    }



    public static MarketOddsReport TIMED_OUT(){
        return new MarketOddsReport(ErrorType.TIMED_OUT);
    }


    public static MarketOddsReport ERROR(String msg){
        MarketOddsReport mor = new MarketOddsReport(ErrorType.GENERIC);
        mor.errorMessage = msg;
        return mor;
    }


    public String getErrorMessage(){
        if (errorType.equals(ErrorType.GENERIC)){
            return String.format("%s: %s", errorType.toString(), String.valueOf(errorMessage));
        }
        return errorType.toString();
    }


    public static MarketOddsReport RATE_LIMITED(){
        return new MarketOddsReport(ErrorType.RATE_LIMITED);
    }


    public boolean noError(){
        return errorType.equals(ErrorType.NONE);
    }



    public void addBetOffer(BetOffer betOffer){
        BetExchange betExchange = bet_site_exchanges
                .computeIfAbsent(betOffer.bet.id(), k->new HashMap<String, BetExchange>())
                .computeIfAbsent(betOffer.site.getName(), k->new BetExchange(betOffer.site, betOffer.event, betOffer.bet));

        betExchange.add(betOffer);
        sites_used.add(betOffer.site.getName());
    }


    public void addExchange(BetExchange betExchange){
        bet_site_exchanges.computeIfAbsent(betExchange.bet.id(), k -> new HashMap<String, BetExchange>())
                .put(betExchange.site.getName(), betExchange);
    }



    public BigDecimal maxROIRatio(Bet bet){
        // returns the ROI ratio from the best single offer for
        // a bet from any of the sites that offer that bet.

        Collection<BetExchange> betExchanges = getBetExchanges(bet.id());
        BigDecimal best_roi = null;
        for (BetExchange betExchange: betExchanges){
            best_roi = BDMax(best_roi, betExchange.bestROIRatio());
        }
        return best_roi;
    }


    public List<BetGroup> filterPositiveTauts(List<BetGroup> tautologies){
        // Returns a list of tauts from those given, that would create a positive
        // ROI ratio if used at their maximum odds only.

        List<BetGroup> positive_tauts = new ArrayList<BetGroup>();
        Map<String, BigDecimal> inv_cache = new HashMap<String, BigDecimal>();

        for (BetGroup taut: tautologies){

            BigDecimal total_inv = BigDecimal.ZERO;
            for (Bet bet: taut.bets){

                BigDecimal inv_needed_for_penny_ret = inv_cache.computeIfAbsent(bet.id(),
                        k->penny.divide(maxROIRatio(bet), 12, RoundingMode.HALF_UP));
                total_inv = total_inv.add(inv_needed_for_penny_ret);
            }

            BigDecimal taut_roir = penny.divide(total_inv, 12, RoundingMode.HALF_UP);
            if (taut_roir.compareTo(BigDecimal.ONE) > 0){
                positive_tauts.add(taut);
            }
        }

        return positive_tauts;
    }




    public Collection<BetExchange> getBetExchanges(String bet_id){
        try{
            return bet_site_exchanges.get(bet_id).values();
        }
        catch (NullPointerException e){
            return null;
        }
    }



    public int bets_size(){
        return bet_site_exchanges.size();
    }


    public int number_bets_with_offers(){
        int n = 0;
        betsloop:
        for (Map<String, BetExchange> site_exchanges: bet_site_exchanges.values()){
            for (BetExchange betExchange: site_exchanges.values()){
                if (!betExchange.isEmpty()){
                    n++;
                    continue betsloop;
                }
            }
        }
        return n;
    }


    public boolean contains_bet(String bet_id){
        return bet_site_exchanges.containsKey(bet_id);
    }


    public static MarketOddsReport combine(Collection<MarketOddsReport> marketOddsReports){
        /*
        Combines the exchanges that make up Market odds reports, into a single market odds report.
        Does not allow multiple exchanges of the same site/bet to be combined as this makes no real sense.
         */

        if (marketOddsReports.isEmpty()){
            log.severe("Attemping to combine a list o 0 Market Odds Reports, returning null");
            return null;
        }

        MarketOddsReport combined = null;
        for (MarketOddsReport mor: marketOddsReports){

            // Initialise the combined MOR with event of first MOR
            if (combined == null){
                combined = new MarketOddsReport(mor.event);
            }
            // Check subsequent MORs are of the same event
            else if (!mor.event.equals(combined.event)){
                log.severe(String.format("Attemping to combine market odds reports of different event types %s and %s",
                        combined.event, mor.event));
                return null;
            }


            for (String bet_id: mor.bet_site_exchanges.keySet()){

                // The Site Exchanges to be adding
                Map<String, BetExchange> adding_site_exchanges = mor.bet_site_exchanges.get(bet_id);

                // The site exchanges to be adding to, create new map if not present
                Map<String, BetExchange> combined_site_exchanges = combined.bet_site_exchanges
                        .computeIfAbsent(bet_id, k -> new HashMap<String, BetExchange>());

                // Add each new exchange from the adding MOR to the combined
                for (BetExchange betExchange: adding_site_exchanges.values()){

                    // Check this bet does not contain an exchange from the same site already
                    if (combined_site_exchanges.containsKey(betExchange.site.getName())){
                        log.severe("Attempting to combine Market Odds Reports which have odds from the same site.");
                        return null;
                    }

                    combined_site_exchanges.put(betExchange.site.getName(), betExchange);
                }
            }
        }
        return combined;
}


    public JSONObject toJSON(boolean full_offers){
        JSONObject j = new JSONObject();

        if (!noError()){
            j.put("error_type", errorType.toString());
            if (errorMessage != null){
                j.put("error_message", String.valueOf(errorMessage));
            }
            return j;
        }


        JSONObject bet_offers = new JSONObject();
        for (Map.Entry<String, Map<String, BetExchange>> entry: bet_site_exchanges.entrySet()){
            String bet_id = entry.getKey();
            Map<String, BetExchange> site_exchanges = entry.getValue();

            JSONObject sites = new JSONObject();
            for (BetExchange betExchange: site_exchanges.values()){
                sites.put(betExchange.site, betExchange.toJSON().get("offers"));
            }

            bet_offers.put(bet_id, sites);
        }


        j.put("offers", bet_offers);
        j.put("event", stringValue(event));
        j.put("bets", bets_size());
        j.put("bets_with_offers", number_bets_with_offers());
        return j;
    }


    public JSONObject toJSON(){
        return toJSON(true);
    }

    @Override
    public String toString() {
        return String.format("[MOR with %s bets from sites %s]", bets_size(), sites_used.toString());
    }


    public MultiSiteBet getMultiSiteBet_targetReturn(String bet_id, BigDecimal target_return, boolean use_min_bets){
        // Gets the best (least inv)) Multi-site bet available from the market odds report given a bet and a target return

        Collection<BetExchange> betExchanges = getBetExchanges(bet_id);
        if (betExchanges == null){
            return null;
        }
        return MultiSiteBet.fromTargetReturn(betExchanges, target_return, use_min_bets);
    }


    public ProfitReport getTautologyProfitReport_targetReturn(BetGroup tautology, BigDecimal target_return,
                                                              boolean use_min_bets){

        // Finds the profit report of a tautology of bets for this MOR and target return

        List<MultiSiteBet> multiSiteBets = new ArrayList<>(tautology.size());

        for (Bet bet: tautology.bets){

            MultiSiteBet msb = getMultiSiteBet_targetReturn(bet.id(), target_return, use_min_bets);

            // If any of the bets in taut are invalid, then taut is invalid
            if (msb == null){
                return null;
            }

            multiSiteBets.add(msb);
        }

        return new ProfitReport(multiSiteBets);
    }

    public ProfitReportSet getTautologyProfitReportSet_targetReturn(Collection<BetGroup> tautologies,
                                                                 BigDecimal target_return, boolean use_min_bets){

        ProfitReportSet profitReportSet = new ProfitReportSet();

        for (BetGroup tautology: tautologies){
            ProfitReport pr = getTautologyProfitReport_targetReturn(tautology, target_return, use_min_bets);
            if (pr != null) {
                profitReportSet.add(pr);
            }
        }

        return profitReportSet;
    }



    public static void main(String[] args){
        try{

            FootballMatch fm = FootballMatch.parse("2020-06-30T19:15:00Z", "Brighton v man utd");



            List<Bet> bets = FootballBetGenerator._getAllBets();

            Betfair bf = new Betfair();
            SiteEventTracker bft = bf.getEventTracker();
            bft.setupMatch(fm);
            MarketOddsReport bmor = bft.getMarketOddsReport(bets);

            Smarkets sm = new Smarkets();
            SiteEventTracker smt = sm.getEventTracker();
            smt.setupMatch(fm);
            MarketOddsReport smor = smt.getMarketOddsReport(bets);

            List<MarketOddsReport> MORS = Arrays.asList(new MarketOddsReport[]{bmor, smor});
            MarketOddsReport MOR = MarketOddsReport.combine(MORS);
            toFile(MOR.toJSON());


        }
        catch (Exception e){
            e.printStackTrace();
        }


    }
}
