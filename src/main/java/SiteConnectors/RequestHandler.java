package SiteConnectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class RequestHandler {

    public Object request;
    public Object response;
    public BlockingQueue<Boolean> responseQueue;


    public RequestHandler(){
        responseQueue = new ArrayBlockingQueue<>(1);
    }

    public RequestHandler(Object request){
        this();
        this.request = request;
    }


    public void setResponse(Object resp) {
        response = resp;
        responseQueue.add(true);
    }


    public Object getResponse() throws InterruptedException {
        responseQueue.take();
        return response;
    }

    public Object pollReponse(long timeout, TimeUnit timeUnit) throws InterruptedException {
        responseQueue.poll(timeout, timeUnit);
        return response;
    }

    public Object pollReponse() {
        responseQueue.poll();
        return response;
    }


    public Object removeResponse(){
        responseQueue.remove();
        return response;
    }


    public void clear(){
        request = null;
        response = null;
        responseQueue.clear();
    }
}
