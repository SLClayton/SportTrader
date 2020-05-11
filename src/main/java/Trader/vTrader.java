package Trader;

import Bet.Bet;
import SiteConnectors.Betdaq.Betdaq;
import Sport.Horse;
import com.globalbettingexchange.externalapi.*;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.codec.binary.StringUtils;
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

    public static String file_path_flats = "resources/vtrader_data/racer_data_horse_flats.json";
    public static String file_path_jumps = "resources/vtrader_data/racer_data_horse_jumps.json";
    public static String file_path_greyhound = "resources/vtrader_data/racer_data_greyhound.json";
    public static String file_path_speedway = "resources/vtrader_data/racer_data_speedway.json";
    public static String file_path_cycling = "resources/vtrader_data/racer_data_cycling.json";
    public static String file_path_cars = "resources/vtrader_data/racer_data_cars.json";

    public String file_path;
    public Betdaq betdaq;
    public ScreenReader sr;
    public long event_id;
    public String event_type;
    public String config_file;

    public int x;
    public int y;

    int runners_width = 196;
    int runners_height = 170;
    int winner_height = 28;
    int winner_width = 170;


    public vTrader(String config_file) throws InterruptedException, URISyntaxException, ParseException, IOException {
        betdaq = new Betdaq(false);
        sr = new ScreenReader();

        this.config_file = config_file;
        JSONObject config = getJSON(config_file);
        event_type = (String) config.get("type");
        x = (int) (long) config.get("x");
        y = (int) (long) config.get("y");


        if (event_type.equals("flats")){
            file_path = file_path_flats;
            event_id = Betdaq.VIRTUAL_HORSES_FLATS_ID;
        }
        else if (event_type.equals("jumps")){
            file_path = file_path_jumps;
            event_id = Betdaq.VIRTUAL_HORSES_JUMPS_ID;
        }
        else if (event_type.equals("dogs")){
            file_path = file_path_greyhound;
            event_id = Betdaq.VIRTUAL_GREYHOUND_ID;
            runners_width += 40;
            winner_width += 40;
        }
        else if (event_type.equals("speedway")){
            file_path = file_path_speedway;
            event_id = Betdaq.VIRTUAL_SPEEDWAY_ID;
        }
        else if (event_type.equals("cycling")){
            file_path = file_path_cycling;
            event_id = Betdaq.VIRTUAL_CYCLING_ID;
        }
        else if (event_type.equals("car")){
            file_path = file_path_cars;
            event_id = Betdaq.VIRTUAL_CARS_ID;
            runners_width += 40;
            winner_width += 40;
        }
        else{
            print("Invalid event type " + event_type);
            System.exit(0);
        }

    }




    public void setup(){
        for (int i=0; true; i++){
            BufferedImage image = sr.screenshot(x, y, runners_width, runners_height);
            image = sr.scale(image, 400 / image.getWidth());
            sr.show(image);
            try {
                sleepUntil(Instant.now().plusMillis(250));

                if (i==8) {
                    JSONObject config = getJSON(config_file);
                    x = (int) (long) config.get("x");
                    y = (int) (long) config.get("y");
                    i=0;
                }


            } catch (InterruptedException | FileNotFoundException | ParseException e) {
                e.printStackTrace();
            }


        }
    }

    public void setupWinner(){
        while (true){
            BufferedImage image = sr.screenshot(x, y, winner_width, winner_height);
            image = sr.scale(image, 400 / image.getWidth());
            sr.show(image);
            try {
                sleepUntil(Instant.now().plusMillis(500));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public String findWinner(Collection<String> possible_winners){
        BufferedImage screenshot = sr.screenshot(x, y, winner_width, winner_height);
        screenshot = sr.scale(screenshot, 4.5);
        sr.show(screenshot);

        double from = 0.6;
        double to = 0.7;
        double num_images = 5.0;
        double interval = (to - from) / (num_images - 1);

        for (int i=0; i<num_images; i++){
            double t = from + (i * interval);
            BufferedImage this_image = sr.black(screenshot, t);

            String rawtext = sr.getText(this_image).toLowerCase().replaceAll("\\s", "");

            for (String horse_name: possible_winners){
                if (rawtext.contains(horse_name)){
                    return horse_name;
                }
            }
        }

        return null;
    }


    public EventClassifierType getNextRace(){
        List<EventClassifierType> races = null;
        try {
            races = Betdaq.getNestedEventsWithMarkets(
                    betdaq.getEventTree(event_id, true).getEventClassifiers());
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
        Map<String, List<String>> horseName_odds_map = new HashMap<>();
        for (String horse_name: horse_names){
            horseName_odds_map.put(horse_name, new ArrayList<>());
        }

        for (String runner: runners){
            // Split horse name and odds
            String name = runner.split(" ")[0];
            String odds = runner.split(" ")[1];

            // If text matches a horse name, add its odds to the list
            if (horseName_odds_map.containsKey(name)){
                horseName_odds_map.get(name).add(odds);
            }
        }

        List<Horse> horses = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry: horseName_odds_map.entrySet()){

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


    public static boolean isNumber(String strNum){
        if (strNum == null) {
            return false;
        }
        try {
            int d = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }


    public static String runner2name(String runner_name){
        String[] name_parts = runner_name.toLowerCase().split("\\s");

        if (isNumber(name_parts[0])){
            name_parts[0] = "";
        }

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

                print(String.format("\n\nUsing next %s race: %s", event_type,  String.valueOf(nextRace.getName())));

                // Collect runners names
                Set<String> horse_names = new HashSet<>();
                for (SelectionType selection : market.getSelections()) {
                    horse_names.add(runner2name(selection.getName()));
                }
                print(horse_names.toString());

                print("Waiting until 1:30 before race.");
                sleepUntil(nextRace_start.minusSeconds(90));
                print("Checking screen and collecting racer odds");

                // Constantly check screen for horse runners and their odds and add to list
                List<String> runners = new ArrayList<>();
                while (Instant.now().isBefore(nextRace_start.minusSeconds(15))) {

                    // Get runners from screen
                    List<String> these_runners = sr.extractRunnersFromScreen(x, y, runners_width, runners_height,
                            0.55, 0.75, 7, true);
                    runners.addAll(these_runners);
                    sleep(800);
                }

                // Get all horses from runners
                List<Horse> horses = getHorsesFromRunners(horse_names, runners);
                print(String.format("Found oods for %s/%s racers.", horses.size(), horse_names.size()));
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

                print("Waiting on race winner");
                String winner_name = null;
                while (Instant.now().isBefore(nextRace_start.minusSeconds(90))){
                    winner_name = findWinner(horse_names);
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


            } catch (InterruptedException | IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args){
        while (true) {
            try {

                if (args.length != 1) {
                    print("1 argument needed");
                }
                String config_path = args[0];
                JSONObject config = getJSON(config_path);
                boolean setup = (boolean) config.get("test");


                vTrader vTrader = new vTrader(config_path);
                if (setup) {
                    vTrader.setup();
                } else {
                    vTrader.run();
                }


            } catch (Exception e) {
                e.printStackTrace();
                try {
                    sleep(60000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
