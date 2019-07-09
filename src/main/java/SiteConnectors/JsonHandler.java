package SiteConnectors;

import org.json.simple.JSONArray;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class JsonHandler {

    public JSONArray request;
    public JSONArray response;
    public BlockingQueue<JSONArray> responseQueue;


    public JsonHandler(){
        responseQueue = new ArrayBlockingQueue<>(1);
    }

    public void setResponse(JSONArray resp) throws InterruptedException {
        response = resp;
        responseQueue.put(response);
    }

    public JSONArray getResponse() throws InterruptedException {
        return responseQueue.take();
    }
}
