package SiteConnectors;

import Bet.FootballBet.FootballBet;
import Bet.MarketOddsReport;
import Sport.FootballMatch;
import Trader.SportsTrader;

import java.util.Set;
import java.util.logging.Logger;

public abstract class SiteEventTracker {

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    public MarketOddsReport marketOddsReport;
    public FootballMatch match;
    public Set<String> bet_blacklist;


    public abstract boolean setupMatch(FootballMatch match) throws Exception;

    public abstract void updateMarketOddsReport(FootballBet[] bets) throws Exception;


    public SiteEventTracker(){
    }

}
