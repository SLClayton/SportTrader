package Sport;

import SiteConnectors.SportData;
import Trader.SportsTrader;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public abstract class Match {

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    public SportData sportData;

    public Instant start_time;
    public String name;
    public String id;
    public Map<String, String> metadata;

    public Match(SportData sportData){
        this.sportData = sportData;
        metadata = new HashMap<String, String>();
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


    public abstract Boolean same_match(Match match);



}


