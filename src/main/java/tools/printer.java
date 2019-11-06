package tools;


import com.google.gson.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileSystems;
import java.sql.Statement;
import java.util.*;

public abstract class printer {

    public static String resource_path = "src/main/resources/";


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


    public static void print(int output){
        System.out.println(output);
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


    public static Map getJSONResourceMap(String filename) throws FileNotFoundException {
        filename = resource_path + filename;
        String jsonString = new Scanner(new File(filename)).useDelimiter("\\Z").next();
        Gson gson = new Gson();
        Map map = gson.fromJson(jsonString, HashMap.class);
        return map;
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
        filename = "src/main/resources/" + filename;
        String jsonString = new Scanner(new File(filename)).useDelimiter("\\Z").next();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(jsonString);
        return json;
    }

    public static void saveJSONResource(JSONObject json, String filename){
        filename = "src/main/resources/" + filename;
        toFile(json, filename);
    }

    public static void saveJSONResource(JSONArray json, String filename){
        filename = "src/main/resources/" + filename;
        toFile(json, filename);
    }

    public static Map getJSON(String filename) throws FileNotFoundException {
        String jsonString = new Scanner(new File(filename)).useDelimiter("\\Z").next();
        Gson gson = new Gson();
        Map map = gson.fromJson(jsonString, Map.class);
        return map;
    }

    public static String fromFile(String filename) throws FileNotFoundException {
        String s = new Scanner(new File(filename)).useDelimiter("\\Z").next();
        return s;
    }


    public static ArrayList<ArrayList<String>> shard(String[] unchunked_list, int shard_size){

        ArrayList<ArrayList<String>> chunked_list = new ArrayList<ArrayList<String>>();
        ArrayList<String> this_chunk = new ArrayList<String>();

        for (int i=0; i<unchunked_list.length; i++){

            this_chunk.add(unchunked_list[i]);

            if (this_chunk.size() >= shard_size || i == (unchunked_list.length - 1)){
                chunked_list.add(this_chunk);
                this_chunk = new ArrayList<String>();
            }
        }
        return chunked_list;
    }


    public static void main(String[] args) {

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



}
