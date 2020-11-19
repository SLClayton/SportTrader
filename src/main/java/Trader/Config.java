package Trader;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static tools.printer.getResourceFileString;

public class Config {

    static Logger log = SportsTrader.log;

    String file_path;
    JSONObject json;


    public boolean RUN_STATS;
    public String LOG_LEVEL;
    public String SSL_DIR;

    public int MAX_MATCHES;
    public boolean IN_PLAY;
    public int HOURS_AHEAD;

    public boolean SINGLE_MATCH_TEST;
    public String SM_NAME;
    public String SM_TIME;

    public boolean CHECK_MARKETS;
    public long RATE_LIMIT;
    public long RATE_LOCKSTEP_INTERVAL;
    public long REQUEST_TIMEOUT;
    public boolean LIMIT_LOW_PROFIT;
    public int PRINT_STATS_INTERVAL;

    public boolean PLACE_BETS;
    public boolean END_ON_BET;
    public BigDecimal MIN_PROFIT_RATIO;
    public BigDecimal ODDS_RATIO_BUFFER;
    public BigDecimal TARGET_INVESTMENT;
    public BigDecimal MAX_INVESTMENT;

    public Map<String, Boolean> ACTIVE_SITES;
    public String EVENT_SOURCE;
    public int MIN_SITES_PER_MATCH;


    public long BETDAQ_RH_WAIT;



    private Config(String file_path) throws FileNotFoundException, ParseException, ConfigException {
        this.file_path = file_path;
        setupConfig();
        verifyConfig();
    }

    public static Config getConfig(String file_path){
        try{
            return new Config(file_path);
        }
        catch (Exception e){
            log.severe("Exception getting config - " + e.toString());
            return null;
        }
    }


    public void setupConfig() throws FileNotFoundException, ParseException, ConfigException {

        JSONObject config = null;
        String json_string = getResourceFileString(file_path);
        json = (JSONObject) new JSONParser().parse(json_string);

        MAX_MATCHES = getInt("MAX_MATCHES");
        MIN_SITES_PER_MATCH = getInt("MIN_SITES_PER_MATCH");
        SSL_DIR = getString("SSL_DIR");
        IN_PLAY = getBoolean("IN_PLAY");
        HOURS_AHEAD = getInt("HOURS_AHEAD");
        CHECK_MARKETS = getBoolean("CHECK_MARKETS");
        PLACE_BETS = getBoolean("PLACE_BETS");
        ACTIVE_SITES = (Map<String, Boolean>) getObject("ACTIVE_SITES");
        RATE_LIMIT = getLong("RATE_LIMIT");
        ODDS_RATIO_BUFFER = getBigDecimal("ODDS_RATIO_BUFFER");
        EVENT_SOURCE = getString("EVENT_SOURCE");
        MAX_INVESTMENT = getBigDecimal("MAX_INVESTMENT");
        MIN_PROFIT_RATIO = getBigDecimal("MIN_PROFIT_RATIO");
        END_ON_BET = getBoolean("END_ON_BET");
        TARGET_INVESTMENT = getBigDecimal("TARGET_INVESTMENT");
        REQUEST_TIMEOUT = getLong("REQUEST_TIMEOUT");
        RUN_STATS = getBoolean("RUN_STATS");
        SINGLE_MATCH_TEST = getBoolean("SINGLE_MATCH_TEST");
        SM_NAME = getString("SM_NAME");
        SM_TIME = getString("SM_TIME");
        RATE_LOCKSTEP_INTERVAL = getLong("RATE_LOCKSTEP_INTERVAL");
        LOG_LEVEL = getString("LOG_LEVEL");
        LIMIT_LOW_PROFIT = getBoolean("LIMIT_LOW_PROFIT");
        PRINT_STATS_INTERVAL = getInt("PRINT_STATS_INTERVAL");

        BETDAQ_RH_WAIT = getLong("BETDAQ_RH_WAIT");

    }



    public void verifyConfig() throws ConfigException {
        // Check target inv per bet is lower than max investment per bet
        if (TARGET_INVESTMENT.compareTo(MAX_INVESTMENT) == 1){
            String msg = String.format("TARGET_INVESTMENT (%s) is higher than MAX_INVESTMENT (%s). Exiting",
                    TARGET_INVESTMENT.toString(), MAX_INVESTMENT.toString());
            log.severe(msg);
            throw new ConfigException(msg);
        }

        // Check number active sites is not lower than min number of sites per event
        int number_active_sites = 0;
        for (Map.Entry<String, Boolean> entry: ACTIVE_SITES.entrySet()){
            if (entry.getValue()){
                number_active_sites += 1;
            }
        }
        if (number_active_sites < MIN_SITES_PER_MATCH){
            String msg = String.format("MIN_SITES_PER_MATCH (%d) is lower than number of active sites (%d). Exiting",
                    MIN_SITES_PER_MATCH, number_active_sites);
            log.severe(msg);
            throw new ConfigException(msg);
        }
    }


    public Object getObject(String key) throws ConfigException {
        Object value = json.get(key);
        if (value == null){
            throw new ConfigException(String.format("'%s' not found in config json.", key));
        }
        return value;
    }


    public String getString(String key) throws ConfigException {
        return String.valueOf(getObject(key));
    }

    public int getInt(String key) throws ConfigException {
        return ((Long) getObject(key)).intValue();
    }

    public long getLong(String key) throws ConfigException {
        return (long) getObject(key);
    }

    public BigDecimal getBigDecimal(String key) throws ConfigException {
        return new BigDecimal(String.valueOf((Double) getObject(key)));
    }

    public boolean getBoolean(String key) throws ConfigException {
        return (boolean) getObject(key);
    }


    public class ConfigException extends Exception {
        public ConfigException(List<String> fields){
            super(String.join(", ", fields));
        }

        public ConfigException(String msg){
            super(msg);
        }

        public ConfigException(){
            super();
        }
    }


    public static void main(String[] args){

        try{
            Config c = new Config("config.json");
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }


}
