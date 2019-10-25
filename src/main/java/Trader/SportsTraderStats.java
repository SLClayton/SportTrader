package Trader;

import Bet.*;
import SiteConnectors.SiteEventTracker;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import tools.printer;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static tools.printer.*;

public class SportsTraderStats implements Runnable {

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    public String filename;

    public Thread thread;
    public boolean exit_flag;
    public Map<String, EventTraderStats> eventTraderStatsMap;
    public BlockingQueue<Object[]> queue;
    public Instant start_time;


    public SportsTraderStats(String filename){
        exit_flag = false;
        eventTraderStatsMap = new HashMap<>();
        queue = new LinkedBlockingQueue<>();

        thread = new Thread(this);
        thread.setName("StatKeeper");

        this.filename = filename;
    }


    public void start(){
        thread.start();
        start_time = Instant.now();
    }


    @Override
    public void run() {

        Object[] queueObjects;
        Instant next_save = null;

        while (!exit_flag) {

            queueObjects = null;
            print("In Q: " + queue.size());
            try {
                while (!exit_flag && queueObjects == null) {
                    queueObjects = queue.poll(1, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (queueObjects == null) {
                continue;
            }

            Instant start = Instant.now();


            EventTrader eventTrader = (EventTrader) queueObjects[0];
            ProfitReportSet profitReportSet = (ProfitReportSet) queueObjects[1];
            ArrayList<MarketOddsReport> marketOddsReports = (ArrayList<MarketOddsReport>) queueObjects[2];

            print("unpack: " + (Instant.now().toEpochMilli() - start.toEpochMilli()) + "ms");

            start = Instant.now();
            _update(eventTrader, profitReportSet, marketOddsReports);

            //TODO: stats update gets gradually lober and longer, find out why

            print("Update: " + (Instant.now().toEpochMilli() - start.toEpochMilli()) + "ms");

            if (next_save == null || Instant.now().isAfter(next_save)){
                save(false, 8);
                next_save = Instant.now().plus(10, ChronoUnit.SECONDS);
            }
        }

    }


    public void update(EventTrader eventTrader, ProfitReportSet profitReportSet,
                       ArrayList<MarketOddsReport> marketOddsReports){

        Object[] queueObjects = new Object[] {eventTrader, profitReportSet, marketOddsReports};
        queue.add(queueObjects);
    }


    private void _update(EventTrader eventTrader, ProfitReportSet profitReportSet,
                             ArrayList<MarketOddsReport> marketOddsReports){

        EventTraderStats eventTraderStats = eventTraderStatsMap.get(eventTrader.id());
        if (eventTraderStats == null){
            eventTraderStats = new EventTraderStats();
            eventTraderStatsMap.put(eventTrader.id(), eventTraderStats);
        }
        eventTraderStats.update(profitReportSet, marketOddsReports);
    }


    public void save(boolean full_sites, Integer n_tauts){
        saveJSONResource(toJSON(full_sites, n_tauts), filename);
    }


    public JSONObject toJSON(boolean full_sites, Integer n_tauts){

        BigDecimal best_taut_avg = BigDecimal.ZERO;
        EventTraderStats summary_eventTraderStat = new EventTraderStats();


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

        JSONObject j = new JSONObject();
        j.put("EventTraderStats", eventTraders_obj);
        j.put("summary_tautologies", summary_tauts_obj);

        long s = Duration.between(start_time, Instant.now()).getSeconds();
        j.put("elapsed_time", String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60)));

        return j;
    }



    public List<Taut> getSummaryTautologies(){

        Map<String, BigDecimal> sum_best_ratio_map = new HashMap<>();
        Map<String, Integer> n_best_ratio_map = new HashMap<>();
        Map<String, BigDecimal> sum_avg_ratio_map = new HashMap<>();
        Map<String, Integer> n_avg_ratio_map = new HashMap<>();

        for (EventTraderStats eventTraderStats: eventTraderStatsMap.values()){
            for (Taut tautology: eventTraderStats.tautologies.values()){
                String id = tautology.id;

                if (tautology.best_ratio != null){
                    BigDecimal current_sum = sum_best_ratio_map.get(id);
                    if (current_sum == null){
                        current_sum = BigDecimal.ZERO;
                        n_best_ratio_map.put(id, 0);
                    }
                    sum_best_ratio_map.put(id, current_sum.add(tautology.best_ratio));
                    n_best_ratio_map.put(id, n_best_ratio_map.get(id) + 1);
                }

                if (tautology.avg_ratio != null){
                    BigDecimal current_sum = sum_avg_ratio_map.get(id);
                    if (current_sum == null){
                        current_sum = BigDecimal.ZERO;
                        n_avg_ratio_map.put(id, 0);
                    }
                    sum_avg_ratio_map.put(id, current_sum.add(tautology.avg_ratio));
                    n_avg_ratio_map.put(id, n_avg_ratio_map.get(id) + 1);
                }
            }
        }

        ArrayList<Taut> summary_tauts = new ArrayList<Taut>();
        for (Map.Entry<String, BigDecimal> entry: sum_best_ratio_map.entrySet()){
            String taut_id = entry.getKey();
            BigDecimal best_sum = entry.getValue();
            int n_best = n_best_ratio_map.get(taut_id);
            BigDecimal avg_sum = sum_avg_ratio_map.get(taut_id);
            int n_avg = n_avg_ratio_map.get(taut_id);

            Taut t = new Taut(taut_id);
            t.best_ratio = best_sum.divide(new BigDecimal(n_best), 5, RoundingMode.HALF_UP);
            t.avg_ratio = avg_sum.divide(new BigDecimal(n_avg), 5, RoundingMode.HALF_UP);
            t.n = new BigDecimal(n_best);
            summary_tauts.add(t);
        }

        Collections.sort(summary_tauts, Collections.reverseOrder());
        return summary_tauts;
    }


    public class Taut implements Comparable<Taut>{

        public String id;
        public BigDecimal best_ratio;
        public BigDecimal avg_ratio;
        public BigDecimal n;

        public Taut(String id){
            this.id = id;
            n = BigDecimal.ZERO;
        }


        @Override
        public String toString(){
            String best = "null";
            if (best_ratio != null){
                best = best_ratio.setScale(5, RoundingMode.HALF_UP).toString();
            }

            String avg = "null";
            if (avg_ratio != null){
                avg = avg_ratio.setScale(5, RoundingMode.HALF_UP).toString();
            }

            return String.format("%s: n=%s   best: %s   avg: %s", id, n, best, avg);
        }


        public void update(BigDecimal new_profit_ratio){
            update_best(new_profit_ratio);
            update_average(new_profit_ratio);
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


        private void update_average(BigDecimal new_profit_ratio){
            n = n.add(BigDecimal.ONE);

            if (avg_ratio == null){
                avg_ratio = new_profit_ratio;
            }
            else{
                BigDecimal old_ratio = n.subtract(BigDecimal.ONE).divide(n, 20, RoundingMode.HALF_UP);

                avg_ratio = avg_ratio.multiply(old_ratio)
                        .add(new_profit_ratio.divide(n, 20, RoundingMode.HALF_UP));
            }
        }


        @Override
        public int compareTo(Taut o) {
            if (o == null){
                return 1;
            }
            if (best_ratio == null){
                if (o.best_ratio == null){
                    return 0;
                }
                return -1;
            }
            else if (o.best_ratio == null){
                return 1;
            }

            return best_ratio.compareTo(o.best_ratio);
        }
    }

    public class EventTraderStats{

        public long total_checks;
        public Instant start_time;
        public Map<String, Taut> tautologies;
        public Map<String, SiteTrackerStats> siteTrackerStatsMap;

        public EventTraderStats(){
            total_checks = 0;
            tautologies = new HashMap<>();
            siteTrackerStatsMap = new HashMap<>();
        }

        public void update(ProfitReportSet profitReportSet, ArrayList<MarketOddsReport> oddsReports){
            if (start_time == null){
                start_time = Instant.now();
            }

            // TODO: stats take too long (about 50-60 ms) to update. Fix that

            // Update each siteTrackerStats with its corresponding odds report
            for (MarketOddsReport oddsReport: oddsReports){
                String site_name = oddsReport.site_name();

                SiteTrackerStats siteTrackerStats = siteTrackerStatsMap.get(site_name);
                if (siteTrackerStats == null){
                    siteTrackerStats = new SiteTrackerStats();
                    siteTrackerStatsMap.put(site_name, siteTrackerStats);
                }
                siteTrackerStats.update(oddsReport);
            }

            for (ProfitReport profitReport: profitReportSet.profitReports){
                String taut_id = profitReport.getTautology().id();
                BigDecimal profit_ratio = profitReport.profit_ratio;

                // update best profit if this beats it for this tautology.
                Taut current_taut = tautologies.get(taut_id);
                if (current_taut == null){
                    current_taut = new Taut(taut_id);
                    tautologies.put(taut_id, new Taut(taut_id));
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
                site_trackers.put(entry.getKey(), entry.getValue().toJSON(full_sites));
            }


            JSONObject j = new JSONObject();
            if (n_tauts != null){
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
        }


        public void update(MarketOddsReport oddsReport){
            bets_available = new HashSet<>();
            bets_with_offers = new HashSet<>();

            for (Map.Entry<String, ArrayList<BetOffer>> entry: oddsReport.betOffers.entrySet()){
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

        public String toJSON(boolean full){
            if (full){
                Map<String, String> j = new HashMap<>();
                if (most_bets_available.size() > bets_available.size()){
                    j.put("most_bets_available", Arrays.asList(most_bets_available.toArray()).toString());
                }
                if (most_bets_with_offers.size() > bets_with_offers.size()){
                    j.put("most_bets_with_offers", Arrays.asList(most_bets_with_offers.toArray()).toString());
                }
                j.put("bets_with_offers", Arrays.asList(bets_with_offers.toArray()).toString());
                j.put("bets_available", Arrays.asList(bets_available.toArray()).toString());
                j.put("n", String.valueOf(total_checks));
                return j.toString();
            }
            else {
                Map<String, Integer> j = new HashMap<>();
                if (most_bets_available.size() > bets_available.size()){
                    j.put("most_bets_available", most_bets_available.size());
                }
                if (most_bets_with_offers.size() > bets_with_offers.size()){
                    j.put("most_bets_with_offers", most_bets_with_offers.size());
                }
                j.put("bets_available", bets_available.size());
                j.put("bets_with_offers", bets_with_offers.size());
                j.put("n", (int) total_checks);
                return j.toString();
            }
        }
    }
}
