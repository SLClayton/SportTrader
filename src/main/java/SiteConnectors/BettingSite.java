package SiteConnectors;

import Bet.BetOffer;
import Sport.FootballMatch;
import Trader.SportsTrader;
import tools.MyLogHandler;
import tools.Requester;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.logging.Logger;

public abstract class BettingSite {

    public static Logger log = Logger.getLogger(SportsTrader.class.getName());

    public String name;
    public String ssldir;
    public Requester requester;

    public BigDecimal balance;

    public BettingSite(){

        if (System.getProperty("os.name").toLowerCase().contains("win")){
            ssldir = "C:/ssl/";
        }
        else{ // Assume linux
            ssldir = System.getProperty("user.home") + "/ssl/";
        }

        balance = new BigDecimal("0.00");
    }

    public abstract void login() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, IOException, URISyntaxException;

    public abstract String getSessionToken() throws IOException, CertificateException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, UnrecoverableKeyException, URISyntaxException;

    public abstract BigDecimal commission();

    public abstract BigDecimal minBet();

    public abstract SiteEventTracker getEventTracker();

    public abstract ArrayList<FootballMatch> getFootballMatches(Instant from, Instant until) throws IOException, URISyntaxException, InterruptedException;

    public BigDecimal ROI(BetOffer bet_offer, BigDecimal investment, boolean real){
        // Default ROI, commission on profits only

        BigDecimal stake = investment;
        BigDecimal ret;
        BigDecimal profit;
        BigDecimal commission;
        BigDecimal roi;

        if (bet_offer.isBack()){
            ret = stake.multiply(bet_offer.odds);
            profit = ret.subtract(stake);
            commission = profit.multiply(commission());
            roi = ret.subtract(commission);
        }
        else{ // Lay Bet
            BigDecimal lay = bet_offer.getLayFromStake(stake, real);
            profit = lay;
            commission = profit.multiply(commission());
            ret = stake.add(profit);
            roi = ret.subtract(commission);
        }

        if (real){
            roi = roi.setScale(2, RoundingMode.DOWN);
        }

        return roi;
    }

}
