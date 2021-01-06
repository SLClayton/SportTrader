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

import static tools.printer.sf;

public class MarketOddsReportWorker implements Runnable {

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    public final SportsTrader sportsTrader;
    public final int id;
    public final Instant created_time;
    public final String name;
    public Thread thread;
    public BlockingQueue<RequestHandler> queue;
    public boolean waiting;

    public String status = "new";
    public Instant time = Instant.now();
    public SiteEventTracker siteEventTracker;

    private boolean exit_flag;


    public MarketOddsReportWorker(SportsTrader sportsTrader, int id, BlockingQueue<RequestHandler> queue){
        created_time = Instant.now();
        this.sportsTrader = sportsTrader;
        this.id = id;
        name = sf("MORW-%s", id);
        waiting = false;
        exit_flag = false;
        this.queue = queue;
        thread = new Thread(this);
        thread.setName(name);
    }


    public void safe_exit(boolean interrupt){
        exit_flag = true;
        if (interrupt){
            thread.interrupt();
        }
    }

    public void safe_exit(){
        safe_exit(true);
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


    @Override
    public void run() {

        RequestHandler requestHandler = null;

        while (!exit_flag && !sportsTrader.exit_flag){
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
                log.severe(sf("Exception %s in market odds report worker", e.toString()));
                e.printStackTrace();
                if (requestHandler != null){
                    requestHandler.setResponse(MarketOddsReport.ERROR(
                            String.format("General exception in MOR worker loop. %s", e)));
                }
            }
        }

        log.info(String.format("Ending MarketOddsReport worker."));
    }

    @Override
    public String toString() {
        return name;
    }
}
