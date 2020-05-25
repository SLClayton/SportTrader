package SiteConnectors.Betdaq;

import Bet.*;
import Bet.FootballBet.*;
import SiteConnectors.SiteEventTracker;
import Sport.FootballMatch;
import com.globalbettingexchange.externalapi.*;

import javax.xml.bind.JAXBElement;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static tools.printer.pp;

public class BetdaqEventTracker extends SiteEventTracker {

    Betdaq betdaq;

    Long event_id;
    public Map<String, Long> marketName_id_map;
    public Map<String, Long> market_selection_id_map;
    public Map<Long, Long> selectionId_marketId_map;
    public Integer correct_score_max;
    public Integer correct_score_max_ht;

    // Pattern to match things like "(Manc - Sheff)" at the end of market names
    public static Pattern unneeded_market_suffix = Pattern.compile("\\(\\w+ - \\w+\\)\\z");


    public static List<String> market_names = Arrays.asList(
            "Event Odds",
            "Correct Score",
            "Half Time Score",
            "First-Half Result",
            "Asian Handicap (-0.5)",
            "Asian Handicap (+0.5)",
            "Asian Handicap (-1.5)",
            "Asian Handicap (+1.5)",
            "Asian Handicap (-2.5)",
            "Asian Handicap (+2.5)",
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
        List<MarketTypeWithPrices> marketPrices = null;
        try {
            marketPrices = betdaq._getPrices(marketName_id_map.values());
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        if (marketPrices == null){
            log.severe(String.format("Market prices for %s found to be null when requesting from betdaq.",
                    event.toString()));
            return MarketOddsReport.ERROR("Betdaq marketPrices returned null.");
        }


        MarketOddsReport marketOddsReport = new MarketOddsReport();
        for (Bet bet: bets){

            // Ensure bet isn't blacklisted
            if (bet_blacklist.contains(bet.id())){
                continue;
            }

            // Generate what the selection name would be for this bet
            String selection_name = bet_to_betdaq_selection_name(bet);

            // Try and find if there is a selection id for this bet
            Long selection_id = market_selection_id_map.get(selection_name);
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

            // Find right market by id
            MarketTypeWithPrices marketType = null;
            for (MarketTypeWithPrices potential_marketType: marketPrices){
                if (potential_marketType.getId() == market_id){
                    marketType = potential_marketType;
                    break;
                }
            }
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
            if (selectionType == null){
                log.severe(String.format("Could not find selection %s in betdaq prices.", selection_id));
                continue;
            }


            List<BetOffer> betOffers = new ArrayList<>();
            for (JAXBElement<PricesType> offer: selectionType.getForSidePricesAndAgainstSidePrices()){

                if ((bet.isBack() && offer.getName().getLocalPart().equals("ForSidePrices"))
                ||  (bet.isLay()  && offer.getName().getLocalPart().equals("AgainstSidePrices"))){

                    BigDecimal price = offer.getValue().getPrice();
                    BigDecimal volume = offer.getValue().getStake();

                    BetOffer bo = new BetOffer(lastMarketOddsReport_start_time, event, bet, site, price, volume);
                    bo.addMetadata(Betdaq.BETDAQ_EVENT_ID, String.valueOf(market_id));
                    bo.addMetadata(Betdaq.BETDAQ_SELECTION_ID, String.valueOf(selection_id));
                    bo.addMetadata(Betdaq.BETDAQ_SELECTION_RESET_COUNT, String.valueOf(selectionType.getResetCount()));
                    bo.addMetadata(Betdaq.BETDAQ_SEQ_NUMBER, String.valueOf(marketType.getWithdrawalSequenceNumber()));
                    betOffers.add(bo);
                }
            }


            marketOddsReport.addBetOffers(bet.id(), betOffers);
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
        EventClassifierType betdaq_event =  betdaq.getEventTree(event_id, true);

        // Create list of market ids for specific market types in this event
        List<String> all_market_names = new ArrayList<>();
        marketName_id_map = new HashMap<>();
        market_selection_id_map = new HashMap<>();
        selectionId_marketId_map = new HashMap<>();
        correct_score_max = null;
        correct_score_max_ht = null;
        for (MarketType marketType: betdaq_event.getMarkets()){
            all_market_names.add(marketType.getName());

            // Skip if market name not in whitelist
            if (!market_names.contains(marketType.getName())) {
                continue;
            }
            // Add a link between market name and id for this event
            marketName_id_map.put(marketType.getName().toLowerCase(), marketType.getId());

            // Add a link for selection name (with market name concat to front) and selection id.
            for (SelectionType selectionType: marketType.getSelections()){

                // If selection name contains some shortened names like '(Shef - ManC)' remove it
                // Add full selection name to map for its ID
                String selectionName = remove_selectionName_suffix(selectionType.getName());
                String fullMarketSelectionName = String.format("%s_%s", marketType.getName(), selectionName).toLowerCase();
                market_selection_id_map.put(fullMarketSelectionName, selectionType.getId());
                selectionId_marketId_map.put(selectionType.getId(), marketType.getId());
            }

            // If 'correct score' market, find the number of selections
            if (marketType.getName().equals("Correct Score")){
                correct_score_max = sqrt(marketType.getSelections().size() - 3) - 1;
            }
            else if (marketType.getName().equals("Half Time Score")){
                correct_score_max_ht = sqrt(marketType.getSelections().size() - 1) - 1;
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


    public Integer sqrt(int selections){
        if (selections == 4){ return 2;}
        if (selections == 9){ return 3;}
        if (selections == 16){ return 4;}
        if (selections == 25){ return 5;}
        log.severe("betdaq sqrt searched but no sqrt found. - " + String.valueOf(selections));
        return null;
    }


    public String bet_to_betdaq_selection_name(Bet bet) {

        if (bet instanceof FootballBet) {
            return football_bet_to_betdaq_selection_name((FootballBet) bet, (FootballMatch) event);
        }

        log.severe(String.format("Cannot find betdaq selection name for bet %s as sport instance is not supported.",
                    bet.toString()));
        return null;
    }


    public String football_bet_to_betdaq_selection_name(FootballBet bet, FootballMatch footballMatch) {

        // Event Odds
        if (bet instanceof FootballResultBet) {
            FootballResultBet fbrb = (FootballResultBet) bet;
            String market_name = "event odds";
            if (fbrb.halftime){market_name = "First-Half Result";}
            String selection_name = null;
            if      (fbrb.winnerA()) { selection_name = footballMatch.team_a.name.toLowerCase();}
            else if (fbrb.winnerA()) { selection_name = footballMatch.team_b.name.toLowerCase();}
            else if (fbrb.isDraw())  { selection_name = "draw";}
            return String.format("%s_%s", market_name, selection_name);
        }

        // Correct Score
        else if (bet instanceof FootballScoreBet) {
            FootballScoreBet fbsb = (FootballScoreBet) bet;
            String marketName = "correct_score";
            String halftime_brackets = "";
            if (fbsb.halftime){
                marketName = "half time score";
                halftime_brackets = "(h/t) ";
            }
            if      (fbsb.winA()){ return String.format("%s_%s %s%s-%s",
                    marketName, footballMatch.team_a.name.toLowerCase(), halftime_brackets, fbsb.score_a, fbsb.score_b);}
            else if (fbsb.winB()){ return String.format("%s_%s %s%s-%s",
                    marketName, footballMatch.team_b.name.toLowerCase(), halftime_brackets, fbsb.score_b, fbsb.score_a);}
            else if (fbsb.isDraw()){ return String.format("%s_draw %s%s-%s",
                    marketName, halftime_brackets, fbsb.score_a, fbsb.score_b);}
        }

        // Correct score (any other score)
        else if (bet instanceof FootballOtherScoreBet) {
            FootballOtherScoreBet fbosb = (FootballOtherScoreBet) bet;
            if (fbosb.halftime && fbosb.over_score == correct_score_max_ht){
                if (fbosb.isAnyResult()){ return "half time score_any other score (h/t)";}
            }
            else if (!fbosb.halftime && fbosb.over_score == correct_score_max){
                if (fbosb.winnerA()){ return "correct score_any other home win";}
                if (fbosb.winnerB()){ return "correct score_any other away win";}
                if (fbosb.isDraw()){ return "correct score_any other draw";}
            }
            else {
                return null;
            }
        }

        // Over Under
        else if (bet instanceof FootballOverUnderBet){
            FootballOverUnderBet fboub = (FootballOverUnderBet) bet;
            return String.format("under/over - goals (%1$s)_%2$s (%1$s)", fboub.goals.toString(), fboub.side.toLowerCase());
        }

        // Handicap bet
        else if (bet instanceof FootballHandicapBet){
            FootballHandicapBet fbhb = (FootballHandicapBet) bet;
            if (fbhb.a_handicap.compareTo(BigDecimal.ZERO) == 1){
                if (fbhb.winnerA()){ return String.format("asian handicap (+%1$s)_%2$s (+%1$s)",
                            fbhb.a_handicap.toString(), footballMatch.team_a.name.toLowerCase());}
                else if (fbhb.winnerB()){ return String.format("asian handicap (+%1$s)_%2$s (-%1$s)",
                            fbhb.a_handicap.toString(), footballMatch.team_b.name.toLowerCase()); }
            }
            else if (fbhb.a_handicap.compareTo(BigDecimal.ZERO) == -1){
                if (fbhb.winnerA()){ return String.format("asian handicap (%1$s)_%2$s (%1$s)",
                        fbhb.a_handicap.toString(), footballMatch.team_a.name.toLowerCase());}
                else if (fbhb.winnerB()){ return String.format("asian handicap (%1$s)_%2$s (+%3$s)",
                        fbhb.a_handicap.toString(), footballMatch.team_b.name.toLowerCase(),
                        fbhb.a_handicap.multiply(new BigDecimal(-1)));}
            }
            return null;
        }


        log.severe(String.format("Could not generate betdaq selection name for bet %s", bet.toString()));
        return null;
    }


}
