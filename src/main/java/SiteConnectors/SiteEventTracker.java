package SiteConnectors;

import Sport.FootballMatch;
import Sport.Match;
import Trader.SportsTrader;
import tools.MyLogHandler;

import java.util.logging.Logger;

public abstract class SiteEventTracker {

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());


    public abstract boolean setupMatch(FootballMatch match);

    public SiteEventTracker(){
    }

}
