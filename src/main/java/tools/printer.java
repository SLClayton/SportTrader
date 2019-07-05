package tools;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.google.gson.*;
import com.google.gson.JsonElement;
import com.github.cliftonlabs.json_simple.JsonObject;

import java.io.*;
import java.lang.reflect.Type;


public abstract class printer {

    public static void print(String output){
        System.out.println(output);
    }

    public static void print(int output){
        System.out.println(output);
    }

    public static void p(JsonObject j, String filename){
        JsonElement jsonElement = new JsonParser().parse(j.toJson());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettjson = gson.toJson(jsonElement);
        toFile(prettjson, filename);
    }

    public static void p(JsonArray j, String filename){
        JsonElement jsonElement = new JsonParser().parse(j.toJson());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettjson = gson.toJson(jsonElement);
        toFile(prettjson, filename);
    }

    public static void p(JsonObject j){
        p(j, "output.json");
    }

    public static void p(JsonArray j){
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


    public static void main(String[] args){
        JsonObject j = new JsonObject();
        j.put("name", "Test");
        j.put("age", 34);
        j.put("occupation", "Testing");
        p(j);
    }




}
