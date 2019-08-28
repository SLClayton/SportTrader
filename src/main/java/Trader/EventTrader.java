package Trader;

import Bet.*;
import Bet.FootballBet.FootballBetGenerator;
import SiteConnectors.BettingSite;
import SiteConnectors.SiteEventTracker;
import Sport.FootballMatch;
import org.apache.commons.codec.binary.StringUtils;
import org.json.simple.JSONArray;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import static tools.printer.*;

public class EventTrader implements Runnable {

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    public Thread thread;

    public FootballMatch match;
    public HashMap<String, BettingSite> sites;
    public HashMap<String, SiteEventTracker> siteEventTrackers;
    public BlockingQueue<String> siteMarketOddsToGetQueue;

    public FootballBetGenerator footballBetGenerator;
    public ArrayList<Tautology> tautologies;

    public EventTrader(FootballMatch match, HashMap<String, BettingSite> sites, FootballBetGenerator footballBetGenerator){
        this.match = match;
        this.sites = sites;
        this.footballBetGenerator = footballBetGenerator;
        tautologies = this.footballBetGenerator.getAllTautologies();
        siteEventTrackers = new HashMap<String, SiteEventTracker>();
        siteMarketOddsToGetQueue = new LinkedBlockingQueue<>();
    }


    @Override
    public void run() {
        log.info(String.format("Running new Event Trader for %s.", match));

        // Create and setup new SiteEventTracker for each site, this manages the data for this particular
        // match for each particular betting site.
        ArrayList<String> failed_sites = new ArrayList<String>();
        int total_sites = sites.size();
        HashMap<String, BettingSite> accepted_sites = new HashMap<>();
        for (Map.Entry<String, BettingSite> entry: sites.entrySet()){
            String site_name = entry.getKey();
            BettingSite site = entry.getValue();

            // Try to setup match, remove site if fail
            SiteEventTracker eventTracker = site.getEventTracker();
            boolean setup_success = false;
            try {
                setup_success = eventTracker.setupMatch(match);
            } catch (Exception e) {
                e.printStackTrace();
                setup_success = false;
            }
            if (!(setup_success)){
                log.warning(String.format("Unsuccessful setup of %s for %s event tracker", match, site_name));
                failed_sites.add(site_name);
                continue;
            }
            accepted_sites.put(site_name, site);

            // Add tracker to map
            siteEventTrackers.put(site_name, eventTracker);
            log.info(String.format("Successfully setup %s in %s event tracker.", match, site_name));
        }

        sites = accepted_sites;
        log.info(String.format("%d/%d sites setup %s successfully. Failures: %s",
                siteEventTrackers.size(), total_sites, match, failed_sites.toString()));


        // End thread if all setups fail
        if (siteEventTrackers.size() == 0){
            log.info(String.format("All sites failed to setup %s. Finishing Event Trader", match));
            return;
        }

        // Blocker for testing.
        if (true){
            return;
        }

        // Start MarketOddsReportWorker threads, 1 for each site
        // This is a thread for each site to go off and collect new odds report asynchronously
        for (int i=0; i<sites.size(); i++){
            MarketOddsReportWorker morw = new MarketOddsReportWorker(siteMarketOddsToGetQueue, siteEventTrackers);
            Thread thread = new Thread(morw);
            thread.start();
        }
        
        // Check for arbs constantly
        ArrayList<Long> arb_times = new ArrayList<>();
        int max_times = 100;
        for (int i=0; true; i++){
            try {
                Instant start = Instant.now();
                checkArbs();
                Instant end = Instant.now();
                long ms = end.toEpochMilli() - start.toEpochMilli();
                arb_times.add(ms);

                if (arb_times.size() >= max_times){
                    long avg_ms = 0;
                    for (long arb_time: arb_times){
                        avg_ms += arb_time;
                    }
                    avg_ms = avg_ms / arb_times.size();
                    String ms_string = String.valueOf(avg_ms);
                    String padding = "";
                    while (padding.length() < (4 - ms_string.length())){ padding += " "; }

                    log.info(String.format("Arb Checks. %d avg: %d ms%s for %s", max_times, avg_ms, padding, match ));
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

        // Ceate list of profit reports with profits over min_prof_margain
        BigDecimal min_profit_margain = new BigDecimal("0.00");
        ArrayList<ProfitReport> in_profit = new ArrayList<ProfitReport>();
        for (ProfitReport pr: tautologyProfitReports){
            if (pr.profit_ratio.compareTo(min_profit_margain) == 1){
                in_profit.add(pr);
            }
            else{
                // List is ordered so break on first to not fit criteria.
                break;
            }
        }

        // If any profit reports are found to be IN profit
        if (in_profit.size() > 0){
            log.info(String.format("PROFIT FOUND IN %s", match));
        }
    }
}
