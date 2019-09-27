package SiteConnectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class RequestHandler {

    public Object request;
    public JSONObject response;
    public BlockingQueue<JSONObject> responseQueue;
    public boolean valid_response;


    public RequestHandler(){
        responseQueue = new ArrayBlockingQueue<>(1);
    }

    public void setResponse(JSONObject resp) throws InterruptedException {
        response = resp;
        valid_response = true;
        responseQueue.put(response);
    }

    public void setFail() throws InterruptedException {
        response = null;
        valid_response = false;
        responseQueue.put(new JSONObject());
    }

    public JSONObject getResponse() throws InterruptedException {
        return responseQueue.take();
    }
}
