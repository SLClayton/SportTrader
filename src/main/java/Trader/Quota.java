package Trader;

import Bet.Bet;
import Bet.FootballBet.FootballBet;
import Bet.FootballBet.FootballBetGenerator;
import Bet.MarketOddsReport;
import SiteConnectors.Betfair.Betfair;
import SiteConnectors.BettingSite;
import SiteConnectors.Matchbook.Matchbook;
import SiteConnectors.SiteEventTracker;
import SiteConnectors.Smarkets.Smarkets;
import Sport.FootballMatch;
import Sport.Match;
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
import java.util.Collection;
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
        sites.add(new Matchbook());
        sites.add(new Smarkets());

        // Get football matches
        List<FootballMatch> matches = sites.get(0).getFootballMatches(Instant.now(), Instant.now().plusSeconds(60*60*24*7));
        print(String.format("Found %s matches.", matches.size()));

        // Setup event trackers for sites in list, minimum one is required that isn't betfair.
        FootballMatch match = null;
        SiteEventTracker bf_tracker = betfair.getEventTracker();
        List<SiteEventTracker> siteEventTrackers = new ArrayList<>();
        while (siteEventTrackers.size() == 0){

            // Check any matches are left
            if (matches.size() == 0){
                print("No more matches. All failed to match at least 1.");
                return;
            }

            // Take match from start of list
            match = matches.remove(0);
            print("Trying to use match " + match.toString());

            // Setup match in each event tracker, adding to list if successful.
            for (BettingSite site: sites){
                SiteEventTracker siteEventTracker = site.getEventTracker();
                if (siteEventTracker.setupMatch(match)){
                    siteEventTrackers.add(siteEventTracker);
                }
            }
        }

        bf_tracker.setupMatch(match);
        print(String.format("Setup match %s in betfair and %s other sites.", match, siteEventTrackers.size()));


        FootballBetGenerator fbbg = new FootballBetGenerator();
        List<BetGroup> tautologies = fbbg.getAllTautologies();
        List<Bet> bets = (List<Bet>) (Object) fbbg.getAllBets();
        print("Generated bets.");


        List<MarketOddsReport> other_mors = new ArrayList<>();
        for (SiteEventTracker siteEventTracker: siteEventTrackers){
            MarketOddsReport mor = siteEventTracker.getMarketOddsReport(bets);
            if (mor.noError()){
                other_mors.add(mor);
            }
            else{
                print("Error getting mor for " + siteEventTracker.site.getName());
            }
        }

        if (other_mors.size() == 0){
            print("No other MOR generated");
            return;
        }


        // Get MOR for all
        other_mors.add(bf_tracker.getMarketOddsReport(bets));
        MarketOddsReport MOR = MarketOddsReport.combine(other_mors);


        // Generate profit reports for each tautology.
        ProfitReportSet tautologyProfitReports = ProfitReportSet.getTautologyProfitReports(tautologies, MOR);
        tautologyProfitReports.sort_by_profit();


        // Filter all reports that contains betfair and has >1 site
        List<ProfitReport> valid_profitReports = new ArrayList<>();
        for (ProfitReport pr: tautologyProfitReports.profitReports){
            Set<BettingSite> sites_used = pr.sitesUsed();
            if (sites_used.contains(betfair) && sites_used.size() > 1){
                valid_profitReports.add(pr);
            }
        }

        if (valid_profitReports.size() == 0){
            print("No matching profit report found that has bf bet and >1 other site bet.");
            return;
        }


        ProfitReport profitReport = null;
        for (ProfitReport ratio_pr: valid_profitReports){
            ProfitReport min_pr = ratio_pr.newProfitReportReturn(ratio_pr.ret_from_min_stake);
            ProfitReport max_pr = ratio_pr.newProfitReportReturn(ratio_pr.ret_from_max_stake);

            if ((min_pr.compareTo(max_pr) == -1) &&
                    (min_pr.total_investment.compareTo(new BigDecimal("10.00")) == -1)){
                profitReport = min_pr;
                break;
            }
        }
        if (valid_profitReports.size() == 0){
            print("No matching profit report found that has min_pr under max_pr and 10.00");
            return;
        }

        pp(profitReport.toJSON(true));


        List<PlacedBet> placedBets = new ArrayList<>();
        for (BetOrder bo: profitReport.betOrders){
            PlacedBet placedBet = bo.site().placeBet(bo, new BigDecimal("0.9"));
            placedBets.add(placedBet);
        }

        PlacedProfitReport placedProfitReport = new PlacedProfitReport(placedBets, profitReport);

        pp(placedProfitReport.toJSON(true));
    }




    public static void main(String[] args){

        try {

            SportsTrader sportsTrader = new SportsTrader();

            betfairBet();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
