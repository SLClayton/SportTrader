package SiteConnectors;

import Bet.BetOffer;
import Bet.FootballBet.FootballBet;
import Sport.FootballMatch;
import Trader.SportsTrader;

import java.util.HashMap;
import java.util.logging.Logger;

public abstract class SiteEventTracker {

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());


    public abstract boolean setupMatch(FootballMatch match);

    public abstract HashMap<String, BetOffer[]> getMarketOddsReport(FootballBet[] bets) throws Exception;


    public SiteEventTracker(){
    }

}
