package Bet;


import Bet.FootballBet.FootballBet;
import SiteConnectors.BettingSite;
import SiteConnectors.Smarkets.Smarkets;
import org.json.simple.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static tools.printer.BDString;
import static tools.printer.print;

public abstract class Bet {

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


    public BetType getType(){
        return type;
    }


    public static BigDecimal backStake2LayStake(BigDecimal back_stake, BigDecimal odds){
        // AKA
        // Back stake to Back Profit
        // Lay profit to lay stake
        return odds.subtract(BigDecimal.ONE).multiply(back_stake);
    }


    public static BigDecimal layStake2backStake(BigDecimal lay_stake, BigDecimal odds){
        // AKA
        // Lay Stake to Lay Profit
        // Back Profit to Back Stake
        return lay_stake.divide((odds.subtract(BigDecimal.ONE)), 12, RoundingMode.HALF_UP);
    }


    public static BigDecimal ROI_ratio(BetType betType, BigDecimal odds, BigDecimal win_com_rate,
                                 BigDecimal loss_com_rate){
        return ROI(betType, BigDecimal.ONE, odds, win_com_rate, loss_com_rate, null);
    }

    public static BigDecimal ROI(BetType betType, BigDecimal investment, BigDecimal odds, BigDecimal win_com_rate,
                                 BigDecimal loss_com_rate){
        return ROI(betType, investment, odds, win_com_rate, loss_com_rate, null);
    }

    public static BigDecimal ROI(BetType betType, BigDecimal investment, BigDecimal odds, BigDecimal win_com_rate,
                      BigDecimal loss_com_rate, Integer scale){

        BigDecimal backersStake_layersProfit;
        BigDecimal backersProfit_layersStake;
        BigDecimal pot_profit_b4_com;

        // Calculate the stake part (minus loss commission possibility) and profit of bet
        // depending on bet type.
        if (betType == BetType.BACK){
            backersStake_layersProfit = stakePartOfInvestment(investment, loss_com_rate);
            backersProfit_layersStake = backStake2LayStake(backersStake_layersProfit, odds);
            pot_profit_b4_com = backersProfit_layersStake;
        }
        else if (betType == BetType.LAY){
            backersProfit_layersStake = stakePartOfInvestment(investment, loss_com_rate);
            backersStake_layersProfit = layStake2backStake(backersProfit_layersStake, odds);
            pot_profit_b4_com = backersStake_layersProfit;
        }
        else{
            return null;
        }

        // Win commission from rate X profit
        BigDecimal win_commission = pot_profit_b4_com.multiply(win_com_rate);

        // Find returns by summing original investment with profit and minus commission
        BigDecimal pot_return_b4_com = investment.add(pot_profit_b4_com);
        BigDecimal pot_return = pot_return_b4_com.subtract(win_commission);

        // Round if asked.
        if (scale != null){
            pot_return = pot_return.setScale(scale, RoundingMode.HALF_UP);
        }

        return pot_return;
    }


    public static BigDecimal stakePartOfInvestment(BigDecimal investment, BigDecimal loss_commission_rate) {
        // Examples in comments use 1% commission

        // 100% of stake + 1% loss commission is the total investment so a total 1.01 ratio for 1%
        BigDecimal total_ratio = BigDecimal.ONE.add(loss_commission_rate);

        // The amount of the total investment which is the stake (1.00 out of 1.01)
        BigDecimal stake_ratio = BigDecimal.ONE.divide(total_ratio, 12, RoundingMode.HALF_UP);

        // Multiply total investment by the amount of it that is stake, to get the total stake
        BigDecimal stake_amount = investment.multiply(stake_ratio);

        return stake_amount;
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

    public Map<String, BetGroup> sortByCategory(Collection<Bet> bets){
        Map<String, BetGroup> map = new HashMap<>();
        for (Bet bet: bets){
            BetGroup current = map.get(bet.category);
            if (current == null){
                current = new BetGroup();
                map.put(bet.category, current);
            }
            current.add(bet);
        }
        return map;
    }

    public static JSONArray getTautIds(Bet[][] tauts) {
        JSONArray taut_list = new JSONArray();
        for (int i = 0; i < tauts.length; i++) {
            JSONArray taut_ids = new JSONArray();

            for (int j = 0; j < tauts[i].length; j++) {
                taut_ids.add(tauts[i][j].id());
            }

            taut_list.add(taut_ids);
        }
        return taut_list;
    }


    public static void main(String[] args){

    }

}
