package Bet.FootballBet;

import java.math.BigDecimal;
import java.util.*;


import Bet.Bet;
import Bet.Bet.BetType;
import Bet.BetGroup;
import Trader.SportsTrader;
import org.json.simple.JSONObject;

import java.util.logging.Logger;

import static tools.printer.*;


public class FootballBetGenerator {

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    public static final String tauts_filename = "football_tautologies.json";
    public static final String bets_filename = "football_bets.json";

    static int GOALSCOUNT_MAX = 9;
    static int OTHERGOALS_MIN = 3;
    static int OTHERGOALS_MAX = 5;
    static int OTHERGOALS_HT_MIN = 2;
    static int OTHERGOALS_HT_MAX = 2;
    static int SCORE_MAX = 4;
    static int SCORE_MAX_HT = 2;
    static int HANDICAP_MAX = 3;

    ArrayList<FootballScoreBet> score_bets;
    ArrayList<FootballScoreBet> score_bets_ht;
    ArrayList<FootballOtherScoreBet> other_score_bets;
    ArrayList<FootballOtherScoreBet> other_score_bets_ht;
    ArrayList<FootballResultBet> result_bets;
    ArrayList<FootballResultBet> result_bets_ht;
    ArrayList<FootballHandicapBet> handicap_bets;
    ArrayList<FootballOverUnderBet> over_under_bets;

    ArrayList<BetGroup> allTautologies;



    public FootballBetGenerator(){
        score_bets = getScoreBets(SCORE_MAX, false);
        score_bets_ht = getScoreBets(SCORE_MAX_HT, true);

        other_score_bets = getOtherScoreBets(OTHERGOALS_MIN, OTHERGOALS_MAX);
        other_score_bets_ht = getOtherScoreBetsHT(OTHERGOALS_HT_MIN, OTHERGOALS_HT_MAX);

        result_bets = getResultBets(false);
        result_bets_ht = getResultBets(true);

        handicap_bets = getHandicapBets(HANDICAP_MAX);

        over_under_bets = getOverUnderBets(GOALSCOUNT_MAX);

        save();
    }

    public List<FootballBet> getAllBets(){
        ArrayList<FootballBet> all_bets = new ArrayList<FootballBet>();
        all_bets.addAll(score_bets);
        all_bets.addAll(score_bets_ht);
        all_bets.addAll(other_score_bets);
        all_bets.addAll(other_score_bets_ht);
        all_bets.addAll(result_bets);
        all_bets.addAll(result_bets_ht);
        all_bets.addAll(handicap_bets);
        all_bets.addAll(over_under_bets);
        return all_bets;
    }


    public static List<FootballBet> _getAllFootballBets(){
        FootballBetGenerator fbg = new FootballBetGenerator();
        return fbg.getAllBets();
    }

    public static List<Bet> _getAllBets(){
        FootballBetGenerator fbg = new FootballBetGenerator();
        return new ArrayList<Bet>(fbg.getAllBets());
    }


    public void save(){
        log.info("Saving football bets to file in resources.");
        JSONObject bet_cat = new JSONObject();
        for (FootballBet fb: getAllBets()){

            BetGroup current = (BetGroup) bet_cat.get(fb.category);
            if (current == null){
                current = new BetGroup();
                bet_cat.put(fb.category, current);
            }
            current.add(fb);
        }
        saveJSONResource(bet_cat, bets_filename);

        log.info("Saving football tautologies to file in resources.");
        saveJSONResource(BetGroup.list2JSON(getAllTautologies(), false), tauts_filename);
    }



    public ArrayList<FootballScoreBet> getScoreBets(int highest, Boolean halftime){
        ArrayList<FootballScoreBet> bets = new ArrayList<FootballScoreBet>();
        for (int a_score=0; a_score<=highest; a_score++){
            for (int b_score=0; b_score<=highest; b_score++){
                for (int i = 0; i< BetType.values().length ; i++){
                    BetType type = BetType.values()[i];

                    bets.add(new FootballScoreBet(type, a_score, b_score, halftime));
                }
            }
        }
        return bets;
    }


    public ArrayList<FootballOtherScoreBet> getOtherScoreBets(int lowest, int highest){
        ArrayList<FootballOtherScoreBet> bets = new ArrayList<FootballOtherScoreBet>();
        for (int score=lowest; score<=highest; score++){
            for (int i=0; i<FootballOtherScoreBet.RESULTS.length; i++){
                String result = FootballOtherScoreBet.RESULTS[i];
                for (int j=0; j<BetType.values().length; j++){
                    BetType type = BetType.values()[j];

                    FootballOtherScoreBet b = new FootballOtherScoreBet(type, score, result, false);
                    bets.add(b);
                }
            }
        }
        return bets;
    }


    public static ArrayList<FootballOtherScoreBet> getOtherScoreBetsHT(int lowest, int highest){
        ArrayList<FootballOtherScoreBet> bets = new ArrayList<>();
        for (int score=lowest; score<=highest; score++){
            for (int j=0; j<BetType.values().length; j++) {
                BetType type = BetType.values()[j];

                bets.add(new FootballOtherScoreBet(type, score, FootballBet.ANY, true));
            }
        }
        return bets;
    }


    public ArrayList<FootballResultBet> getResultBets(Boolean halftime){
        ArrayList<FootballResultBet> bets = new ArrayList<FootballResultBet>();
        for (int i=0; i<FootballResultBet.RESULTS.length; i++){
            String result = FootballResultBet.RESULTS[i];
            for (int j=0; j<BetType.values().length; j++) {
                BetType type = BetType.values()[j];

                bets.add(new FootballResultBet(type, result, halftime));
            }
        }
        return bets;
    }


    public static ArrayList<FootballHandicapBet> getHandicapBets(int highest){
        ArrayList<FootballHandicapBet> bets = new ArrayList<FootballHandicapBet>();

        BigDecimal smallest = new BigDecimal(-1*highest);
        BigDecimal largest = new BigDecimal(highest);
        BigDecimal half = new BigDecimal("0.5");

        for (BigDecimal handicap = smallest; handicap.compareTo(largest) != 1; handicap = handicap.add(half)){
            for (String result: FootballBet.RESULTS){

                if (result.equals(FootballBet.DRAW)
                        && handicap.remainder(half).compareTo(BigDecimal.ZERO) == 0
                        && handicap.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
                    continue;
                }

                for (BetType type: BetType.values()){
                    bets.add(new FootballHandicapBet(type, handicap, result));
                }
            }
        }
        return bets;
    }


    public ArrayList<FootballOverUnderBet> getOverUnderBets(int highest){
        ArrayList<FootballOverUnderBet> bets = new ArrayList<FootballOverUnderBet>();

        BigDecimal smallest = new BigDecimal("0.5");
        BigDecimal largest = (new BigDecimal(highest)).add(new BigDecimal("0.5"));

        for (BigDecimal goals = new BigDecimal("0.5"); goals.compareTo(largest) != 1; goals = goals.add(BigDecimal.ONE)){

            for (int i=0; i<FootballOverUnderBet.OVER_UNDER_ENUM.length; i++){
                String side = FootballOverUnderBet.OVER_UNDER_ENUM[i];
                for (int j = 0; j < BetType.values().length; j++) {
                    BetType type = BetType.values()[j];

                    bets.add(new FootballOverUnderBet(type, side, goals));
                }
            }

        }
        return bets;
    }


    // ------------------------------------------------------------------------


    public List<BetGroup> getAllTautologies(){

        if (allTautologies != null){
            return BetGroup.copyList(allTautologies);
        }

        ArrayList<Bet[]> tauts = new ArrayList<Bet[]>();

        tauts.addAll(tautsBackWithLay());
        tauts.addAll(tautsResultsWithScores());
        tauts.addAll(tautsResultsWithScoresHT());
        tauts.addAll(tautsScoresOtherGoalsHT());
        tauts.addAll(tautsTotalGoalsWithScores());
        tauts.addAll(tautsAllScores());
        tauts.addAll(tautTotalGoalsSandwich());

        tauts.addAll(getTwinSwaps(getTwins(), tauts));

        HashSet<String> hashes = new HashSet<String>();
        int duplicates = 0;
        for (int i=0; i<tauts.size(); i++){
            Bet[] taut = tauts.get(i);

            if (taut.length <= 1){
                print("SIZE LESS THAN 2");
                tauts.remove(i);
                i--;
                continue;
            }

            String[] ids = new String[taut.length];
            for (int j=0; j<taut.length; j++){
                ids[j] = taut[j].id();
            }
            Arrays.sort(ids);

            String hash = "";
            for (String id: ids){
                hash += id;
            }

            if (hashes.contains(hash)){
                duplicates++;
                tauts.remove(i);
                i--;
                continue;
            } else{
                hashes.add(hash);
            }
        }
        log.info(String.format("%d duplicate tautologies found.", duplicates));

        Bet[][] array = new Bet[tauts.size()][];
        ArrayList<BetGroup> tautologies = new ArrayList<BetGroup>();

        for (int i=0; i<tauts.size(); i++){
            tautologies.add(new BetGroup(new ArrayList<Bet>(Arrays.asList(tauts.get(i)))));
        }

        log.info(String.format("Returning %d generated tautologies.", array.length));

        allTautologies = tautologies;
        return tautologies;
    }


    public ArrayList<Bet[]> tautsBackWithLay(){
        ArrayList<Bet[]> tauts = new ArrayList<Bet[]>();
        List<FootballBet> all_bets = getAllBets();

        for (int i=0; i<all_bets.size(); i++){
            Bet bet = all_bets.get(i);

            if (bet.isBack()){
                String lay_id = bet.id().replace("BACK", "LAY");

                for (int j=0; j<all_bets.size(); j++) {
                    Bet check_bet = all_bets.get(j);

                    if (check_bet.id().equals(lay_id)) {
                        tauts.add(new Bet[]{bet, check_bet});
                    }
                }
            }
        }
        return tauts;
    }


    public ArrayList<Bet[]> tautsResultsWithScores(){
        ArrayList<Bet[]> tauts = new ArrayList<Bet[]>();

        for (int max_score=OTHERGOALS_MIN; max_score<=OTHERGOALS_MAX; max_score++){
            for (FootballResultBet rb: result_bets){

                ArrayList<Bet> taut = new ArrayList<Bet>();
                taut.add(rb);

                for (FootballOtherScoreBet osb: other_score_bets){
                    try {
                        if (osb.isBack() && osb.over_score == max_score && !(rb.overlap(osb))){
                            taut.add(osb);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                for (FootballScoreBet sb: score_bets){

                    if (sb.isBack() && sb.score_a <= max_score
                            && sb.score_b <= max_score && !(rb.possibleScore(sb))){

                        taut.add(sb);
                    }
                }
                tauts.add(toArray(taut));
            }
        }
        return tauts;
    }


    public ArrayList<Bet[]> tautsResultsWithScoresHT(){
        ArrayList<Bet[]> tauts = new ArrayList<Bet[]>();

        for (int max_score=OTHERGOALS_HT_MIN; max_score<=OTHERGOALS_HT_MAX; max_score++){
            for (FootballResultBet rb: result_bets_ht){

                ArrayList<Bet> taut = new ArrayList<Bet>();
                taut.add(rb);

                for (FootballOtherScoreBet osb: other_score_bets_ht){
                    if (osb.isBack() && osb.over_score == max_score){
                        taut.add(osb);
                    }
                }

                for (FootballScoreBet sb: score_bets_ht){
                    if (sb.isBack() && sb.score_a <= max_score
                            && sb.score_b <= max_score && !(rb.possibleScore(sb))){
                        taut.add(sb);
                    }
                }

                tauts.add(toArray(taut));
            }
        }
        return tauts;
    }


    public ArrayList<Bet[]> tautsScoresOtherGoalsHT(){
        ArrayList<Bet[]> tauts = new ArrayList<Bet[]>();

        for (int max_score=OTHERGOALS_HT_MIN; max_score<=OTHERGOALS_HT_MAX; max_score++){
            ArrayList<Bet> taut = new ArrayList<Bet>();

            for (FootballOtherScoreBet osb: other_score_bets_ht){
                if (osb.isBack() && osb.over_score == max_score){
                    taut.add(osb);
                }
            }
            for (FootballScoreBet sb: score_bets_ht){
                if (sb.isBack() && sb.score_a <= max_score && sb.score_b <= max_score){
                    taut.add(sb);
                }
            }
            tauts.add(toArray(taut));
        }
        return tauts;
    }


    public ArrayList<Bet[]> tautsTotalGoalsWithScores(){
        ArrayList<Bet[]> tauts = new ArrayList<Bet[]>();

        for (int max_score=OTHERGOALS_MIN; max_score<=OTHERGOALS_MAX; max_score++){
            for (FootballOverUnderBet oub: over_under_bets){
                if (oub.isLay() || oub.goals.compareTo(new BigDecimal(max_score)) == 1){
                    continue;
                }

                ArrayList<Bet> taut = new ArrayList<Bet>();
                taut.add(oub);

                if (oub.under()){
                    for (FootballOtherScoreBet osb: other_score_bets){
                        if (osb.isBack() && osb.over_score == max_score){
                            taut.add(osb);
                        }
                    }
                }

                for (FootballScoreBet sb: score_bets){
                    if (sb.isLay() || sb.score_a > max_score || sb.score_b > max_score){
                        continue;
                    }

                    if (oub.under() && (new BigDecimal(sb.total_goals())).compareTo(oub.goals) == 1){
                        taut.add(sb);
                    }
                    else if (oub.over() && (new BigDecimal(sb.total_goals())).compareTo(oub.goals) == -1){
                        taut.add(sb);
                    }
                }
                tauts.add(toArray(taut));
            }
        }
        return tauts;
    }


    public ArrayList<Bet[]> tautsAllScores(){
        ArrayList<Bet[]> tauts = new ArrayList<Bet[]>();

        for (int max_score=OTHERGOALS_MIN; max_score<=OTHERGOALS_MAX; max_score++){
            ArrayList<Bet> taut = new ArrayList<Bet>();

            for (FootballOtherScoreBet osb: other_score_bets){
                if (osb.isBack() && osb.over_score == max_score){
                    taut.add(osb);
                }
            }
            for (FootballScoreBet sb: score_bets){
                if (sb.isBack() && sb.score_a <= max_score && sb.score_b <= max_score){
                    taut.add(sb);
                }
            }
            tauts.add(toArray(taut));
        }
        return tauts;
    }


    public ArrayList<Bet[]> tautTotalGoalsSandwich(){
        ArrayList<Bet[]> tauts = new ArrayList<Bet[]>();

        ArrayList<FootballOverUnderBet> under_back_bets = new ArrayList<FootballOverUnderBet>();
        ArrayList<FootballOverUnderBet> over_back_bets = new ArrayList<FootballOverUnderBet>();
        for (FootballOverUnderBet oub: over_under_bets){
            if (oub.isBack()){
                if (oub.under()){
                    under_back_bets.add(oub);
                }
                else if (oub.over()){
                    over_back_bets.add(oub);
                }
            }
        }

        for (FootballOverUnderBet underbet: under_back_bets){
            for (FootballOverUnderBet overbet: over_back_bets){

                if (underbet.goals.compareTo(overbet.goals) != -1){
                    continue;
                }

                ArrayList<Bet> taut = new ArrayList<Bet>();
                taut.add(underbet);
                taut.add(overbet);

                for (FootballScoreBet sb: score_bets){
                    BigDecimal totalGoals = new BigDecimal(sb.total_goals());
                    if (sb.isBack() && underbet.goals.compareTo(totalGoals) == -1
                            && totalGoals.compareTo(overbet.goals) == -1){

                        taut.add(sb);
                    }
                }
                tauts.add(toArray(taut));
            }
        }
        return tauts;
    }


    public ArrayList<Bet[]> getTwins(){
        ArrayList<Bet[]> twins = new ArrayList<Bet[]>();
        for (FootballOverUnderBet oub: over_under_bets){
            if (oub.isLay()){
                continue;
            }

            if (oub.over()){
                String match_id = oub.id().replace(FootballOverUnderBet.OVER, FootballOverUnderBet.UNDER)
                                          .replace(String.valueOf(BetType.BACK), String.valueOf(BetType.LAY));

                for (FootballOverUnderBet twinbet: over_under_bets){
                    if (twinbet.id().equals(match_id)){
                        twins.add(new Bet[] {oub, twinbet});
                    }
                }
            }
            else if (oub.under()){
                String match_id = oub.id().replace(FootballOverUnderBet.UNDER,
                                                   FootballOverUnderBet.OVER)
                                          .replace(String.valueOf(BetType.BACK),
                                                   String.valueOf(BetType.LAY));
                for (FootballOverUnderBet twinbet: over_under_bets){
                    if (twinbet.id().equals(match_id)){
                        twins.add(new Bet[] {oub, twinbet});
                    }
                }
            }
        }
        return twins;
    }


    public ArrayList<Bet[]> getTwinSwaps(ArrayList<Bet[]> twins, ArrayList<Bet[]> tauts){
        ArrayList<Bet[]> new_tauts = new ArrayList<Bet[]>();

        for (Bet[] tautology: tauts){
            for (Bet[] pair: twins){

                ArrayList<Bet> new_taut_0 = removeIfContains(tautology, pair[0]);
                ArrayList<Bet> new_taut_1 = removeIfContains(tautology, pair[1]);
                if (new_taut_0.size() < tautology.length && new_taut_1.size() < tautology.length){
                    continue;
                }
                else if (new_taut_0.size() < tautology.length){
                    new_taut_0.add(pair[1]);
                    new_tauts.add(toArray(new_taut_0));
                }
                else if (new_taut_1.size() < tautology.length){
                    new_taut_1.add(pair[0]);
                    new_tauts.add(toArray(new_taut_1));
                }

            }
        }
        return new_tauts;
    }


    public ArrayList<Bet> removeIfContains(Bet[] array, Bet bet){
        ArrayList<Bet> newlist = new ArrayList<Bet>();
        for (Bet bet_inarray: array){
            if (!(bet_inarray.equals(bet))){
                newlist.add(bet_inarray);
            }
        }
        return newlist;
    }


    public Bet[] toArray(ArrayList<Bet> bet_list){
        Bet[] bets = new Bet[bet_list.size()];
        for (int i=0; i<bets.length; i++){
            bets[i] = bet_list.get(i);
        }
        return bets;
    }



}
