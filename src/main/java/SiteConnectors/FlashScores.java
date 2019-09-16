package SiteConnectors;

import Sport.FootballEventState;
import Sport.FootballMatch;
import Sport.Match;
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
import java.text.Normalizer;
import java.time.Instant;
import java.util.*;
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


    public static FootballMatch getMatch(Team team, Instant start_time) throws InterruptedException, IOException,
            URISyntaxException {

        // Returns match if team and time have a match, null if not

        ArrayList<FootballMatch> teamFixtures = getTeamFixtures(team);
        for (FootballMatch fm: teamFixtures){
            if (fm.start_time.equals(start_time)){
                return fm;
            }
        }
        return null;
    }


    public static ArrayList<FootballMatch> getTeamFixtures(Team team) throws InterruptedException, IOException,
            URISyntaxException {

        // Create url and get raw response
        String url = String.format("https://www.flashscore.co.uk/team/%s/%s/fixtures/",
                team.FS_URLNAME, team.FS_ID);
        Requester requester = new Requester();
        String raw = requester.getRaw(url);

        // Find needed element by id in html and extract text
        Document doc = Jsoup.parse(raw);
        Element fixtures_element = doc.getElementById("participant-page-data-fixtures");
        if (fixtures_element == null){
            log.severe(String.format("Fixture data cannot be found in raw page while searching for '%s' fixtures.",
                    url));
            return null;
        }
        String raw_fixtures = fixtures_element.text();

        // Split into large chunks for each row of table (I think)
        String[] split = raw_fixtures.split("~");

        // Create a list to store hashmap for each row of table
        ArrayList<SortedMap<String, String>> parts = new ArrayList<>();
        for (String part_string: split){
            String[] split_row = part_string.split("[÷¬]");

            // Create MAP for each row
            SortedMap<String, String> row_map = new TreeMap<>();
            for (int i=0; i<split_row.length-1; i+=2){
                row_map.put(split_row[i], split_row[i+1]);
            }
            parts.add(row_map);
        }

        ArrayList<FootballMatch> footballMatches = new ArrayList<>();
        for (Map<String, String> row: parts){
            String match_id = row.get("AA");
            String start_epoch = row.get("AD");
            String team_a_name = row.get("AE");
            String team_b_name = row.get("AF");
            String team_a_FSID = row.get("PX");
            String team_b_FSID = row.get("PY");

            // Skip if any value is null
            if (match_id == null || start_epoch == null || team_a_name == null || team_b_name == null
            || team_a_FSID == null || team_b_FSID == null) {
                continue;
            }

            Instant start_time = Instant.ofEpochSecond(Long.valueOf(start_epoch));

            // Create team A
            Team team_a = new Team(team_a_name);
            team_a.FS_ID = team_a_FSID;
            team_a.FS_Title = team_a_name;

            // Create team b
            Team team_b = new Team(team_b_name);
            team_b.FS_ID = team_b_FSID;
            team_b.FS_Title = team_b_name;

            // Add match to list
            FootballMatch fm = new FootballMatch(start_time, team_a, team_b);
            fm.FSID = match_id;
            footballMatches.add(fm);
        }

        return footballMatches;
    }


    public static ArrayList<Team> searchTeams(String query, Integer sport_id) throws InterruptedException, IOException,
            URISyntaxException {

        // Get raw response from flashscores
        Requester requester = new Requester();
        Map<String, Object> params = new HashMap<>();
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

        // Extract results then filter for correct sport ID and participant type (team)
        JSONArray results = (JSONArray) json.get("results");
        ArrayList<JSONObject> filtered_results = new ArrayList<>();
        if (results != null){
            for (Object result_obj: results) {
                JSONObject result = (JSONObject) result_obj;

                if (((long) result.get("sport_id")) == sport_id
                        && ((long) result.get("participant_type_id")) == 1) {

                    filtered_results.add(result);
                }
            }
        }


        // Create list of team objects and return
        ArrayList<Team> teams = new ArrayList<>();
        for (JSONObject fm_json: filtered_results){
            Team team = new Team((String) fm_json.get("title"));
            team.FS_ID = (String) fm_json.get("id");
            team.FS_Title = (String) fm_json.get("title");
            team.FS_URLNAME = (String) fm_json.get("url");

            teams.add(team);
        }
        return teams;
    }


    public static class verificationException extends Exception {

    }


    public static FootballMatch verifyMatch(FootballMatch match) throws InterruptedException, IOException,
            URISyntaxException, verificationException {

        // Normalise team names before searching (remove punctuation/accents etc)
        String team_a = Normalizer.normalize(match.team_a.name.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{P}", "");
        String team_b = Normalizer.normalize(match.team_b.name.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{P}", "");

        // Get team search results for both teams names
        ArrayList<Team> possible_teams_a = searchTeams(team_a, FOOTBALL);
        ArrayList<Team> possible_teams_b = searchTeams(team_b, FOOTBALL);

        int max_size = Integer.max(possible_teams_a.size(), possible_teams_b.size());
        FootballMatch verifiedMatch = null;

        for (int i=0; i<max_size; i++){

            // Update next row of search results for each team to search for their fixtures
            if (i < possible_teams_a.size()){
                possible_teams_a.get(i).getFixtures();
            }
            if (i < possible_teams_b.size()){
                possible_teams_b.get(i).getFixtures();
            }

            // Compile all fixtures for all possible A teams that match time
            ArrayList<FootballMatch> all_matches_a = new ArrayList<>();
            for (Team t: possible_teams_a){
                if (t.fixtures != null){
                    for (FootballMatch m: t.fixtures){
                        if (m.start_time.equals(match.start_time)){
                            all_matches_a.add(m);
                        }
                    }
                }
            }

            // Compile all fixtures for all possible B teams that match time
            ArrayList<FootballMatch> all_matches_b = new ArrayList<>();
            for (Team t: possible_teams_b){
                if (t.fixtures != null){
                    for (FootballMatch m: t.fixtures){
                        if (m.start_time.equals(match.start_time)){
                            all_matches_b.add(m);
                        }
                    }
                }
            }

            // Check any matches appear in both lists
            ArrayList<FootballMatch> in_both_lists = new ArrayList<>();
            for (FootballMatch m: all_matches_a){
                if (m.inList(all_matches_b)){
                    in_both_lists.add(m);
                }
            }

            // Break if match found
            if (in_both_lists.size() == 1){
                verifiedMatch = in_both_lists.get(0);
                break;
            }
            // Error if more than one found
            if (in_both_lists.size() >= 2){
                log.severe(String.format("2 or more matches found in flashscores for %s", match));
                throw new verificationException();
            }
        }

        // None found
        if (verifiedMatch == null){
            throw new verificationException();
        }

        // Fill in Flashscores related data to match and return it
        match.FSID =                verifiedMatch.FSID;
        match.team_a.FS_ID =        verifiedMatch.team_a.FS_ID;
        match.team_a.FS_URLNAME =   verifiedMatch.team_a.FS_URLNAME;
        match.team_a.FS_Title =     verifiedMatch.team_a.FS_Title;
        match.team_b.FS_ID =        verifiedMatch.team_b.FS_ID;
        match.team_b.FS_URLNAME =   verifiedMatch.team_b.FS_URLNAME;
        match.team_b.FS_Title =     verifiedMatch.team_b.FS_Title;
        return match;
    }



    public static void main(String[] args){
        FlashScores fs = new FlashScores();
        try {

            ArrayList<Team> teams = searchTeams("south", FOOTBALL);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
