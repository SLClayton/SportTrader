package SiteConnectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import java.util.concurrent.locks.Lock;

public class JsonHandler {

    public JSONArray request;
    public JSONArray response;

    public JsonHandler(){
        // Nothing to do
    }

    public void setResponse(JSONArray resp){
        response = resp;
        this.notifyAll();
    }
}
