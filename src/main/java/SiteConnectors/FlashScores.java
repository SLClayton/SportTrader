package SiteConnectors;

import Sport.FootballEventState;
import Sport.FootballMatch;
import Sport.Team;
import Trader.SportsTrader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import tools.Requester;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static tools.printer.*;

public class FlashScores {

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    public static final Integer FOOTBALL = 1;

    Requester requester;

    public FlashScores(){
        requester = new Requester();

    }


    public void getEvent(String id) throws InterruptedException, IOException, URISyntaxException {
        String url = String.format("https://www.flashscore.co.uk/match/%s/#match-summary", id);

        String response = requester.getRaw(url);
    }

    public FootballEventState getFootballState(String match_id) throws InterruptedException, IOException, URISyntaxException {
        String url = String.format("https://www.flashscore.co.uk/match/%s/#match-summary", match_id);
        String response = requester.getRaw(url);

        Document doc = Jsoup.parse(response);
        Element result = doc.getElementById("event_detail_current_result");
        Elements scores = result.getElementsByClass("scoreboard");

        if (scores.size() != 2){
            log.severe(String.format("Something other than 2 scores found when searching for score in flashscores." +
                    "\n%s", scores.toString()));
        }


        FootballEventState fes = new FootballEventState();
        fes.score_a = Integer.valueOf(scores.get(0).text());
        fes.score_b = Integer.valueOf(scores.get(1).text());

        return fes;

    }

    public static String getMatchID(Team team, Instant start_time) throws InterruptedException, IOException,
            URISyntaxException {

        String url = String.format("https://www.flashscore.co.uk/team/%s/%s/fixtures/",
                team.FS_URLNAME, team.FS_ID);
        Requester requester = new Requester();
        String raw = requester.getRaw(url);

        Document doc = Jsoup.parse(raw);
        Element fixtures_element = doc.getElementById("participant-page-data-fixtures");
        if (fixtures_element == null){
            log.severe(String.format("Fixture data cannot be found in raw page while searching for '%s' fixtures.",
                    url));
            return null;
        }

        String raw_fixtures = fixtures_element.text();
        toFile(raw_fixtures, "output");

        Pattern pattern = Pattern.compile("÷[a-zA-Z]{8}¬");
        Matcher m = pattern.matcher(raw_fixtures);
        while (m.find()){
            String id = m.group().substring(1, 9);

            // TODO: Got fixture IDs, find which one matches the time given and return that
        }

        return null;

    }

    public static Team searchTeam(String query, Integer sport_id) throws InterruptedException, IOException,
            URISyntaxException {

        // Get raw response from flashscores
        Requester requester = new Requester();
        HashMap<String, String> params = new HashMap<>();
        params.put("q", query);
        params.put("l", "5");
        params.put("s", "0");
        params.put("f", "1;1");
        params.put("pid", "5");
        params.put("sid", "1");
        String response = requester.getRaw("https://s.livesport.services/search/", params).trim();

        // Confirm pattern with JSON in right place
        Pattern pattern = Pattern.compile("\\Acjs.search.jsonpCallback\\(.*\\);\\z");
        Matcher m = pattern.matcher(response);
        if (!m.matches()){
            log.severe(String.format("Cannot decipher raw return when searching for teamID in flashscores.\n%s",
                    response));
            return null;
        }

        // Extract JSON and parse to JSONObject
        String json_string = response.substring(25, response.length()-2);
        JSONObject json = null;
        try {
            json = (JSONObject) new JSONParser().parse(json_string);
        } catch (ParseException e) {
            e.printStackTrace();
            log.severe(String.format("Cannot decipicher JSON return from flashscores when searching for '%s'.\n%S",
                    query, json_string));
            return null;
        }

        // Extract results then filter for correct sport ID
        JSONArray results = (JSONArray) json.get("results");
        ArrayList<JSONObject> filtered_results = new ArrayList<>();
        for (Object result_obj: results) {
            JSONObject result = (JSONObject) result_obj;

            if (((long) result.get("sport_id")) == sport_id) {
                filtered_results.add(result);
            }
        }

        if (filtered_results.size() == 0){
            log.warning(String.format("No results found searching for sportid:%d '%s' in flashscores.",
                    sport_id, query));
            return null;
        }

        JSONObject first_result = (JSONObject) filtered_results.get(0);

        Team team = new Team();
        team.FS_ID = (String) first_result.get("id");
        team.FS_Title = (String) first_result.get("title");
        team.FS_URLNAME = (String) first_result.get("url");

        return team;
    }





    public static void main(String[] args){
        FlashScores fs = new FlashScores();
        try {

            FlashScores.getMatchID(searchTeam("Darlington", FlashScores.FOOTBALL), Instant.now());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
