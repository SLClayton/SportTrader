package Sport;

import Bet.FootballBet;

import java.util.Date;

public abstract class Match {

    public Date start_time;
    public String name;
}


class FootballMatch extends Match{

    public String team_a;
    public String team_b;

    public FootballMatch(Date START, String NAME) throws Exception {
        start_time = START;
        name = NAME;

        String[] parts = name.toLowerCase().split(" v ");
        if (parts.length != 2){
            parts = name.toLowerCase().split(" vs ");
            if (parts.length != 2){
                throw new Exception(String.format("Cannot find teams from name '%s'", name));
            }
        }

        team_a = parts[0];
        team_b = parts[1];
    }

    public FootballMatch(Date START, String TEAM_A, String TEAM_B){
        start_time = START;
        team_a = TEAM_A;
        team_b = TEAM_B;
        name = team_a + " v " + team_b;
    }
}
