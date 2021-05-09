package Bet;

import SiteConnectors.BettingSite;
import Sport.Event;
import Trader.SportsTrader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import tools.BigDecimalTools;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;

import static tools.printer.*;
import static tools.BigDecimalTools.*;

public class ProfitReport implements Comparable<ProfitReport> {

    /*
        A list of Mulit-Site Bets representing a whole implemented tautology.
        Then showing the financial implications of it.
     */

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    private final Collection<MultiSiteBet> items;

    private BigDecimal total_investment;
    private BigDecimal min_return;
    private BigDecimal max_return;



    public ProfitReport(Collection<MultiSiteBet> items){

        // Sort all items into a separate ItemSet for each EVENT/BET
        // so that only one set wins in any eventuality.
        this.items = items;

        total_investment = BigDecimal.ZERO;
        for (MultiSiteBet item: items){

            total_investment = total_investment.add(item.getInvestment());
            BigDecimal item_return = item.getReturn();

            // Find smallest and largest returns possible if all items placed
            min_return = BDMin(min_return, item_return);
            max_return = BDMax(max_return, item_return);
        }
    }

    public static ProfitReport fromPlacedBets(Collection<PlacedBet> placedBets){
        Map<String, MultiSiteBet> multiSiteBetMap = new HashMap<>();

        for (PlacedBet placedBet: placedBets){
            multiSiteBetMap.computeIfAbsent(placedBet.getBet().id(), k-> new MultiSiteBet()).add(placedBet);
        }

        return new ProfitReport(multiSiteBetMap.values());
    }


    @Override
    public int compareTo(ProfitReport profitReport) {
        return this.minProfitRatio().compareTo(profitReport.minProfitRatio());
    }


    public Collection<MultiSiteBet> getItems(){
        return items;
    }



    public Class<?> getType(){
        Set<Class<?>> item_types = new HashSet<>(2);
        for (MultiSiteBet item: getItems()){
            item_types.add(item.getClass());
        }

        if (item_types.size() == 1){
            return item_types.iterator().next();
        }
        return null;
    }


    public BetGroup getBets(){
        BetGroup bets = new BetGroup();
        for (MultiSiteBet msb: getItems()){
            bets.add(msb.getBet());
        }
        return bets;
    }

    public Map<String, Map<String, BigDecimal>> breakdown(){
        Map<String, Map<String, BigDecimal>> breakdown = new HashMap<>();
        for (MultiSiteBet msb: getItems()){
            breakdown.put(msb.getBet().id(), msb.inv_per_site());
        }
        return breakdown;
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


    public BigDecimal getMinROI(){
        return getMinReturn().divide(getTotalInvestment(), 12, RoundingMode.HALF_UP);
    }


    public BigDecimal minProfit(){
        return getMinReturn().subtract(getTotalInvestment());
    }

    public BigDecimal maxProfit(){
        return getMaxReturn().subtract(getTotalInvestment());
    }

    public boolean isValid(){
        return minProfit() != null;
    }


    public Set<BettingSite> sites_used() {
        Set<BettingSite> sites_used = new HashSet<>();
        for (MultiSiteBet item: items){
            sites_used.addAll(item.sites_used());
        }
        return sites_used;
    }


    public int total_bets(){
        int n = 0;
        for (MultiSiteBet msb: items){
            n += msb.total_bets();
        }
        return n;
    }

    public Set<String> sites_used_strings(){
        Set<String> sites_used = new HashSet<>();
        for (BettingSite site: sites_used()){
            sites_used.add(site.getName());
        }
        return sites_used;
    }


    public boolean uses_site(String site_name){
        for (BettingSite bs: sites_used()){
            if (bs.getName().equals(site_name)){
                return true;
            }
        }
        return false;
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


    public boolean inProfit(){
        return minProfitRatio().compareTo(BigDecimal.ONE) > 0;
    }


    public JSONObject toJSON(){
        return toJSON(true);
    }

    public JSONObject toJSON(boolean include_items) {
        JSONObject j = new JSONObject();

        j.put("Site_Bets", items.size());
        j.put("min_return", BDString(min_return));
        j.put("max_return", BDString(max_return));
        j.put("total_investment", BDString(total_investment));
        j.put("min_ProfitRatio", BDString(minProfitRatio()));
        j.put("min_ROI", BDString(getMinROI()));


        JSONArray bet_ids = new JSONArray();
        for (MultiSiteBet item: items){
            bet_ids.add(item.getBet().id());
        }
        Collections.sort(bet_ids);
        j.put("bets", bet_ids);


        if (include_items){
            JSONArray j_items = new JSONArray();
            for (MultiSiteBet item: items){
                j_items.add(item.toJSON());
            }
            j.put("items", j_items);
        }

        return j;
    }


    public List<PlacedBet> placeBets(){
        List<BetPlan> betPlans = new ArrayList<>();
        for (MultiSiteBet msb: items){
            betPlans.addAll(msb.getBetPlans());
        }
        return BetPlan.placeBets(betPlans);
    }


    @Override
    public String toString() {
        return sf("[PR: inv %s, minRet %s, mROI %s, mProfRatio %s. %s bets using %s %s]",
                BDString(getTotalInvestment(), 4),
                BDString(getMinReturn(), 4),
                BDString(getMinROI(), 4),
                BDString(minProfitRatio(), 4),
                total_bets(),
                String.join(", ", sites_used_strings()),
                getBets().toString());
    }

    public void saveJSON(Instant time, String output_dir){

        String time_string =  time.truncatedTo(ChronoUnit.MILLIS)
                .toString().replace(":", "-").substring(0, 18);
        String name = this.getItems().iterator().next().getEvent().name;
        String profit_ratio_string = BDString(this.minProfitRatio(), 5);

        String filepath = sf("%s/%s %s %s.json", output_dir, time_string, name, profit_ratio_string);
        toFile(this.toJSON(), filepath);
        log.info(sf("Saved profit report to %s", filepath));
    }

}
