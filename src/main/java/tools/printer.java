package tools;


import com.google.gson.*;
import org.json.simple.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public abstract class printer {

    public static void print(String output){
        System.out.println(output);
    }

    public static void print(int output){
        System.out.println(output);
    }

    public static void p(JSONObject j, String filename){

        JsonElement jsonElement = new JsonParser().parse(j.toString());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettjson = gson.toJson(jsonElement);
        toFile(prettjson, filename);
    }

    public static void p(JSONArray j, String filename){
        JsonElement jsonElement = new JsonParser().parse(j.toString());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettjson = gson.toJson(jsonElement);
        toFile(prettjson, filename);
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
        Map map = gson.fromJson(jsonString, Map.class);
        return map;
    }

    public static Map getJSON(String filename) throws FileNotFoundException {
        String jsonString = new Scanner(new File(filename)).useDelimiter("\\Z").next();
        Gson gson = new Gson();
        Map map = gson.fromJson(jsonString, Map.class);
        return map;
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




}
