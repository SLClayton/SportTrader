package Trader;

import Bet.*;
import Bet.FootballBet.FootballBetGenerator;
import SiteConnectors.BettingSite;
import SiteConnectors.RequestHandler;
import SiteConnectors.SiteEventTracker;
import Sport.FootballMatch;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;

import static tools.printer.*;

public class EventTrader implements Runnable {

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    private Config config = SportsTrader.config;

    boolean exit_flag;
    public Thread thread;
    public SportsTrader sportsTrader;
    public SportsTraderStats stats;

    public FootballMatch match;
    public Map<String, BettingSite> sites;
    public Map<String, SiteEventTracker> siteEventTrackers;

    public FootballBetGenerator footballBetGenerator;
    public Collection<Bet> bets;
    public Collection<BetGroup> tautologies;

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
        tautologies = (ArrayList<BetGroup>) this.footballBetGenerator.getAllTautologies().clone();
        siteEventTrackers = new HashMap<>();

        stats = sportsTrader.stats;

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
        int loops_per_check = 100;
        int secs_per_check = 20;
        Instant next_check = Instant.now().plusSeconds(secs_per_check);
        for (long i=0; !exit_flag; i++){
            try {

                // RATE LIMITER: Sleeps until minimum wait period between calls is done.
                sleepUntil(wait_until, config.RATE_LOCKSTEP_INTERVAL);
                wait_until = Instant.now().plus(config.RATE_LIMIT, ChronoUnit.MILLIS);

                // Check arbs and time how long it takes
                Instant start = Instant.now();
                checkArbs();
                loop_times.add(Instant.now().toEpochMilli() - start.toEpochMilli());

                // Update best profit found in check interval
                if (last_best_profit != null && best_profit != null) {
                    best_profit = best_profit.max(last_best_profit);
                }
                else if (last_best_profit != null){
                    best_profit = last_best_profit;
                }

                // Calculate and print stats of last group of arb checks
                if (Instant.now().isAfter(next_check)){
                    String best = String.valueOf(best_profit);
                    if (best.length() > 8){ best = best.substring(0, 8);}
                    log.info(String.format("%s ArbsChcks: avg=%dms OK%s TMT%s LMT%s NA%s Bst: %s",
                            loop_times.size(), avg(loop_times),
                            sum_map(ok_site_oddsReports),
                            sum_map(timeout_site_oddsReports),
                            sum_map(rateLimited_site_oddsReports),
                            sum_map(error_site_oddsReports),
                            best));

                    loop_times.clear();
                    ok_site_oddsReports.clear();
                    timeout_site_oddsReports.clear();
                    rateLimited_site_oddsReports.clear();
                    error_site_oddsReports.clear();
                    best_profit = null;
                    next_check = Instant.now().plusSeconds(secs_per_check);
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


    public static long getSleepTime(BigDecimal prev_profit){
        if (prev_profit == null || prev_profit.compareTo(new BigDecimal("-0.08")) == -1) {
            return 10000;
        } else if (prev_profit.compareTo(new BigDecimal("-0.05")) == -1){
            return 6000;
        } else if (prev_profit.compareTo(new BigDecimal("-0.03")) == -1){
            return 5000;
        } else if (prev_profit.compareTo(new BigDecimal("-0.02")) == -1){
            return 3000;
        } else if (prev_profit.compareTo(new BigDecimal("-0.01")) == -1){
            return 2000;
        } else if (prev_profit.compareTo(new BigDecimal("-0.005")) == -1){
            return 1000;
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


    public RequestHandler requestMarketOddsReport(SiteEventTracker siteEventTracker, Collection<Bet> bets){
        return sportsTrader.requestMarketOddsReport(siteEventTracker, bets);
    }


    private void checkArbs() throws InterruptedException {

        // Send off requests to get marketOddsReports for each site event tracker
        Map<SiteEventTracker, RequestHandler> requestHandlers = new HashMap<>();
        for (SiteEventTracker siteEventTracker: siteEventTrackers.values()){
            RequestHandler rh = requestMarketOddsReport(siteEventTracker, bets);
            requestHandlers.put(siteEventTracker, rh);
        }

        // Wait for results to be generated in each thread and collect them all
        // Use null if time-out occurs for any site
        ArrayList<MarketOddsReport> marketOddsReports = new ArrayList<>();
        Instant timeout = Instant.now().plusMillis(config.REQUEST_TIMEOUT);
        for (Map.Entry<SiteEventTracker, RequestHandler> entry: requestHandlers.entrySet()){
            BettingSite site = entry.getKey().site;
            RequestHandler rh = entry.getValue();

            // Wait for each marketOddsReport of timout
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
            else{
                log.warning(String.format("Failed to get MarkerOddsReport from %s - %s",
                    site.getName(), mor.getErrorMessage()));
                error_site_oddsReports.add(site.getID());
            }
        }


        // Combine all odds reports into one.
        MarketOddsReport fullOddsReport = MarketOddsReport.combine(marketOddsReports);
        log.fine(String.format("Combined %d site odds together for %s.", marketOddsReports.size(), match));


        // Generate profit report for each tautology and order by profit ratio
        ProfitReportSet tautologyProfitReports = ProfitReportSet.getTautologyProfitReports(tautologies, fullOddsReport);
        tautologyProfitReports.sort_by_profit();
        last_best_profit = tautologyProfitReports.best_profit();


        // Create list of profit reports with profits over min_prof_margin
        ProfitReportSet in_profit = tautologyProfitReports.filter_reports(config.MIN_PROFIT_RATIO);


        // If any profit reports are found to be IN profit
        if (in_profit.size() > 0){
            profitFound(in_profit);
        }


        // Update the stats
        if (config.RUN_STATS) {
            stats._update(this, tautologyProfitReports, marketOddsReports);
        }
    }


    public void profitFound(ProfitReportSet in_profit) {

        String profit_timeString = Instant.now().toString().replace(":", "-").substring(0, 18) + "0";
        String report_timeString = Instant.now().truncatedTo(ChronoUnit.MILLIS).toString().replace(":", "-");
        log.info(String.format("%s profit reports found to be over %s PROFIT RATIO.",
                in_profit.size(), config.MIN_PROFIT_RATIO.toString()));


        // Create profit folder if it does not exist
        String profit_dir = "profit";
        makeDirIfNotExists(profit_dir);


        // Get the best (first in list) profit report
        BetOrderProfitReport ratioBetOrderProfitReport = in_profit.get(0);


        // Find profit reports for the lowest and highest possible through min bet size and max volume available
        BetOrderProfitReport min_profit_report = ratioBetOrderProfitReport.newProfitReportReturn(
                ratioBetOrderProfitReport.ret_from_min_stake.add(new BigDecimal("0.01")));
        BetOrderProfitReport max_profit_report = ratioBetOrderProfitReport.newProfitReportReturn(
                ratioBetOrderProfitReport.ret_from_max_stake.subtract(new BigDecimal("0.01")));
        BetOrderProfitReport target_profit_report = ratioBetOrderProfitReport.newProfitReportInvestment(config.TARGET_INVESTMENT);

        log.info(String.format("Profit reports generated. Investment( Min=%s  Target=%s  Max=%s )",
                min_profit_report.total_investment.toString(),
                target_profit_report.total_investment.toString(),
                max_profit_report.total_investment.toString()));


        BetOrderProfitReport betOrderProfitReport;
        // If target profit report between min and max
        if (target_profit_report != null
                && min_profit_report.smallerInvestment(target_profit_report)
                && max_profit_report.biggerInvestment(target_profit_report)) {

            log.info(String.format("Target profit report used. Target investment of %s.",
                    target_profit_report.total_investment.toString()));
            betOrderProfitReport = target_profit_report;
        }
        // If target profit report smaller than min and min smaller than max
        else if ((target_profit_report == null || target_profit_report.smallerInvestment(min_profit_report))
                && min_profit_report.smallerInvestment(max_profit_report)) {

            log.info(String.format("Target profit report has total investment (%s) below minimum needed (%s). Using minimum.",
                    target_profit_report.total_investment, min_profit_report.total_investment));
            betOrderProfitReport = min_profit_report;
        }
        else if (max_profit_report.biggerInvestment(min_profit_report)) {
            log.info(String.format("Target profit report has total investment above max of %s available volume. Using max.",
                    max_profit_report.total_investment.toString()));
            betOrderProfitReport = min_profit_report;
        }
        else {
            log.warning(String.format("Bet not possible with current offers. No bets placed."));
            return;
        }


        // Ensure report investment is not over max investment
        if (betOrderProfitReport.total_investment.compareTo(config.MAX_INVESTMENT) == 1) {
            log.warning(String.format("Profit report needs too high investment. Req: %s  MAX: %s.",
                    betOrderProfitReport.total_investment, config.MAX_INVESTMENT.toString()));

            // Try next best profit report if available
            in_profit.remove(0);
            if (in_profit.size() > 0) {
                profitFound(in_profit);
            }
            return;
        }


        log.info(String.format("Chosen profit report of inv %s is below max inv %s.",
                betOrderProfitReport.total_investment, config.MAX_INVESTMENT));


        // If ending after bet, lock out all other threads.
        if (config.END_ON_BET) {
            sportsTrader.betlock.lock();
        }


        // Place bets if config allows it
        if (config.PLACE_BETS) {
            log.info("Attempting to place bets.");

            // Placed bets and generate profit report
            List<PlacedBet> placeBets = placeBets(betOrderProfitReport.betOrders);
            PlacedOrderProfitReport_legacy placedOrderProfitReport = new PlacedOrderProfitReport_legacy(placeBets, betOrderProfitReport);

            // Construct file paths for saved PlacedBetReport
            String placedBetsDir = "placed_bets";
            String placedBetsFilename = String.format("%s %s %s.json",
                    report_timeString, match, placedOrderProfitReport.min_profit);
            String placedBetsPath = placedBetsDir + "/" + placedBetsFilename;

            // Make directory and save profit report
            log.info(String.format("Saving PlacedOrderProfitReport to %s", placedBetsPath));
            makeDirIfNotExists(placedBetsDir);
            toFile(placedOrderProfitReport.toJSON(true), placedBetsPath);
        }


        // Save profit report as json file
        String profitString = betOrderProfitReport.profit_ratio.setScale(5, RoundingMode.HALF_UP).toString();
        String filename = profit_timeString + " -  " + match.name + " " + profitString + ".json";
        toFile(betOrderProfitReport.toJSON(true), profit_dir + "/" + filename);


        if (config.END_ON_BET){
            log.info("END_ON_BET=true so exiting program.");
            sportsTrader.safe_exit();
        }
    }


    public List<PlacedBet> placeBets(List<BetOrder> betOrders){

        // Sort placed bets into lists depending on the site they're going to.
        Map<String, List<BetOrder>> site_bets = BetOrder.splitListBySite(betOrders);


        // Send list of bets of to their respective Betting site objects to be placed
        ArrayList<PlaceBetsRunnable> placeBetsRunnables = new ArrayList<>();
        for (Map.Entry<String, List<BetOrder>> entry: site_bets.entrySet()){
            List<BetOrder> site_betOrders = entry.getValue();

            PlaceBetsRunnable placeBetsRunnable = new PlaceBetsRunnable(site_betOrders);
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
                while (failedbets.size() < placeBetsRunnable.betOrders.size()){
                    BetOrder betOrder = placeBetsRunnable.betOrders.get(failedbets.size());

                    PlacedBet generic_failbet = null;

                    failedbets.add(generic_failbet);
                }
                placedBets.addAll(failedbets);
            }
        }

        return placedBets;
    }


    public class PlaceBetsRunnable implements Runnable{

        public List<BetOrder> betOrders;
        public List<PlacedBet> placedBets;
        public BettingSite site;
        public Thread thread;

        public PlaceBetsRunnable(List<BetOrder> betOrders){
            this.betOrders = betOrders;
            site = this.betOrders.get(0).getSite();
            thread = new Thread(this);
            thread.setName(String.format("%s-BetPlacer", site.getName()));
        }

        public void start(){
            thread.start();
        }

        @Override
        public void run() {
            try {
                placedBets = site.placeBets(betOrders, config.ODDS_RATIO_BUFFER);
            } catch (IOException | URISyntaxException e) {
                log.severe(String.format("Error while sending bets off to %s", site.getName()));
                e.printStackTrace();
                placedBets = new ArrayList<>();
                while (placedBets.size() < betOrders.size()){
                    placedBets.add(null);
                }
            }
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

        Instant a = Instant.now();

        for (int i=0; i<1000; i++){
            Worker worker = new Worker();
            worker.t = new Thread(worker);
            worker.t.start();

        }

        Instant b = Instant.now();
        print(b.toEpochMilli() - a.toEpochMilli());



    }
}
