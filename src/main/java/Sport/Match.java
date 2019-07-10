package Sport;

import Trader.SportsTrader;

import java.time.Instant;
import java.util.logging.Logger;

public abstract class Match {

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    public Instant start_time;
    public String name;
    public String id;

    public Match(){
    }
}


