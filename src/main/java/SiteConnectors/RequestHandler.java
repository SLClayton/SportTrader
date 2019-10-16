package SiteConnectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class RequestHandler {

    public Object request;
    public Object response;
    public BlockingQueue<Object> responseQueue;


    public RequestHandler(){
        responseQueue = new ArrayBlockingQueue<>(1);
    }

    public RequestHandler(Object request){
        this();
        this.request = request;
    }

    public boolean valid_response(){
        return response != null;
    }

    public void setResponse(Object resp) throws InterruptedException {
        response = resp;
        responseQueue.put(response);
    }

    public void setFail() throws InterruptedException {
        response = null;
        responseQueue.put(new JSONObject());
    }

    public Object getResponse() throws InterruptedException {
        return responseQueue.take();
    }

    public Object pollReponse(long timeout, TimeUnit timeUnit) throws InterruptedException {
        return responseQueue.poll(timeout, timeUnit);
    }

    public Object pollReponse() {
        return responseQueue.poll();
    }
}
