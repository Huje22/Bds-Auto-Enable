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

    public static String formatTime(final long millis) {
        final long totalSeconds = millis / 1000;
        final long milliseconds = millis % 1000;

        final long totalMinutes = totalSeconds / 60;
        final long seconds = totalSeconds % 60;

        final long totalHours = totalMinutes / 60;
        final long minutes = totalMinutes % 60;

        final long hours = totalHours;

        return hours + " godzin " + minutes + " minuty " + seconds + " sekund " + milliseconds + " milisekund";
    }

    public static double bytesToKb(final long bytes) {
        return format(((double) bytes / 1024), 2);
    }

    public static long bytesToMB(long bytes) {
        return bytes / (1024 * 1024);
    }
}
