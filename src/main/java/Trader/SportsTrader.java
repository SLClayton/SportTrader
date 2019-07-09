package Trader;

import SiteConnectors.Betfair;
import SiteConnectors.BetfairEventTracker;
import SiteConnectors.SiteEventTracker;
import Sport.FootballMatch;
import org.apache.commons.logging.Log;
import tools.MyLogHandler;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

public class SportsTrader {

    private static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    public SportsTrader(){
        log.setUseParentHandlers(false);
        log.setLevel(Level.ALL);
        log.addHandler(new MyLogHandler());
    }


    public void run(){

    }


    public static void main(String[] args){
        //LogManager.getLogManager().reset();

        SportsTrader st = new SportsTrader();
        st.run();

        try {
            Betfair b = new Betfair();

            BetfairEventTracker be = (BetfairEventTracker) b.getEventTracker();
            FootballMatch fm = new FootballMatch(Instant.parse("2019-07-09T22:15:00.000Z"), "Vitoria BA ", "Cuiaba");

            be.setupMatch(fm);
            sleep(20000);
            be.updateMarketData();



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
