package Bet;

import SiteConnectors.BettingSite;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProfitReportMultipleBetItem implements ProfitReportItem {

    private List<ProfitReportItem> items;


    public ProfitReportMultipleBetItem(){
        items = new ArrayList<>();
    }

    public void add(ProfitReportItem new_item) throws DifferentBetsException {
        if (items.isEmpty()){
            items.add(new_item);
        }
        else if (new_item.getBet().equals(this.getBet())){
            items.add(new_item);
        }
        else{
            throw new DifferentBetsException(String.format("Trying to use bet %s in multibet with %s",
                    new_item.getBet(), getBet()));
        }
    }


    public static ProfitReportMultipleBetItem fromList(List<ProfitReportItem> new_items) throws DifferentBetsException {
        ProfitReportMultipleBetItem mulit_bet = new ProfitReportMultipleBetItem();
        for (ProfitReportItem new_item: new_items){
            mulit_bet.add(new_item);
        }
        return mulit_bet;
    }





    @Override
    public Bet getBet(){
        if (items.isEmpty()){
            return null;
        }
        return items.get(0).getBet();
    }


    @Override
    public BigDecimal getInvestment() {
        BigDecimal total_inv = BigDecimal.ZERO;
        for (ProfitReportItem item: items){
            total_inv = total_inv.add(item.getInvestment());
        }
        return total_inv;
    }

    @Override
    public BigDecimal getReturn() {
        BigDecimal total_return = BigDecimal.ZERO;
        for (ProfitReportItem item: items){
            total_return = total_return.add(item.getReturn());
        }
        return total_return;
    }



    @Override
    public JSONObject toJSON() {
        JSONArray items_json = new JSONArray();
        for (ProfitReportItem item: items){
            items_json.add(item.toJSON());
        }

        JSONObject j = new JSONObject();
        j.put("multi_bet", getBet().id());
        j.put("parts", items_json);
        return j;
    }

    @Override
    public Set<String> sites_used() {
        Set<String> sites_used = new HashSet<>();
        for (ProfitReportItem item: items){
            sites_used.addAll(item.sites_used());
        }
        return sites_used;
    }

    public class DifferentBetsException extends Exception {

        public DifferentBetsException(String msg){
            super(msg);
        }

    }
}
