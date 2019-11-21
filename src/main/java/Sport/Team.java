package Sport;

import SiteConnectors.FlashScores;
import SiteConnectors.SportData;
import Trader.SportsTrader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;

public abstract class Team {

    public SportData sportData;

    public String id;
    public String name;



    public Team(String name){
        sportData = SportsTrader.getSportData();
        this.name = name;
    }


    @Override
    public String toString(){
        return name;
    }


    public static String normalize(String string){
        return Normalizer.normalize(string.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{P}", "");
    }


    public String normal_name(){
        return normalize(name);
    }


    public abstract Boolean same_team(Team team);


    public abstract String getID();



}
