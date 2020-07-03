package Bet;

import SiteConnectors.BettingSite;
import Sport.Event;
import Trader.SportsTrader;
import org.json.simple.JSONObject;

import java.lang.management.BufferPoolMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

public class BetExchange {

    // Represents the offers from a betting site for a particular bet on a particular event

    public static final Logger log = SportsTrader.log;

    public final Instant time_created;
    public final BettingSite site;
    public final Event event;
    public final Bet bet;
    private final List<BetOffer> betOffers; // Ordered from Best ROI, to worst
    private final Map<String, String> metadata;

    public BetExchange(BettingSite site, Event event, Bet bet){
        this.time_created = Instant.now();
        this.site = site;
        this.event = event;
        this.bet = bet;
        this.betOffers = new ArrayList<BetOffer>();
        this.metadata = new HashMap<>();
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


    public void add(BetOffer betOffer){

        // Ensure offer is of correct site, event and bet.
        if (!betOffer.event.toString().equals(event.toString()) ||
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


    public boolean isEmpty(){
        return betOffers.size() == 0;
    }


    public BigDecimal minBackStake(){
        return site.minBackersStake();
    }


    public BigDecimal ROI(BigDecimal investment){
        // How much return would you get if you invested an amount into this exchange.
        return BetOffer.ROI(betOffers, investment, true);
    }


    public BigDecimal avgOddsFromBackStake(BigDecimal back_stake){
        BigDecimal lay_stake = BetOffer.backStake2LayStake(betOffers, back_stake, true);
        BigDecimal total_return = back_stake.add(lay_stake);
        return total_return.divide(back_stake, 12, RoundingMode.HALF_UP);
    }



    public BigDecimal investmentForReturn(BigDecimal target_return){
        return BetOffer.investmentForReturn(betOffers, target_return, true);
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


    public JSONObject toJSON(){
        JSONObject j = new JSONObject();
        j.put("event", event.name);
        j.put("site", site.getName());
        j.put("offers", BetOffer.list2JSON(betOffers));
        return j;
    }

}
