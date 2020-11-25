package SiteConnectors.Betdaq;

import Bet.*;
import Bet.FootballBet.*;
import SiteConnectors.BettingSite;
import SiteConnectors.SiteEventTracker;
import Sport.FootballMatch;
import com.globalbettingexchange.externalapi.*;
import org.json.simple.parser.ParseException;

import javax.xml.bind.JAXBElement;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static tools.BigDecimalTools.secs_since;
import static tools.printer.*;

public class BetdaqEventTracker extends SiteEventTracker {

    Betdaq betdaq;

    Long event_id;

    public Map<String, Long> bet_selectionId_map;
    public Map<String, Long> marketName_id_map;
    public Map<String, Long> market_selection_id_map;
    public Map<Long, Long> selectionId_marketId_map;
    public Integer correct_score_max;
    public Integer correct_score_max_ht;

    // Pattern to match things like "(Manc - Sheff)" at the end of market names
    public static Pattern unneeded_market_suffix = Pattern.compile("\\(\\w+ - \\w+\\)\\z");
    public static Pattern score_pattern = Pattern.compile("\\b\\d+-\\d+\\b");
    public static Pattern decimal_number_pattern = Pattern.compile("\\([\\+\\-]?\\d+(\\.\\d+)?\\)");



    public static List<String> valid_market_names = Arrays.asList(
            "Match Odds",
            "First-Half Result",
            "Correct Score",
            "Half Time Score",
            "Asian Handicap (-0.5)",
            "Asian Handicap (+0.5)",
            "Asian Handicap (-1.5)",
            "Asian Handicap (+1.5)",
            "Asian Handicap (-2.5)",
            "Asian Handicap (+2.5)",
            "Asian Handicap (-3.5)",
            "Asian Handicap (+3.5)",
            "1st Half Under Over - Goals (0.5)",
            "1st Half Under Over - Goals (1.5)",
            "1st Half Under Over - Goals (2.5)",
            "Under/Over - Goals (0.5)",
            "Under/Over - Goals (1.5)",
            "Under/Over - Goals (2.5)",
            "Under/Over - Goals (3.5)",
            "Under/Over - Goals (4.5)",
            "Under/Over - Goals (5.5)",
            "Under/Over - Goals (6.5)",
            "Under/Over - Goals (7.5)",
            "Under/Over - Goals (8.5)",
            "Under/Over - Goals (9.5)"
    );


    public BetdaqEventTracker(Betdaq betdaq) {
        super(betdaq);
        this.betdaq = betdaq;
    }

    @Override
    public String name() {
        return Betdaq.name;
    }


    @Override
    public MarketOddsReport _getMarketOddsReport(Collection<Bet> bets) throws InterruptedException {
        lastMarketOddsReport_start_time = Instant.now();

        // Ensure event has been setup
        if (event == null){
            log.severe("Trying to get market odds report on null event.");
            return MarketOddsReport.ERROR("NULL event in betdaq  event tracker");
        }

        // Get list of prices for each market
        GetPricesResponse resp = null;
        try {
            resp = betdaq.getPrices(marketName_id_map.values());
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        if (resp == null){
            log.severe(String.format("Market prices for %s returned null.", event.toString()));
            return MarketOddsReport.ERROR("Betdaq marketPrices returned null.");
        }
        ReturnStatus ret_status = resp.getGetPricesResult().getReturnStatus();
        if (ret_status.getCode() == 406){
            return MarketOddsReport.RATE_LIMITED();
        }
        else if (ret_status.getCode() != 0){
            String error_string = sf("%s %s", ret_status.getCode(), ret_status.getDescription());
            log.severe(String.format("Betdaq error %s when getting prices.", error_string, event.toString()));
            return MarketOddsReport.ERROR(error_string);
        }

        // Index all markets in a map with their ID as the keys
        Map<Long, MarketTypeWithPrices> id_market_map = new HashMap<>();
        for (MarketTypeWithPrices m: resp.getGetPricesResult().getMarketPrices()){
            id_market_map.put(m.getId(), m);
        }

        MarketOddsReport marketOddsReport = new MarketOddsReport(event);
        for (Bet bet: bets){

            // Ensure bet isn't blacklisted
            if (bet_blacklist.contains(bet.id())){
                continue;
            }

            // Find the selection Id from the bet given
            Long selection_id = bet_selectionId_map.get(bet.id());
            if (selection_id == null){
                bet_blacklist.add(bet.id());
                continue;
            }

            // Find market id from the selection id
            Long market_id = selectionId_marketId_map.get(selection_id);
            if (market_id == null){
                log.severe(String.format("Betdaq selection %s/%s has no market id in map.", bet.id(), selection_id));
                bet_blacklist.add(bet.id());
                continue;
            }

            // Get the market from the MAP using the market ID
            MarketTypeWithPrices marketType = id_market_map.get(market_id);
            if (marketType == null){
                log.severe(String.format("Could not find marketType %s in betdaq prices.", market_id));
                continue;
            }

            // Find right selection within market by id
            SelectionTypeWithPrices selectionType = null;
            for (SelectionTypeWithPrices potential_selectionType: marketType.getSelections()){
                if (potential_selectionType.getId() == selection_id){
                    selectionType = potential_selectionType;
                    break;
                }
            }
            if (selectionType == null) {
                log.severe(String.format("Could not find selection %s in betdaq prices.", selection_id));
                continue;
            }


            // Create a bet exchange and fill with offers for this selection
            BetExchange betExchange = new BetExchange(site, event, bet);
            betExchange.addMetadata(Betdaq.BETDAQ_EVENT_ID, market_id);
            betExchange.addMetadata(Betdaq.BETDAQ_SELECTION_ID, selection_id);
            betExchange.addMetadata(Betdaq.BETDAQ_SELECTION_RESET_COUNT, selectionType.getResetCount());
            betExchange.addMetadata(Betdaq.BETDAQ_SEQ_NUMBER, marketType.getWithdrawalSequenceNumber());

            for (JAXBElement<PricesType> offer: selectionType.getForSidePricesAndAgainstSidePrices()){

                if ((bet.isBack() && offer.getName().getLocalPart().equals("ForSidePrices")) ||
                    (bet.isLay()  && offer.getName().getLocalPart().equals("AgainstSidePrices"))){

                    BigDecimal price = offer.getValue().getPrice();
                    BigDecimal volume = offer.getValue().getStake();
                    betExchange.add(new BetOffer(site, event, bet, price, volume));
                }
            }
            marketOddsReport.addExchange(betExchange);
        }

        return marketOddsReport;
    }


    public static String remove_selectionName_suffix(String raw_selectionName){
        // If selection name contains some shortened names like '(Shef - ManC)' remove it
        Matcher m = unneeded_market_suffix.matcher(raw_selectionName);
        String new_selectionName = raw_selectionName;
        if (m.find()){
            new_selectionName = new_selectionName.substring(0, m.start()) + new_selectionName.substring(m.end()).trim();
        }
        return new_selectionName;
    }

    @Override
    public boolean siteSpecificSetup() throws IOException, URISyntaxException, InterruptedException {

        // Get the Betdaq specific event ID from the parent class event metadata and get details from betdaq
        event_id = Long.parseLong(event.metadata.get(Betdaq.BETDAQ_EVENT_ID));
        EventClassifierType betdaq_event =  betdaq.getEventTree(event_id);

        // Create list of market ids for specific market types in this event
        List<String> all_market_names = new ArrayList<>();

        bet_selectionId_map = new HashMap<>();
        marketName_id_map = new HashMap<>();
        selectionId_marketId_map = new HashMap<>();
        correct_score_max = null;
        correct_score_max_ht = null;


        // Find what each team is referred to as in betdaqs selector names
        String[] team_names = getTeamNames(betdaq_event);
        String team_a = team_names[0];
        String team_b = team_names[1];


        for (MarketType marketType: betdaq_event.getMarkets()){
            all_market_names.add(marketType.getName());
            String market_name = marketType.getName().trim();

            // Skip if market name not in whitelist
            if (!valid_market_names.contains(market_name)) {
                continue;
            }
            // Add a link between market name and id for this event
            marketName_id_map.put(market_name.toLowerCase(), marketType.getId());


            // If 'correct score' market, find the number of selections
            if (market_name.equals("Correct Score")){
                correct_score_max = max_score_selections(marketType.getSelections());
            }
            else if (market_name.equals("Half Time Score")){
                correct_score_max_ht = max_score_selections(marketType.getSelections());
            }



            for (SelectionType selectionType: marketType.getSelections()){
                Bet bet = null;
                String sel_name = selectionType.getName();

                // Add link between selection and market IDs
                selectionId_marketId_map.put(selectionType.getId(), marketType.getId());

                // RESULT
                if (market_name.equals("Match Odds") || market_name.equals("First-Half Result")){
                    boolean halftime = false;
                    if (market_name.equals("First-Half Result")){ halftime = true; }
                    String result = null;
                    if (selectionType.getDisplayOrder() == 1){ result = FootballBet.TEAM_A; }
                    else if (selectionType.getDisplayOrder() == 2){ result = FootballBet.DRAW; }
                    else if (selectionType.getDisplayOrder() == 3){ result = FootballBet.TEAM_B; }
                    if (result != null){
                        bet = new FootballResultBet(Bet.BetType.BACK, result, halftime);
                    }
                }

                // CORRECT SCORE
                else if (market_name.equals("Correct Score") || market_name.equals("Half Time Score")) {
                    boolean halftime = false;
                    int score = correct_score_max;
                    String halftime_str = "";
                    if (market_name.equals("Half Time Score")) {
                        halftime = true;
                        score = correct_score_max_ht;
                        halftime_str = "(H/T) ";
                    }

                    if (sel_name.toLowerCase().startsWith("any other")){
                        String result = null;
                        if (sel_name.equals("Any Other Home Win")){ result = FootballBet.TEAM_A; }
                        else if (sel_name.equals("Any Other Away Win")){ result = FootballBet.TEAM_B; }
                        else if (sel_name.equals("Any Other Draw")){ result = FootballBet.DRAW; }
                        else if (sel_name.startsWith("Any Other Score (H/T)")){ result = FootballBet.ANY; }
                        bet = new FootballOtherScoreBet(Bet.BetType.BACK, score, result, halftime);
                    }
                    else{
                        int[] scores = score_extractor(sel_name);
                        if (scores == null){
                            log.severe(sf("Could not extract single score from selection name '%s'",
                                    sel_name));
                            continue;
                        }

                        // Betdaq displays scores by winner first, so swap if needed for our format
                        String selector_name_a = sf("%s %s%s-%s", team_a, halftime_str, scores[0], scores[1])
                                .replaceAll("\\s+", " ");
                        String selector_name_b = sf("%s %s%s-%s", team_b, halftime_str, scores[0], scores[1])
                                .replaceAll("\\s+", " ");
                        String sel_name_edit = sel_name.replaceAll("\\s+", " ");
                        if (sel_name_edit.equals(selector_name_a) || scores[0] == scores[1]){
                            bet = new FootballScoreBet(Bet.BetType.BACK, scores[0], scores[1], halftime);
                        }
                        else if (sel_name_edit.equals(selector_name_b)){
                            bet = new FootballScoreBet(Bet.BetType.BACK, scores[1], scores[0], halftime);
                        }
                        else{
                            log.severe(sf("Selector name '%s' does not match '%s' or '%s'",
                                    sel_name, selector_name_a, selector_name_b));
                        }
                    }
                }

                // ASIAN HANDICAP
                else if (marketType.getType() == 10){
                    BigDecimal a_handicap = brackets_decimal_extractor(market_name);
                    String result = null;
                    if (selectionType.getDisplayOrder() == 1){ result = FootballBet.TEAM_A; }
                    else if (selectionType.getDisplayOrder() == 2){ result = FootballBet.TEAM_B; }

                    if (result != null){
                        bet = new FootballHandicapBet(Bet.BetType.BACK, a_handicap, result);
                    }
                }

                // UNDER OVER
                if (marketType.getType() == 4 || marketType.getType() == 13){
                    boolean halftime = false;
                    if (marketType.getType() == 13){ halftime = true; }
                    BigDecimal goals = brackets_decimal_extractor(sel_name);

                    String side = null;
                    if (selectionType.getDisplayOrder() == 1){ side = FootballBet.UNDER; }
                    else if (selectionType.getDisplayOrder() == 2){ side = FootballBet.OVER; }

                    if (side != null){
                        bet = new FootballOverUnderBet(Bet.BetType.BACK, side, goals, halftime);
                    }
                }



                // Put both BACK and LAY versions of bet, into selection ID map
                if (bet != null){
                    bet_selectionId_map.put(bet.id(), selectionType.getId());
                    bet_selectionId_map.put(bet.altId(), selectionType.getId());
                    //print(sf("%s - %s - %s", market_name, sel_name, bet.id()));
                }
            }
        }

        // Ensure at least 1 market is found
        if (marketName_id_map.size() == 0){
            log.warning(String.format("Could not find any relevant markets when setting up %s in betdaq. markets: %s",
                    event.name, all_market_names));
            return false;
        }

        return true;
    }



    public static int[] score_extractor(String selection_name){
        // Takes a string (eg "Leicester 1-0") and extracts the
        // scores as Ints (eg 1 and 0)

        Matcher m = score_pattern.matcher(selection_name);

        List<String> matches = new ArrayList<>();
        while (m.find()){
            matches.add(m.group());
        }

        if (matches.size() == 1){
            String[] string_scores = matches.get(0).split("-");
            return new int[] {Integer.valueOf(string_scores[0]), Integer.valueOf(string_scores[1])};
        }


        return null;
    }

    public static BigDecimal brackets_decimal_extractor(String selection_name){
        // Takes a string (eg "AEK Athens (+2.5)") and extracts the
        // number (eg +2.5)

        Matcher m = decimal_number_pattern.matcher(selection_name);
        List<String> matches = new ArrayList<>();
        while (m.find()){
            matches.add(m.group());
        }

        if (matches.size() == 1){
            String match = matches.get(0);
            return new BigDecimal(match.substring(1, match.length()-1));
        }

        log.severe(sf("Couldn't extract decimal from '%s' with pattern '%s'",
                selection_name , decimal_number_pattern.toString()));
        return null;
    }


    public Integer max_score_selections(List<SelectionType> selectionTypes){
        Integer max_score = null;

        for (SelectionType selectionType: selectionTypes){
            int[] scores = score_extractor(selectionType.getName());

            if (scores == null){ continue; }
            int max_this = Integer.max(scores[0], scores[1]);
            if (max_score == null){ max_score = max_this; }
            else{ max_score = Integer.max(max_score, max_this); }
        }
        return max_score;
    }


    public static String[] getTeamNames(EventClassifierType eventClassifierType){

        // Split event name into list of words
        String event_name = eventClassifierType.getName().trim();

        return extract_team_names(event_name);
    }

    public static String[] extract_team_names(String event_name){

        // Scrub name of prefixs and suffixs
        event_name = Betdaq.scrub_event_name(event_name).trim();

        // Find all occurances of ' v ' in the event name (team name seperator)
        List<Integer> v_occurances = new ArrayList<>();
        int last_occurance = 0;
        while (last_occurance != -1){
            last_occurance = event_name.indexOf(" v ", last_occurance+1);
            if (last_occurance != -1){
                v_occurances.add(last_occurance);
            }
        }

        if (v_occurances.size() != 1){
            log.severe(sf("The betdaq event name '%s' has %s occurances of ' v ' and it should have 1.",
                    event_name, v_occurances.size()));
            return null;
        }

        int _v_pos = v_occurances.get(0);
        String team_a = event_name.substring(0, _v_pos).trim();
        String team_b = event_name.substring(_v_pos+3).trim();
        return new String[] {team_a, team_b};
    }



    public static void main1(String[] args) throws Exception {

        FootballMatch event = FootballMatch.parse("2020-11-11T20:10:00.0Z", "France v Finland");

        BettingSite b = new Betdaq();
        SiteEventTracker set = b.getEventTracker();
        set.setupMatch(event);


        MarketOddsReport mor = set.getMarketOddsReport(FootballBetGenerator._getAllBets());
        toFile(mor.toJSON());
    }

    public static void main(String[] args) throws Exception {

        Betdaq b = new Betdaq();

        Instant start = Instant.now();
        List<FootballMatch> fms = b.getFootballMatches(Instant.now(), Instant.now().plusSeconds(60*60*48));
        print(secs_since(start));

        for (FootballMatch fm: fms){
            print(fm.toString());
        }
    }


}
