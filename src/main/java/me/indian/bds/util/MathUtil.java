package me.indian.bds.util;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.util.Locale;

public final  class MathUtil {

    private static final DecimalFormat df = new DecimalFormat();

    static {
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
        df.setDecimalFormatSymbols(decimalFormatSymbols);
    }

    public static long minutesToMilliseconds(int minutes) {
        return Duration.ofMinutes(minutes).toMillis();
    }

    public static double format(final double decimal, final int format) {
        df.setMaximumFractionDigits(format);
        return Double.parseDouble(df.format(decimal));
    }

    public static long kilobytesToMb(long kilobytes) {
        return kilobytes / 1024;
    }

    public static long kilobytesToGb(long kilobytes) {
        return kilobytes / (1024 * 1024);
    }

    public static long kilobytesToBytes(long kilobytes) {
        return kilobytes * 1024;
    }

    public static long bytesToKB(final long bytes) {
        return bytes / 1024;
    }

    public static long bytesToMB(final long bytes) {
        return bytes / (1024 * 1024);
    }

    public static long bytesToGB(final long bytes) {
        return bytes / (1024 * 1024 * 1024);
    }
}
