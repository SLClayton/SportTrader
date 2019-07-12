package SiteConnectors;

import Bet.FootballBet.FootballBet;
import Sport.FootballMatch;
import Trader.SportsTrader;

import java.util.logging.Logger;

public abstract class SiteEventTracker {

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());


    public abstract boolean setupMatch(FootballMatch match) throws Exception;

    public abstract void updateMarketOddsReport(FootballBet[] bets) throws Exception;


    public SiteEventTracker(){
    }

}
