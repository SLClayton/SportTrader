package SiteConnectors;

import Bet.FootballBet.FootballBet;
import Sport.FootballMatch;
import Sport.Match;
import Trader.EventTrader;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class SmarketsEventTracker extends SiteEventTracker {

    public Smarkets smarkets;
    public String event_id;
    public FootballMatch match;


    public SmarketsEventTracker(Smarkets smarkets){
        this.smarkets = smarkets;
    }


    @Override
    public String name() {
        return "smarkets";
    }

    @Override
    public boolean setupMatch(FootballMatch setup_match) throws IOException, URISyntaxException, InterruptedException {

        // Get events from smarkets which match the sport and time
        ArrayList<FootballMatch> events = smarkets.getEvents(
                setup_match.start_time.minus(1, ChronoUnit.SECONDS),
                setup_match.start_time.plus(1, ChronoUnit.SECONDS),
                smarkets.FOOTBALL);

        match = null;
        // Verify each match in flashscores and see if it matches
        for (FootballMatch fm: events){

            try{
                fm.verify();
            } catch (InterruptedException | IOException | URISyntaxException | FlashScores.verificationException e){
                log.warning(String.format("Could not verify smarkets match %s in flashscores.", fm));
                continue;
            }

            if (fm.FSID.equals(setup_match.FSID)){
                match = fm;
                event_id = fm.metadata.get("smarkets_event_id");
                break;
            }
        }

        // Check for no match
        if (match == null){
            log.warning(String.format("No match for %s found in smarkets. Searched %d events %s.",
                    setup_match, events.size(), Match.listtostring(events)));
            return false;
        }


        this.match = setup_match;
        return true;
    }

    @Override
    public void updateMarketOddsReport(FootballBet[] bets) throws Exception {

        //TODO: THIS

    }
}
