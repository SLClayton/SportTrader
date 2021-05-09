package Trader;

import Bet.*;
import Bet.FootballBet.FootballBetGenerator;
import SiteConnectors.BettingSite;
import SiteConnectors.RequestHandler;
import SiteConnectors.SiteEventTracker;
import Sport.FootballMatch;
import tools.BigDecimalTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;

import static tools.BigDecimalTools.*;
import static tools.printer.*;

public class EventTrader implements Runnable {

    public static final Logger log = SportsTrader.log;
    private Config config = SportsTrader.config;

    boolean exit_flag;
    public Thread thread;
    public SportsTrader sportsTrader;

    public FootballMatch match;
    public Map<String, BettingSite> sites;
    public Map<String, SiteEventTracker> siteEventTrackers;

    public FootballBetGenerator footballBetGenerator;
    public Collection<Bet> bets;
    public Map<Integer, BetGroup> tautologies;

    public ArrayList<String> ok_site_oddsReports;
    public ArrayList<String> timeout_site_oddsReports;
    public ArrayList<String> rateLimited_site_oddsReports;
    public ArrayList<String> error_site_oddsReports;
    public BigDecimal last_best_profit;





    public EventTrader(SportsTrader sportsTrader, FootballMatch match, Map<String, BettingSite> sites, FootballBetGenerator footballBetGenerator){
        exit_flag = false;
        this.sportsTrader = sportsTrader;
        this.match = match;
        this.sites = sites;
        this.footballBetGenerator = footballBetGenerator;
        bets = (Collection<Bet>) (Object) footballBetGenerator.getAllBets();
        siteEventTrackers = new HashMap<>();

        // Build tautology map
        tautologies = new HashMap<Integer, BetGroup>();
        Instant milli_ago = Instant.now().minusMillis(1);
        for (BetGroup tautology: this.footballBetGenerator.getAllTautologies()){
            tautology.next_usage = milli_ago;
            tautologies.put(tautology.id, tautology);
        }

        ok_site_oddsReports = new ArrayList<>();
        timeout_site_oddsReports = new ArrayList<>();
        rateLimited_site_oddsReports = new ArrayList<>();
        error_site_oddsReports = new ArrayList<>();


    }


    public Integer setupMatch(){

        // Create lists for sites which fail and succeed setting up
        int total_sites = sites.size();
        ArrayList<String> failed_sites = new ArrayList<>();
        HashMap<String, BettingSite> accepted_sites = new HashMap<>();

        //Connect each site to event tracker
        for (Map.Entry<String, BettingSite> entry: sites.entrySet()){
            String site_name = entry.getKey();
            BettingSite site = entry.getValue();

            // Spawn an empty event tracker from the site object
            SiteEventTracker eventTracker = site.getEventTracker();

            // Try to setup event ion site event tracker, remove site if fail
            boolean setup_success = false;
            try {
                setup_success = eventTracker.setupMatch(match);
            } catch (IOException | URISyntaxException | InterruptedException e) {
                log.warning(e.toString());
                setup_success = false;
            }
            if (!(setup_success)){
                failed_sites.add(site_name);
                log.info(String.format("%s failed to setup in %s Event Tracker.", match, site_name));
                continue;
            }

            // Add successful sites and trackers into maps
            siteEventTrackers.put(site_name, eventTracker);
            accepted_sites.put(site_name, site);
            log.info(String.format("%s successfully setup in %s Event Tracker.", match, site_name));
        }

        sites = accepted_sites;
        log.info(String.format("%d/%d sites setup successfully for %s. Failures: %s",
                siteEventTrackers.size(), total_sites, match, failed_sites.toString()));


        // return number of sites setup.
        return siteEventTrackers.size();
    }



    @Override
    public void run() {
        log.info(String.format("Running Event Trader."));

        // Create new odds report worker for every
        for (int i=0; i<siteEventTrackers.size(); i++){
            sportsTrader.newMarketOddsReportWorker();
        }
        
        // Check for Arbs
        Instant wait_until = null;
        ArrayList<Long> loop_times = new ArrayList<>();
        Set<String> site_ids = BettingSite.getIDs(sites.values());
        BigDecimal best_profit = null;
        Instant last_check = Instant.now();
        Instant next_check = last_check.plusSeconds(config.PRINT_STATS_INTERVAL);
        for (long i=0; !exit_flag; i++){
            try {

                // RATE LIMITER: Sleeps until minimum wait period between calls is done.
                sleepUntil(wait_until, config.RATE_LOCKSTEP_INTERVAL);
                wait_until = Instant.now().plusMillis(config.RATE_LIMIT);

                // Check arbs and time how long it takes
                Instant start = Instant.now();
                checkArbs();
                Instant end = Instant.now();
                loop_times.add(Instant.now().toEpochMilli() - start.toEpochMilli());

                // Update best profit found in check interval
                best_profit = BDMax(last_best_profit, best_profit);

                // Calculate and print stats of last group of arb checks
                if (end.isAfter(next_check)){
                    log.info(sf("%s ArbChcks in %ss: avg=%dms OK%s TMT%s LMT%s NA%s Bst: %s",
                            loop_times.size(),
                            secs_since(last_check),
                            avg(loop_times),
                            sum_map(ok_site_oddsReports),
                            sum_map(timeout_site_oddsReports),
                            sum_map(rateLimited_site_oddsReports),
                            sum_map(error_site_oddsReports),
                            BDString(best_profit, 4)));

                    loop_times.clear();
                    ok_site_oddsReports.clear();
                    timeout_site_oddsReports.clear();
                    rateLimited_site_oddsReports.clear();
                    error_site_oddsReports.clear();
                    best_profit = null;
                    last_check = next_check;
                    next_check = Instant.now().plusSeconds(config.PRINT_STATS_INTERVAL);
                }


                // Sleep for a time depending on best profit found.
                if (config.LIMIT_LOW_PROFIT) {
                    try {
                        sleep(getSleepTime(last_best_profit));
                    } catch (InterruptedException e) {
                        log.warning("Event trader sleep time interrupted.");
                    }
                }


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public static long getSleepTime(BigDecimal profit_ratio){
        if (profit_ratio == null || profit_ratio.compareTo(new BigDecimal("-0.08")) == -1) {
            return 10000;
        } else if (profit_ratio.compareTo(new BigDecimal("-0.05")) == -1){
            return 12000;
        } else if (profit_ratio.compareTo(new BigDecimal("-0.03")) == -1){
            return 8000;
        } else if (profit_ratio.compareTo(new BigDecimal("-0.02")) == -1){
            return 6000;
        } else if (profit_ratio.compareTo(new BigDecimal("-0.01")) == -1){
            return 4000;
        } else if (profit_ratio.compareTo(new BigDecimal("-0.005")) == -1){
            return 2000;
        } else {
            return 0;
        }
    }



    public String id(){
        return match.name;
    }


    public void safe_exit(){
        exit_flag = true;
        for (SiteEventTracker siteEventTracker: siteEventTrackers.values()){
            siteEventTracker.safe_exit();
        }
    }


    private void checkArbs() throws InterruptedException {

        Instant now = Instant.now();

        // Collect every bet for the tautologies that are due for a test
        Set<Bet> bets_to_request = new HashSet<>();
        Set<Integer> used_tautology_ids = new HashSet<>();
        for (BetGroup tautology: tautologies.values()) {
            if (now.isAfter(tautology.next_usage)) {
                bets_to_request.addAll(tautology.getBets());
                used_tautology_ids.add(tautology.id);
            }
        }

        // Start threads to get Markets Odds Reports for this event for each site
        Map<SiteEventTracker, RequestHandler> requestHandlers = new HashMap<>();
        for (SiteEventTracker siteEventTracker: siteEventTrackers.values()){
            RequestHandler rh = sportsTrader.requestMarketOddsReport(siteEventTracker, bets_to_request);
            requestHandlers.put(siteEventTracker, rh);
        }

        // Wait for results to be generated in each thread and collect them all
        // Use null if time-out occurs for any site
        ArrayList<MarketOddsReport> marketOddsReports = new ArrayList<>();
        Instant timeout = now.plusMillis(config.REQUEST_TIMEOUT);
        for (Map.Entry<SiteEventTracker, RequestHandler> entry: requestHandlers.entrySet()){
            BettingSite site = entry.getKey().site;
            RequestHandler rh = entry.getValue();

            // Wait for each marketOddsReport or time-out
            MarketOddsReport mor;
            if (rh == null){
                mor = MarketOddsReport.ERROR("Request handler is null");
            }
            else if (Instant.now().isBefore(timeout)) {
                long millis_until_timeout = timeout.toEpochMilli() - Instant.now().toEpochMilli();
                mor = (MarketOddsReport) rh.pollReponse(millis_until_timeout);
            }
            else {
                mor = (MarketOddsReport) rh.pollReponse();
            }

            if (mor == null){
                // Use timeout MOR and cancel report worker.
                mor = MarketOddsReport.TIMED_OUT();
            }
            rh.finish();

            // Sort each marketOddsReport once received.
            if (mor.noError()) {
                marketOddsReports.add(mor);
                ok_site_oddsReports.add(site.getID());
            }
            else if (mor.rate_limited()){ rateLimited_site_oddsReports.add(site.getID()); }
            else if (mor.timed_out()){ timeout_site_oddsReports.add(site.getID()); }
            else {
                log.warning(String.format("Failed to get MarkerOddsReport from %s - %s",
                    site.getName(), mor.getErrorMessage()));
                error_site_oddsReports.add(site.getID());
            }
        }

        if (marketOddsReports.isEmpty()){
            return;
        }


        // Combine all odds reports into one.
        MarketOddsReport fullOddsReport = MarketOddsReport.combine(marketOddsReports);
        log.fine(String.format("Combined %d site odds together for %s.", marketOddsReports.size(), match));


        // Create a profit report for each tautology, made of the best bets that return a penny (0.01)
        ProfitReportSet penny_profitReports = fullOddsReport
                .getTautologyProfitReportSet_targetReturn(tautologies.values(), penny, false);
        penny_profitReports.sort_by_profit();

        // Filter these for those that have a profit over the configured amount
        ProfitReportSet in_profit = penny_profitReports.filter_reports(config.MIN_PROFIT_RATIO);
        if (!in_profit.isEmpty()){
            log.info(sf("Found %s profit reports with profit ratio > %s", in_profit.size(), config.MIN_PROFIT_RATIO));
            profitFound(fullOddsReport, in_profit);
        }

        // Update the best profit for the last round of arbs checked.
        last_best_profit = null;
        if (!penny_profitReports.isEmpty()){
            last_best_profit = penny_profitReports.get(0).minProfitRatio();
        }

        // Update sleep times for certain tautologies based on profit ratio of last check
        Map<Integer, BigDecimal> latest_profit_ratios = new HashMap<>();
        for (ProfitReport profitReport: penny_profitReports.profitReports()){
            latest_profit_ratios.put(profitReport.getBets().id, profitReport.minProfitRatio());
        }
        for (BetGroup tautology: tautologies.values()){
            if (used_tautology_ids.contains(tautology.id)){
                BigDecimal latest_roi = latest_profit_ratios.get(tautology.id);
                tautology.next_usage = now.plusMillis(getSleepTime(latest_roi));
            }
        }
    }


    public void profitFound(MarketOddsReport marketOddsReport, ProfitReportSet reports_in_profit){

        // Using Profit Reports that hypothetically return a penny (not real).
        // Test out real world uses and make any bets if good ones found.

        Instant time = Instant.now();
        ProfitReport chosen_ProfitReport = null;
        ProfitReport chosen_Penny_ProfitReport = null;

        for (ProfitReport this_profitReport: reports_in_profit.profitReports()){

            log.info(sf("Trying out penny Profit Report %s", this_profitReport));

            // Get target return by multiplying target investment (from config)
            // by the ROI on the penny profit report
            BigDecimal target_return = this_profitReport.getMinROI().multiply(config.TARGET_INVESTMENT);
            log.info(sf("Re-doing profit report with target ret %s",
                    BDString(target_return, 4)));

            // Re-calculate the profit report, using the new target return
            // (new odds may not be valid for target
            ProfitReport PR_targetReturn = marketOddsReport.getTautologyProfitReport_targetReturn(
                    this_profitReport.getBets(), target_return, true);
            log.info(sf("New Profit Report = %s", PR_targetReturn));

            log.info(sf("Profit Ratio = %s, min Profit Ratio = %s",
                    BDString(PR_targetReturn.minProfitRatio(), 4),
                    BDString(config.MIN_PROFIT_RATIO, 4)));
            log.info(sf("Total Inv = %s, max Total Inv = %s",
                    BDString(PR_targetReturn.getTotalInvestment(), 4),
                    BDString(config.MAX_INVESTMENT, 4)));

            // Check the new profit report generated to see if it is valid, break if so.
            if (PR_targetReturn == null){
                log.info(sf("Profit report NULL (impossible)."));
            }
            else if (PR_targetReturn.minProfitRatio().compareTo(config.MIN_PROFIT_RATIO) < 0){
                log.info("MinProfitRatio too low.");
            }
            else if (PR_targetReturn.getTotalInvestment().compareTo(config.MAX_INVESTMENT) > 0){
                log.info("Investment too high.");
            }
            else{
                log.info("New Profit report passed checks.");
                chosen_Penny_ProfitReport = this_profitReport;
                chosen_ProfitReport = PR_targetReturn;
                break;
            }
        }

        // Return if no valid PRs found.
        if (chosen_ProfitReport == null){
            log.warning("No profit reports checked were valid after using new target returns.");
            return;
        }

        // If ending after bet, lock out all other threads to prevent multiple bets being placed.
        if (config.END_ON_BET){
            sportsTrader.betlock.lock();
        }

        log.info(sf("chosen PR %s", chosen_ProfitReport));

        if (config.PLACE_BETS){

            // Place Bets!
            List<PlacedBet> placedBets = chosen_ProfitReport.placeBets();
            ProfitReport placedBets_profitReport = chosen_ProfitReport.fromPlacedBets(placedBets);
            log.info(sf("Placed Bets %s", placedBets_profitReport));


            String pennyProf_dir = "penny_prof";
            makeDirIfNotExists(pennyProf_dir);
            chosen_Penny_ProfitReport.saveJSON(time, pennyProf_dir);

            String prePlacement_dir = "pre_placed_bets";
            makeDirIfNotExists(prePlacement_dir);
            chosen_ProfitReport.saveJSON(time, prePlacement_dir);

            String placed_dir = "placed_bets";
            makeDirIfNotExists(placed_dir);
            placedBets_profitReport.saveJSON(time, placed_dir);
        }


        // Save hypothetical profit reports in profit folder
        String profit_dir = "profit";
        makeDirIfNotExists(profit_dir);
        for (ProfitReport pr: reports_in_profit.profitReports()){
            pr.saveJSON(time, profit_dir);
        }


        if (config.END_ON_BET){
            sportsTrader.safe_exit();
        }
    }



    public static class Worker implements Runnable{

        public Thread t;

        @Override
        public void run() {
            try {

                for (int i = 0; i < 999; i++) {
                    for (int j = 0; j < 5999; j++) {
                        BigDecimal a = new BigDecimal("2313.3123")
                                .divide(new BigDecimal("213123423423.12312"), 90, RoundingMode.HALF_UP);
                    }
                }
            }
            catch (Exception e){
            }
        }
    }


    public static void main(String[] args){

        List<BigDecimal> randoms = randomBDs(1000000);
        BigDecimal x = BD("212.1233312312331932837");

        for (RoundingMode roundingMode: RoundingMode.values()){

            Instant start = Instant.now();

            for (BigDecimal bd: randoms){
                BigDecimal rounded_bd = bd.divide(x, 5, roundingMode);
            }

            Instant end = Instant.now();

            print(sf("%sms  -  %s", end.toEpochMilli() - start.toEpochMilli(), roundingMode.toString()));
        }


    }
}
