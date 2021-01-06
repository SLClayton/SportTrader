package SiteConnectors;

import Bet.Bet;
import Bet.Bet.BetType;
import Bet.BetPlan;
import Bet.FootballBet.FootballBet;
import Bet.FootballBet.FootballResultBet;
import Bet.PlacedBet;
import SiteConnectors.Betdaq.Betdaq;
import Sport.FootballMatch;
import Trader.Config;
import Trader.SportsTrader;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import tools.Requester;

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
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import static java.lang.System.exit;
import static tools.printer.*;

public abstract class BettingSite {

    public static Logger log = Logger.getLogger(SportsTrader.class.getName());
    private Config config = SportsTrader.config;

    //public final static String name = "ABSTRACT_BETTING_SITE";
    public String ssldir;
    public Requester requester;

    public boolean exit_flag;

    public BigDecimal balance;
    public BigDecimal exposure;

    public Lock balanceLock = new ReentrantLock();
    public BigDecimal balance_buffer = new BigDecimal("10.00");


    public JSONObject RATE_LIMITED_JSON;
    public final static String RATE_LIMITED = "RATE_LIMITED";



    public BettingSite() {
        exit_flag = false;

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            ssldir = "C:/ssl/";
        } else { // Assume linux
            ssldir = System.getProperty("user.home") + "/ssl/";
        }
        ssldir = config.SSL_DIR;

        balance = new BigDecimal("0.00");

        RATE_LIMITED_JSON = new JSONObject();
        RATE_LIMITED_JSON.put(RATE_LIMITED, RATE_LIMITED);
    }


    public abstract void login() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, IOException, URISyntaxException, InterruptedException, ParseException;


    public abstract String getSessionToken() throws IOException, CertificateException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, UnrecoverableKeyException, URISyntaxException, ParseException;


    public abstract BigDecimal winCommissionRate();

    public abstract BigDecimal lossCommissionRate();


    public abstract String getName();


    public abstract String getID();

    public abstract BigDecimal minValidOdds();
    public abstract BigDecimal maxValidOdds();


    public abstract BigDecimal minBackersStake();


    public BigDecimal minLayersStake(BigDecimal odds) {
        return Bet.backStake2LayStake(minBackersStake(), odds).setScale(2, RoundingMode.UP);
    }



    public static Set<String> getIDs(Collection<BettingSite> sites){
        Set<String> ids = new HashSet<>();
        for (BettingSite site: sites){
            ids.add(site.getID());
        }
        return ids;
    }


    public BigDecimal getBalance() {
        balanceLock.lock();
        BigDecimal b = balance;
        balanceLock.unlock();
        return b;
    }


    public void setBalance(BigDecimal new_balance){
        balanceLock.lock();
        balance = new_balance;
        balanceLock.unlock();
    }


    public abstract void safe_exit();


    public abstract void updateAccountInfo() throws InterruptedException, IOException, URISyntaxException;


    public boolean useBalance(BigDecimal amount) {
        // A request for using the balance, removes balance and returns true if successful

        balanceLock.lock();
        boolean result = false;
        BigDecimal new_balance = balance.subtract(amount);

        if (new_balance.compareTo(balance_buffer) == 1) {
            result = true;
            balance = new_balance;
        } else {
            result = false;
            log.warning(String.format("Request to use %s of %s balance failed as this would take it below the buffer of %s.",
                    amount.toString(), this.getName(), balance_buffer.toString()));
        }

        balanceLock.unlock();
        return result;
    }


    public abstract SiteEventTracker getEventTracker();



    public BigDecimal investment2Stake(BigDecimal investment) {
        return Bet.investment2Stake(investment, lossCommissionRate());
    }


    public BigDecimal investmentNeededForStake(BigDecimal stake) {
        return Bet.investmentNeededForStake(stake, lossCommissionRate());
    }


    public BigDecimal getValidPayout(BigDecimal payout){
        // Should return how the particular betting site rounds exact figures
        // for bet results.

        return payout.setScale(2, RoundingMode.HALF_UP);
    }


    public BigDecimal getValidOdds(BigDecimal odds, RoundingMode roundingMode){
        // Should return the odds value, rounded to a valid Odds ladder price for
        // the particular betting site.

        return odds.setScale(2, roundingMode);
    }


    public BigDecimal getValidStake(BigDecimal stake, RoundingMode roundingMode){
        // Should return the closest valid stake for a particular betting site.
        return stake.setScale(2, roundingMode);
    }




    public abstract List<FootballMatch> getFootballMatches(Instant from, Instant until)
            throws IOException, URISyntaxException, InterruptedException;


    public List<FootballMatch> getFootballMatches() throws InterruptedException, IOException, URISyntaxException {
        return getFootballMatches(Instant.now(), Instant.now().plus(24, ChronoUnit.HOURS));
    }




    public PlacedBet placeBet(BetPlan betPlan, BigDecimal MIN_ODDS_RATIO) throws IOException, URISyntaxException {
        List<BetPlan> betPlans = new ArrayList<>();
        betPlans.add(betPlan);
        List<PlacedBet> placedBets = placeBets(betPlans, MIN_ODDS_RATIO);
        if (placedBets.size() != 1){
            log.severe(String.format("SENT 1 BETORDER BUT GOT BACK %s PLACED BETS", placedBets.size()));
        }
        return placedBets.get(0);
    }


    public abstract List<PlacedBet> placeBets(List<BetPlan> betPlans, BigDecimal odds_ratio_buffer)
            throws IOException, URISyntaxException;



    public BigDecimal ROI(BetType betType, BigDecimal odds, BigDecimal investment){
        return Bet.ROI(betType, odds, investment, winCommissionRate(), lossCommissionRate());
    }

    public BigDecimal return2Investment(BetType betType, BigDecimal odds, BigDecimal target_return){
        return Bet.return2Investment(betType, odds, target_return, winCommissionRate(), lossCommissionRate());
    }

    public BigDecimal return2Stake(BetType betType, BigDecimal odds, BigDecimal target_return){
        return Bet.return2Stake(betType, odds, target_return, winCommissionRate(), lossCommissionRate());
    }

    public BigDecimal return2BackStake(BetType betType, BigDecimal odds, BigDecimal target_return){
        return Bet.return2BackStake(betType, odds, target_return, winCommissionRate(), lossCommissionRate());
    }



    @Override
    public boolean equals(Object o){
        try{
            return getName().equals(((BettingSite) o).getName());
        }
        catch (ClassCastException e){
            return false;
        }
    }

    @Override
    public String toString(){
        return getName();
    }


    public static void testBetOrder() throws Exception {


        String time = "2020-06-06T16:30:00.0Z";
        String matchname = "Borussia Dortmund v Hertha Berlin";
        String stake = "0.77";
        Bet bet = new FootballResultBet(BetType.LAY, FootballBet.TEAM_A, false);
        //Bet bet = new FootballOverUnderBet(BetType.LAY, FootballBet.UNDER, new BigDecimal("2.5"));


        BettingSite b = new Betdaq();
        SiteEventTracker set = b.getEventTracker();
        set.setupMatch(FootballMatch.parse(time, matchname));
        print(set);

        exit(0);

    }


    public boolean sendHeartbeat() throws URISyntaxException, IOException {
        log.warning("NO HEARTBEAT SETUP FOR THIS SITE");
        return true;
    }

    public static void main(String[] args) throws Exception{

        try {
            testBetOrder();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        print("END.");
    }
}
