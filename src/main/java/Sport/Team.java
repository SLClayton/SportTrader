package Sport;

import SiteConnectors.FlashScores;
import SiteConnectors.SportData;
import Trader.SportsTrader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;

public class Team {

    public SportData sportData;

    public String id;
    public String name;



    public Team(String name){
        this.name = name;
        sportData = SportsTrader.getSportData();
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


    public String id(){
        if (id == null){
            id = sportData.getTeamID(this);
        }
        return id;
    }


    public void set_id(String id){
        this.id = id;
        sportData.update_team_id_map(this);
    }
}
