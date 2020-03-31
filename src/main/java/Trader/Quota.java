package Trader;

import Bet.Bet;
import Bet.FootballBet.FootballBetGenerator;
import Bet.MarketOddsReport;
import SiteConnectors.Betfair.Betfair;
import SiteConnectors.BettingSite;
import SiteConnectors.Matchbook.Matchbook;
import SiteConnectors.SiteEventTracker;
import SiteConnectors.Smarkets.Smarkets;
import Sport.FootballMatch;
import Bet.*;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static tools.printer.pp;
import static tools.printer.print;

public class Quota {

    public Quota(){}


    public static void betfairBet() throws IOException, CertificateException, NoSuchAlgorithmException,
            UnrecoverableKeyException, InterruptedException, URISyntaxException, ParseException,
            KeyStoreException, KeyManagementException {

        // Creates any bet in betfair and cashes out with best odds somewhere else.
        // This is to make sure betfair sees bets on this account.

        // Create site objects
        Betfair betfair = new Betfair();
        List<BettingSite> sites = new ArrayList<>();
        Matchbook mb = new Matchbook();
        Smarkets sm = new Smarkets();
        sites.add(mb);
        sites.add(sm);

        // Get football matches
        List<FootballMatch> matches = mb.getFootballMatches(Instant.now(), Instant.now().plusSeconds(60*60*24*7));
        print(String.format("Found %s matches.", matches.size()));


        // Try setting up a event from list of matches. Must successfully setup in
        // betfair and at least 1 other.
        List<SiteEventTracker> siteEventTrackers = new ArrayList<>();
        FootballMatch match = null;
        for (FootballMatch potential_match: matches){
            print(String.format("Attempting to use event %s", potential_match));

            // Clear event trackers
            siteEventTrackers.clear();

            // Try setup with betfair
            SiteEventTracker bf_tracker = betfair.getEventTracker();
            if (bf_tracker.setupMatch(potential_match)){
                print("Successfully setup in betfair.");
                siteEventTrackers.add(bf_tracker);
            }
            else{
                // Restart loop with different event
                print("Failed setup setup in betfair.");
                continue;
            }

            // Try adding all other sites in
            for (BettingSite site: sites){
                print(String.format("Attempting setup in %s.", site.getName()));

                SiteEventTracker siteEventTracker = site.getEventTracker();
                if (siteEventTracker.setupMatch(potential_match)){
                    print(String.format("Successfully setup in %s.", site.getName()));
                    siteEventTrackers.add(siteEventTracker);
                }
                else{
                    print(String.format("Failed setup in %s.", site.getName()));
                }
            }


            if (siteEventTrackers.size() >= 2){
                print(String.format("Successfully setup in %s sites inc betfair.",
                        siteEventTrackers.size()));
                match = potential_match;
                break;
            }
            print(String.format("Only setup in %s site/s. Trying next event.",
                    siteEventTrackers.size()));
        }

        if (match == null){
            print(String.format("Tried all %s matches and could not find event", matches.size()));
            return;
        }


        // Generate football bets
        FootballBetGenerator fbbg = new FootballBetGenerator();
        List<BetGroup> tautologies = fbbg.getAllTautologies();
        List<Bet> bets = (List<Bet>) (Object) fbbg.getAllBets();
        print("Generated bets.");


        List<MarketOddsReport> mors = new ArrayList<>();
        for (SiteEventTracker siteEventTracker: siteEventTrackers){
            MarketOddsReport mor = siteEventTracker.getMarketOddsReport(bets);
            if (mor.noError()){
                mors.add(mor);
            }
            else{
                print("Error getting mor for " + siteEventTracker.site.getName());
            }
        }

        if (mors.size() == 0){
            print("No other MOR generated");
            return;
        }


        // Get MOR for all
        MarketOddsReport MOR = MarketOddsReport.combine(mors);


        // Generate profit reports for each tautology.
        ProfitReportSet tautologyProfitReports = ProfitReportSet.getTautologyProfitReports(tautologies, MOR);
        tautologyProfitReports.sort_by_profit();


        // Filter all reports that contains betfair and has >1 site
        List<BetOrderProfitReport> valid_BetOrder_profitReports = new ArrayList<>();
        for (BetOrderProfitReport pr: tautologyProfitReports.betOrderProfitReports){
            Set<BettingSite> sites_used = pr.sitesUsed();
            if (sites_used.contains(betfair) && sites_used.size() > 1){
                valid_BetOrder_profitReports.add(pr);
            }
        }
        if (valid_BetOrder_profitReports.size() == 0){
            print("No matching profit report found that has bf bet and >1 other site bet.");
            return;
        }


        BetOrderProfitReport betOrderProfitReport = null;
        for (BetOrderProfitReport ratio_pr: valid_BetOrder_profitReports){
            BetOrderProfitReport min_pr = ratio_pr.newProfitReportReturn(ratio_pr.ret_from_min_stake);
            BetOrderProfitReport max_pr = ratio_pr.newProfitReportReturn(ratio_pr.ret_from_max_stake);

            if ((min_pr.compareTo(max_pr) == -1) &&
                    (min_pr.total_investment.compareTo(new BigDecimal("10.00")) == -1)){
                betOrderProfitReport = min_pr;
                break;
            }
        }
        if (valid_BetOrder_profitReports.size() == 0){
            print("No matching profit report found that has min_pr under max_pr and 10.00");
            return;
        }

        pp(betOrderProfitReport.toJSON(true));


        List<PlacedBet> placedBets = new ArrayList<>();
        for (BetOrder bo: betOrderProfitReport.betOrders){
            PlacedBet placedBet = bo.site().placeBet(bo, new BigDecimal("0.9"));
            placedBets.add(placedBet);
        }

        PlacedOrderProfitReport placedOrderProfitReport = new PlacedOrderProfitReport(placedBets, betOrderProfitReport);

        pp(placedOrderProfitReport.toJSON(true));
    }




    public static void main(String[] args){

        try {

            SportsTrader sportsTrader = new SportsTrader();

            betfairBet();

            sportsTrader.safe_exit();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
