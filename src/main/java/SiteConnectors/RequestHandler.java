package SiteConnectors;

import Trader.MarketOddsReportWorker;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class RequestHandler {

    public Object request;
    public Object response;
    public BlockingQueue<Boolean> responseQueue;
    public boolean active;

    public MarketOddsReportWorker marketOddsReportWorker;


    public RequestHandler(){
        active = true;
        responseQueue = new ArrayBlockingQueue<>(1);
    }


    public RequestHandler(Object request){
        this();
        this.request = request;
    }

    public boolean isActive(){
        return active;
    }

    public boolean notActive() {
        return !active;
    }


    public void finish(){
        active = false;
        if (marketOddsReportWorker != null){
            marketOddsReportWorker.interrupt();
        }
    }


    public void setResponse(Object resp) {
        response = resp;
        responseQueue.add(true);
    }


    public Object getResponse() throws InterruptedException {
        responseQueue.take();
        return response;
    }

    public Object pollReponse(long timeout) throws InterruptedException {
        responseQueue.poll(timeout, TimeUnit.MILLISECONDS);
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
