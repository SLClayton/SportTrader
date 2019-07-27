package SiteConnectors;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Sport.FootballMatch;
import Sport.Match;
import org.json.simple.JSONObject;
import tools.Requester;
import tools.printer;
import tools.printer.*;

import static tools.printer.*;

public class Matchbook extends BettingSite {

    public static String name = "matchbook";
    public static String baseurl = "https://api.matchbook.com/edge/rest";
    public static String[] marketTypes = new String[]{
            "one_x_two",
            "total",
            "handicap",
            "both_to_score",
            "correct_score"};
    public static String FOOTBALL_ID = "15";


    public BigDecimal commission = new BigDecimal("0.017");
    public BigDecimal min_bet = new BigDecimal("0.10");


    public Matchbook(){

    }

    @Override
    public String getSessionToken() throws IOException, CertificateException,
            NoSuchAlgorithmException, KeyStoreException, KeyManagementException,
            UnrecoverableKeyException, URISyntaxException {

        String path = ssldir + "/matchbook-login.json";
        Map creds = printer.getJSON(path);
        JSONObject data = new JSONObject();
        data.put("username", creds.get("u"));
        data.put("password", creds.get("p"));

        String url = "https://api.matchbook.com/bpapi/rest/security/session";
        Requester requester = new Requester(url);
        requester.setHeader("Content-Type", "application/json");

        JSONObject r = (JSONObject) requester.post(url, data);

        if (!r.containsKey("session-token")){
            String msg = String.format("No session token found in matchbook login response.\n%s",
                    ps(r));
            log.severe(msg);
            throw new IOException(msg);
        }

        return (String) r.get("session-token");
    }

    @Override
    public BigDecimal commission() {
        return commission;
    }

    @Override
    public BigDecimal minBet() {
        return min_bet;
    }

    @Override
    public SiteEventTracker getEventTracker() {
        return new MatchbookEventTracker(this);
    }


    public ArrayList<FootballMatch> getEvents(Instant before, Instant after, String[] event_types){
        long start = before.toEpochMilli() / 1000;
        long end = after.toEpochMilli() / 1000;

        // TODO getEvents for matchbook
    }



    public static void main(String[] args){

        try {
            Matchbook m = new Matchbook();
            String token = m.getSessionToken();
            print(token);




        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
