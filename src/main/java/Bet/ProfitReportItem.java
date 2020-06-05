package Bet;

import SiteConnectors.BettingSite;
import Bet.PlacedBet;

import java.math.BigDecimal;

public interface ProfitReportItem {

    BigDecimal getInvestment();
    BigDecimal getReturn();
    BetOffer getBetOffer();
    BettingSite getSite();
    Bet getBet();
    PlacedBet.State getState();
}
