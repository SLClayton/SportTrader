package Sport;

public class FootballEventState extends EventState {

    public int score_a;
    public int score_b;


    public int totalGoals(){
        return score_a + score_b;
    }

    public boolean winningA(){
        return (score_a > score_b);
    }

    public boolean winningB(){
        return (score_b > score_a);
    }
}
