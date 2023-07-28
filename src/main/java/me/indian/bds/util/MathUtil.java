package me.indian.bds.util;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class MathUtil {

    private static final DecimalFormat df = new DecimalFormat();

    static {
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
        df.setDecimalFormatSymbols(decimalFormatSymbols);
    }

    public static int minutesToMilliseconds(int minutes) {
        return minutes * 60000;
    }

    public static double format(final double decimal, final int format) {
        df.setMaximumFractionDigits(format);
        return Double.parseDouble(df.format(decimal));
    }
}
