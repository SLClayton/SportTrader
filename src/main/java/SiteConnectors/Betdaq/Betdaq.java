package SiteConnectors.Betdaq;

import Bet.Bet;
import Bet.BetOrder;
import Bet.PlacedBet;
import SiteConnectors.BettingSite;
import SiteConnectors.SiteEventTracker;
import Sport.FootballMatch;
import Trader.EventTrader;
import org.json.simple.parser.ParseException;
import tools.Requester;
import tools.printer;

import javax.xml.soap.*;

import java.io.FileNotFoundException;
import java.io.IOException;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static tools.printer.getJSON;
import static tools.printer.print;

public class Betdaq extends BettingSite {

    public final static String name = "betdaq";
    public final static String id = "BD";


    public static final long FOOTBALL_ID = 100003;
    public static final long HORSE_RACING_ID = 100004;
    public static final long TENNIS_ID = 100005;
    public static final long GOLF_ID = 100006;
    public static final long CRICKET_ID = 100007;

    public static final String WSDL = "http://api.betdaq.com/v2.0/API.wsdl";
    public static final String readOnlyUrl = "https://api.betdaq.com/v2.0/ReadOnlyService.asmx";
    public static final String secureServiceUrl = "https://api.betdaq.com/v2.0/Secure/SecureService.asmx";

    public String username;
    public String password;


    public Betdaq() throws IOException, ParseException, InterruptedException, URISyntaxException {

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

    }

    @Override
    public String getSessionToken() throws IOException, CertificateException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, UnrecoverableKeyException, URISyntaxException {
        return null;
    }

    @Override
    public BigDecimal commission() {
        return null;
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

        String envolope = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ext=\"http://www.GlobalBettingExchange.com/ExternalAPI/\">" +
                getHeader() +
                "<soapenv:Body>" +
                "<ext:GetAccountBalances>" +
                "<ext:getAccountBalancesRequest/>" +
                "</ext:GetAccountBalances>" +
                "</soapenv:Body>" +
                "</soapenv:Envelope>";

        String reply = requester.SOAPrequest(secureServiceUrl, envolope);

        print(reply);

        //TODO: here

    }

    @Override
    public SiteEventTracker getEventTracker() {
        return null;
    }


    @Override
    public ArrayList<FootballMatch> getFootballMatches(Instant from, Instant until) throws IOException, URISyntaxException, InterruptedException {
        return null;
    }


    @Override
    public List<PlacedBet> placeBets(List<BetOrder> betOrders, BigDecimal MIN_ODDS_RATIO)
            throws IOException, URISyntaxException {

        return null;
    }


    public static void main(String[] args){

        try {
            Betdaq b = new Betdaq();


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
