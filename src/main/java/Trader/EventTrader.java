package Trader;

import Bet.Bet;
import Bet.FootballBet.FootballBetGenerator;
import SiteConnectors.BettingSite;
import Sport.Match;

import java.util.HashMap;

public class EventTrader implements Runnable {

    public Thread thread;

    public Match match;
    public HashMap<String, BettingSite> sites;

    public FootballBetGenerator footballBetGenerator;
    public Bet[][] tautologies;

    public EventTrader(Match match, HashMap<String, BettingSite> sites, FootballBetGenerator footballBetGenerator){
        this.match = match;
        this.footballBetGenerator = footballBetGenerator;
    }


    @Override
    public void run() {

    }
}
