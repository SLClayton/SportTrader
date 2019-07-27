package Trader;

import Bet.*;
import Bet.FootballBet.FootballBetGenerator;
import SiteConnectors.BettingSite;
import SiteConnectors.SiteEventTracker;
import Sport.FootballMatch;
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

        // Create and setup new SiteEventTracker for each site
        ArrayList<String> failed_sites = new ArrayList<String>();
        int total_sites = sites.size();
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
                sites.remove(site_name);
                continue;
            }

            // Add tracker to map
            siteEventTrackers.put(site_name, eventTracker);
            log.info(String.format("Successfully setup %s in %s event tracker.", match, site_name));
        }

        log.info(String.format("%d/%d sites setup %s successfully. Failures %s",
                siteEventTrackers.size(), total_sites, match, failed_sites.toString()));

        // End thread if all setups fail
        if (siteEventTrackers.size() == 0){
            log.info(String.format("All sites failed to setup %s. Finishing Event Trader", match));
            return;
        }

        // Start MarketOddsReportWorker threads, 1 for each site
        for (int i=0; i<sites.size(); i++){
            MarketOddsReportWorker morw = new MarketOddsReportWorker(siteMarketOddsToGetQueue, siteEventTrackers);
            Thread thread = new Thread(morw);
            thread.start();
        }
        
        // Check for arbs constantly
        for (int i=0; true; i++){
            try {
                Instant start = Instant.now();
                checkArbs();
                Instant end = Instant.now();
                long ms = end.toEpochMilli() - start.toEpochMilli();

                log.info(String.format("Arbs checked for %s in %dms", match, ms));
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

                // Get site name from queue and update the corresponding event trader
                try {
                    String site_name = job_queue.take();
                    SiteEventTracker siteEventTracker = siteEventTrackers.get(site_name);
                    siteEventTrackers.get(site_name).updateMarketOddsReport(footballBetGenerator.getAllBets());
                    synchronized (siteEventTracker) {
                        siteEventTracker.notifyAll();
                    }
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
            siteMarketOddsToGetQueue.put(site_name);
        }
        ArrayList<MarketOddsReport> marketOddsReports = new ArrayList<MarketOddsReport>();
        // Wait for results and add them to list
        for (Map.Entry<String, SiteEventTracker> entry : siteEventTrackers.entrySet()) {
            SiteEventTracker siteEventTracker = entry.getValue();
            synchronized (siteEventTracker){
                siteEventTracker.wait();
            }
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
