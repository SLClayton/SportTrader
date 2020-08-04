package Bet;

import SiteConnectors.BettingSite;
import Sport.Event;
import org.json.simple.JSONObject;

import java.math.BigDecimal;

public interface SiteBet {

    Event getEvent();
    Bet getBet();
    BettingSite getSite();
    BigDecimal getInvestment();
    BigDecimal getReturn();
    JSONObject toJSON();
    BigDecimal avgOdds();
    BigDecimal getBackersStake();

}
