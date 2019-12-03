package SiteConnectors;

import Bet.Bet;
import Bet.FootballBet.FootballBet;
import Bet.MarketOddsReport;
import Sport.FootballMatch;
import Sport.Match;
import Trader.EventTrader;
import Trader.MarketOddsReportWorker;
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

import static net.dongliu.commons.Prints.print;

public abstract class SiteEventTracker {

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());


    public BettingSite site;
    public EventTrader eventTrader;
    public SportData sportData;

    public Match match;
    public Collection<Bet> bets;
    public Set<String> bet_blacklist;

    public Instant lastMarketOddsReport_start_time;
    public Instant lastMarketOddsReport_end_time;

    public MarketOddsReportWorker marketOddsReportWorker;

    public String status = "new_set";
    public Instant time = Instant.now();


    public String getStatus(){
        return status;
    }

    public void setStatus(String s){
        status = s;
        time = Instant.now();
    }

    public long getTime(){
        return Instant.now().getEpochSecond() - time.getEpochSecond();
    }



    public SiteEventTracker(BettingSite site, EventTrader eventTrader, Collection<Bet> bets){
        this.site = site;
        this.eventTrader = eventTrader;
        this.bets = bets;
        sportData = SportsTrader.getSportData();
        bet_blacklist = new HashSet<>();
    }


    public abstract String name();


    public void safe_exit(){
        marketOddsReportWorker.safe_exit();
    }


    public Long lastMarketOddsTime(){
        if (lastMarketOddsReport_end_time == null || lastMarketOddsReport_end_time == null){
            return null;
        }
        return lastMarketOddsReport_end_time.toEpochMilli() - lastMarketOddsReport_start_time.toEpochMilli();
    }


    public RequestHandler requestMarketOddsReport(Collection<Bet> bets){
        // Pack up args
        Object[] args = new Object[]{this, bets};
        RequestHandler rh = new RequestHandler(args);

        // Pass into queue and return the handler
        eventTrader.sportsTrader.marketOddsReportRequestQueue.add(rh);

        // Add more workers if not enough are waiting
        if (eventTrader.sportsTrader.MORWwaiting() <= 1){
            eventTrader.sportsTrader.newMarketOddsReportWorker();
        }

        return rh;
    }



    public MarketOddsReport getMarketOddsReport(Collection<Bet> bets) throws InterruptedException{
        // Its important that he MarketOddsReport isn't null when pass back so ensure this with wrapper
        MarketOddsReport mor = _getMarketOddsReport(bets);
        if (mor == null){
            String msg = String.format("getMarketOdds report for %s %s has retuned null when it should never do so.",
                    site, match);
            log.severe(msg);
            mor = MarketOddsReport.ERROR(msg);
        }
        return mor;
    }


    public abstract MarketOddsReport _getMarketOddsReport(Collection<Bet> bets) throws InterruptedException;


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
