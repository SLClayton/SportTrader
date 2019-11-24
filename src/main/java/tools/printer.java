package tools;


import com.google.gson.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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


public abstract class printer {

    public static String resource_path = "resources/";


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


    public static void pp(JSONArray j){
        print(jstring(j));
    }


    public static void pp(JSONObject j){
        print(jstring(j));
    }


    public static void print(Object output){
        if (output == null){
            print("null");
        }
        else {
            System.out.println(String.valueOf(output));
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





    public static void makeDirIfNotExists(String dir_name){
        File dir = new File(FileSystems.getDefault().getPath(".") + "/" + dir_name);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }


    public static void main(String[] args) {
        ArrayList<String> ints = new ArrayList<>();
        ints.add("1");
        ints.add("2");
        ints.add("3");
        ints.add("4");
        ints.add("5");
        ints.add("6");
        ints.add("7");
        ints.add("8");
        ints.add("9");
        ints.add("10");
        ints.add("11");
        ints.add("12");
        print(shard(ints, 2));
    }


    public static boolean nully(Object obj, boolean printNotNull){
        if (obj == null){
            print("Object is NULL.");
            return true;
        }
        if (printNotNull) {
            print("Object is NOT NULL.");
        }
        return false;
    }


    public static boolean nully(Object obj){
        return nully(obj, true);
    }


    public static Map<String, Integer> count(Set<String> options, Collection<String> from_list){
        int total = 0;
        Map<String, Integer> count = new HashMap<>();
        for (String site_name: options){
            int n = Collections.frequency(from_list, site_name);
            count.put(site_name, n);
            total += n;
        }
        return count;
    }
}
