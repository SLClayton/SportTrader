package SiteConnectors.Betdaq;

import Bet.Bet;
import Bet.MarketOddsReport;
import SiteConnectors.BettingSite;
import SiteConnectors.SiteEventTracker;
import com.globalbettingexchange.externalapi.MarketTypeWithPrices;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

public class BetdaqEventTracker extends SiteEventTracker {

    Betdaq betdaq;


    public BetdaqEventTracker(Betdaq betdaq) {
        super(betdaq);
        this.betdaq = betdaq;
    }

    @Override
    public String name() {
        return Betdaq.name;
    }

    @Override
    public MarketOddsReport _getMarketOddsReport(Collection<Bet> bets) throws InterruptedException {

        List<MarketTypeWithPrices> marketPrices = betdaq._getPrices()

                //TODO: this bit

        return null;
    }

    @Override
    public boolean siteSpecificSetup() throws IOException, URISyntaxException, InterruptedException {
        return true;
    }
}
