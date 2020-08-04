package Bet;


import Bet.FootballBet.FootballBet;
import SiteConnectors.BettingSite;
import SiteConnectors.Smarkets.Smarkets;
import com.lowagie.text.html.simpleparser.ALink;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.json.simple.*;

import java.lang.management.BufferPoolMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import static tools.BigDecimalTools.*;

import static tools.printer.*;

public abstract class Bet {

    public static final BigDecimal penny = new BigDecimal("0.01");

    public enum BetType {BACK, LAY}

    public Sport sport;
    public String category;
    protected BetType type;

    public enum Sport{FOOTBALL, TENNIS, RUGBY}

    public Bet(BetType bet_type) {
        type = bet_type;
    }


    public Boolean isLay(){
        return type.equals(BetType.LAY);
    }

    public Boolean isBack(){
        return type.equals(BetType.BACK);
    }

    public abstract JSONObject toJSON();

    public abstract String id();

    @Override
    public String toString(){
        return id();
    }


    @Override
    public boolean equals(Object obj) {
        try {
            return id().equals(((Bet) obj).id());
        }
        catch (ClassCastException e){
            return false;
        }
    }

    public BetType getType(){
        return type;
    }


    public static BigDecimal backStake2LayStake(BigDecimal back_stake, BigDecimal odds){
        // AKA
        // Back stake to Back Profit
        // Lay profit to lay stake
        return odds.subtract(BigDecimal.ONE).multiply(back_stake);
    }

    public static BigDecimal layStake2BackStake(BigDecimal lay_stake, BigDecimal odds){
        // AKA
        // Lay Stake to Lay Profit
        // Back Profit to Back Stake
        return lay_stake.divide((odds.subtract(BigDecimal.ONE)), 12, RoundingMode.HALF_UP);
    }



    public static BigDecimal ROI(BetType betType, BigDecimal odds, BigDecimal investment, BigDecimal win_com_rate,
                                 BigDecimal loss_com_rate){

        BigDecimal stake = investment2Stake(investment, loss_com_rate);

        // Profit = LAY STAKE for Back bets. And BACK STAKE for Lay bets
        BigDecimal profit_b4_com;
        if (betType == BetType.BACK){
            profit_b4_com = backStake2LayStake(stake, odds);
        }
        else if (betType == BetType.LAY){
            profit_b4_com = layStake2BackStake(stake, odds);
        }
        else{
            return null;
        }

        BigDecimal prof_after_com = profitAfterCommission(profit_b4_com, win_com_rate);
        BigDecimal potential_return = investment.add(prof_after_com);

        return potential_return.setScale(12, RoundingMode.HALF_UP);
    }



    public static BigDecimal return2Investment(BetType betType, BigDecimal odds, BigDecimal target_return,
                                               BigDecimal win_com_rate, BigDecimal loss_com_rate){

        // Finds the investment needed to get the target return
        BigDecimal roi_ratio = ROI(betType, odds, BigDecimal.ONE, win_com_rate, loss_com_rate);
        return target_return.divide(roi_ratio, 12, RoundingMode.HALF_UP);
    }

    public static BigDecimal return2Stake(BetType betType, BigDecimal odds, BigDecimal target_return,
                                          BigDecimal win_com_rate, BigDecimal loss_com_rate){
        BigDecimal investment = return2Investment(betType, odds, target_return, win_com_rate, loss_com_rate);
        return investment2Stake(investment, loss_com_rate);
    }

    public static BigDecimal return2BackStake(BetType betType, BigDecimal odds, BigDecimal target_return,
                                              BigDecimal win_com_rate, BigDecimal loss_com_rate) {

        BigDecimal stake = return2Stake(betType, odds, target_return, win_com_rate, loss_com_rate);
        if (betType == BetType.BACK){
            return stake;
        }
        else if (betType == BetType.LAY){
            return layStake2BackStake(stake, odds);
        }
        return null;
    }


    public static BigDecimal profitAfterCommission(BigDecimal profit_b4_com, BigDecimal win_com_rate){
        BigDecimal win_com = profit_b4_com.multiply(win_com_rate);
        return profit_b4_com.subtract(win_com);
    }



    public static BigDecimal dec2americ(BigDecimal decimal_odds){

        BigDecimal american_odds;

        if (decimal_odds.compareTo(new BigDecimal(2)) == -1){
            american_odds = new BigDecimal(-100)
                    .divide(decimal_odds.subtract(BigDecimal.ONE), 12, RoundingMode.HALF_UP);
        }
        else{
            american_odds = decimal_odds.subtract(BigDecimal.ONE).multiply(new BigDecimal(100));
        }

        return american_odds;
    }


    public static BigDecimal americ2dec(BigDecimal american_odds){
        BigDecimal decimal_odds;

        if (american_odds.compareTo(new BigDecimal(100)) != -1){
            decimal_odds = american_odds
                    .divide(new BigDecimal(100), 12, RoundingMode.HALF_UP).add(BigDecimal.ONE);
        }
        else if (american_odds.compareTo(new BigDecimal(-100)) != 1){
            decimal_odds = new BigDecimal(-100)
                    .divide(american_odds, 12, RoundingMode.HALF_UP).add(BigDecimal.ONE);
        }
        else{
            return null;
        }

        return decimal_odds;
    }


    public static BigDecimal multiplyDecimalOdds(BigDecimal odds, BigDecimal multiplicand){
        return odds.subtract(BigDecimal.ONE)
                .multiply(multiplicand)
                .add(BigDecimal.ONE);
    }


    public static BigDecimal investment2Stake(BigDecimal investment, BigDecimal loss_commission_rate) {
        // Examples in comments use 1% commission

        // 100% of stake + 1% loss commission is the total investment so a total 1.01 ratio for 1%
        BigDecimal total_ratio = BigDecimal.ONE.add(loss_commission_rate);

        // The amount of the total investment which is the stake (1.00 out of 1.01) multiplied by investment
        BigDecimal stake_amount = investment.divide(total_ratio, 12, RoundingMode.HALF_UP);

        return stake_amount;
    }


    public static BigDecimal stake2Investment(BigDecimal stake, BigDecimal loss_commission_rate){
        // Using 1% example
        // stake + (stake x loss_com)
        return stake.add(stake.multiply(loss_commission_rate));
    }



    public static BigDecimal combine_odds(BigDecimal scale1, BigDecimal odds1, BigDecimal scale2, BigDecimal odds2){
        try {
            BigDecimal new_total_stake = scale1.add(scale2);
            BigDecimal upper = odds1.multiply(scale1).add(odds2.multiply(scale2));
            return upper.divide(new_total_stake, 12, RoundingMode.HALF_UP);
        }
        catch (ArithmeticException e){
            return null;
        }
    }


    public static BigDecimal new_stake_needed_for_odds(BigDecimal current_stake, BigDecimal current_odds,
                                                       BigDecimal new_odds, BigDecimal target_odds){

        // Given some current odds with an amount, what amount of some new odds, would make the combined
        // odds equal to the target odds


        // If the current odds and the target are the same, ZERO stake is
        // needed for the next set of odds to make them equal
        int current_compare_target = current_odds.compareTo(target_odds);
        if (current_compare_target == 0){
            return BigDecimal.ZERO;
        }

        // If current and new odds are the same then no amount of stake will change anything.
        int current_compare_new = current_odds.compareTo(new_odds);
        if (current_compare_new == 0){
            return null;
        }

        // If target odds does not lie between current and new odds, then it cannot be possible to get there.
        int new_compare_target = new_odds.compareTo(target_odds);
        if ((current_compare_target < 0 && new_compare_target < 0) ||
            (current_compare_target > 0 && new_compare_target > 0)){

            return null;
        }


        try {
            BigDecimal upper = current_stake.multiply(target_odds.subtract(current_odds));
            BigDecimal lower = new_odds.subtract(target_odds);
            return upper.divide(lower, 12, RoundingMode.HALF_UP);
        }
        catch (ArithmeticException e){
            return null;
        }
    }



    public static BigDecimal investmentNeededForStake(BigDecimal stake, BigDecimal loss_commission_rate){
        // Total amount of commission charged if lost bet
        BigDecimal loss_commission_amount = stake.multiply(loss_commission_rate);

        // Sum up the total amount of money needed
        BigDecimal total_inv_needed = stake.add(loss_commission_amount);

        return total_inv_needed;
    }


    public Boolean equals(Bet bet){
        return id().equals(bet.id());
    }


    public static void ROItester(){
        for (int i=0; i<50; i++){

            BetType betType;
            if (i%2==0)
                betType = BetType.BACK;
            else
                betType = BetType.LAY;

            BigDecimal odds = randomBD();
            BigDecimal inv = randomBD();
            BigDecimal winc = randomBD();
            BigDecimal lossc = randomBD();

            /*
            BigDecimal roi1 = ROI(betType, odds, inv, winc, lossc);
            BigDecimal roi2 = ROI_old(betType, odds, inv, winc, lossc);

            print(roi1);
            print(roi2);

            if (!roi1.equals(roi2)){
                print("ERROR");
                break;
            }
             */

        }
    }




    public static void main(String[] args){

        BigDecimal x = new_stake_needed_for_odds(
                new BigDecimal("2.00"),
                new BigDecimal("2.5"),
                new BigDecimal("3.00"),
                new BigDecimal("3.1")
        );

        print(x);


    }

}
