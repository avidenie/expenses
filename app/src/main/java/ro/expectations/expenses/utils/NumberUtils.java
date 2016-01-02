package ro.expectations.expenses.utils;

public class NumberUtils {

    public static double roundToTwoPlaces(double d) {
        return ((long) (d < 0 ? d * 100 - 0.5 : d * 100 + 0.5)) / 100.0;
    }
}
