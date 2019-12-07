package SiteConnectors.Betdaq;

import Bet.Bet;
import Bet.BetOrder;
import Bet.PlacedBet;
import SiteConnectors.BettingSite;
import SiteConnectors.SiteEventTracker;
import Sport.FootballMatch;
import Trader.EventTrader;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static tools.printer.print;

public class Betdaq extends BettingSite {

    public final static String name = "betdaq";
    public final static String id = "BD";

    public static final String WSDL = "http://api.betdaq.com/v2.0/API.wsdl";



    @Override
    public void login() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, IOException, URISyntaxException {

    }

    @Override
    public String getSessionToken() throws IOException, CertificateException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, UnrecoverableKeyException, URISyntaxException {
        return null;
    }

    @Override
    public BigDecimal commission() {
        return null;
    }


    @Override
    public String getName() {
        return name;
    }


    @Override
    public String getID() {
        return id;
    }

    @Override
    public BigDecimal minBackersStake() {
        return null;
    }


    @Override
    public void updateAccountInfo() throws InterruptedException, IOException, URISyntaxException {

    }

    @Override
    public SiteEventTracker getEventTracker() {
        return null;
    }


    @Override
    public ArrayList<FootballMatch> getFootballMatches(Instant from, Instant until) throws IOException, URISyntaxException, InterruptedException {
        return null;
    }



    @Override
    public List<PlacedBet> placeBets(List<BetOrder> betOrders, BigDecimal MIN_ODDS_RATIO)
            throws IOException, URISyntaxException {

        return null;
    }


    public static void main(String[] args){




        print("END");
    }



}
