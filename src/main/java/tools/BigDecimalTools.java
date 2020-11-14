package tools;

import Bet.Bet;
import Trader.SportsTrader;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

import static tools.printer.*;

public abstract class BigDecimalTools {

    public static Logger log = SportsTrader.log;

    public static final BigDecimal penny = new BigDecimal("0.01");
    public static final BigDecimal half = new BigDecimal("0.5");
    public static final BigDecimal thousand = new BigDecimal(1000);


    public static BigDecimal randomBD(){
        int integer = randomInt(0, 999999);
        int decimal = randomInt(0, 999999);
        return new BigDecimal(String.format("%s.%s", integer, decimal));
    }


    public static List<BigDecimal> randomBDs(int n){
        List<BigDecimal> randoms = new ArrayList<>();
        for (int i=0; i<n; i++){
            randoms.add(randomBD());
        }
        return randoms;
    }


    public static BigDecimal sumBD(Collection<BigDecimal> items){
        return items.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal round(BigDecimal value, BigDecimal increment, RoundingMode roundingMode) {
        int signum = increment.signum();
        if (signum == -1){
            return null;
        }
        else if (signum == 0) {
            // 0 increment does not make much sense, but prevent division by 0
            return value;
        }
        else {
            BigDecimal divided = value.divide(increment, 0, roundingMode);
            BigDecimal result = divided.multiply(increment);
            return result;
        }
    }


    public static BigDecimal smallestStep(BigDecimal[] arr){
        BigDecimal smallest_step = null;
        for (int i=0; i<arr.length-1; i++){
            BigDecimal step_size = arr[i+1].subtract(arr[i]);
            smallest_step = BDMin(smallest_step, step_size);
        }
        return smallest_step;
    }


    public static Integer[] aboveBelowTargetValues(int[] values, BigDecimal exact_target){

        if (exact_target.compareTo(new BigDecimal(values[0])) < 0){
            return new Integer[] {null, values[0]};
        }

        int last_value = values[values.length-1];
        if (exact_target.compareTo(new BigDecimal(last_value)) > 0){
            return new Integer[] {last_value, null};
        }

        long rounded_down_price = exact_target.setScale(0, RoundingMode.DOWN).longValue();

        for (int i=0; i<values.length-1; i++){

            int below = values[i];
            int above = values[i+1];

            if (below <= rounded_down_price && rounded_down_price < above){
                if (new BigDecimal(below).compareTo(exact_target) == 0){
                    above = below;
                }
                return new Integer[] {below, above};
            }
        }

        if (new BigDecimal(last_value).compareTo(exact_target) == 0){
            return new Integer[] {last_value, last_value};
        }
        return null;
    }



    public static BigDecimal findClosest(BigDecimal[] sorted_array, BigDecimal target, RoundingMode roundingMode){

        int index_closest = findIndexClosest(sorted_array, target);
        BigDecimal closest = sorted_array[index_closest];

        int lower_index;
        int  upper_index;

        switch (closest.compareTo(target)){

            // Closest value is higher than target
            case 1:
                lower_index = index_closest - 1;
                upper_index = index_closest;
                break;

            // Closest value was lower than target
            case -1:
                lower_index = index_closest;
                upper_index = index_closest + 1;
                break;

            // Closest value is equal to target
            default:
                return closest;
        }


        if (lower_index < 0){
            return sorted_array[upper_index];
        }
        if (upper_index >= sorted_array.length){
            return sorted_array[lower_index];
        }

        BigDecimal lower_value = sorted_array[lower_index];
        BigDecimal upper_value = sorted_array[upper_index];

        return getClosest(lower_value, upper_value, target, roundingMode);
    }


    public static BigDecimal findClosest(BigDecimal[] arr, BigDecimal target){
        return arr[findIndexClosest(arr, target)];
    }


    public static int findIndexClosest(BigDecimal[] arr, BigDecimal target)
    {
        int n = arr.length;

        if (target.compareTo(arr[0]) <= 0) {
            return 0;
        }
        if (target.compareTo(arr[n-1]) >= 0) {
            return n-1;
        }


        int i = 0, j = n, mid = 0;
        while (i < j) {
            mid = (i + j) / 2;

            if (arr[mid].compareTo(target) == 0) {
                return mid;
            }

            if (target.compareTo(arr[mid]) < 0){

                if (mid > 0 && target.compareTo(arr[mid-1]) > 0){
                    if (getClosest(arr[mid-1], arr[mid], target).equals(arr[mid])){
                        return mid;
                    }
                    return mid-1;
                }

                j = mid;
            }

            else {
                if (mid < n-1 && target.compareTo(arr[mid-1]) < 0){
                    if (getClosest(arr[mid], arr[mid+1], target).equals(arr[mid])){
                        return mid;
                    }
                    return mid+1;
                }

                i = mid + 1;
            }
        }

        return mid;
    }

    public static BigDecimal getClosest(BigDecimal val1, BigDecimal val2, BigDecimal target)
    {
        return getClosest(val1, val2, target, RoundingMode.HALF_UP);
    }


    public static BigDecimal getClosest(BigDecimal val1, BigDecimal val2, BigDecimal target, RoundingMode roundingMode){

        BigDecimal lower_value = BDMin(val1, val2);
        BigDecimal upper_value = BDMax(val1, val2);

        if (roundingMode == RoundingMode.UP){
            return upper_value;
        }
        if (roundingMode == RoundingMode.DOWN){
            return lower_value;
        }

        BigDecimal dist_below = target.subtract(lower_value).abs();
        BigDecimal dist_above = target.subtract(upper_value).abs();

        if (dist_below.compareTo(dist_above) < 0){
            return lower_value;
        }
        if (dist_above.compareTo(dist_below) < 0){
            return upper_value;
        }

        // If gotten this far, then value is equa-distant, return based on rounding mode.
        if (roundingMode == RoundingMode.HALF_DOWN){
            return lower_value;
        }
        if (roundingMode == RoundingMode.HALF_UP){
            return upper_value;
        }

        log.severe("Invalid rounding mode for getClosest - " + stringValue(roundingMode));
        return null;
    }

    public static BigDecimal BDMax(BigDecimal a, BigDecimal b){
        if (a == null){
            return b;
        }
        if (b == null){
            return a;
        }
        return a.max(b);
    }

    public static BigDecimal BDMin(BigDecimal a, BigDecimal b){
        if (a == null){
            return b;
        }
        if (b == null){
            return a;
        }
        return a.min(b);
    }

    public static String BDString(BigDecimal input){
        return BDString(input, null);
    }

    public static String BDString(BigDecimal input, Integer scale){
        if (input == null){
            return "null";
        }
        if (scale != null){
            input = input.setScale(scale, RoundingMode.HALF_UP);
        }
        return input.stripTrailingZeros().toPlainString();
    }


    public static Map<String, BigDecimal> combine_map(Map<String, BigDecimal> a, Map<String, BigDecimal> b){
        Map<String, BigDecimal> combined = new HashMap<>();
        combined.putAll(a);
        for (Map.Entry<String, BigDecimal> entry: b.entrySet()){
            combined.put(entry.getKey(), combined.getOrDefault(entry.getKey(), BigDecimal.ZERO).add(entry.getValue()));
        }
        return combined;
    }

    public static boolean BDInteger(BigDecimal value){
        return value.stripTrailingZeros().scale() <= 0;
    }


    public static BigDecimal BD(String bd){
        return new BigDecimal(bd);
    }

    public static BigDecimal BD(int bd){
        return new BigDecimal(bd);
    }

    public static BigDecimal BD(double bd){
        return new BigDecimal(bd);
    }


    public static boolean isInteger(BigDecimal bd){
        return bd.signum() == 0 || bd.scale() <= 0 || bd.stripTrailingZeros().scale() <= 0;
    }


    public static BigDecimal secs(Instant from, Instant too){
        long millis = too.toEpochMilli() - from.toEpochMilli();
        return BD(millis).divide(thousand, 3, RoundingMode.HALF_UP);
    }

    public static BigDecimal secs_until(Instant until){
        return secs(Instant.now(), until);
    }

    public static BigDecimal secs_since(Instant from){
        return secs(from, Instant.now());
    }


    public static void main(String[] args){

        Map<String, BigDecimal> a = new HashMap<>();
        a.put("sam", new BigDecimal(34));
        a.put("sam1", new BigDecimal(1));
        a.put("sam4", new BigDecimal(4));
        a.put("sam5", new BigDecimal(6));

        Map<String, BigDecimal> b = new HashMap<>();
        b.put("sam", new BigDecimal(12));
        b.put("sam2", new BigDecimal(8));
        b.put("sam3", new BigDecimal(10));
        b.put("sam5", new BigDecimal(2));


        print(combine_map(a,b));


    }


}
