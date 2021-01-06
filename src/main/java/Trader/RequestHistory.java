package Trader;

import tools.printer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static tools.BigDecimalTools.secs_between;
import static tools.BigDecimalTools.sumBD;
import static tools.printer.print;
import static tools.printer.sf;

public class RequestHistory {

    public int REQUEST_LIMIT;
    public int REQUEST_LIMIT_WINDOW;

    List<RequestItem> history;


    public RequestHistory(int REQUEST_LIMIT, int REQUEST_LIMIT_WINDOW){
        this.REQUEST_LIMIT = REQUEST_LIMIT;
        this.REQUEST_LIMIT_WINDOW = REQUEST_LIMIT_WINDOW;
        history = new ArrayList<>();
    }

    public void addItem(int size){
        remove_old_reqs();
        history.add(new RequestItem(Instant.now(), size));
    }

    public void remove_old_reqs(){

        if (history.isEmpty()){
            return;
        }

        Instant window_start = windowStart();

        // Go through list, and break when index of first item not older than window is found.
        int i;
        for (i=0; i<history.size(); i++){
            if (!history.get(i).older_than(window_start)){
                break;
            }
        }

        // Delete all items that come before that index in the list
        history = history.subList(i, history.size());
    }

    public Instant windowStart(){
        return Instant.now().minusSeconds(REQUEST_LIMIT_WINDOW);
    }

    public int reqsWithinWindow(){
        Instant window_start = windowStart();

        int size = 0;
        for (int i=history.size()-1; i>=0; i--){
            RequestItem item = history.get(i);

            if (item.older_than(window_start)){
                break;
            }
            else{
                size += item.size;
            }
        }
        return size;
    }

    public int reqLimitUnused(){
        return REQUEST_LIMIT - reqsWithinWindow();
    }

    public void printHistory(){
        Instant now = Instant.now();
        StringBuilder s = new StringBuilder();
        s.append("{\n");
        for (RequestItem item: history){
            s.append(sf("%s secs ago, size: %s\n", secs_between(item.time, now), item.size));
        }
        s.append("}\n");
        print(s.toString());
    }


    public class RequestItem{

        public final Instant time;
        public final int size;

        public RequestItem(Instant time, int size) {
            this.time = time;
            this.size = size;
        }

        public boolean older_than(Instant oldest_age){
            return time.isBefore(oldest_age);
        }
    }


}
