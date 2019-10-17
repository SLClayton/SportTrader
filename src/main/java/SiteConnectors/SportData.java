package SiteConnectors;

import Sport.FootballMatch;
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
    Map<String, String> alias_id_map = new HashMap<>();


    ArrayList<Team> queryTeam(String query, String sport_id);

    FootballMatch getFootballMatch(Team team, Instant start_time);

    ArrayList<FootballMatch> getFootballFixtures(Team team);

    void saveFootballAliases();

    void loadFootballAliases();



}
