package SiteConnectors;

import Sport.FootballMatch;
import Sport.FootballTeam;
import Sport.Match;
import Sport.Team;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public interface SportData {

    String football_aliases_filename = "football_aliases.json";
    BlockingQueue<Boolean> save_requests_queue = new LinkedBlockingQueue<>();
    Map<String, String> football_alias_id_map = new HashMap<>();
    Map<String, String> match_id_map = new HashMap<>();


    ArrayList<FootballTeam> queryFootballTeam(String query);


    FootballMatch getFootballMatch(Team team, Instant start_time);


    ArrayList<FootballMatch> getFootballFixtures(Team team);


    void saveFootballAliases();


    void loadFootballAliases();


    String getTeamID(Team team);


    String getMatchID(Match match);


    void update_match_id_map(Match match);


    void update_team_id_map(Team team);


    void save_all();

    FootballMatch verifyFootballMatch(FootballMatch match) throws FlashScores.verificationException;


    static class verificationException extends Exception {}
}
