package Sport;

import SiteConnectors.SportData;
import Trader.SportsTrader;

import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

import static tools.printer.print;

public abstract class Event {

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    public SportData sportData;

    public Instant start_time;
    public String name;
    public String id;
    public Map<String, String> metadata;

    public Event(){
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
            id = sportData.getEventID(this);
        }
        return id;
    }

    public String addMetaData(String key, String value){
        return metadata.put(key, value);
    }

    public void updateMetaData(Map<String, String> new_metadata){
        metadata.putAll(new_metadata);
    }


    public abstract boolean isVerified();


    public abstract boolean notVerified();


    public abstract boolean verify();


    public abstract void refreshIDs();


    public abstract String key();


    public abstract Boolean same_match(Event event);


    public boolean starts_within(Instant start, Instant end){
        return !start_time.isBefore(start) && !start_time.isAfter(end);
    }


    @Override
    public String toString() {
        return String.format("[%s : %s]", start_time.toString(), name);
    }

    @Override
    public boolean equals(Object obj) {
        try {
            return toString().equals(((Event) obj).toString());
        }
        catch (ClassCastException e){
            return false;
        }
    }
}


