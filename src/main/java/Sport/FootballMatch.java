package Sport;

import Bet.FootballBet.FootballBet;
import SiteConnectors.BettingSite;
import SiteConnectors.SportData;
import org.json.simple.JSONArray;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.Normalizer;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;

import static tools.printer.print;

public class FootballMatch extends Match{

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
            String msg = String.format("Cannot parse Match name '%s'", name);
            throw new ParseException(msg, 1);
        }
        return new FootballMatch(start, new FootballTeam(teams[0]), new FootballTeam(teams[1]));
    }


    @Override
    public String getID() {
        if (id == null){
            id = sportData.getMatchID(this);
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
    public Boolean same_match(Match match){
        // Checks if match is the same given the currently known local information (no online verification)

        // Check Start time, false if different
        if (!start_time.equals(match.start_time)){
            return false;
        }


        // If both have IDs which match then they're the same
        if (getID() != null && match.getClass() != null){
            return getID().equals(match.getID());
        }


        // If argument match isn't a football match then they're not the same
        FootballMatch fm;
        try{
            fm = (FootballMatch) match;
        } catch (ClassCastException e){
            return false;
        }


        // If both teams same or one same and other null, then match.
        // If both null skip
        // If one same and other not, then log error and dont match
        Boolean sameAteam = team_a.same_team(fm.team_a);
        Boolean sameBteam = team_b.same_team(fm.team_b);
        if (Boolean.TRUE.equals(sameAteam)){
            if (Boolean.TRUE.equals(sameBteam) || sameBteam == null){
                return true;
            }
            else{
                log.severe(String.format("Matches %s and %s have same time, same A team but different B team.",
                        this.toString(), match.toString()));
                return false;
            }
        }
        else if (Boolean.FALSE.equals(sameAteam)){
            if (Boolean.TRUE.equals(sameBteam)){
                log.severe(String.format("Matches %s and %s have same time, same B team but different A team.",
                        this.toString(), match.toString()));
            }
            return false;
        }
        else if (sameAteam == null && sameBteam != null){
            return sameBteam.booleanValue();
        }

        // Unable to say for sure
        return null;
    }



    public static boolean same_team_old(String T1, String T2){
        return same_team_old(T1, T2, true);
    }


    public static boolean same_team_old(String T1, String T2, boolean deep_check){
        //log.fine(String.format("Checking teams match for %s and %s.", T1, T2));

        // Check exact strings
        if (T1.equals(T2)){
            //log.fine(String.format("Match found for teams '%s' & '%s'. Exact.", T1, T2));
            return true;
        }

        // Normalise strings. Lowercase, replace punctuation, normalise accented chars
        String t1 = Normalizer.normalize(T1.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{P}", "");
        String t2 = Normalizer.normalize(T2.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{P}", "");

        // Check normalised strings
        if (t1.equals(t2)){
            //log.fine(String.format("Match found for teams '%s' & '%s'. Normalised '%s' & '%s'.", T1, T2, t1, t2));
            return true;
        }

        // Check strings are the same without whitespace (as a list of words)
        ArrayList<String> p1 = new ArrayList<String>(Arrays.asList(t1.split("\\s+")));
        ArrayList<String> p2 = new ArrayList<String>(Arrays.asList(t2.split("\\s+")));
        if (p1.equals(p2)){
            //log.fine(String.format("Match found for teams '%s' & '%s'. Same words %s %s.", T1, T2, p1, p2));
            return true;
        }

        // Check if words all appear even if out of order (as a set)
        HashSet<String> s1 = new HashSet<String>(p1);
        HashSet<String> s2 = new HashSet<String>(p2);
        if (s1.equals(s2)){
            //log.fine(String.format("Match found for teams '%s' & '%s'. Mixed order %s %s.", T1, T2, s1, s2));
            return true;
        }

        // Only check further if triggered to. This stops recurred calls from going further.
        if (!deep_check){
            return false;
        }


        // Removes keywords such as FC from name to see if match occurs.
        for (String keyword: removable_keywords){
            if (s1.contains(keyword) || s2.contains(keyword)){
                p1.remove(keyword);
                p2.remove(keyword);

                boolean success = same_team_old(String.join(" ", p1), String.join(" ", p2), false);
                if (success){
                    //log.fine(String.format("Match found for teams once '%s' removed. '%s' & '%s'.", keyword, T1, T2));
                    return true;
                }
            }
        }


        log.fine(String.format("No match found for %s and %s.", T1, T2));
        return false;
    }


    public boolean inList(Collection<FootballMatch> matches){
        return appearsInList(this, matches);
    }


    public static boolean appearsInList(FootballMatch match, Collection<FootballMatch> matches){
        for (Object item: matches){
            Match m = (Match) item;
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
}
