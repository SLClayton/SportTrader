package Bet;

import SiteConnectors.BettingSite;
import Bet.PlacedBet;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.util.*;

public interface ProfitReportItem {

    BigDecimal getInvestment();
    BigDecimal getReturn();
    Bet getBet();
    JSONObject toJSON();
    Set<String> sites_used();


    static Map<String, List<ProfitReportItem>> split_by_bet(List<ProfitReportItem> items){
        Map<String, List<ProfitReportItem>> split_items = new TreeMap<>();
        for (ProfitReportItem item: items){
            split_items.computeIfAbsent(item.getBet().id(), k -> new ArrayList<ProfitReportItem>())
                    .add(item);
        }
        return split_items;
    }

}
