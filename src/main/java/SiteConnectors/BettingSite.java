package SiteConnectors;

import Bet.Bet;
import Bet.BetOffer;
import Bet.BetOrder;
import Bet.PlacedBet;
import SiteConnectors.Smarkets.Smarkets;
import Sport.FootballMatch;
import Trader.EventTrader;
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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import static tools.printer.print;

public abstract class BettingSite {

    public static Logger log = Logger.getLogger(SportsTrader.class.getName());

    //public final static String name = "ABSTRACT_BETTING_SITE";
    public String ssldir;
    public Requester requester;

    public boolean exit_flag;

    public BigDecimal balance;
    public BigDecimal commission_rate;
    public BigDecimal min_back_stake;
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
            KeyStoreException, KeyManagementException, IOException, URISyntaxException, InterruptedException;


    public abstract String getSessionToken() throws IOException, CertificateException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, UnrecoverableKeyException, URISyntaxException;


    public abstract BigDecimal commission();


    public abstract String getName();


    public abstract BigDecimal minBackersStake();


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


    public BigDecimal investment2Stake(BigDecimal investment) {
        return investment;
    }


    public BigDecimal stake2Investment(BigDecimal stake) {
        return stake;
    }


    public abstract SiteEventTracker getEventTracker(EventTrader eventTrader);


    public abstract ArrayList<FootballMatch> getFootballMatches(Instant from, Instant until) throws IOException, URISyntaxException, InterruptedException;


    public abstract ArrayList<PlacedBet> placeBets(ArrayList<BetOrder> betOrders, BigDecimal MIN_ODDS_RATIO) throws IOException, URISyntaxException;


    public BigDecimal ROI_old(BetOffer bet_offer, BigDecimal investment, boolean real) {
        // Default ROI, commission on profits only

        BigDecimal stake = investment;
        BigDecimal ret;
        BigDecimal profit;
        BigDecimal commission;
        BigDecimal roi;

        if (bet_offer.isBack()) {
            ret = stake.multiply(bet_offer.odds);
            profit = ret.subtract(stake);
            commission = profit.multiply(commission());
            roi = ret.subtract(commission);
        } else { // Lay Bet
            BigDecimal lay = BetOffer.backStake2LayStake(investment, bet_offer.odds);
            profit = lay;
            commission = profit.multiply(commission());
            ret = stake.add(profit);
            roi = ret.subtract(commission);
        }

        if (real) {
            roi = roi.setScale(2, RoundingMode.DOWN);
        }

        return roi;
    }


    public BigDecimal ROI(BetOffer bet_offer, BigDecimal investment, boolean real) {
        // Default ROI, commission on profits only

        return ROI(bet_offer.betType(), bet_offer.odds, bet_offer.commission(), investment, real);
    }


    public static BigDecimal ROI(String BACK_LAY, BigDecimal odds, BigDecimal commission_rate, BigDecimal investment,
                                 boolean real) {
        // Default ROI, commission on profits only

        BigDecimal roi;

        // BACK
        if (BACK_LAY.equals(Bet.BACK)) {
            BigDecimal backers_stake = investment;
            BigDecimal backers_profit = BetOffer.backStake2LayStake(backers_stake, odds);
            BigDecimal commission = backers_profit.multiply(commission_rate);
            roi = backers_stake.add(backers_profit).subtract(commission);
        }

        // LAY
        else {
            BigDecimal layers_stake = investment;
            BigDecimal layers_profit = BetOffer.layStake2backStake(layers_stake, odds);
            BigDecimal commission = layers_profit.multiply(commission_rate);
            roi = layers_stake.add(layers_profit).subtract(commission);
        }

        // Round to nearest penny if 'real' value;
        if (real) {
            roi = roi.setScale(2, RoundingMode.HALF_UP);
        }

        return roi;
    }





    public static void main(String[] args) {

        try {
            Smarkets s = new Smarkets();

            print(s.useBalance(new BigDecimal(70)));


        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
