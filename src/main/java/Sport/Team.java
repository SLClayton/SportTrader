package Sport;

import SiteConnectors.FlashScores;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;

public class Team {

    public String id;
    public String name;

    public ArrayList<FootballMatch> fixtures;


    public Team(String name){
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


    public boolean same_team(Team team){
        return (id != null && id.equals(team.id)) || normal_name().equals(team.normal_name());
    }





}
