package Trader;

import Bet.Bet;
import Bet.FootballBet.FootballBet;
import Bet.FootballBet.FootballBetGenerator;
import SiteConnectors.BettingSite;
import SiteConnectors.SiteEventTracker;
import Sport.FootballMatch;
import Sport.Match;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class EventTrader implements Runnable {

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    public Thread thread;

    public FootballMatch match;
    public HashMap<String, BettingSite> sites;
    public HashMap<String, SiteEventTracker> siteEventTrackers;

    public FootballBetGenerator footballBetGenerator;
    public Bet[][] tautologies;

    public EventTrader(FootballMatch match, HashMap<String, BettingSite> sites, FootballBetGenerator footballBetGenerator){
        this.match = match;
        this.sites = sites;
        this.footballBetGenerator = footballBetGenerator;
        siteEventTrackers = new HashMap<String, SiteEventTracker>();
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
        
        
        for (int i=0; true; i++){
            checkArbs();
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
                    siteEventTrackers.get(site_name).updateMarketOddsReport(footballBetGenerator.getAllBets());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }


    private void checkArbs() {

        // TODO setup the launch of the worker threads and do checkArbs
    }
}
