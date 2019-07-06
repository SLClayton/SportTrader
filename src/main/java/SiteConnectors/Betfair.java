package SiteConnectors;

import net.dongliu.requests.Requests;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import tools.printer;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

public class Betfair extends BettingSite {

    public String url = "https://api.betfair.com/exchange/betting/json-rpc/v1";
    public BigDecimal min_bet;
    public BigDecimal commission_discount;
    public String app_id = "3BD65v2qKzw9ETp9";
    public String app_id_dev = "DfgkZAnb0qi6Wmk1";

    public static String[] football_market_types = new String[] {
            "OVER_UNDER_05",
            "OVER_UNDER_15",
            "OVER_UNDER_25",
            "OVER_UNDER_35",
            "OVER_UNDER_45",
            "OVER_UNDER_55",
            "OVER_UNDER_65",
            "OVER_UNDER_75",
            "OVER_UNDER_85",
            "MATCH_ODDS",
            "CORRECT_SCORE"};


    public Betfair() throws MalformedURLException {

        min_bet = new BigDecimal("2.00");
        commission_discount = BigDecimal.ZERO;

    }

    @Override
    public String getSessionToken() throws IOException, CertificateException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, UnrecoverableKeyException {

        String loginurl = "https://identitysso-cert.betfair.com/api/certlogin";

        Map login_details = printer.getJSON(ssldir + "betfair-login.json");
        String username = login_details.get("u").toString();
        String password = login_details.get("p").toString();

        String payload = String.format("username=%s&password=%s".format(username, password));
        HashMap<String, String> params = new HashMap<String, String>();

        SSLContext sslcontext = SSLContexts.custom()
                .loadTrustMaterial(new File(ssldir + "betfair-client-2048.crt"), null, new TrustSelfSignedStrategy())
                .loadKeyMaterial(new File(ssldir + "betfair-client-2048.key"), null, null)
                .build();


        HttpClient client = HttpClient.newBuilder()
                .sslContext(sslcontext)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .header("X-Application", app_id)
                .header("Content-type", "application/x-www-form-urlencoded")
                .POST()
                .build();

        HttpPost httppost = new HttpPost("http://www.a-domain.com/foo/");
        httppost.setEntity();

        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair("param-1", "12345"));
        params.add(new BasicNameValuePair("param-2", "Hello!"));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        //Execute and get the response.
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            try (InputStream instream = entity.getContent()) {
                // do something useful
            }
        }




    }


    @Override
    public BigDecimal commission() {
        return new BigDecimal("0.05").subtract(commission_discount);
    }

    @Override
    public BigDecimal minBet() {
        return null;
    }


    public static void main(String[] args){
        try {
            Betfair b = new Betfair();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
