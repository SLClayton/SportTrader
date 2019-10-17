package SiteConnectors;

import Bet.FootballBet.FootballBet;
import Bet.MarketOddsReport;
import Sport.FootballMatch;
import Trader.SportsTrader;

import java.io.IOException;
import java.net.URISyntaxException;
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

    public MarketOddsReport marketOddsReport;
    public Long marketOddsReportTime;
    public FootballMatch match;
    public Set<String> bet_blacklist;


    public SiteEventTracker(){
        bet_blacklist = new HashSet<>();
    }


    public abstract String name();


    public abstract boolean setupMatch(FootballMatch match) throws IOException,
            URISyntaxException, InterruptedException;


    public abstract MarketOddsReport getMarketOddsReport(FootballBet[] bets) throws Exception;

}
