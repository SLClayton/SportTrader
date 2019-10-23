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
import java.time.temporal.ChronoUnit;
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

    public Thread thread;
    public SportsTrader sportsTrader;

    public FootballMatch match;
    public HashMap<String, BettingSite> sites;
    public HashMap<String, SiteEventTracker> siteEventTrackers;
    public BlockingQueue<RequestHandler> siteMarketOddsToGetQueue;
    public ArrayList<MarketOddsReportWorker> marketOddsReportWorkers;

    public FootballBetGenerator footballBetGenerator;
    public ArrayList<BetGroup> tautologies;

    public EventTrader(SportsTrader sportsTrader, FootballMatch match, HashMap<String, BettingSite> sites, FootballBetGenerator footballBetGenerator){
        this.sportsTrader = sportsTrader;
        this.match = match;
        this.sites = sites;
        this.footballBetGenerator = footballBetGenerator;
        tautologies = (ArrayList<BetGroup>) this.footballBetGenerator.getAllTautologies().clone();
        siteEventTrackers = new HashMap<String, SiteEventTracker>();
        siteMarketOddsToGetQueue = new LinkedBlockingQueue<>();

        MIN_ODDS_RATIO = sportsTrader.MIN_ODDS_RATIO;
        MIN_PROFIT_RATIO = sportsTrader.MIN_PROFIT_RATIO;
        MAX_INVESTMENT = sportsTrader.MAX_INVESTMENT;
        END_ON_BET = sportsTrader.END_ON_BET;
        TARGET_INVESTMENT = sportsTrader.TARGET_INVESTMENT;
        REQUEST_TIMEOUT = sportsTrader.REQUEST_TIMEOUT;
    }


    public Integer setupMatch(){

        // Create lists for sites which fail and succeed setting up
        int total_sites = sites.size();
        ArrayList<String> failed_sites = new ArrayList<String>();
        HashMap<String, BettingSite> accepted_sites = new HashMap<>();

        //Connect each site to event tracker
        for (Map.Entry<String, BettingSite> entry: sites.entrySet()){

            String site_name = entry.getKey();
            BettingSite site = entry.getValue();

            // Try to setup match, remove site if fail
            SiteEventTracker eventTracker = site.getEventTracker(this);

            boolean setup_success = false;
            try {
                setup_success = eventTracker.setupMatch(match);
            } catch (IOException | URISyntaxException | InterruptedException e) {
                log.warning(e.toString());
                setup_success = false;
            }
            if (!(setup_success)){
                failed_sites.add(site_name);
                log.info(String.format("%s failed to setup in %s Event Tracker.",
                        match, site_name));
                continue;
            }

            // Add successful sites and trackers into maps
            siteEventTrackers.put(site_name, eventTracker);
            accepted_sites.put(site_name, site);
            log.info(String.format("%s successfully setup in %s Event Tracker.",
                    match, site_name));
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

        // Start MarketOddsReportWorker threads, 1 for each site
        // This is a thread for each site to go off and collect new odds report asynchronously
        marketOddsReportWorkers = new ArrayList<>();
        for (int i=0; i<sites.size()*3; i++){
            MarketOddsReportWorker morw = new MarketOddsReportWorker(siteMarketOddsToGetQueue, siteEventTrackers);
            morw.thread.setName(Thread.currentThread().getName() + " OR-" + i);
            morw.start();
            marketOddsReportWorkers.add(morw);
        }
        
        // Check for arbs, and update event constantly
        Instant wait_until = null;
        ArrayList<Long> arb_times = new ArrayList<>();
        int max_times = 100;
        for (int i=0; !sportsTrader.exit_flag; i++){
            try {
                // RATE LIMITER: Sleeps until minimum wait period between calls is done.
                Instant now = Instant.now();
                if (wait_until != null && now.isBefore(wait_until)){
                    long time_to_wait = wait_until.toEpochMilli() - now.toEpochMilli();
                    Thread.sleep(time_to_wait);
                }
                wait_until = Instant.now().plus(sportsTrader.RATE_LIMIT, ChronoUnit.MILLIS);


                // Check arbs and time how long it takes
                Instant start = Instant.now();
                checkArbs();
                arb_times.add(Instant.now().toEpochMilli() - start.toEpochMilli());

                // Calculate the timing metrics over past timings
                if (arb_times.size() >= max_times){
                    long avg_ms = 0;
                    for (long arb_time: arb_times){ avg_ms += arb_time; }
                    avg_ms = avg_ms / arb_times.size();
                    String padding = String.join("", Collections.nCopies(4 - String.valueOf(avg_ms).length(), " "));
                    log.info(String.format("Arb Checks. %d avg: %d ms%s", max_times, avg_ms, padding));
                    arb_times.clear();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public class MarketOddsReportWorker implements Runnable{

        BlockingQueue<RequestHandler> job_queue;
        HashMap<String, SiteEventTracker> siteEventTrackers;
        Thread thread;

        public MarketOddsReportWorker(BlockingQueue<RequestHandler> job_queue, HashMap<String,
                SiteEventTracker> siteEventTrackers){

            this.job_queue = job_queue;
            this.siteEventTrackers = siteEventTrackers;
            thread = new Thread(this);
        }


        public void start(){
            thread.start();
        }


        @Override
        public void run() {

            while (!sportsTrader.exit_flag){

                RequestHandler requestHandler = null;
                try {
                    // Wait for request handler to arrive telling us which market odds to update
                    while (!sportsTrader.exit_flag && requestHandler == null){
                        requestHandler = job_queue.poll(1, TimeUnit.SECONDS);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    continue;
                }
                if (requestHandler == null) {
                    continue;
                }

                // Find the object from its name
                String site_name = (String) requestHandler.request;
                SiteEventTracker set = siteEventTrackers.get(site_name);

                try {
                    set.marketOddsReportTime = null;
                    Instant start = Instant.now();
                    MarketOddsReport mor = set.getMarketOddsReport(footballBetGenerator.getAllBets());
                    set.marketOddsReportTime = Instant.now().toEpochMilli() - start.toEpochMilli();
                    requestHandler.setResponse(mor);
                }
                catch (InterruptedException e){
                    continue;
                }
                catch (Exception e) {
                    e.printStackTrace();
                    log.severe(e.getStackTrace().toString());
                    requestHandler.setFail();
                }
            }
            log.info("Exiting Event Trader Odds Report updater.");
        }
    }


    private void checkArbs() throws InterruptedException {

        // Create request handler and add to queue to get market odds for each site
        ArrayList<RequestHandler> requestHandlers = new ArrayList<>();
        for (Map.Entry<String, SiteEventTracker> entry : siteEventTrackers.entrySet()){
            String site_name = entry.getKey();
            RequestHandler rh = new RequestHandler(site_name);
            siteMarketOddsToGetQueue.add(rh);
            requestHandlers.add(rh);
        }

        // Wait for results to be generated in each thread and collect them all
        // Use null if timout occurs for any site
        ArrayList<MarketOddsReport> marketOddsReports = new ArrayList<MarketOddsReport>();
        Instant timeout = Instant.now().plus(REQUEST_TIMEOUT, ChronoUnit.MILLIS);
        for (RequestHandler rh: requestHandlers){
            String site_name = (String) rh.request;

            MarketOddsReport mor;
            if (Instant.now().isBefore(timeout)){
                long millis_until_timeout = timeout.toEpochMilli() - Instant.now().toEpochMilli();
                mor = (MarketOddsReport) rh.pollReponse(millis_until_timeout, TimeUnit.MILLISECONDS);
            }
            else{
                mor = (MarketOddsReport) rh.pollReponse();
            }

            if (mor != null){
                marketOddsReports.add(mor);
            }
        }


        // Combine all odds reports into one.
        MarketOddsReport fullOddsReport = MarketOddsReport.combine(marketOddsReports);
        log.fine(String.format("Combined %d site odds together for %s.", marketOddsReports.size(), match));


        // Generate profit report for each tautology and order by profit ratio
        ProfitReportSet tautologyProfitReports = ProfitReportSet.getTautologyProfitReports(tautologies, fullOddsReport);
        tautologyProfitReports.sort_by_profit();


        // Create list of profit reports with profits over min_prof_margin
        ProfitReportSet in_profit = tautologyProfitReports.filter_reports(MIN_PROFIT_RATIO);


        // If any profit reports are found to be IN profit
        if (in_profit.size() > 0){
            profitFound(in_profit);
        }
    }


    public void profitFound(ProfitReportSet in_profit){

        log.info(String.format("%s profit reports found to be over %s profit ratio.",
                in_profit.size(), MIN_PROFIT_RATIO.toString()));

        // If ending after bet, lock out all other threads.
        if (END_ON_BET){
            sportsTrader.betlock.lock();
        }

        // Create profit folder if it does not exist
        File profit_dir = new File(FileSystems.getDefault().getPath(".") + "/profit");
        if (!profit_dir.exists()){
            profit_dir.mkdir();
        }

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
            && max_profit_report.biggerInvestment(target_profit_report)){

            log.info(String.format("Target profit report used. Target investment of %s.",
                    target_profit_report.total_investment.toString()));
            profitReport = target_profit_report;
        }
        // If target profit report smaller than min and min smaller than max
        else if ((target_profit_report == null || target_profit_report.smallerInvestment(min_profit_report))
            && min_profit_report.smallerInvestment(max_profit_report)){

            log.info(String.format("Target profit report has total investment below minimum %s needed. Using minimum.",
                    min_profit_report.total_investment.toString()));
            profitReport = min_profit_report;
        }

        else if (max_profit_report.biggerInvestment(min_profit_report)){

            log.info(String.format("Target profit report has total investment above max of %s available volume. Using max.",
                    max_profit_report.total_investment.toString()));
            profitReport = min_profit_report;
        }
        else{
            log.warning(String.format("Bet not possible with current offers. No bets placed."));
            return;
        }



        // Ensure report investment is not over max investment
        if (profitReport.total_investment.compareTo(MAX_INVESTMENT) == 1){
            log.warning(String.format("Profit report needs too high investment. Req: %s  MAX: %s.",
                    profitReport.total_investment, MAX_INVESTMENT.toString()));

            // Try next best profit report if available
            in_profit.remove(0);
            if (in_profit.size() > 0){
                profitFound(in_profit);
            }
            return;
        }



        // Save profit report as json file
        String profitString = profitReport.profit_ratio.setScale(5, RoundingMode.HALF_UP).toString();
        String filename = timeString + " -  " + match.name + " " + profitString + ".json";
        toFile(profitReport.toJSON(true), profit_dir.toString() + "/" + filename);



        // Check config allows bets
        if (!sportsTrader.PLACE_BETS){
            in_profit.remove(0);
            if (in_profit.size() > 0){
                profitFound(in_profit);
            }
            return;
        }


        ArrayList<PlacedBet> placeBets = placeBets(profitReport.betOrders);
        PlacedProfitReport placedProfitReport = new PlacedProfitReport(placeBets, profitReport);
        toFile(placedProfitReport.toJSON(true));

        if (END_ON_BET){
            log.info("Bets Placed, END_ON_BET=true so exiting program.");
            sportsTrader.safe_exit();
        }
    }


    public ArrayList<PlacedBet> placeBets(ArrayList<BetOrder> betOrders){

        //Sort placed bets into seperate lists depending on their size
        Map<String, ArrayList<BetOrder>> site_bets = new HashMap<>();
        for (BetOrder betOrder: betOrders){

            if (!site_bets.containsKey(betOrder.bet_offer.site.name)){
                site_bets.put(betOrder.bet_offer.site.name, new ArrayList<>());
            }
            site_bets.get(betOrder.bet_offer.site.name).add(betOrder);
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
                log.severe(String.format("Error with bets sent to %s.", placeBetsRunnable.site.name));
                e.printStackTrace();

                ArrayList<PlacedBet> failedbets = new ArrayList<>();
                while (failedbets.size() < placeBetsRunnable.betOrders.size()){
                    BetOrder betOrder = placeBetsRunnable.betOrders.get(failedbets.size());
                    failedbets.add(new PlacedBet(PlacedBet.FAILED_STATE, betOrder,
                            String.format("Error with all bets sent in this batch to %s.",
                                    placeBetsRunnable.site.name)));
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
                log.severe(String.format("Error while sending bets off to %s", site.name));
                e.printStackTrace();
                placedBets = new ArrayList<>();
                while (placedBets.size() < betOrders.size()){
                    placedBets.add(new PlacedBet(PlacedBet.FAILED_STATE,
                            betOrders.get(placedBets.size()),
                            String.format("placeBets batch fail for %s", site.name)));
                }
            }
        }
    }


    public static void main(String[] args){


    }
}
