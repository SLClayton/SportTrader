package tools;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
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
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class Requester {

    private static final Logger log = Logger.getLogger(Requester.class.getName());

    HttpClient httpClient;
    HttpPost httpPost;


    public Requester(String hostname) throws KeyStoreException, IOException, CertificateException,
            NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {


        httpClient = HttpClients.createDefault();
        httpPost = new HttpPost(hostname);
        httpPost.setHeader("content-type", "application/json");
        httpPost.setHeader("Accept", "application/json");
    }

    public void setHeader(String key, String value){
        httpPost.setHeader(key, value);
    }



    public Object post(String url, String json, Map<String, String> headers) throws IOException, URISyntaxException {
        httpPost.setURI(new URI(url));

        // Set headers if given
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
            String msg = String.format("ERROR in HTTP request - %s - %s",
                    response.toString(), response.getStatusLine().toString());
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
}
