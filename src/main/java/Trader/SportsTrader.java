package Trader;

import Bet.*;
import Bet.FootballBet.FootballBet;
import Bet.FootballBet.FootballBetGenerator;
import SiteConnectors.Betfair;
import SiteConnectors.BetfairEventTracker;
import SiteConnectors.SiteEventTracker;
import Sport.FootballMatch;
import org.apache.commons.logging.Log;
import org.json.simple.JSONObject;
import tools.MyLogHandler;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;
import static tools.printer.p;
import static tools.printer.print;

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
            FootballMatch fm = new FootballMatch(Instant.parse("2019-07-11T00:30:00.000Z"), "Atletico PR ", "Flamengo");

            be.setupMatch(fm);

            FootballBetGenerator fbg = new FootballBetGenerator();
            FootballBet[] bets = fbg.getAllBets();

            for (FootballBet bbb: bets){
                print(bbb.id());
            }

            HashMap<String, BetOffer[]> report = be.getMarketOddsReport(bets);


            for (Map.Entry<String, BetOffer[]> entry : report.entrySet()) {
                print(entry.getKey());
                for (BetOffer betoffer: entry.getValue()){
                    print(betoffer.toString());
                    print("---");
                }
                print("\n-----------------------------------\n");
            }

            print(report.size());



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
