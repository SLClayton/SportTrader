package SiteConnectors.Betdaq;

import Bet.Bet;
import Bet.BetOrder;
import Bet.PlacedBet;
import SiteConnectors.BettingSite;
import SiteConnectors.SiteEventTracker;
import Sport.FootballMatch;
import Trader.EventTrader;
import com.ctc.wstx.exc.WstxParsingException;
import com.globalbettingexchange.externalapi.*;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import tools.Requester;
import tools.printer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.soap.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

import static tools.printer.*;

public class Betdaq extends BettingSite {

    public final static String name = "betdaq";
    public final static String id = "BD";


    public static final long FOOTBALL_ID = 100003;
    public static final long HORSE_RACING_ID = 100004;
    public static final long TENNIS_ID = 100005;
    public static final long GOLF_ID = 100006;
    public static final long CRICKET_ID = 100007;

    public static final Short MATCH_ODDS_TYPE = 3;

    public static final String WSDL = "http://api.betdaq.com/v2.0/API.wsdl";
    public static final String readOnlyUrl = "https://api.betdaq.com/v2.0/ReadOnlyService.asmx";
    public static final String secureServiceUrl = "https://api.betdaq.com/v2.0/Secure/SecureService.asmx";

    public String username;
    public String password;


    public Betdaq() throws IOException, ParseException, InterruptedException, URISyntaxException {

        commission_rate = new BigDecimal("0.02");

        // Read login info from file
        Map login_details = getJSON(ssldir + "betdaq-login.json");
        username = login_details.get("u").toString();
        password = login_details.get("p").toString();

        requester = Requester.SOAPRequester();

        login();
    }


    public String getHeader(){
        String header = String.format(
                "<soapenv:Header>" +
                "<ext:ExternalApiHeader " +
                "version=\"2\" " +
                "languageCode=\"en\" " +
                "username=\"%s\" " +
                "password=\"%s\" " +
                "applicationIdentifier=\"ST\"/>" +
                "</soapenv:Header>", username, password);
        return header;
    }


    @Override
    public void login() throws IOException, URISyntaxException, InterruptedException {

        updateAccountInfo();
        log.info(String.format("Successfully logged into Betdaq. Balance: %s  Exposure: %s",
                balance.toString(), exposure.toString()));

    }

    @Override
    public String getSessionToken() throws IOException, CertificateException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, UnrecoverableKeyException, URISyntaxException {
        return null;
    }

    @Override
    public BigDecimal commission() {
        return commission_rate;
    }


    @Override
    public String getName() {
        return name;
    }


    @Override
    public String getID() {
        return id;
    }

    @Override
    public BigDecimal minBackersStake() {
        return null;
    }


    @Override
    public void safe_exit() {

    }


    @Override
    public void updateAccountInfo() throws InterruptedException, IOException, URISyntaxException {

        String soap_body =
                "<ext:GetAccountBalances>" +
                "<ext:getAccountBalancesRequest/>" +
                "</ext:GetAccountBalances>";

        GetAccountBalancesResponse b = (GetAccountBalancesResponse)
                requester.SOAPRequest(secureServiceUrl, getHeader(), soap_body, GetAccountBalancesResponse.class);

        balance = b.getGetAccountBalancesResult().getAvailableFunds();
        exposure = b.getGetAccountBalancesResult().getExposure();
    }


    @Override
    public SiteEventTracker getEventTracker() {
        return null;
    }


    @Override
    public List<FootballMatch> getFootballMatches(Instant from, Instant until)
            throws IOException, URISyntaxException, InterruptedException {

        String body = String.format("<ext:GetEventSubTreeNoSelections>" +
                "<ext:getEventSubTreeNoSelectionsRequest WantDirectDescendentsOnly=\"false\" WantPlayMarkets=\"false\">" +
                "<ext:EventClassifierIds>%s</ext:EventClassifierIds>" +
                "</ext:getEventSubTreeNoSelectionsRequest>" +
                "</ext:GetEventSubTreeNoSelections>",
                FOOTBALL_ID);

        GetEventSubTreeNoSelectionsResponse r = (GetEventSubTreeNoSelectionsResponse)
                requester.SOAPRequest(readOnlyUrl, getHeader(), body,
                                      GetEventSubTreeNoSelectionsResponse.class, false);

        ReturnStatus rs = r.getGetEventSubTreeNoSelectionsResult().getReturnStatus();
        if (rs.getCode() != 0){
            log.severe(String.format("Could not get football matches from betdaq. Error %s - '%s'",
                    rs.getCode(), rs.getDescription()));
            return null;
        }

        // From all event types returned, find the lowest level events which should be singular matches
        // by checking all nested events for ones with markets.
        List<EventClassifierType> events =
                getNestedEventsWithMarkets(r.getGetEventSubTreeNoSelectionsResult().getEventClassifiers());

        // Compile string regex for parts of name to remove
        String time_regex = "\\d\\d:\\d\\d";
        String day_regex = "\\((mon|tue|wed|thur|fri|sat|sun)\\)";
        String extra_regex = "\\(i\\/r\\)";
        Pattern illegal_front_words = Pattern.compile(String.format("(%s)|(%s)|(%s)", time_regex, day_regex, extra_regex));

        List<FootballMatch> footballMatches = new ArrayList<>();
        for (EventClassifierType event: events){

            // Check event has a Match odds Market (ensures its a match)
            MarketType matchOddsMarket = null;
            for (MarketType market: event.getMarkets()){
                if (market.getType() == MATCH_ODDS_TYPE){
                    matchOddsMarket = market;
                    break;
                }
            }
            if (matchOddsMarket == null){
                continue;
            }

            // Start time of match odds market is start time of match
            Instant starttime = matchOddsMarket.getStartTime().toGregorianCalendar().toInstant();
            if (starttime.isAfter(until) || starttime.isBefore(from)){
                continue;
            }

            // Find first and last words in name and remove if illegal add-ons
            String[] words = event.getName().toLowerCase().split("\\s");
            if (illegal_front_words.matcher(words[0]).matches()){
                words[0] = "";
            }
            if (words[words.length-1].equals("(live)")){
                words[words.length-1] = "";
            }
            String name = String.join(" ", words).trim();

            try {
                FootballMatch fm = FootballMatch.parse(starttime, name);
                footballMatches.add(fm);
            }
            catch (java.text.ParseException e){
                continue;
            }
        }

        return footballMatches;
    }


    public List<EventClassifierType> getNestedEventsWithMarkets(List<EventClassifierType> eventClassifierTypes){

        // Through the layers of nested Events, find the events that have markets
        List<EventClassifierType> with_markets = new ArrayList<>();
        for (EventClassifierType event: eventClassifierTypes){

            List<EventClassifierType> child_events = event.getEventClassifiers();
            List<MarketType> markets = event.getMarkets();

            // If this event has nested events, recurse this function and add them to markets.
            if (child_events != null && child_events.size() > 0){
                with_markets.addAll(getNestedEventsWithMarkets(child_events));
            }
            // Add to list if event has any markets
            else if (markets != null && markets.size() > 0){
                with_markets.add(event);
            }
        }

        return with_markets;
    }


    @Override
    public List<PlacedBet> placeBets(List<BetOrder> betOrders, BigDecimal MIN_ODDS_RATIO)
            throws IOException, URISyntaxException {

        return null;
    }


    public static void main(String[] args){

        try {
            Betdaq b = new Betdaq();

            List<FootballMatch> fms = b.getFootballMatches(Instant.now(),
                    Instant.now().plusSeconds(60*60*24));

            for (FootballMatch fm: fms){
                print(fm.toString());
            }



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        print("END");
    }



}
