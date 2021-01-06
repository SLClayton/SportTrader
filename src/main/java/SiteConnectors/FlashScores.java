package SiteConnectors;

import Sport.*;
import Trader.SportsTrader;
import org.apache.http.client.HttpResponseException;
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

    public Set<String> invalid_team_ids;

    Requester requester;

    public FlashScores(){
        requester = Requester.JSONRequester();
        loadFootballAliases();
        invalid_team_ids = new HashSet<>();
    }


    @Override
    public ArrayList<FootballTeam> queryFootballTeam(String query) {

        // Get raw response from flashscores
        Requester requester = Requester.JSONRequester();
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
        } catch (NullPointerException | IOException | URISyntaxException e) {
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

                if (String.valueOf((long) result.get("sport_id")).equals(FOOTBALL)
                        && ((long) result.get("participant_type_id")) == 1) {

                    filtered_results.add(result);
                }
            }
        }


        // Create list of team objects and return
        ArrayList<FootballTeam> teams = new ArrayList<>();
        for (JSONObject fm_json: filtered_results){

            String FS_ID = (String) fm_json.get("id");
            String FS_Title = (String) fm_json.get("title");
            String FS_URLNAME = (String) fm_json.get("url");

            FootballTeam team = new FootballTeam(FS_Title);
            team.id = id_creator(FS_URLNAME, FS_ID);

            teams.add(team);
        }
        return teams;
    }


    @Override
    public FootballMatch getFootballMatch(Team team, Instant start_time) {
        // Returns event if team and time have a event, null if not

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
        // Returns a list of fully ID'd up football matches from a particular team.

        if (team.id == null || invalid_team_ids.contains(team.id)){
            return null;
        }

        String[] id_parts = seperate_id(team.id);
        String FS_URLNAME = id_parts[0];
        String FS_ID = id_parts[1];


        // Create url and get raw response
        String url = String.format("https://www.flashscore.co.uk/team/%s/%s/fixtures/", FS_URLNAME, FS_ID);
        Requester requester = Requester.JSONRequester();

        String raw = null;
        try {
            raw = requester.getRaw(url);
            if (raw == null){
                throw new NullPointerException();
            }
        }
        catch (NullPointerException | IOException | URISyntaxException e){
            log.severe(String.format("Failed to get fixtures for " + team.name + " in flashscores."));
            if (e instanceof HttpResponseException){
                log.warning(String.format("Flashscores 404'd when trying to get team fixtures for %s", team.id));
                invalid_team_ids.add(team.id);
            }
            else{
                log.severe(((Exception) e).toString());
            }
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

            // Extract data from weird coded row of raw html
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
            FootballTeam team_a = new FootballTeam(team_a_name);
            team_a.id = id_creator(team_a_URLNAME, team_a_FSID);

            // Create team b
            FootballTeam team_b = new FootballTeam(team_b_name);
            team_b.id = id_creator(team_b_URLNAME, team_b_FSID);

            // Add event to list
            FootballMatch fm = new FootballMatch(start_time, team_a, team_b);
            fm.id = match_id;
            footballMatches.add(fm);
        }

        return footballMatches;
    }


    @Override
    public void saveFootballAliases() {

        Map<String, JSONArray> id_aliases_map = new HashMap<>();
        for (Map.Entry<String, String> entry: football_alias_id_map.entrySet()){
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


        // Load id json for flashscores
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


        football_alias_id_map.clear();
        for (Object item: team_aliases_json){
            JSONArray aliases = (JSONArray) item;

            for (Object alias_obj: aliases){
                String alias = (String) alias_obj;

                if (flashscores_id_json.containsKey(alias)){
                    String id = (String) flashscores_id_json.get(alias);
                    for (Object alias_obj2: aliases) {
                        String alias2 = (String) alias_obj2;
                        football_alias_id_map.put(alias2, id);
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


    @Override
    public String getFootballTeamID(Team team){
        return football_alias_id_map.get(team.normal_name());
    }


    @Override
    public String getEventID(Event event){
        return match_id_map.get((event.key()));
    }


    @Override
    public void update_match_id_map(Event event){
        String key = event.key();
        if (key == null){
            throw new NullPointerException();
        }

        String current_id = match_id_map.get(event.key());
        if (current_id != null){
            if (!event.id.equals(current_id)) {
                log.severe(String.format("Trying to add %s to match_id_map but key %s already exists " +
                                "in mapping for id %s",
                        event.toString(), event.key(), current_id));
                print(match_id_map.toString());
            }
            return;
        }
        match_id_map.put(event.key(), event.id);
    }


    @Override
    public boolean update_football_team_id_map(FootballTeam team){
        if (team.id == null){
            log.severe(String.format("Trying to save football team id for '%s', but id is null.", team.name));
            return false;
        }

        String normal_name = team.normal_name();
        String current_id = football_alias_id_map.get(normal_name);

        if (current_id != null){
            if (!team.id.equals(current_id)) {
                log.severe(String.format("Trying to add '%s' to football_alias_id_map with ID '%s' but " +
                        "it already has an entry with ID '%s'. Will not update.",
                        normal_name, team.id, current_id));
            }
            return false;
        }

        football_alias_id_map.put(normal_name, team.id);
        save();
        return true;
    }


    @Override
    public void save_all() {
        save_requests_queue.clear();
        saveFootballAliases();
    }


    @Override
    public boolean verifyFootballMatch(FootballMatch match) {
        // Tries to find event in flashscores and updates event ID and team IDs in local maps
        // Objects need to be refreshed after this to get their newly found IDs

        if (unverifiable_matches.contains(match.name)){
            log.fine(String.format("Tried to verify event %s but its already failed previously.", match));
            return false;
        }
        log.fine(String.format("Attempting to verify %s in flashscores", match));

        FootballTeam[] teams = new FootballTeam[] {match.team_a, match.team_b};

        // Begin to create lists of all possible teams for team A and B
        ArrayList<FootballTeam> possible_teams_a = new ArrayList<>();
        ArrayList<FootballTeam> possible_teams_b = new ArrayList<>();
        ArrayList<FootballTeam>[] possible_teams = new ArrayList[] {possible_teams_a, possible_teams_b};

        // If team already has an ID, then it can only be one event, so add that single event to list
        // Otherwise, fill possibility list with the results from a search of its name.
        for (int i=0; i<2; i++){
            if (teams[i].getID() != null){
                log.fine(String.format("Team %s already in local mem with id %s.", teams[i], teams[i].getID()));
                possible_teams[i].add(teams[i]);
            }
            else{
                possible_teams[i].addAll(queryFootballTeam(teams[i].normal_name()));
                log.fine(String.format("Queried flashscores for team name '%s' and found %s",
                        teams[i].normal_name(), possible_teams[i]));
            }
        }
        // Unable to verify match if either teams possibilities are empty.
        if (possible_teams_a.size() == 0 || possible_teams_b.size() == 0){
            return false;
        }

        ArrayList<FootballMatch> all_matches_a = new ArrayList<>();
        ArrayList<FootballMatch> all_matches_b = new ArrayList<>();
        ArrayList<FootballMatch>[] all_matches = new ArrayList[] {all_matches_a, all_matches_b};
        int max_size = Integer.max(possible_teams_a.size(), possible_teams_b.size());
        FootballMatch verifiedMatch = null;

        for (int i=0; i<max_size; i++){

            // Compile all fixtures for all possible teams for A and B teams
            for (int j=0; j<2; j++) {
                all_matches[j].clear();
                for (FootballTeam t : possible_teams[j]) {
                    if (t.getFixtures() != null) {
                        for (FootballMatch m : t.fixtures) {
                            if (m.start_time.equals(match.start_time)) {
                                all_matches[j].add(m);
                            }
                        }
                    }
                }
            }
            log.fine(String.format("all_matches from both lists on this loop = %s and %s",
                    all_matches_a, all_matches_b));

            // Check any matches appear in both lists
            List<FootballMatch> in_both_lists = FootballMatch.listOverlap(all_matches_a, all_matches_b);

            // Break if event found
            if (in_both_lists.size() == 1){
                verifiedMatch = in_both_lists.get(0);
                log.fine(String.format("Single event found in verification %s.", verifiedMatch));
                break;
            }

            // Error if more than one found
            if (in_both_lists.size() >= 2){
                String in_both_string = "";
                for (FootballMatch m: in_both_lists){ in_both_string += m.toString() + ", ";}
                log.severe(String.format("2 or more matches found in flashscores for %s\n%s", match, in_both_string));
                unverifiable_matches.add(match.name);
                return false;
            }
        }

        // None found
        if (verifiedMatch == null){
            log.warning(String.format("Could not verify event %s in flashscores.", match.toString()));
            unverifiable_matches.add(match.name);
            return false;
        }

        // Fill in Flashscores related data to event and return it
        update_football_team_id_map(verifiedMatch.team_a);
        update_football_team_id_map(verifiedMatch.team_b);
        update_match_id_map(verifiedMatch);

        return true;
    }


    public void save(){
        save_requests_queue.add(true);
    }



    public static void main(String[] args){



    }
}
