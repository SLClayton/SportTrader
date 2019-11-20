package Sport;

import SiteConnectors.SportData;
import Trader.SportsTrader;

import java.util.ArrayList;

public class FootballTeam extends Team {


    public ArrayList<FootballMatch> fixtures;


    public FootballTeam(String name) {
        super(name);
        id = this.getID();
    }


    public Boolean same_team(Team team){

        // Ensure other team is a football team
        FootballTeam ft;
        try{
            ft = (FootballTeam) team;
        } catch (ClassCastException e){
            return false;
        }

        // Check IDs
        if (getID() != null && ft.getID() != null){
            return id.equals(team.id);
        }

        // Check names
        if (normal_name().equals(ft.normal_name())){
            return true;
        }

        // Cannot confirm one way or the other
        return null;
    }


    @Override
    public String getID(){
        if (id == null){
            id = sportData.getFootballTeamID(this);
        }
        return id;
    }


    @Override
    public void setID(String id){
        this.id = id;
        sportData.update_football_team_id_map(this);
    }


    public ArrayList<FootballMatch> getFixtures(){
        if (fixtures == null){
            fixtures = sportData.getFootballFixtures(this);
        }
        return fixtures;
    }


}
