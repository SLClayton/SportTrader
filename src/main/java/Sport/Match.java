package Sport;

import SiteConnectors.BettingSite;
import SiteConnectors.SportData;
import Trader.SportsTrader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

public abstract class Match {

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    public SportData sportData;

    public Instant start_time;
    public String name;
    public String id;
    public Map<String, String> metadata;

    public Match(){
        this.sportData = SportsTrader.getSportData();
        metadata = new HashMap<>();
    }

    public static String listtostring(ArrayList<FootballMatch> matches){
        String s = "[";
        for (int i=0; i<matches.size(); i++){
            s += matches.get(i).toString();
            if (i + 1 != matches.size()){
                s += ", ";
            }
        }
        s += "]";
        return s;
    }


    public String getID(){
        if (id == null){
            id = sportData.getMatchID(this);
        }
        return id;
    }


    public abstract boolean isVerified();


    public abstract boolean notVerified();


    public abstract boolean verify();


    public abstract void refreshIDs();


    public abstract String key();


    public abstract Boolean same_match(Match match);



}


