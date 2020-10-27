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
import tools.BigDecimalTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.*;

import static java.lang.System.exit;
import static tools.BigDecimalTools.BD;
import static tools.BigDecimalTools.penny;
import static tools.printer.*;

public class Quota {

    static BigDecimal max_inv = new BigDecimal("20.00");

    public Quota(){}


    public static void betfairBet() throws IOException, CertificateException, NoSuchAlgorithmException,
            UnrecoverableKeyException, InterruptedException, URISyntaxException, ParseException,
            KeyStoreException, KeyManagementException {

        // Creates any bet in betfair and cashes out with best odds somewhere else.
        // This is to make sure betfair sees bets on this account.

        // Create site objects
        Betfair betfair = new Betfair();
        List<BettingSite> sites = new ArrayList<>();
        //sites.add(new Matchbook());
        Smarkets sm = new Smarkets();
        sites.add(sm);

        // Get football matches
        List<FootballMatch> matches = sm.getFootballMatches(Instant.now(), Instant.now().plusSeconds(60*60*24*7));
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
        print(String.format("Generated %s bets.", bets.size()));


        // Collect Market Odds Report for each site and combine
        List<MarketOddsReport> mors = new ArrayList<>();
        for (SiteEventTracker siteEventTracker: siteEventTrackers){
            MarketOddsReport mor = siteEventTracker.getMarketOddsReport(bets);
            if (mor.noError()){
                print(String.format("Found MOR from %s with %s bets with offers.",
                        siteEventTracker.site.getName(), mor.bets_size()));
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
        MarketOddsReport MOR = MarketOddsReport.combine(mors);
        print(String.format("Combined %s MORs into one with %s bets with offers.",
                mors.size(), MOR.number_bets_with_offers()));


        toFile(MOR.toJSON());

        // Find the profit reports whereby the return is a fiver and order by best

        ProfitReportSet profitReportSet = MOR.getTautologyProfitReportSet_targetReturn(
                tautologies, BD(5), true);
        profitReportSet.sort_by_profit();

        List<PlacedBet> placedBets = null;
        Instant time = null;
        for (ProfitReport pr: profitReportSet.profitReports()){
            if (pr.uses_site(Betfair.name)){
                print("Attempting to place bets...");
                time = Instant.now();
                placedBets = pr.placeBets();
                break;
            }
        }


        if (placedBets == null){
            print("No valid bets found");
        }



        ProfitReport final_pr = ProfitReport.fromPlacedBets(placedBets);

        String output_dir = "quota_placed_bets";
        makeDirIfNotExists(output_dir);
        final_pr.saveJSON(time, output_dir);

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
