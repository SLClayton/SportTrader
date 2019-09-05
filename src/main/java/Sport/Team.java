package Sport;

import SiteConnectors.FlashScores;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;

public class Team {

    public String FS_ID;
    public String FS_URLNAME;
    public String FS_Title;
    public String name;

    public ArrayList<FootballMatch> fixtures;


    public Team(String name){
        this.name = name;
    }


    @Override
    public String toString(){
        return FS_Title;
    }

    public FootballMatch getMatch(Instant start_time) throws InterruptedException, IOException,
            URISyntaxException {
        return FlashScores.getMatch(this, start_time);
    }

    public ArrayList<FootballMatch> getFixtures() throws InterruptedException, IOException, URISyntaxException {
        fixtures = FlashScores.getTeamFixtures(this);
        return fixtures;
    }
}
