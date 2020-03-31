package Sport;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Horse {

    public int number;
    public String name;
    public String odds;

    public static final Pattern odds_regex = Pattern.compile("^\\d++/\\d++$");

    public Horse(String name, String odds){
        this.name = name;
        this.odds = odds;
    }

    public BigDecimal decimal_odds(){
        if (!odds_regex.matcher(odds).matches()){
            return null;
        }

        String[] parts = odds.split("/");
        BigDecimal numerator = new BigDecimal(parts[0]);
        BigDecimal denominator = new BigDecimal(parts[1]);

        BigDecimal decimal = numerator.divide(denominator, 3, RoundingMode.HALF_UP).add(BigDecimal.ONE);
        return decimal;
    }

    public String toString(){
        return name + " " + odds + " (" + decimal_odds().toString() + ")";
    }
}
