package Sport;

import SiteConnectors.SportData;
import Trader.SportsTrader;

import java.util.ArrayList;

public class FootballTeam extends Team {


    public ArrayList<FootballMatch> fixtures;


    public FootballTeam(String name) {
        super(name);
    }


    public Boolean same_team(Team team){

        FootballTeam ft;
        try{
            ft = (FootballTeam) team;
        } catch (ClassCastException e){
            return false;
        }

        if (id() != null && ft.id() != null){
            return id.equals(team.id);
        }

        if (normal_name().equals(ft.normal_name())){
            return true;
        }

        return null;
    }


    public ArrayList<FootballMatch> getFixtures(){
        if (fixtures == null){
            fixtures = sportData.getFootballFixtures(this);
        }
        return fixtures;
    }


}
