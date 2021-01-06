package Bet;

import Bet.Bet.BetType;
import Bet.FootballBet.FootballBet;
import Bet.FootballBet.FootballResultBet;
import SiteConnectors.Betdaq.Betdaq;
import SiteConnectors.Betfair.Betfair;
import SiteConnectors.BettingSite;
import SiteConnectors.Smarkets.Smarkets;
import SiteConnectors.TestBetSite;
import Sport.Event;
import Sport.FootballMatch;
import Trader.SportsTrader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import tools.BigDecimalTools;

import java.lang.management.BufferPoolMXBean;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Exchanger;
import java.util.logging.Logger;

import static java.lang.System.exit;
import static java.lang.System.in;
import static tools.printer.*;
import static tools.BigDecimalTools.*;

public class BetExchange {

    // Represents the offers from a betting site for a particular bet on a particular event

    public static final Logger log = SportsTrader.log;

    public final Instant time_created;
    public final BettingSite site;
    public final Event event;
    public final Bet bet;

    private final List<BetOffer> betOffers; // Ordered from Best ROI, to worst
    private final Map<String, String> metadata;


    private BigDecimal min_stake;
    private BigDecimal min_investment;
    private BigDecimal min_return;


    public BetExchange(BettingSite site, Event event, Bet bet){
        this.time_created = Instant.now();
        this.site = site;
        this.event = event;
        this.bet = bet;
        this.betOffers = new ArrayList<BetOffer>();
        this.metadata = new HashMap<>();

        this.min_stake = null;
        this.min_investment = null;
    }


    public static BetExchange fromOffers(List<BetOffer> betOffers){
        BetExchange betExchange = null;
        for (BetOffer betOffer: betOffers){
            if (betExchange == null) {
                betExchange = new BetExchange(betOffer.site, betOffer.event, betOffer.bet);
            }
            betExchange.add(betOffer);
        }
        return betExchange;
    }


    public String siteName(){
        return site.getName();
    }


    public void add(BetOffer betOffer){

        // Ensure offer is of correct site, event and bet.
        if (!betOffer.event.equals(event) ||
                !betOffer.site.equals(site) ||
                !betOffer.bet.equals(bet)){
            log.severe(String.format("FAILED adding betOffer of %s/%s/%s to BetExchange of %s/%s/%s",
                    betOffer.site, betOffer.event, betOffer.bet, site, event, bet));
            return;
        }

        // Go through list until correct placement is found then insert.
        int i;
        for (i=0; i<betOffers.size(); i++){
            if (betOffer.largerROI(betOffers.get(i))){
                break;
            }
        }
        betOffer.updateMetadata(metadata);
        betOffers.add(i, betOffer);
    }


    public void addAll(Collection<BetOffer> new_betOffers){
        for (BetOffer new_betOffer: new_betOffers){
            add(new_betOffer);
        }
    }

    public BigDecimal bestROIRatio(){
        // returns the ROI ratio of the best bet offer in the exchange
        try {
            return betOffers.get(0).ROI_ratio();
        }
        catch (IndexOutOfBoundsException e){
            return null;
        }
    }


    public String addMetadata(String key, String value){
        return metadata.put(key, value);
    }

    public String addMetadata(String key, Object value){
        return addMetadata(key, String.valueOf(value));
    }

    public String getMetadata(String key){
        return metadata.get(key);
    }

    public Long getMetadataLong(String key){
        return Long.parseLong(getMetadata(key));
    }

    public BigDecimal getMetadataBD(String key){
        return new BigDecimal(getMetadata(key));
    }


    public boolean isBack(){
        return bet.isBack();
    }

    public boolean isLay(){
        return bet.isLay();
    }


    public boolean isEmpty(){
        return betOffers.size() == 0;
    }


    public BigDecimal minBackStake(){
        return site.minBackersStake();
    }


    public BigDecimal minStake(){
        if (min_stake == null) {
            if (isBack()) {
                min_stake = minBackStake();
            }
            else if (isLay()) {
                min_stake = backStake2LayStake(minBackStake());
            }
        }
        if (min_stake != null){
            min_stake = min_stake.setScale(2, RoundingMode.UP);
        }
        return min_stake;
    }


    public BigDecimal minInvestment(){
        if (min_investment == null){
            BigDecimal min_stake = minStake();
            if (min_stake == null){
                return null;
            }
            min_investment = Bet.stake2Investment(minStake(), site.lossCommissionRate())
                    .setScale(2, RoundingMode.UP);
        }
        return min_investment;
    }

    public BigDecimal minReturn(){
        if (min_return == null){
            if (minBackStake().compareTo(volume()) > 0){
                return null;
            }
            BetPlan min_back_stake_betplan = applyBackStake(minBackStake());
            if (min_back_stake_betplan == null){
                return null;
            }
            min_return = min_back_stake_betplan.getReturn();
        }
        return min_return;
    }


    public BigDecimal backStake2LayStake(BigDecimal back_stake){
        return BetOffer.backStake2LayStake(getBetOffers(), back_stake, true);
    }

    public BigDecimal layStake2BackStake(BigDecimal lay_stake){
        return BetOffer.layStake2BackStake(getBetOffers(), lay_stake, true);
    }




    public BetPlan applyBackStake(BigDecimal back_stake){
        return BetPlan.fromBackersStake(this, back_stake);
    }



    public BigDecimal maxStake(BigDecimal target_odds){
        return BetOffer.max_stake_for_specific_odds(betOffers, target_odds, true);
    }


    public BetOffer bestValidOffer(){
        for (BetOffer betOffer: betOffers){
            if (betOffer.hasMinVolumeNeeded()){
                return betOffer;
            }
        }
        return null;
    }


    public BigDecimal volume(){
        BigDecimal volume = BigDecimal.ZERO;
        for (BetOffer bo: betOffers){
            volume = volume.add(bo.volume);
        }
        return volume;
    }


    public List<BetOffer> getBetOffers(){
        return betOffers;
    }


    public BetExchange removeBackStake(BigDecimal back_stake){
        // creates new exchange identical to this one but with a back stake removed.

        BetExchange new_exchange = new BetExchange(site, event, bet);
        List<BetOffer> new_betOffers = BetOffer.remove_stake(betOffers, siteName(), back_stake);
        new_exchange.addAll(new_betOffers);
        return new_exchange;
    }


    public BigDecimal largestBackStakeForSameROIRatio(BigDecimal back_stake){
        // Given a certain back stake applied to this exchange, find the
        // largest back stake possible which results in the same ROI ratio.

        // Find what offers are used when applying this back stake
        List<BetOfferStake> betOfferStakes = BetOffer.applyStake(betOffers, back_stake, true, true);
        if (betOfferStakes == null || betOfferStakes.isEmpty()){
            return null;
        }

        // If only 1 bet is used, then ROI ratio remains the same for the whole volume of that offer.
        if (betOfferStakes.size() == 1){
            return betOfferStakes.get(0).betOffer.volume;
        }
        // If >1 offer used, the ROI ratio is dynamic and always increases so we are already at max.
        return back_stake;
    }


    public static Map<String, BetExchange> list2Map(Collection<BetExchange> exchanges){
        Map<String, BetExchange> exchangeMap = new HashMap<String, BetExchange>();
        for (BetExchange betExchange: exchanges){
            exchangeMap.put(betExchange.siteName(), betExchange);
        }
        return exchangeMap;
    }


    @Override
    public String toString() {
        List<String> offer_strings = new ArrayList<>();
        for (BetOffer betOffer: betOffers){
            offer_strings.add(betOffer.toString() + "\n");
        }
        return offer_strings.toString();
    }


    public JSONObject toJSON(){
        JSONObject j = new JSONObject();
        j.put("event", stringValue(event));
        j.put("site", site.getName());
        j.put("created", stringValue(time_created));
        j.put("bet", stringValue(bet));


        JSONArray offers = new JSONArray();
        for (BetOffer betOffer: betOffers){
            JSONObject offer_json = betOffer.toJSON();
            offer_json.remove("event");
            offer_json.remove("created");
            offer_json.remove("site");
            offer_json.remove("bet");
            offers.add(offer_json);
        }
        j.put("offers", offers);

        return j;
    }


    public static List<BetOffer> getAllBetOffers(Collection<BetExchange> betExchanges){
        List<BetOffer> betOffers = new ArrayList<BetOffer>();
        for (BetExchange betExchange: betExchanges){
            betOffers.addAll(betExchange.getBetOffers());
        }
        return betOffers;
    }





    public static void main(String[] args){
        try {


            Bet bet = new FootballResultBet(BetType.BACK, FootballBet.TEAM_A, false);
            Event event = FootballMatch.parse("2020-06-01T12:00:00.00Z", "utd vs wolf");

            List<BetExchange> betExchanges = new ArrayList<BetExchange>();

            Betfair bf = new Betfair();
            List<BetOffer> bf_betOffers = new ArrayList<>();
            bf_betOffers.add(new BetOffer(bf, event, bet, new BigDecimal("3.45"), new BigDecimal("1.00")));
            bf_betOffers.add(new BetOffer(bf, event, bet, new BigDecimal("3.33"), new BigDecimal("4.50")));
            bf_betOffers.add(new BetOffer(bf, event, bet, new BigDecimal("1.01"), new BigDecimal("2.342")));
            BetExchange bf_betExchange = BetExchange.fromOffers(bf_betOffers);
            betExchanges.add(bf_betExchange);

            Smarkets sm = new Smarkets();
            List<BetOffer> sm_betOffers = new ArrayList<>();
            sm_betOffers.add(new BetOffer(sm, event, bet, new BigDecimal("3.50"), new BigDecimal("5.00")));
            sm_betOffers.add(new BetOffer(sm, event, bet, new BigDecimal("2.50"), new BigDecimal("10.00")));
            sm_betOffers.add(new BetOffer(sm, event, bet, new BigDecimal("1.01"), new BigDecimal("50.534")));
            BetExchange sm_betExchange = BetExchange.fromOffers(sm_betOffers);
            betExchanges.add(sm_betExchange);

            BettingSite t1 = new TestBetSite(1, "0.05", "0.05", "2.00");
            List<BetOffer> t1_betOffers = new ArrayList<>();
            t1_betOffers.add(new BetOffer(t1, event, bet, new BigDecimal("3.47"), new BigDecimal("3.00")));
            t1_betOffers.add(new BetOffer(t1, event, bet, new BigDecimal("2.90"), new BigDecimal("6.00")));
            t1_betOffers.add(new BetOffer(t1, event, bet, new BigDecimal("1.01"), new BigDecimal("50.534")));
            BetExchange t1_betExchange = BetExchange.fromOffers(t1_betOffers);
            betExchanges.add(t1_betExchange);


            List<BetOffer> all_betOffers = BetExchange.getAllBetOffers(betExchanges);
            Collections.sort(all_betOffers, Collections.reverseOrder());

            BetOffer.printList(all_betOffers);
            print("\n\n");



            MultiSiteBet msb = MultiSiteBet.fromTargetReturn(betExchanges, BD("22"), true);

            if (msb == null){
                print("\n---- Could not create valid MSB ----");
            }
            else{
                psf("\n\nFinal MSB: inv %s  ret: %s  -> %s",
                        BDString(msb.getInvestment(), 4),
                        BDString(msb.getReturn(), 4),
                        msb.inv_per_site());
                msb.printBetOfferStakes();
            }



        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
