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

    public Match(){
        this.sportData = SportsTrader.getSportData();
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


    public String id(){
        if (id == null){
            id = sportData.getMatchID(this);
        }
        return id;
    }


    public void set_id(String id){
        this.id = id;
        sportData.update_match_id_map(this);
    }

    public abstract String key();


    public abstract Boolean same_match(Match match, boolean attempt_verify);
}


