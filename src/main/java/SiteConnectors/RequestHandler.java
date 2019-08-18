package SiteConnectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class RequestHandler {

    public String request;
    public JSONObject response;
    public BlockingQueue<JSONObject> responseQueue;


    public RequestHandler(){
        responseQueue = new ArrayBlockingQueue<>(1);
    }

    public void setResponse(JSONObject resp) throws InterruptedException {
        response = resp;
        responseQueue.put(response);
    }

    public JSONObject getResponse() throws InterruptedException {
        return responseQueue.take();
    }
}
