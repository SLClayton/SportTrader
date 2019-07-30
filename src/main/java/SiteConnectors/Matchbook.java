package SiteConnectors;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Sport.FootballMatch;
import Sport.Match;
import org.json.simple.JSONArray;
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

    public Requester requester;
    public BigDecimal commission = new BigDecimal("0.017");
    public BigDecimal min_bet = new BigDecimal("0.10");


    public Matchbook() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException,
            IOException, KeyManagementException, KeyStoreException, URISyntaxException {

        requester = new Requester();
        requester.setHeader("session-token", getSessionToken());
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
        Requester requester = new Requester();
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


    public ArrayList<FootballMatch> getEvents(Instant before, Instant after, String[] event_types) throws IOException,
            URISyntaxException {

        // Setup paramters
        JSONObject params = new JSONObject();
        params.put("after", before.toEpochMilli() / 1000);
        params.put("before", after.toEpochMilli() / 1000);
        params.put("offset", 0);
        params.put("per-page", 1000);
        params.put("states", "open");
        params.put("exchange-type", "back-lay");
        params.put("odds-type", "DECIMAL");
        params.put("include-prices", "false");
        params.put("include-event-participants", "false");

        if (event_types != null){
            StringBuilder s = new StringBuilder();
            for (int i=0; i<event_types.length; i++){
                s.append(event_types[i]);

                if (i < event_types.length-1){
                    s.append(",");
                }
            }
            params.put("sport-ids", s.toString());
        }

        JSONObject r = (JSONObject) requester.get(baseurl + "/events", params);

        // Build footballmatch objects from return json events
        ArrayList<FootballMatch> events = new ArrayList<FootballMatch>();
        for (Object json_event_obj: (JSONArray) r.get("events")){
            JSONObject json_event = (JSONObject) json_event_obj;

            try {
                events.add(FootballMatch.parse((String) json_event.get("start"), (String) json_event.get("name")));
            } catch (ParseException e) {
                log.severe(e.toString());
                e.printStackTrace();
                continue;
            }
        }

        return events;
    }



    public static void main(String[] args){

        try {
            Matchbook m = new Matchbook();

            m.getEvents(Instant.now(), Instant.now().plus(48, ChronoUnit.HOURS), new String[]{FOOTBALL_ID});




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
