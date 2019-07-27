package SiteConnectors;

import Bet.FootballBet.FootballBet;
import Sport.FootballMatch;
import org.json.simple.JSONObject;

import java.time.Instant;
import java.util.HashMap;
import java.util.Set;

public class MatchbookEventTracker extends SiteEventTracker {

    public Matchbook matchbook;
    public String event_id;
    public JSONObject eventMarketData;
    public Instant lastEventUpdate;
    public HashMap<String, String> market_name_id_map;


    public MatchbookEventTracker(Matchbook matchbook) {
        this.matchbook = matchbook;
    }

    @Override
    public boolean setupMatch(FootballMatch match) throws Exception {
        return false;
    }

    @Override
    public void updateMarketOddsReport(FootballBet[] bets) throws Exception {

    }
}
