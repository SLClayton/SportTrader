package Sport;

import Bet.FootballBet;
import SiteConnectors.Betfair;

import java.text.Normalizer;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.logging.Logger;

public abstract class Match {

    public static final Logger log = Logger.getLogger(Match.class.getName());

    public Instant start_time;
    public String name;
    public String id;
}


