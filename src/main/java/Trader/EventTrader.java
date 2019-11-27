package Trader;

import Bet.*;
import Bet.FootballBet.FootballBetGenerator;
import SiteConnectors.Betfair.Betfair;
import SiteConnectors.Betfair.BetfairEventTracker;
import SiteConnectors.BettingSite;
import SiteConnectors.FlashScores;
import SiteConnectors.RequestHandler;
import SiteConnectors.SiteEventTracker;
import Sport.FootballMatch;
import tools.printer.*;

import javax.naming.directory.InvalidAttributesException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.PortUnreachableException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static tools.printer.*;

public class EventTrader implements Runnable {

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    public BigDecimal MIN_ODDS_RATIO;
    public BigDecimal MIN_PROFIT_RATIO;
    public BigDecimal MAX_INVESTMENT;
    public boolean END_ON_BET;
    public BigDecimal TARGET_INVESTMENT;
    public long REQUEST_TIMEOUT;
    public boolean RUN_STATS;
    public long RATE_LOCKSTEP_INTERVAL;

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
    public ArrayList<String> timout_site_oddsReports;
    public ArrayList<String> rateLimited_site_oddsReports;
    public ArrayList<String> error_site_oddsReports;
    public BigDecimal best_profit_last;



    public EventTrader(SportsTrader sportsTrader, FootballMatch match, Map<String, BettingSite> sites, FootballBetGenerator footballBetGenerator){
        exit_flag = false;
        this.sportsTrader = sportsTrader;
        this.match = match;
        this.sites = sites;
        this.footballBetGenerator = footballBetGenerator;
        bets = (Collection<Bet>) (Object) footballBetGenerator.getAllBets();
        tautologies = (ArrayList<BetGroup>) this.footballBetGenerator.getAllTautologies().clone();
        siteEventTrackers = new HashMap<>();

        MIN_ODDS_RATIO = sportsTrader.MIN_ODDS_RATIO;
        MIN_PROFIT_RATIO = sportsTrader.MIN_PROFIT_RATIO;
        MAX_INVESTMENT = sportsTrader.MAX_INVESTMENT;
        END_ON_BET = sportsTrader.END_ON_BET;
        TARGET_INVESTMENT = sportsTrader.TARGET_INVESTMENT;
        REQUEST_TIMEOUT = sportsTrader.REQUEST_TIMEOUT;
        RUN_STATS = sportsTrader.RUN_STATS;
        RATE_LOCKSTEP_INTERVAL = sportsTrader.RATE_LOCKSTEP_INTERVAL;

        stats = sportsTrader.stats;

        ok_site_oddsReports = new ArrayList<>();
        timout_site_oddsReports = new ArrayList<>();
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
            SiteEventTracker eventTracker = site.getEventTracker(
                    this, (Collection<Bet>) (Object) footballBetGenerator.getAllBets());

            // Try to setup match ion site event tracker, remove site if fail
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

        /*
        // Start MarketOddsReportWorker threads, 1 for each site
        // This is a thread for each site to go off and collect new odds report asynchronously
        marketOddsReportWorkers = new ArrayList<>();
        for (int i=0; i<sites.size()*3; i++){
            MarketOddsReportWorker morw = new MarketOddsReportWorker(siteMarketOddsToGetQueue, siteEventTrackers);
            morw.thread.setName(Thread.currentThread().getName() + " OR-" + i);
            morw.start();
            marketOddsReportWorkers.add(morw);
        }
        */

        
        // Check for Arbs
        Instant wait_until = null;
        ArrayList<Long> loop_times = new ArrayList<>();
        Set<String> site_ids = BettingSite.getIDs(sites.values());
        BigDecimal best_profit = null;
        int loops_per_check = 100;
        for (long i=0; !exit_flag; i++){
            try {

                // RATE LIMITER: Sleeps until minimum wait period between calls is done.
                sleepUntil(wait_until, RATE_LOCKSTEP_INTERVAL);
                wait_until = Instant.now().plus(sportsTrader.RATE_LIMIT, ChronoUnit.MILLIS);

                // Check arbs and time how long it takes
                Instant start = Instant.now();
                checkArbs();
                loop_times.add(Instant.now().toEpochMilli() - start.toEpochMilli());

                // Update best profit found in check interval
                if (best_profit_last != null && best_profit != null) {
                    best_profit = best_profit.max(best_profit_last);
                }
                else if (best_profit_last != null){
                    best_profit = best_profit_last;
                }

                // Calculate the timing metrics over past timings
                if (loop_times.size() >= loops_per_check){
                    String best = String.valueOf(best_profit);
                    if (best.length() > 8){ best = best.substring(0, 8);}
                    log.info(String.format("%s Arb Checks: avg=%dms OK%s TIMEOUT%s LIMIT%s NA%s best: %s",
                            loops_per_check, avg(loop_times),
                            count(site_ids, ok_site_oddsReports),
                            count(new HashSet<>(timout_site_oddsReports), timout_site_oddsReports),
                            count(new HashSet<>(rateLimited_site_oddsReports), rateLimited_site_oddsReports),
                            count(new HashSet<>(error_site_oddsReports), error_site_oddsReports),
                            best));

                    loop_times.clear();
                    ok_site_oddsReports.clear();
                    timout_site_oddsReports.clear();
                    rateLimited_site_oddsReports.clear();
                    error_site_oddsReports.clear();
                    best_profit = null;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public long avg(Collection<Long> list){
        long sum = 0;
        for (long item: list){
            sum += item;
        }
        return  sum / list.size();
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


    public class MarketOddsReportWorker_old implements Runnable{

        BlockingQueue<RequestHandler> job_queue;
        Map<String, SiteEventTracker> siteEventTrackers;
        Thread thread;

        public MarketOddsReportWorker_old(BlockingQueue<RequestHandler> job_queue,
                                      Map<String, SiteEventTracker> siteEventTrackers){

            this.job_queue = job_queue;
            this.siteEventTrackers = siteEventTrackers;
            thread = new Thread(this);
        }


        public void start(){
            thread.start();
        }


        @Override
        public void run() {

            mainloop:
            while (!exit_flag){

                RequestHandler requestHandler = null;

                // Wait for an item appears in the job queue, checking every 1 second that
                // the exit flag hasn't beem triggered.
                while (!exit_flag && requestHandler == null){
                    try {
                        requestHandler = job_queue.poll(1, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


                // Restart if exit flag triggered or request handler not found.
                if (requestHandler == null || exit_flag) {
                    continue;
                }


                // TODO: remake bit that in parallel goes to collect market odds report, this should be
                // in the site event tracker and you pass it in a request handler.


                // Find the object from its name
                String site_name = (String) requestHandler.request;
                SiteEventTracker set = siteEventTrackers.get(site_name);

                // Update odds report and deal with errors if they happen during.
                try {
                    Instant a = Instant.now();
                    MarketOddsReport mor = set.getMarketOddsReport(
                            (Collection<Bet>)(Object) footballBetGenerator.getAllBets());
                    Instant b = Instant.now();
                    log.info(String.format("%s mor time = %sms", site_name, b.toEpochMilli()-a.toEpochMilli()));
                    if (mor == null){
                        mor = MarketOddsReport.ERROR("Market Odds Report returned null object");
                    }
                    requestHandler.setResponse(mor);
                }
                catch (InterruptedException e){
                    continue;
                }
                catch (Exception e) {
                    e.printStackTrace();
                    log.severe(String.format("Unexpected error when getting MarketOddsReport for %s.\n%s\n%s",
                            site_name, e.toString(), e.getStackTrace().toString()));
                    requestHandler.setResponse(MarketOddsReport.ERROR(e.toString()));
                }
            }
            log.info("Exiting Event Trader Odds Report updater.");
        }
    }


    private void checkArbs() throws InterruptedException {

        // Send off requests to get marketOddsReports for each site event tracker
        Map<SiteEventTracker, RequestHandler> requestHandlers = new HashMap<>();
        for (SiteEventTracker siteEventTracker: siteEventTrackers.values()){
            requestHandlers.put(siteEventTracker, siteEventTracker.requestMarketOddsReport(bets));
        }

        // Wait for results to be generated in each thread and collect them all
        // Use null if time-out occurs for any site
        ArrayList<MarketOddsReport> marketOddsReports = new ArrayList<MarketOddsReport>();
        Instant timeout = Instant.now().plus(REQUEST_TIMEOUT, ChronoUnit.MILLIS);
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
                mor = (MarketOddsReport) rh.pollReponse(millis_until_timeout, TimeUnit.MILLISECONDS);
            }
            else {
                mor = (MarketOddsReport) rh.pollReponse();
            }
            if (mor == null){
                mor = MarketOddsReport.TIMED_OUT();
            }

            // Sort each marketOddsReport once received.
            if (mor.noError()) {
                marketOddsReports.add(mor);
                ok_site_oddsReports.add(site.getID());
            }
            else if (mor.rate_limited()){ rateLimited_site_oddsReports.add(site.getID()); }
            else if (mor.timed_out()){ timout_site_oddsReports.add(site.getID()); }
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
        best_profit_last = tautologyProfitReports.best_profit();


        // Create list of profit reports with profits over min_prof_margin
        ProfitReportSet in_profit = tautologyProfitReports.filter_reports(MIN_PROFIT_RATIO);


        // If any profit reports are found to be IN profit
        if (in_profit.size() > 0){
            profitFound(in_profit);
        }


        // Update the stats
        if (RUN_STATS) {
            stats._update(this, tautologyProfitReports, marketOddsReports);
        }
    }


    public void profitFound(ProfitReportSet in_profit) {

        log.info(String.format("%s profit reports found to be over %s PROFIT RATIO.",
                in_profit.size(), MIN_PROFIT_RATIO.toString()));

        // Create profit folder if it does not exist
        String profit_dir = "profit";
        makeDirIfNotExists(profit_dir);

        // Get the best (first in list) profit report
        ProfitReport ratioProfitReport = in_profit.get(0);
        String timeString = Instant.now().toString().replace(":", "-").substring(0, 18) + "0";

        // Find profit reports for the lowest and highest possible through min bet size and max volume available
        ProfitReport min_profit_report = ratioProfitReport.newProfitReportReturn(
                ratioProfitReport.ret_from_min_stake.add(new BigDecimal("0.01")));
        ProfitReport max_profit_report = ratioProfitReport.newProfitReportReturn(
                ratioProfitReport.ret_from_max_stake.subtract(new BigDecimal("0.01")));
        ProfitReport target_profit_report = ratioProfitReport.newProfitReportInvestment(TARGET_INVESTMENT);

        log.info(String.format("Profit reports generated. Min=%s  Target=%s  Max=%s",
                min_profit_report.total_investment.toString(),
                target_profit_report.total_investment.toString(),
                max_profit_report.total_investment.toString()));


        ProfitReport profitReport;
        // If target profit report between min and max
        if (target_profit_report != null
                && min_profit_report.smallerInvestment(target_profit_report)
                && max_profit_report.biggerInvestment(target_profit_report)) {

            log.info(String.format("Target profit report used. Target investment of %s.",
                    target_profit_report.total_investment.toString()));
            profitReport = target_profit_report;
        }
        // If target profit report smaller than min and min smaller than max
        else if ((target_profit_report == null || target_profit_report.smallerInvestment(min_profit_report))
                && min_profit_report.smallerInvestment(max_profit_report)) {

            log.info(String.format("Target profit report has total investment below minimum %s needed. Using minimum.",
                    min_profit_report.total_investment.toString()));
            profitReport = min_profit_report;
        } else if (max_profit_report.biggerInvestment(min_profit_report)) {

            log.info(String.format("Target profit report has total investment above max of %s available volume. Using max.",
                    max_profit_report.total_investment.toString()));
            profitReport = min_profit_report;
        } else {
            log.warning(String.format("Bet not possible with current offers. No bets placed."));
            return;
        }


        // Ensure report investment is not over max investment
        if (profitReport.total_investment.compareTo(MAX_INVESTMENT) == 1) {
            log.warning(String.format("Profit report needs too high investment. Req: %s  MAX: %s.",
                    profitReport.total_investment, MAX_INVESTMENT.toString()));

            // Try next best profit report if available
            in_profit.remove(0);
            if (in_profit.size() > 0) {
                profitFound(in_profit);
            }
            return;
        }


        // If ending after bet, lock out all other threads.
        if (END_ON_BET) {
            sportsTrader.betlock.lock();
        }


        // Check config allows bets
        if (sportsTrader.PLACE_BETS) {
            // Placed bets and generate profit report
            ArrayList<PlacedBet> placeBets = placeBets(profitReport.betOrders);
            PlacedProfitReport placedProfitReport = new PlacedProfitReport(placeBets, profitReport);

            // Create dir name and filename for this profit report to save to
            String placed_bets_dir = "placed_bets";
            String placedBetsFilename = String.format("%s %s %s.json",
                    Instant.now().truncatedTo(ChronoUnit.MILLIS), match, placedProfitReport.min_profit);

            // Make directory and save profit report
            makeDirIfNotExists(placed_bets_dir);
            toFile(placedProfitReport.toJSON(true), placed_bets_dir + "/" + placedBetsFilename);
        }


        // Save profit report as json file
        String profitString = profitReport.profit_ratio.setScale(5, RoundingMode.HALF_UP).toString();
        String filename = timeString + " -  " + match.name + " " + profitString + ".json";
        toFile(profitReport.toJSON(true), profit_dir + "/" + filename);


        if (END_ON_BET){
            log.info("Bets Placed, END_ON_BET=true so exiting program.");
            sportsTrader.safe_exit();
        }
    }


    public ArrayList<PlacedBet> placeBets(ArrayList<BetOrder> betOrders){

        //Sort placed bets into seperate lists depending on their size
        Map<String, ArrayList<BetOrder>> site_bets = new HashMap<>();
        for (BetOrder betOrder: betOrders){

            if (!site_bets.containsKey(betOrder.bet_offer.site.getName())){
                site_bets.put(betOrder.bet_offer.site.getName(), new ArrayList<>());
            }
            site_bets.get(betOrder.bet_offer.site.getName()).add(betOrder);
        }

        // Place the list of bets for each site
        ArrayList<PlaceBetsRunnable> placeBetsRunnables = new ArrayList<>();
        for (Map.Entry<String, ArrayList<BetOrder>> entry: site_bets.entrySet()){
            String site_name = entry.getKey();
            ArrayList<BetOrder> site_betOrders = entry.getValue();

            PlaceBetsRunnable placeBetsRunnable = new PlaceBetsRunnable(site_betOrders);
            placeBetsRunnable.thread = new Thread(placeBetsRunnable);
            placeBetsRunnable.thread.setName(String.format("%s-BtchBtPlcr", site_name));
            placeBetsRunnable.thread.start();
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

                    PlacedBet generic_failbet = new PlacedBet(PlacedBet.FAILED_STATE,
                            betOrder,
                            String.format("Error with all bets sent in this batch to %s.", placeBetsRunnable.site.getName()));

                    failedbets.add(generic_failbet);
                }
                placedBets.addAll(failedbets);
            }
        }

        return placedBets;
    }


    public class PlaceBetsRunnable implements Runnable{

        public ArrayList<BetOrder> betOrders;
        public ArrayList<PlacedBet> placedBets;
        public BettingSite site;
        public Thread thread;

        public PlaceBetsRunnable(ArrayList<BetOrder> betOrders){
            this.betOrders = betOrders;
            site = this.betOrders.get(0).bet_offer.site;
        }

        @Override
        public void run() {
            try {
                placedBets = site.placeBets(betOrders, MIN_ODDS_RATIO);
            } catch (IOException | URISyntaxException e) {
                log.severe(String.format("Error while sending bets off to %s", site.getName()));
                e.printStackTrace();
                placedBets = new ArrayList<>();
                while (placedBets.size() < betOrders.size()){
                    placedBets.add(new PlacedBet(PlacedBet.FAILED_STATE,
                            betOrders.get(placedBets.size()),
                            String.format("placeBets batch fail for %s", site.getName())));
                }
            }
        }
    }



    public static class Worker implements Runnable{

        @Override
        public void run() {

            try{
                for (int i=0; i<100; i++){
                    print(i);
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                print("Thread has been interrupted.");
            }
        }
    }


    public static void main(String[] args){

        Worker worker = new Worker();
        Thread t = new Thread(worker);
        t.start();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        t.interrupt();

    }
}
