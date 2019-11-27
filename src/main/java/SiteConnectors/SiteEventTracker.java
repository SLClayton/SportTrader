package SiteConnectors;

import Bet.Bet;
import Bet.FootballBet.FootballBet;
import Bet.MarketOddsReport;
import Sport.FootballMatch;
import Sport.Match;
import Trader.EventTrader;
import Trader.SportsTrader;

import java.awt.image.AreaAveragingScaleFilter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public abstract class SiteEventTracker {

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());


    public BettingSite site;
    public EventTrader eventTrader;
    public SportData sportData;

    public Match match;
    public Collection<Bet> bets;
    public Set<String> bet_blacklist;

    public MarketOddsReport lastMarketOddsReport;
    public Instant lastMarketOddsReport_start_time;
    public Instant lastMarketOddsReport_end_time;

    public MarketOddsReportWorker marketOddsReportWorker;
    public BlockingQueue<RequestHandler> marketOddsReportRequestQueue;


    public SiteEventTracker(BettingSite site, EventTrader eventTrader, Collection<Bet> bets){
        this.site = site;
        this.eventTrader = eventTrader;
        this.bets = bets;
        sportData = SportsTrader.getSportData();
        bet_blacklist = new HashSet<>();

        marketOddsReportRequestQueue = new ArrayBlockingQueue<>(1);
        marketOddsReportWorker = new MarketOddsReportWorker(this, marketOddsReportRequestQueue);
        marketOddsReportWorker.start();
    }


    public abstract String name();


    public void safe_exit(){
        marketOddsReportWorker.safe_exit();
    }


    public Long lastMarketOddsTime(){
        if (lastMarketOddsReport == null){
            return null;
        }
        return lastMarketOddsReport_end_time.toEpochMilli() - lastMarketOddsReport_start_time.toEpochMilli();
    }


    public RequestHandler requestMarketOddsReport(Collection<Bet> bets){
        RequestHandler rh = new RequestHandler(bets);
        try{
            marketOddsReportRequestQueue.add(rh);
        } catch (IllegalStateException e){
            log.severe(String.format("Trying to add job to marketOddsReport worker queue for %s %s " +
                    "but it already has a job in it.", site, match));
            return null;
        }
        return rh;
    }


    public class MarketOddsReportWorker implements Runnable {

        public SiteEventTracker siteEventTracker;
        public Thread thread;
        public BlockingQueue<RequestHandler> queue;

        private boolean exit_flag;


        public MarketOddsReportWorker(SiteEventTracker siteEventTracker, BlockingQueue<RequestHandler> queue){

            exit_flag = false;
            this.siteEventTracker = siteEventTracker;
            this.queue = queue;
            thread = new Thread(this);
        }


        public void safe_exit(){
            exit_flag = true;
            thread.interrupt();
        }


        public void start(){
            thread.start();
        }


        @Override
        public void run() {

            while (!exit_flag){

                RequestHandler requestHandler = null;

                // Wait for an item appears in the job queue.
                try {
                    requestHandler = queue.take();
                } catch (InterruptedException e) {
                    log.fine(String.format("Site event tracker for %s %s interuppted.",
                            siteEventTracker.site, siteEventTracker.match));
                    continue;
                }

                // Restart if exit flag triggered or request handler not found.
                if (requestHandler == null || exit_flag) {
                    continue;
                }

                // Collect bets to use in report, from request handler
                Collection<Bet> bets = (Collection<Bet>) requestHandler.request;
                if (bets == null){
                    String error = String.format("Bets passed into marketoddsreportworker is null.");
                    log.severe(error);
                    requestHandler.setResponse(MarketOddsReport.ERROR(error));
                }

                // Get the market odds report for this event tracker
                MarketOddsReport mor;
                try {
                    mor = siteEventTracker.getMarketOddsReport(bets);
                } catch (InterruptedException e) {
                    log.warning(String.format("%s mor worker was interrupted."));
                    continue;
                } catch (Exception e){
                    log.severe(String.format("Exception '%s' when getting MarketOddsReport for %s ",
                            e.toString(), siteEventTracker.match));
                    mor = MarketOddsReport.ERROR(e.toString());
                }
                if (mor == null){
                    mor = MarketOddsReport.ERROR("getMarketOddsReport returned null");
                }

                // Apply mor to request handler
                requestHandler.setResponse(mor);
            }

            log.info(String.format("Ending MarketOddsReport worker for %s %s",
                    siteEventTracker.site, siteEventTracker.match));

        }
    }


    public abstract MarketOddsReport getMarketOddsReport(Collection<Bet> bets) throws InterruptedException;


    public boolean setupMatch(Match setup_match) throws InterruptedException, IOException, URISyntaxException {

        log.fine(String.format("Setting up match for %s in %s", setup_match, site));
        Instant start = setup_match.start_time.minus(1, ChronoUnit.SECONDS);
        Instant end = setup_match.start_time.plus(1, ChronoUnit.SECONDS);

        // Depending on match type (sport) search site for relevant events
        ArrayList<Match> possible_matches = new ArrayList<>();
        if (setup_match instanceof FootballMatch){
            possible_matches.addAll(site.getFootballMatches(start, end));
        }
        else{
            log.severe(String.format("Setup match '%s' is not of valid type.", setup_match.toString()));
            return false;
        }

        // Check if any of the searched events match the setup_match from local data only
        match = null;
        for (Match potential_match: possible_matches){
            if (Boolean.TRUE.equals(setup_match.same_match(potential_match))){
                log.fine(String.format("Matched %s with %s from %s with local data.", setup_match, potential_match, site));
                match = potential_match;
                break;
            }
        }
        // If no match found, ensure matches are verified with IDs and check again
        if (match == null){
            if (setup_match.notVerified()){
                log.fine(String.format("Trying to verify setup match %s.", setup_match));
                if (!setup_match.verify()){
                    log.warning(String.format("Unable to verify setup match for %s", setup_match.toString()));
                    return false;
                }
            }

            // Check again
            for (Match potential_match: possible_matches){
                if (potential_match.notVerified()){
                    log.fine(String.format("Trying to verify potential match %s.", potential_match));
                    potential_match.verify();
                }
                if (Boolean.TRUE.equals(setup_match.same_match(potential_match))){
                    log.fine(String.format("Matched %s with %s from %s after verifying matches.",
                            setup_match, potential_match, site));
                    match = potential_match;
                    break;
                }
            }
        }

        // If match is still not found then fail this setup process.
        if (match == null){
            log.warning(String.format("No matches found for %s in %s. Checked %d: %s",
                    setup_match.toString(), site.toString(), possible_matches.size(), possible_matches.toString()));
            return false;
        }

        // If gotten this far then a match will have been assigned to the match variable.
        return siteSpecificSetup();
    }


    public abstract boolean siteSpecificSetup() throws IOException, URISyntaxException, InterruptedException;

}
