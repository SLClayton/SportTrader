package SiteConnectors;

import Bet.Bet;
import Bet.Bet.BetType;
import Bet.BetOffer;
import Bet.BetOrder;
import Bet.FootballBet.FootballBet;
import Bet.FootballBet.FootballBetGenerator;
import Bet.FootballBet.FootballOverUnderBet;
import Bet.FootballBet.FootballResultBet;
import Bet.PlacedBet;
import Bet.MarketOddsReport;
import SiteConnectors.Betdaq.Betdaq;
import SiteConnectors.Betfair.Betfair;
import SiteConnectors.Smarkets.Smarkets;
import Sport.FootballMatch;
import Trader.SportsTrader;
import org.json.simple.parser.ParseException;
import tools.Requester;

import java.awt.event.HierarchyListener;
import java.io.BufferedInputStream;
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

    //public final static String name = "ABSTRACT_BETTING_SITE";
    public String ssldir;
    public Requester requester;

    public boolean exit_flag;

    public BigDecimal balance;
    public BigDecimal exposure;

    public Lock balanceLock = new ReentrantLock();
    public BigDecimal balance_buffer = new BigDecimal("10.00");

    public BettingSite() {
        exit_flag = false;

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            ssldir = "C:/ssl/";
        } else { // Assume linux
            ssldir = System.getProperty("user.home") + "/ssl/";
        }

        balance = new BigDecimal("0.00");
    }


    public abstract void login() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, IOException, URISyntaxException, InterruptedException, ParseException;


    public abstract String getSessionToken() throws IOException, CertificateException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, UnrecoverableKeyException, URISyntaxException, ParseException;


    public abstract BigDecimal winCommissionRate();

    public abstract BigDecimal lossCommissionRate();


    public abstract String getName();


    public abstract String getID();


    public abstract BigDecimal minBackersStake();


    public abstract BigDecimal minLayersStake(BigDecimal odds);



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



    public BigDecimal stakePartOfInvestment(BigDecimal investment) {
        return Bet.stakePartOfInvestment(investment, lossCommissionRate());
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



    public PlacedBet placeBet(BetOrder betOrder, BigDecimal MIN_ODDS_RATIO) throws IOException, URISyntaxException {
        List<BetOrder> betOrders = new ArrayList<>();
        betOrders.add(betOrder);
        List<PlacedBet> placedBets = placeBets(betOrders, MIN_ODDS_RATIO);
        if (placedBets.size() != 1){
            log.severe(String.format("SENT 1 BETORDER BUT GOT BACK %s PLACED BETS", placedBets.size()));
        }
        return placedBets.get(0);
    }


    public abstract List<PlacedBet> placeBets(List<BetOrder> betOrders, BigDecimal odds_ratio_buffer)
            throws IOException, URISyntaxException;


    public BigDecimal ROI_ratio(BetType betType, BigDecimal odds){
        return ROI(betType, BigDecimal.ONE, odds);
    }

    public BigDecimal ROI(BetType betType, BigDecimal odds, BigDecimal investment){
        return ROI(betType, odds, investment, null);
    }

    public BigDecimal ROI(BetType betType, BigDecimal odds, BigDecimal investment, Integer scale){
        return Bet.ROI(betType, odds, investment, winCommissionRate(), lossCommissionRate(), scale);
    }



    public static BigDecimal _ROI_lagacy(BetType betType, BigDecimal odds, BigDecimal commission_rate, BigDecimal investment,
                                 boolean real) {
        // Default ROI, commission on profits only

        BigDecimal roi;

        // BACK
        if (betType == BetType.BACK) {
            BigDecimal backers_stake = investment;
            BigDecimal backers_profit = Bet.backStake2LayStake(backers_stake, odds);
            BigDecimal commission = backers_profit.multiply(commission_rate);
            roi = backers_stake.add(backers_profit).subtract(commission);
        }

        // LAY
        else {
            BigDecimal layers_stake = investment;
            BigDecimal layers_profit = Bet.layStake2backStake(layers_stake, odds);
            BigDecimal commission = layers_profit.multiply(commission_rate);
            roi = layers_stake.add(layers_profit).subtract(commission);
        }

        // Round to nearest penny if 'real' value;
        if (real) {
            roi = roi.setScale(2, RoundingMode.HALF_UP);
        }

        return roi;
    }


    @Override
    public boolean equals(Object o){
        return ((o instanceof BettingSite) && getName().equals(((BettingSite) o).getName()));
    }

    @Override
    public String toString(){
        return getName();
    }


    public static void testBetOrder() throws Exception {

        String time = "2020-06-06T13:30:00.0Z";
        String matchname = "Bayer 04 Leverkusen v FC Bayern München";
        String stake = "3.21";
        Bet bet = new FootballResultBet(BetType.BACK, FootballBet.TEAM_A, false);
        //Bet bet = new FootballOverUnderBet(BetType.LAY, FootballBet.UNDER, new BigDecimal("2.5"));


        BettingSite b = new Smarkets();
        SiteEventTracker set = b.getEventTracker();
        set.setupMatch(FootballMatch.parse(time, matchname));
        print(set);

        MarketOddsReport mor = set.getMarketOddsReport(new ArrayList<Bet>(FootballBetGenerator._getAllBets()));
        toFile(mor.toJSON(true));

        BetOffer betOffer = mor.get(bet.id()).get(0);
        pp(betOffer.toJSON());


        BetOrder betOrder = betOffer.betOrderFromStake(new BigDecimal(stake));
        pp(betOrder.toJSON());

        PlacedBet placedBet = b.placeBet(betOrder, new BigDecimal("0.10"));
        pp(placedBet.toJSON());

    }



    public static void main(String[] args) {

        try {
            testBetOrder();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        print("END.");
    }
}
