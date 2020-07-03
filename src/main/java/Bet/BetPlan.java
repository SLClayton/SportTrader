package Bet;

import java.util.List;

public class BetPlan {
    /*
        A single bet with an amount to place for the same bet on different sites to get the best deal

        eg
        Betfair:  place 2.55
        Smarkets: place 0.44
        etc...
     */


    public final Bet bet;
    public final List<BetExchange> betExchanges;


    public BetPlan(Bet bet, List<BetExchange> betExchanges){
        this.bet = bet;
        this.betExchanges = betExchanges;
    }
}
