package SiteConnectors;

import Bet.FootballBet.FootballBet;
import Bet.MarketOddsReport;
import Sport.FootballMatch;
import Trader.EventTrader;
import Trader.SportsTrader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
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

    public EventTrader eventTrader;

    public FootballMatch match;
    public Set<String> bet_blacklist;

    public MarketOddsReport lastMarketOddsReport;
    public Instant lastMarketOddsReport_start_time;
    public Instant lastMarketOddsReport_end_time;


    public SiteEventTracker(EventTrader eventTrader){
        this.eventTrader = eventTrader;
        bet_blacklist = new HashSet<>();
    }


    public abstract String name();


    public Long lastMarketOddsTime(){
        if (lastMarketOddsReport == null){
            return null;
        }
        return lastMarketOddsReport_end_time.toEpochMilli() - lastMarketOddsReport_start_time.toEpochMilli();
    }


    public abstract boolean setupMatch(FootballMatch match) throws IOException,
            URISyntaxException, InterruptedException;


    public abstract MarketOddsReport getMarketOddsReport(FootballBet[] bets) throws Exception;

}
