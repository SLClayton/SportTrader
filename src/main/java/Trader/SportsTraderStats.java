package Trader;

import Bet.*;
import Bet.FootballBet.FootballBetGenerator;
import SiteConnectors.BettingSite;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

import static tools.printer.*;

public class SportsTraderStats implements Runnable {

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    public String filename;
    public FootballBetGenerator footballBetGenerator;

    public Thread thread;
    public boolean exit_flag;
    public Map<String, EventTraderStats> eventTraderStatsMap;
    public BlockingQueue<Object[]> queue;
    public Instant start_time;
    public long total_updates;

    public Queue<Object> eventTraderStatsQueue;


    public SportsTraderStats(String filename, FootballBetGenerator footballBetGenerator){
        total_updates = 0;
        exit_flag = false;
        eventTraderStatsMap = new HashMap<>();

        thread = new Thread(this);
        thread.setName("StatKeeper");

        this.filename = filename;
        this.footballBetGenerator = footballBetGenerator;
    }


    public void start(){
        thread.start();
        start_time = Instant.now();
    }


    @Override
    public void run() {

        Instant next_save = Instant.now().plus(2, ChronoUnit.SECONDS);

        while (!exit_flag) {

            while (!exit_flag && Instant.now().isBefore(next_save)){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            save();
            next_save = Instant.now().plus(10, ChronoUnit.SECONDS);
        }
    }


    public void update(EventTrader eventTrader, ProfitReportSet profitReportSet,
                       ArrayList<MarketOddsReport> marketOddsReports){

        Object[] queueObjects = new Object[] {eventTrader, profitReportSet, marketOddsReports};
        queue.add(queueObjects);
    }


    public void _update(EventTrader eventTrader, ProfitReportSet profitReportSet,
                             ArrayList<MarketOddsReport> marketOddsReports){


        EventTraderStats eventTraderStats = eventTraderStatsMap.get(eventTrader.id());
        if (eventTraderStats == null){
            eventTraderStats = new EventTraderStats(eventTrader);
            eventTraderStatsMap.put(eventTrader.id(), eventTraderStats);
        }

        eventTraderStats.update(profitReportSet, marketOddsReports);
        total_updates++;
    }


    public void save(){
        saveJSONResource(toJSON(false, 10), filename);
    }


    public JSONObject toJSON(boolean full_sites, Integer n_tauts){

        // Shallow copy the map
        Map<String, EventTraderStats> eventTraderStatsMap = new HashMap<>();
        for (Map.Entry<String, EventTraderStats> entry: this.eventTraderStatsMap.entrySet()){
            eventTraderStatsMap.put(entry.getKey(), entry.getValue());
        }


        JSONObject eventTraders_obj = new JSONObject();
        for (Map.Entry<String, EventTraderStats> entry: eventTraderStatsMap.entrySet()){
            EventTraderStats eventTraderStats = entry.getValue();
            eventTraders_obj.put(entry.getKey(), eventTraderStats.toJSON(full_sites, n_tauts));
        }

        JSONArray summary_tauts = new JSONArray();
        for (Taut taut: getSummaryTautologies()){
            summary_tauts.add(taut.toString());
        }
        JSONObject summary_tauts_obj = new JSONObject();
        summary_tauts_obj.put("tautologies", summary_tauts);
        summary_tauts_obj.put("size", summary_tauts.size());

        int max_len = 0;
        for (Bet bet: footballBetGenerator.getAllBets()) {
            max_len = Integer.max(max_len, bet.id().length());
        }

        JSONArray bet_appearances = new JSONArray();
        Map<String, Set<String>> site_bet_appreances = site_bet_appreances();
        for (Bet bet: footballBetGenerator.getAllBets()) {
            String s = bet.id();
            while (s.length() < max_len + 3){ s += " "; }
            if (site_bet_appreances.containsKey(bet.id())) {
                s += site_bet_appreances.get(bet.id()).toString();
            } else {
                s += "[]";
            }
            bet_appearances.add(s);
        }


        JSONObject meta = new JSONObject();
        meta.put("start_time", String.valueOf(start_time));
        long s = Duration.between(start_time, Instant.now()).getSeconds();
        meta.put("elapsed_time", String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60)));
        meta.put("total_updates", total_updates);
        meta.put("event_traders", eventTraders_obj.size());


        JSONObject j = new JSONObject();
        j.put("metadata", meta);
        j.put("event_traders", eventTraders_obj);
        j.put("summary_tautologies", summary_tauts_obj);
        j.put("bet_appearances", bet_appearances);

        return j;
    }


    public List<Taut> getSummaryTautologies(){

        Map<Integer, Taut> summary_tauts = new HashMap<>();
        for (EventTraderStats eventTraderStats: eventTraderStatsMap.values()){

            List<Taut> taut_list = new ArrayList<>(eventTraderStats.tautologies.values());
            for (int i=0; i<taut_list.size(); i++){

                Taut taut = taut_list.get(i);
                int taut_id = taut.getId();

                // Establish taut exists in summary or create summary taut
                Taut summary_taut = summary_tauts.get(taut_id);
                if (summary_taut == null){
                    summary_taut = new Taut(taut.bets);
                    summary_tauts.put(taut_id, summary_taut);
                }

                // Update best of best in summary taut
                summary_taut.update_best(taut.best_ratio);

                // Add on the sum and n of this taut to summary
                if (summary_taut.sum_ratios == null){
                    summary_taut.sum_ratios = BigDecimal.ZERO;
                }
                summary_taut.sum_ratios = summary_taut.sum_ratios.add(taut.sum_ratios);
                summary_taut.n = summary_taut.n.add(taut.n);

                // Update 'average best'of the summary taut
                if (summary_taut.sum_best_ratios == null){
                    summary_taut.sum_best_ratios = BigDecimal.ZERO;
                }
                summary_taut.sum_best_ratios = summary_taut.sum_best_ratios.add(taut.best_ratio);
                summary_taut.total_best_ratios = summary_taut.total_best_ratios.add(BigDecimal.ONE);
            }
        }

        List<Taut> taut_list = new ArrayList(summary_tauts.values());
        Collections.sort(taut_list, Collections.reverseOrder());

        return taut_list;
    }


    public Map<String, Set<String>> site_bet_appreances(){
        Map<String, Set<String>> bet_appearances = new HashMap<>();

        for (EventTraderStats eventTraderStats: eventTraderStatsMap.values()){
            for (Map.Entry<String, SiteTrackerStats> entry: eventTraderStats.siteTrackerStatsMap.entrySet()){
                String site_name = entry.getKey();
                SiteTrackerStats siteTrackerStats = entry.getValue();

                for (String bet: siteTrackerStats.most_bets_available){

                    if (!bet_appearances.containsKey(bet)){
                        bet_appearances.put(bet, new HashSet<>());
                    }
                    bet_appearances.get(bet).add(site_name);
                }
            }
        }

        return bet_appearances;
    }


    public class Taut implements Comparable<Taut>{

        public BetGroup bets;
        public BigDecimal best_ratio;
        public BigDecimal sum_ratios;
        public BigDecimal n;

        public BigDecimal sum_best_ratios;
        public BigDecimal total_best_ratios;

        public Taut(BetGroup bets){
            this.bets = bets;
            n = BigDecimal.ZERO;
            total_best_ratios = BigDecimal.ZERO;
        }


        public BigDecimal avg_ratio(){
            if (sum_ratios == null || n.compareTo(BigDecimal.ZERO) == 0){
                return null;
            }
            return sum_ratios.divide(n, 20, RoundingMode.HALF_UP);
        }


        public BigDecimal avg_best_ratio(){
            if (sum_best_ratios == null || total_best_ratios.compareTo(BigDecimal.ZERO) == 0){
                return null;
            }
            return sum_best_ratios.divide(total_best_ratios, 20, RoundingMode.HALF_UP);
        }


        public int getId(){
            return bets.id();
        }


        @Override
        public String toString(){
            String best = "null";
            if (best_ratio != null){
                best = best_ratio.setScale(4, RoundingMode.HALF_UP).toString();
            }
            String avg = "null";
            if (avg_ratio() != null){
                avg = avg_ratio().setScale(4, RoundingMode.HALF_UP).toString();
            }

            String s = String.valueOf(getId());
            while (s.length() < 12){
                s += " ";
            }

            s += ": n=";
            int l = s.length();
            s += n.toString();
            while (s.length() < l + 7){
                s += " ";
            }

            s += " best: ";
            l = s.length();
            s += String.valueOf(best);
            while (s.length() < l + 8){
                s += " ";
            }

            s += " avg: ";
            l = s.length();
            s += String.valueOf(avg);
            while (s.length() < l + 10){
                s += " ";
            }

            if (avg_best_ratio() != null){
                s += "avg best: ";
                l = s.length();
                s += avg_best_ratio().setScale(4, RoundingMode.HALF_UP);
                while (s.length() < l + 10){
                    s += " ";
                }
            }

            s += bets.toString();
            return s;
        }


        public JSONObject toJSON(){
            JSONObject j = new JSONObject();
            j.put("best", best_ratio.setScale(4, RoundingMode.HALF_UP));
            j.put("avg", avg_ratio().setScale(4, RoundingMode.HALF_UP));
            j.put("n", n.toString());
            j.put("id", getId());
            j.put("bets", bets.toJSON(false));

            return j;
        }



        public void update(BigDecimal new_profit_ratio){
            update_best(new_profit_ratio);
            update_sum(new_profit_ratio);
            n = n.add(BigDecimal.ONE);
        }


        private void update_best(BigDecimal potential_best){
            if (potential_best != null){
                if (best_ratio == null){
                    best_ratio = potential_best;
                }
                else{
                    best_ratio = best_ratio.max(potential_best);
                }
            }
        }


        private void update_sum(BigDecimal new_profit_ratio){
            if (sum_ratios == null){
                sum_ratios = BigDecimal.ZERO;
            }
            sum_ratios = sum_ratios.add(new_profit_ratio);
        }


        @Override
        public int compareTo(Taut o) {
            BigDecimal a = avg_best_ratio();
            BigDecimal b = o.avg_best_ratio();

            if (o == null){
                return 1;
            }
            if (a == null){
                if (b == null){
                    return 0;
                }
                return -1;
            }
            else if (b == null){
                return 1;
            }

            return a.compareTo(b);
        }
    }


    public class EventTraderStats{

        public EventTrader eventTrader;
        public long total_checks;
        public Instant start_time;
        public Map<Integer, Taut> tautologies;
        public Map<String, SiteTrackerStats> siteTrackerStatsMap;


        public EventTraderStats(EventTrader eventTrader){
            this.eventTrader = eventTrader;
            total_checks = 0;
            tautologies = new HashMap<>();
            siteTrackerStatsMap = new HashMap<>();
        }


        public void update(ProfitReportSet profitReportSet, ArrayList<MarketOddsReport> oddsReports){
            if (start_time == null){
                start_time = Instant.now();
            }

            // Update each siteTrackerStats with its corresponding odds report
            for (MarketOddsReport oddsReport: oddsReports) {
                BettingSite site = oddsReport.site();
                String site_name;
                if (site == null){
                    site_name = "null_name";
                }
                else{
                    site_name = oddsReport.site().getName();
                }


                SiteTrackerStats siteTrackerStats = siteTrackerStatsMap.get(site_name);
                if (siteTrackerStats == null) {
                    siteTrackerStats = new SiteTrackerStats();
                    siteTrackerStatsMap.put(site_name, siteTrackerStats);
                }
                siteTrackerStats.update(oddsReport);
            }

            for (BetOrderProfitReport betOrderProfitReport : profitReportSet.betOrderProfitReports){
                int taut_id = betOrderProfitReport.getTautology().id();
                BigDecimal profit_ratio = betOrderProfitReport.profit_ratio;

                // update best profit if this beats it for this tautology.
                Taut current_taut = tautologies.get(taut_id);
                if (current_taut == null){
                    current_taut = new Taut(betOrderProfitReport.getTautology());
                    tautologies.put(taut_id,  current_taut);
                }
                current_taut.update(profit_ratio);

            }

            total_checks += 1;
        }


        public JSONObject toJSON(boolean full_sites, Integer n_tauts){

            ArrayList<Taut> tauts = new ArrayList<Taut>(tautologies.values());
            Collections.sort(tauts, Collections.reverseOrder());

            JSONArray taut_bests = new JSONArray();
            for (Taut taut: tauts){
                if (taut.best_ratio == null){

                }
                taut_bests.add(taut.toString());
            }

            JSONObject site_trackers = new JSONObject();
            for (Map.Entry<String, SiteTrackerStats> entry: siteTrackerStatsMap.entrySet()){
                site_trackers.put(entry.getKey(), entry.getValue().toString());
            }


            JSONObject j = new JSONObject();
            if (n_tauts != null && n_tauts < taut_bests.size()){
                j.put("tautologies", taut_bests.subList(0, n_tauts));
            }
            else{
                j.put("tautologies", taut_bests);
            }
            j.put("tauts_n", taut_bests.size());
            j.put("sites", site_trackers);
            j.put("n", total_checks);

            return j;
        }


        public JSONObject toJSON(boolean full_sites){
            return toJSON(full_sites, null);
        }


    }


    public class SiteTrackerStats{

        public Set<String> most_bets_available;
        public Set<String> bets_available;

        public Set<String> most_bets_with_offers;
        public Set<String> bets_with_offers;

        public long total_checks;

        public SiteTrackerStats(){
            total_checks = 0;
            most_bets_with_offers = new HashSet<>();
            most_bets_available = new HashSet<>();
            bets_available = new HashSet<>();
            bets_with_offers = new HashSet<>();
        }


        public void update(MarketOddsReport oddsReport){

            bets_available.clear();
            bets_with_offers.clear();

            for (Map.Entry<String, ArrayList<BetOffer>> entry: oddsReport.entrySet()){
                String bet_id = entry.getKey();
                ArrayList<BetOffer> offers = entry.getValue();

                bets_available.add(bet_id);
                if (offers.size() > 0) {
                    bets_with_offers.add(bet_id);
                }
            }

            most_bets_available.addAll(bets_available);
            most_bets_with_offers.addAll(bets_with_offers);
            total_checks += 1;
        }

        @Override
        public String toString(){
            String s = "";

            s += " n=" + total_checks;
            s += "   ";

            s += "bets with offers: " + bets_with_offers.size();
            if (most_bets_with_offers.size() > bets_with_offers.size()){
                s += " (max: " + most_bets_with_offers.size() + ")";
            }
            s += "   ";

            s += "total bets: " + bets_available.size();
            if (most_bets_available.size() > bets_available.size()){
                s += " (max: " + most_bets_available.size() + ")";
            }

            return s;
        }


        public JSONObject toJSON(boolean full){
            JSONObject j = new JSONObject();
            if (full){
                if (most_bets_available.size() > bets_available.size()){
                    j.put("max_bets", Arrays.asList(most_bets_available.toArray()).toString());
                }
                if (most_bets_with_offers.size() > bets_with_offers.size()){
                    j.put("max_with_offers", Arrays.asList(most_bets_with_offers.toArray()).toString());
                }
                j.put("with_offers", Arrays.asList(bets_with_offers.toArray()).toString());
                j.put("bets", Arrays.asList(bets_available.toArray()).toString());
                j.put("n", String.valueOf(total_checks));
            }
            else {
                if (most_bets_available.size() > bets_available.size()){
                    j.put("max_bets", most_bets_available.size());
                }
                if (most_bets_with_offers.size() > bets_with_offers.size()){
                    j.put("max_with_offers", most_bets_with_offers.size());
                }
                j.put("bets", bets_available.size());
                j.put("with_offers", bets_with_offers.size());
                j.put("n", (int) total_checks);
            }
            return j;
        }
    }
}
