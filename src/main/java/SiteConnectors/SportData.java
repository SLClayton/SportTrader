package SiteConnectors;

import Sport.FootballMatch;
import Sport.FootballTeam;
import Sport.Match;
import Sport.Team;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public interface SportData {

    String football_aliases_filename = "football_aliases.json";
    BlockingQueue<Boolean> save_requests_queue = new LinkedBlockingQueue<>();
    Map<String, String> football_alias_id_map = new HashMap<>();
    Map<String, String> match_id_map = new HashMap<>();
    Set<String> unverifiable_matches = new HashSet<>();


    ArrayList<FootballTeam> queryFootballTeam(String query);


    FootballMatch getFootballMatch(Team team, Instant start_time);


    ArrayList<FootballMatch> getFootballFixtures(Team team);


    void saveFootballAliases();


    void loadFootballAliases();


    String getFootballTeamID(Team team);


    String getMatchID(Match match);


    void update_match_id_map(Match match);


    boolean update_football_team_id_map(FootballTeam team);


    void save_all();


    boolean verifyFootballMatch(FootballMatch match) throws verificationException;


    //static class verificationException extends Exception {}
}
