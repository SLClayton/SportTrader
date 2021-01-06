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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.event.WindowStateListener;
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

import static Bet.Bet.ROItester;


public abstract class printer {



    public static String resource_path = "resources/";
    public static final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    public static final String digits = "0123456789";


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


    public static String SOAP2XML(Object SOAP_obj) throws JAXBException {
        Class obj_type = SOAP_obj.getClass();
        Marshaller marshaller = JAXBContext.newInstance(obj_type).createMarshaller();
        StringWriter sw = new StringWriter();
        marshaller.marshal(SOAP_obj, sw);
        return sw.toString();
    }

    public static String SOAP2XMLnull(Object SOAP_obj)  {
        try{
            return SOAP2XML(SOAP_obj);
        }
        catch (JAXBException e){
            return String.format("<JAXB EXCEPTION - %s>", e.toString());
        }
    }


    public static void ppx(String xml){
        print(xmlstring(xml));
    }

    public static void ppxs(Object obj){
        ppx(SOAP2XMLnull(obj));
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


    public static void pp(Map map){
        for (Object obj: map.keySet()){
            psf("%s: %s", stringValue(obj), stringValue(map.get(obj)));
        }
    }


    public static void print(){
        print("");
    }


    public static void print(Object output){
         System.out.println(stringValue(output));
    }


    public static String sf(Object... args){
        return String.format(String.valueOf(args[0]), Arrays.copyOfRange(args, 1, args.length));
    }

    public static void psf(Object... args){
        print(sf(args));
    }

    public static void printf(Object... args){
        psf(args);
    }


    public static String stringValue(Object obj){
        if (obj == null){
            return "null";
        }
        else{
            return String.valueOf(obj);
        }
    }

    public static String sv(Object obj){
        return stringValue(obj);
    }


    public static void exit(int code){
        System.exit(code);
    }

    public static void exit(){
        System.exit(0);
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


    public static long avg(Collection<Long> list){
        long sum = 0;
        for (long item: list){
            sum += item;
        }
        return  sum / list.size();
    }


    public static int count_attribute(JSONArray array, String key, String string_value){
        int count = 0;
        for (Object json_obj: array){
            JSONObject json = (JSONObject) json_obj;
            Object value = json.get(key);
            if (value != null && String.valueOf(value).equals(string_value)){
                count++;
            }
        }
        return count;
    }


    public static String rndString(int length, String alphabet){
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while(sb.length() < length){
            sb.append(alphabet.charAt(r.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    public static String rndString(int length){
        return rndString(length, alphabet);
    }

    public static String rndStringNumber(int length){
        return rndString(length, digits);
    }


    public static List<String> rndStringList(int word_length, int list_length){
        List<String> list = new ArrayList<>();
        for (int i=0; i<list_length; i++){
            list.add(rndString(word_length));
        }
        return list;
    }

    public static List<List<String>> rndStringListList(int word_length, int list_length, int list_list_length){
        List<List<String>> list = new ArrayList<>();
        for (int i=0; i<list_list_length; i++){
            list.add(rndStringList(word_length, list_length));
        }
        return list;
    }


    public static String padto(String input, int n){
        StringBuilder inputBuilder = new StringBuilder(input);
        while (inputBuilder.length() < n){
            inputBuilder.append(" ");
        }
        return inputBuilder.toString();
    }


    public static int randomInt(int min, int max){
        return (int) ((Math.random() * (max - min)) + min);
    }


    



    public static void main(String[] args){

        try {

        }
        catch (Exception e){
            e.printStackTrace();
        }
        print("END.");

    }
}
