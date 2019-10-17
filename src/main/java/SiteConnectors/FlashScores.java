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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static tools.printer.*;

public class FlashScores implements SportData {

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    public static final String FOOTBALL = "1";
    public static final String football_team_ids_filename = "football_ids_flashscores.json";



    Requester requester;

    public FlashScores(){
        requester = new Requester();
        loadFootballAliases();
    }


    @Override
    public ArrayList<Team> queryTeam(String query, String sport_id) {

        // Get raw response from flashscores
        Requester requester = new Requester();
        Map<String, Object> params = new HashMap<>();
        params.put("q", query);
        params.put("l", "5");
        params.put("s", "0");
        params.put("f", "1;1");
        params.put("pid", "5");
        params.put("sid", "1");
        String response = null;
        try {
            response = requester.getRaw("https://s.livesport.services/search/", params).trim();
        } catch (IOException | URISyntaxException | InterruptedException e) {
            e.printStackTrace();
            log.severe(String.format("%s while getting response from flashscores for team '%s' query.",
                    e.toString(), query));
            return null;
        }

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

                if (String.valueOf((long) result.get("sport_id")).equals(sport_id)
                        && ((long) result.get("participant_type_id")) == 1) {

                    filtered_results.add(result);
                }
            }
        }


        // Create list of team objects and return
        ArrayList<Team> teams = new ArrayList<>();
        for (JSONObject fm_json: filtered_results){

            String FS_ID = (String) fm_json.get("id");
            String FS_Title = (String) fm_json.get("title");
            String FS_URLNAME = (String) fm_json.get("url");

            Team team = new Team(FS_Title);
            team.id = id_creator(FS_URLNAME, FS_ID);

            teams.add(team);
        }
        return teams;
    }


    @Override
    public FootballMatch getFootballMatch(Team team, Instant start_time) {
        // Returns match if team and time have a match, null if not

        ArrayList<FootballMatch> teamFixtures = getFootballFixtures(team);
        if (teamFixtures == null){
            return null;
        }

        for (FootballMatch fm: teamFixtures){
            if (fm.start_time.equals(start_time)){
                return fm;
            }
        }
        return null;
    }


    @Override
    public ArrayList<FootballMatch> getFootballFixtures(Team team) {

        if (team.id == null){
            return null;
        }

        String[] id_parts = seperate_id(team.id);
        String FS_URLNAME = id_parts[0];
        String FS_ID = id_parts[1];


        // Create url and get raw response
        String url = String.format("https://www.flashscore.co.uk/team/%s/%s/fixtures/", FS_URLNAME, FS_ID);
        Requester requester = new Requester();

        String raw = null;
        try {
            raw = requester.getRaw(url);
        }
        catch (IOException | URISyntaxException | InterruptedException e){
            log.severe("Failed to get fixtures for " + team.name + " in flashscores.");
            return null;
        }

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

            print(row.toString());


            String match_id = row.get("AA");
            String start_epoch = row.get("AD");
            String team_a_name = row.get("AE");
            String team_b_name = row.get("AF");
            String team_a_FSID = row.get("PX");
            String team_b_FSID = row.get("PY");
            String team_a_URLNAME = row.get("WU");
            String team_b_URLNAME = row.get("WV");

            // Skip if any value is null
            if (match_id == null || start_epoch == null
                    || team_a_name == null || team_b_name == null
                    || team_a_FSID == null || team_b_FSID == null
                    || team_a_URLNAME == null || team_b_URLNAME == null) {
                continue;
            }

            Instant start_time = Instant.ofEpochSecond(Long.valueOf(start_epoch));

            // Create team A
            Team team_a = new Team(team_a_name);
            team_a.id = id_creator(team_a_URLNAME, team_a_FSID);

            // Create team b
            Team team_b = new Team(team_b_name);
            team_b.id = id_creator(team_b_URLNAME, team_b_FSID);

            // Add match to list
            FootballMatch fm = new FootballMatch(start_time, team_a, team_b);
            fm.id = match_id;
            footballMatches.add(fm);
        }

        return footballMatches;
    }


    @Override
    public void saveFootballAliases() {

        Map<String, JSONArray> id_aliases_map = new HashMap<>();
        for (Map.Entry<String, String> entry: alias_id_map.entrySet()){
            String alias = entry.getKey();
            String id = entry.getValue();

            if (!id_aliases_map.containsKey(id)) {
                id_aliases_map.put(id, new JSONArray());
            }
            id_aliases_map.get(id).add(alias);
        }


        JSONArray football_aliases_array = new JSONArray();
        JSONObject football_team_ids = new JSONObject();
        for (Map.Entry<String, JSONArray> entry: id_aliases_map.entrySet()){
            String id = entry.getKey();
            JSONArray aliases = entry.getValue();

            football_aliases_array.add(aliases);
            football_team_ids.put((String) aliases.get(0), id);
        }

        JSONObject football_aliases = new JSONObject();
        football_aliases.put("aliases", football_aliases_array);

        saveJSONResource(football_aliases, football_aliases_filename);
        saveJSONResource(football_team_ids, football_team_ids_filename);
    }


    @Override
    public void loadFootballAliases() {
        // Load football alias json from file or use new one.
        JSONArray team_aliases_json;
        try{
            team_aliases_json = (JSONArray) getJSONResource(football_aliases_filename).get("aliases");
        } catch (FileNotFoundException e) {
            team_aliases_json = new JSONArray();
        } catch (ParseException e){
            log.severe(String.format("Error Parsing json file %s", football_aliases_filename));
            renameResourceFile(football_aliases_filename, football_aliases_filename + "_corrupt");
            team_aliases_json = new JSONArray();
        }


        // Load
        JSONObject flashscores_id_json;
        try{
            flashscores_id_json = getJSONResource(football_team_ids_filename);
        } catch (FileNotFoundException e) {
            flashscores_id_json = new JSONObject();
        } catch (ParseException e){
            log.severe(String.format("Error Parsing json file %s", football_team_ids_filename));
            renameResourceFile(football_team_ids_filename, football_team_ids_filename + "_corrupt");
            flashscores_id_json = new JSONObject();
        }


        alias_id_map.clear();
        for (Object item: team_aliases_json){
            JSONArray aliases = (JSONArray) item;

            for (Object alias_obj: aliases){
                String alias = (String) alias_obj;

                if (flashscores_id_json.containsKey(alias)){
                    String id = (String) flashscores_id_json.get(alias);
                    for (Object alias_obj2: aliases) {
                        String alias2 = (String) alias_obj2;
                        alias_id_map.put(alias2, id);
                    }
                }
            }

        }
    }


    public static String id_creator(String FS_URLNAME, String FS_ID){
        return String.format("%s/%s", FS_URLNAME, FS_ID);
    }


    public static String[] seperate_id(String id){
        return id.split("/");
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



    public static class verificationException extends Exception {}




    public FootballMatch verifyFootballMatch(FootballMatch match) throws verificationException {

        // Get normal names
        String team_a = match.team_a.normal_name();
        String team_b = match.team_b.normal_name();


        // For both team A and B, if the alias appears in memory then add the ID onto the object
        // and add to its own list. otherwise search FS for all possible teams.
        ArrayList<Team> possible_teams_a;
        ArrayList<Team> possible_teams_b;
        if (alias_id_map.containsKey(team_a)){
            match.team_a.id = alias_id_map.get(team_a);
            possible_teams_a = new ArrayList<>();
            possible_teams_a.add(match.team_a);
        }
        else{
            possible_teams_a = queryTeam(team_a, FOOTBALL);
        }
        if (alias_id_map.containsKey(team_b)){
            match.team_b.id = alias_id_map.get(team_b);
            possible_teams_b = new ArrayList<>();
            possible_teams_b.add(match.team_b);
        }
        else{
            possible_teams_b = queryTeam(team_b, FOOTBALL);
        }


        int max_size = Integer.max(possible_teams_a.size(), possible_teams_b.size());
        FootballMatch verifiedMatch = null;

        for (int i=0; i<max_size; i++){

            // Update next row of search results for each team to search for their fixtures
            if (i < possible_teams_a.size()){
                Team t = possible_teams_a.get(i);
                t.fixtures = getFootballFixtures(t);
            }
            if (i < possible_teams_b.size()){
                Team t = possible_teams_b.get(i);
                t.fixtures = getFootballFixtures(t);
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
                String in_both_string = "";
                for (Match m: in_both_lists){
                    in_both_string += m.toString() + ", ";
                }
                log.severe(String.format("2 or more matches found in flashscores for %s\n%s",
                        match, in_both_lists));
                throw new verificationException();
            }
        }

        // None found
        if (verifiedMatch == null){
            throw new verificationException();
        }

        // Fill in Flashscores related data to match and return it
        match.id =               verifiedMatch.id;
        match.team_a.id =        verifiedMatch.team_a.id;
        match.team_b.id =        verifiedMatch.team_b.id;


        // Add the alias/ids into the mapping
        add_alias_id(match.team_a.name, verifiedMatch.team_a.id);
        add_alias_id(verifiedMatch.team_a.name, match.team_a.id);
        add_alias_id(match.team_b.name, verifiedMatch.team_b.id);
        add_alias_id(verifiedMatch.team_b.name, match.team_b.id);
        save();

        return match;
    }


    public void add_alias_id(String alias, String id){
        if (alias_id_map.containsKey(alias)){
            String current_id = alias_id_map.get(alias);
            if (!current_id.equals(id)){
                log.severe(String.format("Trying to add id '%s' for alias '%s' but id '%s' already found for this alias.",
                        id, alias, current_id));
            }
        }
        else{
            alias_id_map.put(alias, id);
        }
    }


    public void save(){
        save_requests_queue.add(true);
    }



    public static void main(String[] args){
        FlashScores fs = new FlashScores();

        Team t = fs.queryTeam("newcastle", FOOTBALL).get(0);
        fs.getFootballFixtures(t);

        print("Done");

    }
}
