package Trader;

import Bet.Bet;
import Bet.MarketOddsReport;
import SiteConnectors.RequestHandler;
import SiteConnectors.SiteEventTracker;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class MarketOddsReportWorker implements Runnable {

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    public Thread thread;
    public BlockingQueue<RequestHandler> queue;
    public boolean waiting;

    public String status = "new";
    public Instant time = Instant.now();
    public SiteEventTracker siteEventTracker;

    private boolean exit_flag;


    public MarketOddsReportWorker(BlockingQueue<RequestHandler> queue){
        waiting = false;
        exit_flag = false;
        this.queue = queue;
        thread = new Thread(this);
    }


    public void safe_exit(boolean interrupt){
        exit_flag = true;
        if (interrupt){
            thread.interrupt();
        }
    }


    public void start(){
        thread.start();
    }


    public void interrupt(){
        thread.interrupt();
    }


    public boolean isWaiting(){
        return waiting == true;
    }

    public static Map<String, Integer> status_sums(List<MarketOddsReportWorker> mors){
        Map<String, Integer> sums = new HashMap<>();

        for (int i=0; i<mors.size(); i++){
            MarketOddsReportWorker mor = mors.get(i);

            String status = String.valueOf(mor.status);
            Integer current = sums.get(status);

            if (current == null){
                sums.put(status, 1);
            }
            else{
                sums.put(status, current + 1);
            }
        }

        return sums;
    }

    public static Map<String, Map<String, Integer>> site_sums(List<MarketOddsReportWorker> mors){
        Map<String, Map<String, Integer>> site_maps = new HashMap<>();

        for (int i=0; i<mors.size(); i++){
            MarketOddsReportWorker mor = mors.get(i);

            // Get Site name
            String site = "null";
            if (mor.siteEventTracker != null){
                site = String.valueOf(mor.siteEventTracker.site.getName());
            }

            // Get status name
            String status = String.valueOf(mor.status);


            Map<String, Integer> site_map = site_maps.get(site);
            if (site_map == null){
                site_map = new HashMap<String, Integer>();
                site_maps.put(site, site_map);
            }

            Integer current_status_count = site_map.get(status);

            if (current_status_count == null){
                site_map.put(status, 1);
            }
            else{
                site_map.put(status, current_status_count + 1);
            }
        }

        return site_maps;
    }


    @Override
    public void run() {

        RequestHandler requestHandler = null;

        while (!exit_flag){
            try{
                status = "newloop";
                this.siteEventTracker = null;
                requestHandler = null;

                // Wait for a job from the queue
                waiting = true;
                status = "waiting";
                requestHandler = queue.take();
                waiting = false;

                status = "unpacking";

                requestHandler.marketOddsReportWorker = this;

                // Unpack Site Event Tracker and bets from request.
                Object[] arguments = (Object[]) requestHandler.request;
                SiteEventTracker siteEventTracker = (SiteEventTracker) arguments[0];
                Collection<Bet> bets = (Collection<Bet>) arguments[1];

                this.siteEventTracker = siteEventTracker;


                // If bets null, return error mor and finish loop
                if (bets == null){
                    String error = String.format("Bets passed into MOR is null.");
                    log.severe(error);
                    requestHandler.setResponse(MarketOddsReport.ERROR(error));
                    continue;
                }

                status = "MORequesting";
                MarketOddsReport mor = siteEventTracker.getMarketOddsReport(bets);

                // Apply mor to request handler
                status = "settingResponse";
                requestHandler.setResponse(mor);

                status = "complete";
            }
            catch (InterruptedException e){
                waiting = false;
                status = "interrupted";
                log.fine(String.format("MOR worker interuppted"));
                if (requestHandler != null){
                    requestHandler.setResponse(MarketOddsReport.TIMED_OUT());
                }
            }
            catch (Exception e){
                status = "exception";
                waiting = false;
                log.severe("Exception %s in market odds report worker");
                e.printStackTrace();
                if (requestHandler != null){
                    requestHandler.setResponse(MarketOddsReport.ERROR(
                            String.format("General exception in MOR worker loop. %s", e)));
                }
            }
        }

        log.info(String.format("Ending MarketOddsReport worker."));
    }
}
