package Sport;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.text.Normalizer;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;

public class FootballMatch extends Event {

    public static String[] removable_keywords = {"fc", "town", "city", "women", "w", "of", "and", "&"};

    public FootballTeam team_a;
    public FootballTeam team_b;

    public FootballMatch(Instant START, FootballTeam TEAM_A, FootballTeam TEAM_B){
        super();
        start_time = START;
        team_a = TEAM_A;
        team_b = TEAM_B;
        name = team_a.name + " v " + team_b.name;
        id = getID();
    }


    public FootballMatch(String START, String TEAM_A, String TEAM_B){
        this(Instant.parse(START), new FootballTeam(TEAM_A), new FootballTeam(TEAM_B));
    }


    public static FootballMatch parse(String start, String name) throws ParseException {
        return FootballMatch.parse(Instant.parse(start), name);
    }

    public static FootballMatch parse(Instant start, String name) throws ParseException {
        String[] teams = name.trim().split("\\sv\\s|\\sV\\s|\\svs\\s|\\sVS\\s|\\sVs\\s");
        if (teams.length != 2){
            String msg = String.format("Cannot parse Event name '%s'", name);
            throw new ParseException(msg, 1);
        }
        return new FootballMatch(start, new FootballTeam(teams[0]), new FootballTeam(teams[1]));
    }


    @Override
    public String getID() {
        if (id == null){
            id = sportData.getEventID(this);
        }
        return id;
    }


    @Override
    public boolean isVerified() {
        return (team_a.getID() != null && team_b.getID() != null);
    }


    @Override
    public boolean notVerified() {
        return !isVerified();
    }


    @Override
    public boolean verify() {
        boolean verification_success = sportData.verifyFootballMatch(this);
        if (verification_success){
            refreshIDs();
        }
        return verification_success;
    }


    @Override
    public void refreshIDs() {
        team_a.getID();
        team_b.getID();
        this.getID();
    }


    @Override
    public String key(){
        if (team_a.getID() == null || team_b.getID() == null || start_time == null) {
            return null;
        }
        return String.format("FB/%s/%s/%s", team_a.getID(), team_b.getID(), start_time.toString());
    }


    @Override
    public String toString(){
        return String.format("[%s @ %s]", name, start_time.toString());
    }


    public static JSONArray list2JSON(Collection<FootballMatch> footballMatches){
        JSONArray j = new JSONArray();
        for (FootballMatch fm: footballMatches){
            j.add(fm.toString());
        }
        return j;
    }


    @Override
    public Boolean same_match(Event event){
        // Checks if event is the same given the currently known local information (no online verification)

        // Check Start time, false if different
        if (!start_time.equals(event.start_time)){
            return false;
        }


        // If both have IDs which event then they're the same
        if (getID() != null && event.getClass() != null){
            return getID().equals(event.getID());
        }


        // If argument event isn't a football event then they're not the same
        FootballMatch fm;
        try{
            fm = (FootballMatch) event;
        } catch (ClassCastException e){
            return false;
        }


        // If both teams same or one same and other null, then event.
        // If both null skip
        // If one same and other not, then log error and dont event
        Boolean sameAteam = team_a.same_team(fm.team_a);
        Boolean sameBteam = team_b.same_team(fm.team_b);
        if (Boolean.TRUE.equals(sameAteam)){
            if (Boolean.TRUE.equals(sameBteam) || sameBteam == null){
                return true;
            }
            else{
                log.severe(String.format("Matches %s and %s have same time, same A team but different B team.",
                        this.toString(), event.toString()));
                return false;
            }
        }
        else if (Boolean.FALSE.equals(sameAteam)){
            if (Boolean.TRUE.equals(sameBteam)){
                log.severe(String.format("Matches %s and %s have same time, same B team but different A team.",
                        this.toString(), event.toString()));
            }
            return false;
        }
        else if (sameAteam == null && sameBteam != null){
            return sameBteam.booleanValue();
        }

        // Unable to say for sure
        return null;
    }


    public boolean inList(Collection<FootballMatch> matches){
        return appearsInList(this, matches);
    }


    public static boolean appearsInList(FootballMatch match, Collection<FootballMatch> matches){
        for (Object item: matches){
            Event m = (Event) item;
            if (match.same_match(m)){
                return true;
            }
        }
        return false;
    }

    public static List<FootballMatch> listOverlap(
            Collection<FootballMatch> matches1, Collection<FootballMatch> matches2){

        // Return list of matches that appear in both lists
        List<FootballMatch> in_both_lists = new ArrayList<>();
        for (FootballMatch m: matches1){
            if (m.inList(matches2)){
                in_both_lists.add(m);
            }
        }
        return in_both_lists;
    }


    public JSONObject toJSON(){
        JSONArray t = new JSONArray();
        t.add(team_a.name);
        t.add(team_b.name);

        JSONObject j = new JSONObject();
        j.put("teams", t);
        j.put("name", name);
        j.put("time", start_time.toString());
        j.put("meta", new JSONObject(metadata));
        return j;
    }
}
