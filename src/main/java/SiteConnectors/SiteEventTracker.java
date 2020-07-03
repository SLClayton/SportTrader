package SiteConnectors;

import Bet.Bet;
import Bet.FootballBet.FootballBet;
import Bet.MarketOddsReport;
import Sport.FootballMatch;
import Sport.Event;
import Trader.EventTrader;
import Trader.MarketOddsReportWorker;
import Trader.SportsTrader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

public abstract class SiteEventTracker {

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());


    public BettingSite site;
    public EventTrader eventTrader;
    public SportData sportData;

    public Event event;
    public Collection<Bet> bets;
    public Set<String> bet_blacklist;

    public Instant lastMarketOddsReport_start_time;
    public Instant lastMarketOddsReport_end_time;

    public MarketOddsReportWorker marketOddsReportWorker;

    public String status = "new_set";
    public Instant time = Instant.now();


    public String getStatus(){
        return status;
    }


    public void setStatus(String s){
        status = s;
        time = Instant.now();
    }

    public long getTime(){
        return Instant.now().getEpochSecond() - time.getEpochSecond();
    }



    public SiteEventTracker(BettingSite site){
        this.site = site;
        sportData = SportsTrader.getSportData();
        bet_blacklist = new HashSet<>();
    }


    public abstract String name();


    public void safe_exit(){
        if (marketOddsReportWorker != null) {
            marketOddsReportWorker.safe_exit(true);
        }
    }



    public MarketOddsReport getMarketOddsReport(Collection<Bet> bets) throws InterruptedException{
        // Its important that he MarketOddsReport isn't null when pass back so ensure this with wrapper
        MarketOddsReport mor = _getMarketOddsReport(bets);
        if (mor == null){
            String msg = String.format("getMarketOdds report for %s %s has returned null when it should never do so.",
                    site, event);
            log.severe(msg);
            mor = MarketOddsReport.ERROR(msg);
        }
        return mor;
    }


    public abstract MarketOddsReport _getMarketOddsReport(Collection<Bet> bets) throws InterruptedException;


    public boolean setupMatch(Event setup_event) throws InterruptedException, IOException, URISyntaxException {

        log.fine(String.format("Setting up event for %s in %s", setup_event, site));
        Instant start = setup_event.start_time.minusSeconds(1);
        Instant end = setup_event.start_time.plusSeconds(1);


        // Depending on event type (sport) search site for relevant events
        ArrayList<Event> potential_events = new ArrayList<>();
        if (setup_event instanceof FootballMatch){
            potential_events.addAll(site.getFootballMatches(start, end));
        }
        else{
            log.severe(String.format("Setup event '%s' is not of valid type.", setup_event.toString()));
            return false;
        }


        // Check if any of the searched events event the setup_event from local data only
        Event matching_event = null;
        for (Event potential_event : potential_events){
            if (Boolean.TRUE.equals(setup_event.same_match(potential_event))){
                log.fine(String.format("Matched %s with %s from %s with local data.", setup_event, potential_event, site));
                matching_event = potential_event;
                break;
            }
        }


        // If no event found just from local data, ensure matches are verified with IDs and check again
        if (matching_event == null){

            // Verify event
            if (setup_event.notVerified()){
                log.fine(String.format("Trying to verify setup event %s.", setup_event));
                if (!setup_event.verify()){
                    log.warning(String.format("Unable to verify setup event for %s", setup_event.toString()));
                    return false;
                }
            }

            // Check again if verification worked.
            for (Event potential_event : potential_events){
                if (potential_event.notVerified()){
                    log.fine(String.format("Trying to verify potential event %s.", potential_event));
                    potential_event.verify();
                }
                if (Boolean.TRUE.equals(setup_event.same_match(potential_event))){
                    log.fine(String.format("Matched %s with %s from %s after verifying matches.",
                            setup_event, potential_event, site));
                    matching_event = potential_event;
                    break;
                }
            }
        }

        // If event is still not found then fail this setup process.
        if (matching_event == null){
            log.warning(String.format("No matches found for %s in %s. Checked %d: %s",
                    setup_event.toString(), site.toString(), potential_events.size(), potential_events.toString()));
            return false;
        }

        // If gotten this far then a event will have been assigned to the matching_event variable.
        // Merge the metadata found for this website into the existing event object and then
        // start the site specific setups
        setup_event.updateMetaData(matching_event.metadata);
        event = setup_event;
        return siteSpecificSetup();
    }


    public abstract boolean siteSpecificSetup() throws IOException, URISyntaxException, InterruptedException;

}
