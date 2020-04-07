package Trader;

import Bet.Bet;
import SiteConnectors.Betdaq.Betdaq;
import Sport.Horse;
import com.globalbettingexchange.externalapi.*;
import net.sourceforge.tess4j.TesseractException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import tools.ScreenReader;
import tools.printer.*;

import javax.xml.bind.JAXBElement;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;
import java.util.List;

import static tools.printer.*;

public class vTrader {

    public static String file_path = "resources/horse_data.json";
    public Betdaq betdaq;
    public ScreenReader sr;

    public int minus_x = 410;

    public int x = 1920 - minus_x;
    public int y = 475;




    public vTrader() throws InterruptedException, URISyntaxException, ParseException, IOException {
        betdaq = new Betdaq();
        sr = new ScreenReader();
    }

    public void setup(){
        while (true){
            BufferedImage image = sr.runners_screenshot(x, y);
            image = sr.scale(image, 400 / image.getWidth());
            sr.show(image);
            try {
                sleepUntil(Instant.now().plusMillis(500));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setupWinner(){
        while (true){
            BufferedImage image = sr.winner_screenshot(x, y);
            image = sr.scale(image, 400 / image.getWidth());
            sr.show(image);
            try {
                sleepUntil(Instant.now().plusMillis(500));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public EventClassifierType getNextRace(){
        List<EventClassifierType> races = null;
        try {
            races = Betdaq.getNestedEventsWithMarkets(
                    betdaq.getEventTree(Betdaq.VIRTUAL_HORSES_FLATS_ID, true).getEventClassifiers());
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            return null;
        }

        EventClassifierType nextRace = null;
        for (EventClassifierType potential_nextRace: races){

            // Must be at least 10 seconds from now
            Instant potential_start = potential_nextRace.getMarkets().get(0).getStartTime().toGregorianCalendar().toInstant();
            if (potential_start.isBefore(Instant.now().plusSeconds(15))){
                continue;
            }

            if (nextRace == null){
                nextRace = potential_nextRace;
                continue;
            }

            Instant current_start = nextRace.getMarkets().get(0).getStartTime().toGregorianCalendar().toInstant();
            if (potential_start.isBefore(current_start)){
                nextRace = potential_nextRace;
            }
        }

        return nextRace;
    }


    public List<Horse> getHorsesFromRunners(Collection<String> horse_names, List<String> runners){

        // Create map to add all odds into for each horse
        Map<String, List<String>> horse_odds = new HashMap<>();
        for (String horse_name: horse_names){
            horse_odds.put(horse_name, new ArrayList<>());
        }


        for (String runner: runners){
            // Split horse name and odds
            String name = runner.split(" ")[0];
            String odds = runner.split(" ")[1];

            // If text matches a horse name, add its odds to the list
            if (horse_odds.containsKey(name)){
                horse_odds.get(name).add(odds);
            }
        }

        List<Horse> horses = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry: horse_odds.entrySet()){

            String horse_name = entry.getKey();
            List<String> odds_list = entry.getValue();

            // Find the most frequent odds found by the runners
            Map<String, Integer> count = sum_map(odds_list);
            String most_frequent_odds = null;
            for (String potential_most: count.keySet()){
                if (most_frequent_odds == null || count.get(potential_most) > count.get(most_frequent_odds)){
                    most_frequent_odds = potential_most;
                }
            }

            if (most_frequent_odds == null){
                continue;
            }

            int most_occurances = count.get(most_frequent_odds);
            double percentage = ((double) most_occurances) / ((double) odds_list.size());

            if (odds_list.size() > 5 && percentage >= 0.85){
                Horse h = new Horse(horse_name, most_frequent_odds);
                horses.add(h);
            }
        }

        return horses;
    }




    public static String runner2name(String runner_name){
        String[] name_parts = runner_name.toLowerCase().split("\\s");
        name_parts[0] = "";
        String name = String.join("", name_parts);
        return name;
    }


    public static BigDecimal best_back_price(SelectionTypeWithPrices selection){
        BigDecimal best_price = null;

        for (JAXBElement<PricesType> item: selection.getForSidePricesAndAgainstSidePrices()){
            if (item.getName().getLocalPart().equals("ForSidePrices")){

                BigDecimal this_price = item.getValue().getPrice();

                if (best_price == null || this_price.compareTo(best_price) == 1){
                    best_price = this_price;
                }
            }
        }
        return best_price;
    }


    public void run(){

        JSONArray horse_data = null;
        try {
            horse_data = getJSONArray(file_path);
        } catch (FileNotFoundException e) {
            horse_data = new JSONArray();
        } catch (ParseException e){
            e.printStackTrace();
            return;
        }

        // Get next race info
        EventClassifierType nextRace = getNextRace();
        MarketType market = nextRace.getMarkets().get(0);
        Instant nextRace_start = market.getStartTime().toGregorianCalendar().toInstant();

        while (true) {
            try {

                // Get betdaq exchange prices for each horse
                MarketTypeWithPrices betdaq_prices = betdaq._getPrices(market.getId());
                Map<String, BigDecimal> horse_prices = new HashMap<>();
                for (SelectionTypeWithPrices selection: betdaq_prices.getSelections()){
                    String horse_name = runner2name(selection.getName());
                    BigDecimal back_price = best_back_price(selection);
                    horse_prices.put(horse_name, back_price);
                }

                long race_id = market.getId();
                Instant race_time = nextRace_start;

                print("\n\nUsing next race: " + String.valueOf(nextRace.getName()));

                // Collect runners names
                Set<String> horse_names = new HashSet<>();
                for (SelectionType selection : market.getSelections()) {
                    horse_names.add(runner2name(selection.getName()));
                }
                print(horse_names.toString());


                print("Checking screen and collecting horse odds");

                // Constantly check screen for horse runners and their odds and add to list
                List<String> runners = new ArrayList<>();
                while (Instant.now().isBefore(nextRace_start.minusSeconds(15))) {

                    // Get runners from screen
                    List<String> these_runners = sr.extractRunnersFromScreen(x, y, 0.55, 0.75, 7, true);
                    runners.addAll(these_runners);
                    sleep(800);
                }

                // Get all horses from runners
                List<Horse> horses = getHorsesFromRunners(horse_names, runners);
                print(String.format("Found oods for %s/%s horses.", horses.size(), horse_names.size()));
                for (Horse h: horses){
                    print(h + "    price: " + horse_prices.get(h.name));
                }
                print("\n");

                print("Waiting for race to be under way.");
                sleepUntil(nextRace_start.plusSeconds(15));

                // Get next race info
                nextRace = getNextRace();
                market = nextRace.getMarkets().get(0);
                nextRace_start = market.getStartTime().toGregorianCalendar().toInstant();

                print("Waiting on horse winner");
                String winner_name = null;
                while (Instant.now().isBefore(nextRace_start.minusSeconds(90))){
                    winner_name = sr.findWinner(x, y, horse_names);
                    if (winner_name != null){
                        break;
                    }
                    sleep(500);
                }

                if (winner_name == null){
                    print("Could not determine winner");
                    continue;
                }

                print("Winner: " + winner_name);

                for (Horse horse: horses){
                    BigDecimal price = horse_prices.get(horse.name);
                    if (price == null){
                        continue;
                    }

                    JSONObject h = new JSONObject();
                    h.put("name", horse.name);
                    h.put("odds", horse.odds);
                    h.put("d_odds", horse.decimal_odds());
                    h.put("price", price);
                    h.put("winner", winner_name.equals(horse.name));
                    h.put("race", race_id);
                    h.put("time", race_time.toString());
                    h.put("r_size", horse_names.size());

                    horse_data.add(h);
                }

                toFile(horse_data, file_path);
                print("Saved data");


            } catch (InterruptedException e){
                print("Interrupted");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args){
        try {
            vTrader vTrader = new vTrader();
            vTrader.run();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
