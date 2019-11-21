package SiteConnectors;

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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
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
    public Set<String> bet_blacklist;

    public MarketOddsReport lastMarketOddsReport;
    public Instant lastMarketOddsReport_start_time;
    public Instant lastMarketOddsReport_end_time;


    public SiteEventTracker(BettingSite site, EventTrader eventTrader){
        this.site = site;
        this.eventTrader = eventTrader;
        sportData = SportsTrader.getSportData();
        bet_blacklist = new HashSet<>();
    }


    public abstract String name();


    public Long lastMarketOddsTime(){
        if (lastMarketOddsReport == null){
            return null;
        }
        return lastMarketOddsReport_end_time.toEpochMilli() - lastMarketOddsReport_start_time.toEpochMilli();
    }


    public abstract MarketOddsReport getMarketOddsReport(FootballBet[] bets) throws Exception;


    public boolean setupMatch(Match setup_match) throws InterruptedException, IOException, URISyntaxException {

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
                match = potential_match;
                break;
            }
        }
        // If no match found, ensure matches are verified with IDs and check again
        if (match == null){
            if (!setup_match.isVerified()){
                if (!setup_match.verify()){
                    log.warning(String.format("Unable to verify setup match for %s", setup_match.toString()));
                    return false;
                }
            }

            // Check again
            for (Match potential_match: possible_matches){
                if (!potential_match.isVerified()){
                    potential_match.verify();
                }
                if (Boolean.TRUE.equals(setup_match.same_match(potential_match))){
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
