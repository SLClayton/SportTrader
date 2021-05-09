package tools;


import Trader.SportsTrader;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.execchain.RequestAbortedException;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.jsoup.select.Evaluator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import static tools.printer.*;

public class Requester {

    private static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    HttpClient httpClient;
    public HashMap<String, String> headers;
    XMLInputFactory xmlInputFactory;
    ReentrantLock headerLock = new ReentrantLock();



    public Requester() {
        //httpClient = HttpClients.createDefault();
        httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD)
                        .setConnectTimeout(5000)
                        .build())
                .build();

        headers = new HashMap<>();
    }


    public static void main(String[] args){
        try {
            Requester r = new Requester();
            r.get("https://google.com");


        } catch (Exception e){
            e.printStackTrace();
        }
        print("END");
    }



    public static Requester JSONRequester(){
        Requester requester = new Requester();
        requester.setHeader("Content-Type", "application/json");
        requester.setHeader("Accept", "application/json");
        return requester;
    }


    public static Requester SOAPRequester(){
        Requester requester = new Requester();
        requester.setHeader("Content-Type", "text/xml");
        requester.xmlInputFactory = XMLInputFactory.newFactory();
        return requester;
    }


    public void setHeader(String key, String value){
        headerLock.lock();
        headers.put(key, value);
        headerLock.unlock();
    }



    public Object SOAPRequest(String url, String soap_header, Object soap_java_obj, Class<?> return_class, boolean print)
            throws IOException, URISyntaxException, JAXBException {
        return SOAPRequest(url, soap_header, SOAP2XML(soap_java_obj), return_class, print);
    }


    public Object SOAPRequest(String url, String soap_header, String soap_body, Class<?> return_class)
            throws IOException, URISyntaxException {

        String response_body = SOAPRequestRaw(url, soap_header, soap_body);
        return XML2SOAP(response_body, return_class);
    }


    public String SOAPRequestRaw(String url, String soap_header, String soap_body)
            throws IOException, URISyntaxException {

        // Build soap xml
        String soap_xml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                        "xmlns:ext=\"http://www.GlobalBettingExchange.com/ExternalAPI/\">" +
                        soap_header +
                        "<soapenv:Body>" +
                        soap_body +
                        "</soapenv:Body></soapenv:Envelope>";


        // Create new http POST object
        HttpPost httpPost = new HttpPost(new URI(url));

        // Set default headers from requester object
        headerLock.lock();
        for (Entry<String, String> header: this.headers.entrySet()){
            httpPost.setHeader(header.getKey(), header.getValue());
        }
        headerLock.unlock();

        // Pass in XML as string to body entity then send request
        httpPost.setEntity(new StringEntity(soap_xml));
        HttpResponse response = httpClient.execute(httpPost);

        // Check response code is valid
        int status_code = response.getStatusLine().getStatusCode();
        if (status_code < 200 || status_code >= 300) {
            String response_body = EntityUtils.toString(response.getEntity());
            String msg = String.format("ERROR %d in HTTP SOAP request - %s\n%s",
                    status_code,
                    response.getStatusLine().toString(),
                    response_body.substring(0, Math.min(response_body.length(), 100)));
            log.severe(msg);
            throw new IOException();
        }

        // Convert body to json and return
        String response_body = EntityUtils.toString(response.getEntity());
        return response_body;
    }


    public Object XML2SOAP(String raw_soap_xml, Class<?> return_class) throws IOException {
        try {
            // Find corresponding object in xml response
            XMLStreamReader xml_reader = xmlInputFactory.createXMLStreamReader(
                    new ByteArrayInputStream(raw_soap_xml.getBytes()), "ISO8859-1");
            xml_reader.nextTag();
            while (!xml_reader.getLocalName().equals(return_class.getSimpleName())) {
                xml_reader.nextTag();
            }

            // Turn that xml object into java object
            JAXBContext jaxbContext = JAXBContext.newInstance(return_class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Object return_object = jaxbUnmarshaller.unmarshal(xml_reader);
            return return_object;
        }
        catch (XMLStreamException | JAXBException e) {
            e.printStackTrace();
            String msg = String.format("Could not turn SOAP response into object of type %s\n%s\n%s",
                    return_class.getSimpleName(), e.toString(), raw_soap_xml.substring(0, 200));
            log.severe(msg);
            throw new IOException(msg);
        }
    }


    public Object post(String url, String json, Map<String, String> headers, Collection<Integer> returnCodes)
            throws IOException, URISyntaxException, RequestAbortedException {

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
        if ((status_code < 200 || status_code > 400) &&
                (returnCodes == null || !returnCodes.contains(status_code))){

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


    public Object post(String url, String json) throws IOException, URISyntaxException {
        return post(url, json, null, null);
    }


    public Object post(String url, JSONObject json, Collection<Integer> returnCodes) throws IOException, URISyntaxException {
        return post(url, json.toString(), null, returnCodes);
    }


    public Object post(String url, JSONObject json, Map<String, String> headers) throws IOException, URISyntaxException {
        return post(url, json.toString(), headers, null);
    }


    public Object post(String url, JSONArray json, Map<String, String> headers) throws IOException, URISyntaxException {
        return post(url, json.toString(), headers, null);
    }


    public Object post(String url, JSONObject json) throws IOException, URISyntaxException {
        return post(url, json, new HashMap<>());
    }


    public Object post(String url, JSONArray json) throws IOException, URISyntaxException {
        return post(url, json, null);
    }


    public Object get(String url, Map<String, Object> params) throws IOException, URISyntaxException,
            InterruptedException {

        String raw_response = getRaw(url, params);
        return JSONValue.parse(raw_response);
    }


    public Object get(String url) throws IOException, URISyntaxException,
            InterruptedException {

        String raw_response = getRaw(url);
        if (raw_response == null){
            return null;
        }
        return JSONValue.parse(raw_response);
    }


    public String getRaw(String url, Map<String, Object> params) throws IOException, URISyntaxException {

        // Add in the paramters as the uri is made
        URIBuilder uriBuilder = new URIBuilder(url);
        if (params != null) {
            for (Entry<String, Object> entry : params.entrySet()) {
                String param_name = entry.getKey();
                String param_value = String.valueOf(entry.getValue());
                uriBuilder.addParameter(param_name, param_value);
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

        HttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
        }
        catch (Exception e){
            log.severe(sf("Exception during getRaw httpClient resp: %s", e.toString()));
            return null;
        }

        if (response == null){
            log.severe("HttpResponse object returned is null in getRaw.");
            return "null";
        }

        // Check response code is valid
        int status_code = response.getStatusLine().getStatusCode();
        if (status_code == 429){
            String response_body = EntityUtils.toString(response.getEntity());
            if (response_body == null){
                response_body = "null";
            }
            return response_body;
        }

        if (status_code == 502){
            String response_body = EntityUtils.toString(response.getEntity());
            if (response_body == null){
                response_body = "null";
            }
            log.warning(String.format("502 error error for GET request '%s', trying again.\n%s", url, response_body));
            return getRaw(url, params);
        }

        if (status_code < 200 || status_code >= 300){
            String response_body = EntityUtils.toString(response.getEntity());
            if (response_body == null){
                response_body = "null";
            }
            if (params == null){
                params = new HashMap<>();
            }
            String msg = String.format("ERROR %d in HTTP GET request\n%s\nurl: %s\nparams: %s\nURI:%s\n%s\n%s",
                    status_code,
                    response.toString(),
                    url,
                    String.valueOf(params),
                    httpGet.getURI().toString(),
                    response_body,
                    response.getStatusLine().toString());
            throw new HttpResponseException(status_code, msg);
        }

        // Convert body to json and return
        String response_body = EntityUtils.toString(response.getEntity());
        return response_body;
    }


    public String getRaw(String url) throws IOException, URISyntaxException {
        return getRaw(url, null);
    }


    public Object delete(String url) throws URISyntaxException, IOException {

        HttpDelete httpDelete = new HttpDelete(new URIBuilder(url).build());

        headerLock.lock();
        // Add in default headers form requester object
        for (Entry<String, String> header: headers.entrySet()){
            httpDelete.setHeader(header.getKey(), header.getValue());
        }
        headerLock.unlock();

        HttpResponse response = httpClient.execute(httpDelete);

        // Check response code is valid
        int status_code = response.getStatusLine().getStatusCode();
        if (status_code < 200 || status_code >= 300){
            String response_body = EntityUtils.toString(response.getEntity());
            if (response_body == null){
                response_body = "null";
            }
            String msg = String.format("ERROR %d in HTTP DELETE request\n%s\nurl: %s\nURI:%s\n%s\n%s",
                    status_code,
                    response.toString(),
                    url,
                    httpDelete.getURI().toString(),
                    response_body,
                    response.getStatusLine().toString());
            log.severe(msg);
            throw new HttpResponseException(status_code, msg);
        }

        // Convert body to json and return
        String response_body = EntityUtils.toString(response.getEntity());
        return JSONValue.parse(response_body);
    }

}
