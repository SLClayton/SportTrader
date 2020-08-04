package SiteConnectors;

import Bet.BetPlan;
import Bet.PlacedBet;
import Sport.FootballMatch;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.List;

public class TestBetSite extends BettingSite {


    BigDecimal win_com_rate;
    BigDecimal loss_com_rate;
    String name;
    String id;
    BigDecimal min_odds = new BigDecimal("1.01");
    BigDecimal max_odds = new BigDecimal(1000);
    BigDecimal min_stake;


    public TestBetSite(int number, String win_com, String loss_com, String min_stake){
        this.name = String.format("TestSite%s", number);
        this.id = String.format("T%s", number);
        this.win_com_rate = new BigDecimal(win_com);
        this.loss_com_rate = new BigDecimal(loss_com);
        this.min_stake = new BigDecimal(min_stake);
    }


    @Override
    public void login() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, URISyntaxException, InterruptedException, ParseException {
        // nothing
    }

    @Override
    public String getSessionToken() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, UnrecoverableKeyException, URISyntaxException, ParseException {
        return null;

    }

    @Override
    public BigDecimal winCommissionRate() {
        return win_com_rate;
    }

    @Override
    public BigDecimal lossCommissionRate() {
        return loss_com_rate;
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
    public BigDecimal minValidOdds() {
        return min_odds;
    }

    @Override
    public BigDecimal maxValidOdds() {
        return max_odds;
    }

    @Override
    public BigDecimal minBackersStake() {
        return min_stake;
    }


    @Override
    public void safe_exit() {
    }

    @Override
    public void updateAccountInfo() throws InterruptedException, IOException, URISyntaxException {

    }

    @Override
    public SiteEventTracker getEventTracker() {
        return null;
    }

    @Override
    public List<FootballMatch> getFootballMatches(Instant from, Instant until) throws IOException, URISyntaxException, InterruptedException {
        return null;
    }

    @Override
    public List<PlacedBet> placeBets(List<BetPlan> betPlans, BigDecimal odds_ratio_buffer) throws IOException, URISyntaxException {
        return null;
    }
}
