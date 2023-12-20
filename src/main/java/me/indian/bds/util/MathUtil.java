package me.indian.bds.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class MathUtil {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat();

    private MathUtil() {
    }

    static {
        final DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
        DECIMAL_FORMAT.setDecimalFormatSymbols(decimalFormatSymbols);
    }

    public static int getCorrectNumber(final int number, final int mini, final int max) {
        return Math.max(mini, Math.min(max, number));
    }

    public static double getCorrectNumber(final double number, final double min, final double max) {
        return Math.max(min, Math.min(max, number));
    }

    public static long daysFrom(final long time, final TimeUnit sourceUnit) {
        return sourceUnit.toDays(time);
    }

    public static long daysTo(final long time, final TimeUnit targetUnit) {
        return targetUnit.convert(time, TimeUnit.DAYS);
    }

    public static long hoursFrom(final long time, final TimeUnit sourceUnit) {
        return sourceUnit.toHours(time);
    }

    public static long hoursTo(final long time, final TimeUnit targetUnit) {
        return targetUnit.convert(time, TimeUnit.HOURS);
    }

    public static long minutesFrom(final long time, final TimeUnit sourceUnit) {
        return sourceUnit.toMinutes(time);
    }

    public static long minutesTo(final long time, final TimeUnit targetUnit) {
        return targetUnit.convert(time, TimeUnit.MINUTES);
    }

    public static long millisTo(final long time, final TimeUnit targetUnit) {
        return targetUnit.convert(time, TimeUnit.MILLISECONDS);
    }

    public static long millisFrom(final long time, final TimeUnit sourceUnit) {
        return sourceUnit.toMillis(time);
    }

    public static long secondToMillis(final long seconds) {
        return Duration.ofSeconds(seconds).toMillis();
    }

    public static double format(final double decimal, final int format) {
        DECIMAL_FORMAT.setMaximumFractionDigits(format);
        return Double.parseDouble(DECIMAL_FORMAT.format(decimal));
    }

    public static long kilobytesToMb(final long kilobytes) {
        return kilobytes / 1024;
    }

    public static long kilobytesToGb(final long kilobytes) {
        return kilobytes / (1024 * 1024);
    }

    public static long kilobytesToBytes(final long kilobytes) {
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

    public static long getMbFromBytesGb(final long bytes) {
        final long gb = bytesToGB(bytes);
        return bytesToMB(bytes - (gb * 1024 * 1024 * 1024));
    }

    public static long getMbFromKilobytesGb(final long kilobytes) {
        final long gb = kilobytesToGb(kilobytes);
        return kilobytesToMb(kilobytes - (gb * 1024 * 1024));
    }

    public static long getKbFromBytesGb(final long bytes) {
        return (bytes % (1024 * 1024)) / 1024;
    }
}