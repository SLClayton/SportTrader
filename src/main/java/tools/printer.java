package tools;


import com.google.gson.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileSystems;
import java.util.*;

public abstract class printer {


    public static String ps(JSONArray j){
        JsonElement jsonElement = new JsonParser().parse(j.toString());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettjson = gson.toJson(jsonElement);
        return prettjson;
    }

    public static String ps(JSONObject j){
        JsonElement jsonElement = new JsonParser().parse(j.toString());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettjson = gson.toJson(jsonElement);
        return prettjson;
    }

    public static void pp(JSONArray j){
        print(ps(j));
    }

    public static void pp(JSONObject j){
        print(ps(j));
    }

    public static void print(Object output){
        System.out.println(output.toString());
    }

    public static void print(int output){
        System.out.println(output);
    }

    public static void p(JSONObject j, String filename){
        toFile(ps(j), filename);
    }

    public static void p(JSONArray j, String filename){
        toFile(ps(j), filename);
    }

    public static void p(JSONObject j){
        p(j, "output.json");
    }

    public static void p(JSONArray j){
        p(j, "output.json");
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

    public static Map getJSONResource(String filename) throws FileNotFoundException {
        filename = "src/main/resources/" + filename;
        String jsonString = new Scanner(new File(filename)).useDelimiter("\\Z").next();
        Gson gson = new Gson();
        Map map = gson.fromJson(jsonString, HashMap.class);
        return map;
    }

    public static Map getJSON(String filename) throws FileNotFoundException {
        String jsonString = new Scanner(new File(filename)).useDelimiter("\\Z").next();
        Gson gson = new Gson();
        Map map = gson.fromJson(jsonString, Map.class);
        return map;
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


    public static void main(String[] args){
        Map json = null;
        try {
            json = getJSONResource("config.json");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        print(json.get("HOURS_AHEAD").toString());
        print(FileSystems.getDefault().getPath(".").toAbsolutePath().toString());
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
        return nully(obj, false);
    }



}
