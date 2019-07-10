package Sport;

import java.text.Normalizer;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;

public class FootballMatch extends Match{

    public String team_a;
    public String team_b;

    public FootballMatch(Instant START, String NAME) throws Exception {
        super();

        start_time = START;
        name = NAME;

        String[] parts = name.toLowerCase().split(" v ");
        if (parts.length != 2){
            parts = name.toLowerCase().split(" vs ");
            if (parts.length != 2){
                throw new Exception(String.format("Cannot find teams from name '%s'", name));
            }
        }

        team_a = parts[0];
        team_b = parts[1];
    }


    public FootballMatch(Instant START, String TEAM_A, String TEAM_B){
        start_time = START;
        team_a = TEAM_A;
        team_b = TEAM_B;
        name = team_a + " v " + team_b;
    }

    public String toString(){
        return "[" + name + " @ " + start_time.toString() + "]";
    }


    public boolean same_match(FootballMatch match){
        log.fine(String.format("Checking match for %s and %s.", this, match));

        if (start_time.equals(match.start_time)
                && same_team(team_a, match.team_a) && same_team(team_b, match.team_b)){
            log.info(String.format("Match found for %s and %s.", this, match));
            return true;
        }

        log.fine(String.format("No match for %s and %s.", this, match));
        return false;

    }

    public static boolean same_team(String T1, String T2){
        log.fine(String.format("Checking teams match for %s and %s.", T1, T2));

        if (T1.equals(T2)){
            log.fine(String.format("Match found for teams '%s' & '%s'. Exact.", T1, T2));
            return true;
        }

        String t1 = Normalizer.normalize(T1.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{P}", "");
        String t2 = Normalizer.normalize(T2.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{P}", "");

        if (t1.equals(t2)){
            log.fine(String.format("Match found for teams '%s' & '%s'. Normalised '%s' & '%s'.", T1, T2, t1, t2));
            return true;
        }

        String[] p1 = t1.split("\\s+");
        String[] p2 = t2.split("\\s+");
        if (p1.equals(p2)){
            log.fine(String.format("Match found for teams '%s' & '%s'. Same words %s %s.", T1, T2, p1, p2));
            return true;
        }

        HashSet<String> s1 = new HashSet<String>(Arrays.asList(p1));
        HashSet<String> s2 = new HashSet<String>(Arrays.asList(p2));
        if (s1.equals(s2)){
            log.fine(String.format("Match found for teams '%s' & '%s'. Mixed order %s %s.", T1, T2, s1, s2));
            return true;
        }

        log.fine(String.format("No match found for %s and %s.", T1, T2));
        return false;
    }


}
