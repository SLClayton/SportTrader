package Trader;

import Bet.*;
import Bet.FootballBet.FootballBetGenerator;
import SiteConnectors.BettingSite;
import SiteConnectors.SiteEventTracker;
import SiteConnectors.Smarkets.Smarkets;
import Sport.FootballMatch;

import javax.naming.directory.InvalidAttributesException;
import javax.swing.plaf.basic.BasicButtonUI;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import static tools.printer.*;

public class EventTrader implements Runnable {

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    public static final BigDecimal MIN_ODDS_RATIO = new BigDecimal("0.9");
    public static final BigDecimal MIN_PROFIT_RATIO = new BigDecimal("-0.015");


    public Thread thread;
    public SportsTrader sportsTrader;

    public FootballMatch match;
    public HashMap<String, BettingSite> sites;
    public HashMap<String, SiteEventTracker> siteEventTrackers;
    public BlockingQueue<String> siteMarketOddsToGetQueue;

    public FootballBetGenerator footballBetGenerator;
    public ArrayList<Tautology> tautologies;

    public EventTrader(SportsTrader sportsTrader, FootballMatch match, HashMap<String, BettingSite> sites, FootballBetGenerator footballBetGenerator){
        this.sportsTrader = sportsTrader;
        this.match = match;
        this.sites = sites;
        this.footballBetGenerator = footballBetGenerator;
        tautologies = (ArrayList<Tautology>) this.footballBetGenerator.getAllTautologies().clone();
        siteEventTrackers = new HashMap<String, SiteEventTracker>();
        siteMarketOddsToGetQueue = new LinkedBlockingQueue<>();
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
            SiteEventTracker eventTracker = site.getEventTracker();

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
        for (int i=0; i<sites.size(); i++){
            MarketOddsReportWorker morw = new MarketOddsReportWorker(siteMarketOddsToGetQueue, siteEventTrackers);
            Thread thread = new Thread(morw);
            thread.setName(Thread.currentThread().getName() + " OR-" + i);
            thread.start();
        }
        
        // Check for arbs, and update event constantly
        Instant wait_until = null;
        ArrayList<Long> arb_times = new ArrayList<>();
        int max_times = 100;
        for (int i=0; true; i++){
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

                if (arb_times.size() >= max_times){
                    long avg_ms = 0;
                    for (long arb_time: arb_times){ avg_ms += arb_time; }
                    avg_ms = avg_ms / arb_times.size();
                    String padding = String.join("", Collections.nCopies(4 - String.valueOf(avg_ms).length(), " "));
                    log.info(String.format("Arb Checks. %d avg: %d ms%s", max_times, avg_ms, padding));
                    arb_times.clear();
                }

                //log.info(String.format("Arbs checked for %s in %dms", match, ms));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public class MarketOddsReportWorker implements Runnable{

        BlockingQueue<String> job_queue;
        HashMap<String, SiteEventTracker> siteEventTrackers;

        public MarketOddsReportWorker(BlockingQueue<String> job_queue, HashMap<String, SiteEventTracker> siteEventTrackers){
            this.job_queue = job_queue;
            this.siteEventTrackers = siteEventTrackers;
        }

        @Override
        public void run() {

            while (true){

                try {
                    // Get site name from queue to represent job to update its odds report
                    String site_name = job_queue.take();

                    // Find the object from its name and update this objects odds report
                    SiteEventTracker siteEventTracker = siteEventTrackers.get(site_name);
                    siteEventTrackers.get(site_name).updateMarketOddsReport(footballBetGenerator.getAllBets());

                    // Add value to blocking queue to signal update complete.
                    siteEventTracker.updateComplete.add(true);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }


    private void checkArbs() throws InterruptedException {

        // Add each site name to the queue to have its odds updated
        for (Map.Entry<String, SiteEventTracker> entry : siteEventTrackers.entrySet()){
            String site_name = entry.getKey();
            SiteEventTracker siteEventTracker = entry.getValue();

            siteMarketOddsToGetQueue.put(site_name);
        }

        // Wait for results to be generated in each thread and collect them all
        ArrayList<MarketOddsReport> marketOddsReports = new ArrayList<MarketOddsReport>();
        for (Map.Entry<String, SiteEventTracker> entry : siteEventTrackers.entrySet()) {
            SiteEventTracker siteEventTracker = entry.getValue();

            // Wait for report to finish updating by waiting for queue value to appear and taking.
            siteEventTracker.updateComplete.take();

            // Add report to report list and remove its lock
            marketOddsReports.add(siteEventTracker.marketOddsReport);
        }
        log.fine(String.format("Found %d site odds for %s.", marketOddsReports.size(), match));


        // Combine all odds reports into one.
        MarketOddsReport fullOddsReport = MarketOddsReport.combine(marketOddsReports);
        log.fine(String.format("Combined %d site odds together for %s.", marketOddsReports.size(), match));


        // Generate profit report for each tautology and order by profit ratio
        ArrayList<ProfitReport> tautologyProfitReports = ProfitReport.getTautologyProfitReports(tautologies, fullOddsReport);
        Collections.sort(tautologyProfitReports, Collections.reverseOrder());


        // Create list of profit reports with profits over min_prof_margain
        ArrayList<ProfitReport> in_profit = new ArrayList<ProfitReport>();
        for (ProfitReport pr: tautologyProfitReports){
            if (pr.profit_ratio.compareTo(MIN_PROFIT_RATIO) == 1){
                in_profit.add(pr);
            }
            else{
                // List is ordered so break on first to not fit criteria.
                break;
            }
        }

        // If any profit reports are found to be IN profit
        if (in_profit.size() > 0){
            profitFound(in_profit);
        }
    }


    public void profitFound(ArrayList<ProfitReport> in_profit){

        sportsTrader.betlock.lock();

        // Create profit folder if it does not exist
        File profit_dir = new File(FileSystems.getDefault().getPath(".") + "/profit");
        if (!profit_dir.exists()){
            profit_dir.mkdir();
        }

        // Get the best (first) profit report
        ProfitReport best = in_profit.get(0);
        String timeString = Instant.now().toString().replace(":", "-").substring(0, 18) + "0";

        // Re-create the profit report with real values
        try {
            BigDecimal new_return = best.largest_min_return.add(new BigDecimal("0.05"));
            BigDecimal twenty = new BigDecimal(20);
            if (new_return.compareTo(twenty) == -1){
                new_return = twenty;
            }
            best = best.newProfitReport(new_return);

        } catch (InstantiationException | InvalidAttributesException e) {
            log.severe("Error while creating real profit report. Cancelling this round.");
            e.printStackTrace();
            return;
        }

        String profitString = best.profit_ratio.setScale(5, RoundingMode.HALF_UP).toString();
        String filename = timeString + " -  " + match.name + " " + profitString + ".json";
        p(best.toJSON(true), profit_dir.toString() + "/" + filename);

        // Check config allows bets and investment wouldn't be too much.
        if (!sportsTrader.PLACE_BETS || best.total_investment.compareTo(new BigDecimal("50.01")) == 1){
            print("PROFIT FOUND WITH MIN INV " + best.total_investment.toString() + " too high.");
            p(best.toJSON(true));

            in_profit.remove(0);
            if (in_profit.size() > 0){
                profitFound(in_profit);
            }
            return;
        }

        print("PLACING BETS");
        pp(best.toJSON(true));

        best.placedBets = placeBets(best.bet_orders);

        p(best.toJSON(true));
        print("BETS PLACED AND PROGRAM TERMINATED");
        System.exit(0);

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


}
