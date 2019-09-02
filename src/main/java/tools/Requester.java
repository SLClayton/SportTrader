package tools;


import Trader.SportsTrader;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import static net.dongliu.commons.Prints.print;
import static tools.printer.nully;

public class Requester {

    private static final Logger log = Logger.getLogger(Requester.class.getName());

    HttpClient httpClient;
    HashMap<String, String> headers;
    ReentrantLock headerLock = new ReentrantLock();



    public Requester() {
        //httpClient = HttpClients.createDefault();
        httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        headers = new HashMap<>();
        headers.put("content-type", "application/json");
        headers.put("Accept", "application/json");
    }

    public void setHeader(String key, String value){
        headerLock.lock();
        headers.put(key, value);
        headerLock.unlock();
    }



    public Object post(String url, String json, Map<String, String> headers) throws IOException, URISyntaxException {

        // Create new http POST object
        HttpPost httpPost = new HttpPost(new URI(url));


        // Set default headers from requester object
        headerLock.lock();
        for (Entry<String, String> header: this.headers.entrySet()){
            httpPost.setHeader(header.getKey(), header.getValue());
        }
        headerLock.unlock();

        // Set extra headers if given
        if (headers != null){
            for (Entry<String, String> entry : headers.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
        }

        // Pass in JSON as string to body entity then send request
        httpPost.setEntity(new StringEntity(json));
        HttpResponse response = httpClient.execute(httpPost);

        // Check response code is valid
        int status_code = response.getStatusLine().getStatusCode();
        if (status_code < 200 || status_code >= 300){
            String response_body = EntityUtils.toString(response.getEntity());
            String msg = String.format("ERROR %d in HTTP POST request - %s\n%s\n%s\n%s",
                    status_code, response.toString(), response_body, response.getStatusLine().toString(), json);
            log.severe(msg);
            throw new IOException(msg);
        }

        // Convert body to json and return
        String response_body = EntityUtils.toString(response.getEntity());
        return JSONValue.parse(response_body);
    }


    public Object post(String url, JSONObject json, Map<String, String> headers) throws IOException, URISyntaxException {
        return post(url, json.toString(), headers);
    }


    public Object post(String url, JSONArray json, Map<String, String> headers) throws IOException, URISyntaxException {
        return post(url, json.toString(), headers);
    }


    public Object post(String url, JSONObject json) throws IOException, URISyntaxException {
        return post(url, json, null);
    }


    public Object post(String url, JSONArray json) throws IOException, URISyntaxException {
        return post(url, json, null);
    }


    public Object get(String url, Map<String, Object> params) throws IOException, URISyntaxException {

        // Add in the paramters as the uri is made
        URIBuilder uriBuilder = new URIBuilder(url);
        if (params != null) {
            for (Entry<String, Object> entry : params.entrySet()) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue().toString());
            }
        }

        // Create http GET object
        HttpGet httpGet = new HttpGet(uriBuilder.build());

        headerLock.lock();
        // Add in default headers form requester object
        for (Entry<String, String> header: headers.entrySet()){
            httpGet.setHeader(header.getKey(), header.getValue());
        }
        headerLock.unlock();

        HttpResponse response = httpClient.execute(httpGet);

        // Check response code is valid
        int status_code = response.getStatusLine().getStatusCode();
        if (status_code < 200 || status_code >= 300){
            String response_body = EntityUtils.toString(response.getEntity());
            String msg = String.format("ERROR %d in HTTP GET request - %s\n%s\n%s",
                    status_code, response.toString(), response_body, response.getStatusLine().toString());
            log.severe(msg);
            throw new IOException(msg);
        }

        // Convert body to json and return
        String response_body = EntityUtils.toString(response.getEntity());
        return JSONValue.parse(response_body);
    }


    public String getRaw(String url, Map<String, String> params) throws IOException, URISyntaxException, InterruptedException {

        // Add in the paramters as the uri is made
        URIBuilder uriBuilder = new URIBuilder(url);
        if (params != null) {
            for (Entry<String, String> entry : params.entrySet()) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue().toString());
            }
        }

        // Create http GET object
        HttpGet httpGet = new HttpGet(uriBuilder.build());

        headerLock.lock();
        // Add in default headers form requester object
        for (Entry<String, String> header: headers.entrySet()){
            httpGet.setHeader(header.getKey(), header.getValue());
        }
        headerLock.unlock();

        HttpResponse response = httpClient.execute(httpGet);

        // Check response code is valid
        int status_code = response.getStatusLine().getStatusCode();
        if (status_code == 429){
            long sleeptime = (long)(Math.random() * 1000 + 200);
            log.warning(String.format("TOO MANY REQUESTS error for getRaw request '%s', sleeping for %sms and trying again.",
                    url, String.valueOf(sleeptime)));
            Thread.sleep(sleeptime);
            return getRaw(url, params);
        }
        if (status_code < 200 || status_code >= 300){
            String response_body = EntityUtils.toString(response.getEntity());
            String msg = String.format("ERROR %d in HTTP GET request - %s\n%s\n%s",
                    status_code, response.toString(), response_body, response.getStatusLine().toString());
            log.severe(msg);
            throw new IOException(msg);
        }

        // Convert body to json and return
        String response_body = EntityUtils.toString(response.getEntity());
        return response_body;
    }
}
