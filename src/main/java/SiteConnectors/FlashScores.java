package SiteConnectors;

import Sport.FootballEventState;
import Trader.SportsTrader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import tools.Requester;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.logging.Logger;

import static tools.printer.print;
import static tools.printer.toFile;

public class FlashScores {

    public static final Logger log = Logger.getLogger(SportsTrader.class.getName());

    Requester requester;

    public FlashScores(){
        requester = new Requester();

    }


    public void getEvent(String id) throws InterruptedException, IOException, URISyntaxException {
        String url = String.format("https://www.flashscore.co.uk/match/%s/#match-summary", id);

        String response = requester.getRaw(url);
    }

    public FootballEventState getFootballState(String match_id) throws InterruptedException, IOException, URISyntaxException {
        String url = String.format("https://www.flashscore.co.uk/match/%s/#match-summary", match_id);
        String response = requester.getRaw(url);

        toFile(response, "inplay");

        Document doc = Jsoup.parse(response);
        Element result = doc.getElementById("event_detail_current_result");
        Elements scores = result.getElementsByClass("scoreboard");

        if (scores.size() != 2){
            log.severe(String.format("Something other than 2 scores found when searching for score in flashscores." +
                    "\n%s", scores.toString()));
        }


        FootballEventState fes = new FootballEventState();
        fes.score_a = Integer.valueOf(scores.get(0).text());
        fes.score_b = Integer.valueOf(scores.get(1).text());

        return fes;

    }


    public static void main(String[] args){
        FlashScores fs = new FlashScores();
        try {

            FootballEventState fes = fs.getFootballState("zq6Vkuie");



        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
