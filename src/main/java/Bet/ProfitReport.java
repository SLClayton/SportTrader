package Bet;

import SiteConnectors.BettingSite;
import Trader.SportsTrader;
import tools.printer.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static tools.printer.BDMax;
import static tools.printer.BDMin;

public class ProfitReport {

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    private List<ProfitReportItem> items;

    private BigDecimal total_investment;
    private BigDecimal min_return;
    private BigDecimal max_return;
    private BigDecimal max_minStakeReturn;
    private BigDecimal min_maxStakeReturn;

    public enum OutcomeState {ALL_SUCCESSFUL, NONE_SUCCESSFUL, MIX_STATES}

    public ProfitReport(List<ProfitReportItem> items){

        this.items = items;

        total_investment = BigDecimal.ZERO;
        for (ProfitReportItem item: items){

            // Find total investment of all items
            total_investment = total_investment.add(item.getInvestment());

            // Find smallest and largest returns possible if all items placed
            BigDecimal item_return = item.getReturn();
            min_return = BDMin(min_return, item_return);
            max_return = BDMax(max_return, item_return);


            BetOffer betOffer = item.getBetOffer();
            if (betOffer != null) {
                max_minStakeReturn = BDMax(max_minStakeReturn, betOffer.returnFromMinStake());
                min_maxStakeReturn = BDMin(min_maxStakeReturn, betOffer.returnFromMinStake());
            }
        }
    }


    public List<ProfitReportItem> getItems(){
        return items;
    }


    public Class<?> getType(){
        Set<Class<?>> item_types = new HashSet<>(1);
        for (ProfitReportItem item: getItems()){
            item_types.add(item.getClass());
        }

        if (item_types.size() == 1){
            return item_types.iterator().next();
        }
        return null;
    }


    public OutcomeState getState(){
        int successes = 0;

        for (ProfitReportItem item: items){
            PlacedBet.State item_state  = item.getState();

            if (item_state == null){
                return null;
            }
            if (item_state == PlacedBet.State.SUCCESS){
                successes += 1;
            }
        }

        if (successes == items.size()){
            return OutcomeState.ALL_SUCCESSFUL;
        }
        else if (successes > 0){
            return OutcomeState.MIX_STATES;
        }
        return OutcomeState.NONE_SUCCESSFUL;
    }


    public boolean isvalid(){
        return minProfit() != null;
    }


    public BigDecimal getTotalInvestment(){
        return total_investment;
    }


    public BigDecimal getMinReturn(){
        return min_return;
    }

    public BigDecimal getMaxReturn(){
        return max_return;
    }


    public BigDecimal getMax_minStakeReturn(){
        return max_minStakeReturn;
    }

    public BigDecimal getMin_maxStakeReturn(){
        return min_maxStakeReturn;
    }


    public BigDecimal minProfit(){
        return getMinReturn().subtract(getTotalInvestment());
    }

    public BigDecimal maxProfit(){
        return getMaxReturn().subtract(getTotalInvestment());
    }


    public boolean smallerInvestment(ProfitReport profitReport){
        return getTotalInvestment().compareTo(profitReport.getTotalInvestment()) < 0;
    }

    public boolean largerInvestment(ProfitReport profitReport){
        return getTotalInvestment().compareTo(profitReport.getTotalInvestment()) > 0;
    }


    public BigDecimal minProfitRatio(){
        BigDecimal total_investment = getTotalInvestment();
        if (total_investment.compareTo(BigDecimal.ZERO) == 0){
            return null;
        }
        return minProfit().divide(total_investment, 12, RoundingMode.HALF_UP);
    }

    public BigDecimal maxProfitRatio(){
        BigDecimal total_investment = getTotalInvestment();
        if (total_investment.compareTo(BigDecimal.ZERO) == 0){
            return null;
        }
        return maxProfit().divide(total_investment, 12, RoundingMode.HALF_UP);
    }


    public BetGroup getBetGroup(){
        ArrayList<Bet> bets = new ArrayList<>();
        for (ProfitReportItem item: items){
            bets.add(item.getBet());
        }
        return new BetGroup(bets);
    }
}
