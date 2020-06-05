package tools;


import SiteConnectors.Betdaq.Betdaq;
import SiteConnectors.Betfair.Betfair;
import SiteConnectors.Smarkets.Smarkets;
import Trader.SportsTrader;
import com.google.gson.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Statement;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.logging.Logger;


public abstract class printer {

    public static Logger log = Logger.getLogger(SportsTrader.class.getName());

    public static String resource_path = "resources/";

    public static final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";


    public static String jstring(JSONArray j){
        JsonElement jsonElement = new JsonParser().parse(j.toString());
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        String prettyjson = gson.toJson(jsonElement);
        return prettyjson;
    }

    public static String jstring(JSONObject j){
        JsonElement jsonElement = new JsonParser().parse(j.toString());
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        String prettyjson = gson.toJson(jsonElement);
        return prettyjson;
    }


    public static void ppx(String xml){
        print(xmlstring(xml));
    }

    public static void ppxs(Object obj){
        ppx(Requester.SOAP2XMLnull(obj));
    }

    public static String xmlstring(String xml){
        return xml.replaceAll(">\\s*<", ">\n<");
    }


    public static String mapString(Map<String, ?> map){
        int longest_key = 0;
        for (Object key: map.keySet()){
            String key_string = String.valueOf(key);
            longest_key = Math.max(longest_key, key_string.length());
        }

        List<String> sorted_keys = new ArrayList<>(map.keySet());
        sorted_keys.sort(Comparator.naturalOrder());

        String s = "";
        for (String key: sorted_keys){

            String key_string = String.valueOf(key);
            while (key_string.length() < longest_key){
                key_string += " ";
            }

            s += key_string + " : " + String.valueOf(map.get(key));
            s += "\n";
        }
        if (s.endsWith("\n")){
            s = s.substring(0, s.length() - 2);
        }
        return s;
    }

    public static void pm(Map<String, ?> map){
        print(mapString(map));
    }


    public static void pp(JSONArray j){
        print(jstring(j));
    }


    public static void pp(JSONObject j){
        print(jstring(j));
    }


    public static void print(Object output){
         System.out.println(stringValue(output));
    }


    public static String stringValue(Object obj){
        if (obj == null){
            return "null";
        }
        else{
            return String.valueOf(obj);
        }
    }

    public static String BDString(BigDecimal input){
        if (input == null){
            return "null";
        }
        else{
            return input.stripTrailingZeros().toPlainString();
        }
    }


    public static void toFile(JSONObject j, String filename){
        toFile(jstring(j), filename);
    }


    public static void toFile(JSONArray j, String filename){
        toFile(jstring(j), filename);
    }


    public static void toFile(JSONObject j){
        toFile(j, "output.json");
    }


    public static void toFile(JSONArray j){
        toFile(j, "output.json");
    }


    public static void toFile(JSONObject j, int i){
        toFile(j, "output" + i + ".json");
    }


    public static void toFile(JSONArray j, int i){
        toFile(j, "output" + i + ".json");
    }


    public static void toFile(String s){
        toFile(s, "output.txt");
    }


    public static void toFile(String s, String filename) {
        PrintWriter writer = null;

        try {
            writer = new PrintWriter(filename, "UTF-8");
            writer.println(s);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    public static void renameResourceFile(String original, String new_name){
        File fileold = new File(original);

        for (int i=0; true; i++){

            File filenew = new File(resource_path + new_name + "-" + i);

            boolean success = fileold.renameTo(filenew);
            if (success){
                break;
            }
        }
    }


    public static JSONObject getJSONResource(String filename) throws FileNotFoundException, ParseException {
        return getJSON(resource_path + filename);
    }


    public static JSONObject getJSON(String filename) throws FileNotFoundException, ParseException {
        return (JSONObject) new JSONParser().parse(getFileString(filename));
    }


    public static JSONArray getJSONArray(String filename) throws FileNotFoundException, ParseException {
        return (JSONArray) new JSONParser().parse(getFileString(filename));
    }


    public static String getFileString(String filename) throws FileNotFoundException {
        return new Scanner(new File(filename)).useDelimiter("\\Z").next();
    }


    public static String getResourceFileString(String filename) throws FileNotFoundException {
        return getFileString(resource_path + filename);
    }


    public static void saveJSONResource(JSONObject json, String filename){
        toFile(json, resource_path + filename);
    }


    public static void saveJSONResource(JSONArray json, String filename){
        toFile(json, resource_path + filename);
    }


    public static List<List<String>> shard(Collection<String> unchunked, int shard_size){

        List<List<String>> chunked_list = new ArrayList();
        List<String> this_chunk = new ArrayList();

        for (String item: unchunked){
            this_chunk.add(item);

            if (this_chunk.size() >= shard_size){
                chunked_list.add(this_chunk);
                this_chunk = new ArrayList();
            }
        }
        if (this_chunk.size() > 0){
            chunked_list.add(this_chunk);
        }

        return chunked_list;
    }


    public static void sleepUntil(Instant sleep_until, Long milliLockStep) throws InterruptedException {

        // If null then just return
        if (sleep_until == null){
            return;
        }

        // If a lockstep is in place then work out next step after the sleep until
        if (milliLockStep != null && milliLockStep > 1) {
            long millis_comp = sleep_until.getLong(ChronoField.MILLI_OF_SECOND);
            long extra_millis = 0;
            while (extra_millis < millis_comp) {
                extra_millis += milliLockStep;
            }
            sleep_until = sleep_until.truncatedTo(ChronoUnit.SECONDS).plusMillis(extra_millis);
        }

        // Calculate time needed to sleep and sleep that long.
        long time_to_sleep = sleep_until.toEpochMilli() - Instant.now().toEpochMilli();
        if (time_to_sleep > 0){
            Thread.sleep(time_to_sleep);
        }
    }


    public static void sleepUntil(Instant sleep_until) throws InterruptedException {
        sleepUntil(sleep_until, null);
    }

    public static void sleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }


    public static void makeDirIfNotExists(String dir_name){
        File dir = new File(FileSystems.getDefault().getPath(".") + "/" + dir_name);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }


    public static Map<String, Integer> sum_map(Collection<String> from_list, Collection<String> options){
        HashSet<String> options_set = new HashSet<>(options);
        int total = 0;
        Map<String, Integer> count = new HashMap<>();
        for (String site_name: options_set){
            int n = Collections.frequency(from_list, site_name);
            count.put(site_name, n);
            total += n;
        }
        return count;
    }


    public static Map<String, Integer> sum_map(Collection<String> from_list){
        Map<String, Integer> count = new HashMap<>();
        for (String item: from_list){
            Integer current_count = count.get(item);
            if (current_count == null){
                current_count = 0;
            }
            count.put(item, current_count + 1);
        }
        return count;
    }

    public static String most_common(Collection<String> items){
        Map<String, Integer> count = sum_map(items);

        String current_most = null;
        for (String potential_most: count.keySet()){
            if (current_most == null || count.get(potential_most) > count.get(current_most)){
                current_most = potential_most;
            }
        }
        return current_most;
    }


    public static BigDecimal BDMax(BigDecimal a, BigDecimal b){
        if (a == null){
            return b;
        }
        if (b == null){
            return a;
        }
        return a.max(b);
    }

    public static BigDecimal BDMin(BigDecimal a, BigDecimal b){
        if (a == null){
            return b;
        }
        if (b == null){
            return a;
        }
        return a.min(b);
    }


    public static long avg(Collection<Long> list){
        long sum = 0;
        for (long item: list){
            sum += item;
        }
        return  sum / list.size();
    }


    public static BigDecimal round(BigDecimal value, BigDecimal increment, RoundingMode roundingMode) {
        int signum = increment.signum();
        if (signum == -1){
            return null;
        }
        else if (signum == 0) {
            // 0 increment does not make much sense, but prevent division by 0
            return value;
        }
        else {
            BigDecimal divided = value.divide(increment, 0, roundingMode);
            BigDecimal result = divided.multiply(increment);
            return result;
        }
    }


    public static BigDecimal smallestStep(BigDecimal[] arr){
        BigDecimal smallest_step = null;
        for (int i=0; i<arr.length-1; i++){
            BigDecimal step_size = arr[i+1].subtract(arr[i]);
            smallest_step = BDMin(smallest_step, step_size);
        }
        return smallest_step;
    }


    public static Integer[] aboveBelowTargetValues(int[] values, BigDecimal exact_target){

        if (exact_target.compareTo(new BigDecimal(values[0])) < 0){
            return new Integer[] {null, values[0]};
        }

        int last_value = values[values.length-1];
        if (exact_target.compareTo(new BigDecimal(last_value)) > 0){
            return new Integer[] {last_value, null};
        }

        long rounded_down_price = exact_target.setScale(0, RoundingMode.DOWN).longValue();

        for (int i=0; i<values.length-1; i++){

            int below = values[i];
            int above = values[i+1];

            if (below <= rounded_down_price && rounded_down_price < above){
                if (new BigDecimal(below).compareTo(exact_target) == 0){
                    above = below;
                }
                return new Integer[] {below, above};
            }
        }

        if (new BigDecimal(last_value).compareTo(exact_target) == 0){
            return new Integer[] {last_value, last_value};
        }
        return null;
    }



    public static BigDecimal findClosest(BigDecimal[] sorted_array, BigDecimal target, RoundingMode roundingMode){

        int index_closest = findIndexClosest(sorted_array, target);
        BigDecimal closest = sorted_array[index_closest];

        int lower_index;
        int  upper_index;

        switch (closest.compareTo(target)){

            // Closest value is higher than target
            case 1:
                lower_index = index_closest - 1;
                upper_index = index_closest;
                break;

            // Closest value was lower than target
            case -1:
                lower_index = index_closest;
                upper_index = index_closest + 1;
                break;

            // Closest value is equal to target
            default:
                return closest;
        }


        if (lower_index < 0){
            return sorted_array[upper_index];
        }
        if (upper_index >= sorted_array.length){
            return sorted_array[lower_index];
        }

        BigDecimal lower_value = sorted_array[lower_index];
        BigDecimal upper_value = sorted_array[upper_index];

        return getClosest(lower_value, upper_value, target, roundingMode);
    }


    public static BigDecimal findClosest(BigDecimal[] arr, BigDecimal target){
        return arr[findIndexClosest(arr, target)];
    }


    public static int findIndexClosest(BigDecimal[] arr, BigDecimal target)
    {
        int n = arr.length;

        if (target.compareTo(arr[0]) <= 0) {
            return 0;
        }
        if (target.compareTo(arr[n-1]) >= 0) {
            return n-1;
        }


        int i = 0, j = n, mid = 0;
        while (i < j) {
            mid = (i + j) / 2;

            if (arr[mid].compareTo(target) == 0) {
                return mid;
            }

            if (target.compareTo(arr[mid]) < 0){

                if (mid > 0 && target.compareTo(arr[mid-1]) > 0){
                    if (getClosest(arr[mid-1], arr[mid], target).equals(arr[mid])){
                        return mid;
                    }
                    return mid-1;
                }

                j = mid;
            }

            else {
                if (mid < n-1 && target.compareTo(arr[mid-1]) < 0){
                    if (getClosest(arr[mid], arr[mid+1], target).equals(arr[mid])){
                        return mid;
                    }
                    return mid+1;
                }

                i = mid + 1;
            }
        }

        return mid;
    }

    public static BigDecimal getClosest(BigDecimal val1, BigDecimal val2, BigDecimal target)
    {
        return getClosest(val1, val2, target, RoundingMode.HALF_UP);
    }


    public static BigDecimal getClosest(BigDecimal val1, BigDecimal val2, BigDecimal target, RoundingMode roundingMode){

        BigDecimal lower_value = BDMin(val1, val2);
        BigDecimal upper_value = BDMax(val1, val2);

        if (roundingMode == RoundingMode.UP){
            return upper_value;
        }
        if (roundingMode == RoundingMode.DOWN){
            return lower_value;
        }

        BigDecimal dist_below = target.subtract(lower_value).abs();
        BigDecimal dist_above = target.subtract(upper_value).abs();

        if (dist_below.compareTo(dist_above) < 0){
            return lower_value;
        }
        if (dist_above.compareTo(dist_below) < 0){
            return upper_value;
        }

        // If gotten this far, then value is equa-distant, return based on rounding mode.
        if (roundingMode == RoundingMode.HALF_DOWN){
            return lower_value;
        }
        if (roundingMode == RoundingMode.HALF_UP){
            return upper_value;
        }

        log.severe("Invalid rounding mode for getClosest - " + stringValue(roundingMode));
        return null;
    }


    public static String rndString(int length){
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while(sb.length() < length){
            sb.append(alphabet.charAt(r.nextInt(alphabet.length())));
        }
        return sb.toString();
    }


    public static void main(String[] args){

        try {

            print(rndString(12));



        }
        catch (Exception e){
            e.printStackTrace();
        }
        print("END.");

    }
}
