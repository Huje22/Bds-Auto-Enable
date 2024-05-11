package me.indian.bds.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class MathUtil {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat();

    static {
        final DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
        DECIMAL_FORMAT.setDecimalFormatSymbols(decimalFormatSymbols);
    }

    private MathUtil() {
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

    public static long getGBfromKilobytes(final long kilobytes) {
        final long bytesInGB = 1073741824L; // 1 gigabajt = 1024 * 1024 * 1024 bajtów
        final long gigabytes = kilobytes / bytesInGB;
        return gigabytes;
    }


    public static long getGbFromBytes(final long bytes) {
        return bytes / (1024 * 1024 * 1024);
    }

    public static long getMbFromBytes(final long bytes) {
        return (bytes % (1024 * 1024 * 1024)) / (1024 * 1024);
    }

    public static long getKbFromBytes(final long bytes) {
        return ((bytes % (1024 * 1024 * 1024)) % (1024 * 1024)) / 1024;
    }

    public static long getGbFromKb(final long kb) {
        return kb / (1024 * 1024);
    }

    public static long getMbFromKb(final long kb) {
        return (kb % (1024 * 1024)) / 1024;
    }

    public static long getBytesFromKb(final long kb) {
        return kb * 1024;
    }


    public static String formatKiloBytesDynamic(final long kilobytes, final boolean shortNames) {
        if (kilobytes == 0) return "N/A";

        final List<Character> unitsPattern = new ArrayList<>();
        final long gb = getGbFromKb(kilobytes);
        final long mb = getMbFromKb(kilobytes);

        if (gb > 0) unitsPattern.add('g');
        if (mb > 0) {
            unitsPattern.add('m');
        } else {
            unitsPattern.add('k');
        }

        return formatKilobytes(kilobytes, unitsPattern, shortNames);
    }

    public static String formatBytesDynamic(final long bytes, final boolean shortNames) {
        if (bytes == 0) return "N/A";

        final List<Character> unitsPattern = new ArrayList<>();
        final long gigabytes = getGbFromBytes(bytes);
        final long megabytes = getMbFromBytes(bytes);
        final long kilobytes = getKbFromBytes(bytes);

        if (gigabytes > 0) unitsPattern.add('g');
        if (megabytes > 0) {
            unitsPattern.add('m');
        } else {
            if (kilobytes > 0) {
                unitsPattern.add('k');
            } else {
                unitsPattern.add('b');
            }
        }

        return formatBytes(bytes, unitsPattern, shortNames);
    }

    public static String formatKilobytes(final long millis, final List<Character> unitsPattern, final boolean shortNames) {
        final StringBuilder formattedTime = new StringBuilder();
        final Map<Character, String> unitMap = getUnitKilobytesMap(millis, shortNames);

        for (final char unit : unitsPattern) {
            if (unitMap.containsKey(unit)) {
                formattedTime.append(unitMap.get(unit)).append(" ");
            }
        }

        return formattedTime.toString().trim();
    }

    public static String formatBytes(final long bytes, final List<Character> unitsPattern, final boolean shortNames) {
        final StringBuilder formattedBytes = new StringBuilder();
        final Map<Character, String> unitMap = getUnitBytesMap(bytes, shortNames);

        for (final char unit : unitsPattern) {
            if (unitMap.containsKey(unit)) {
                formattedBytes.append(unitMap.get(unit)).append(" ");
            }
        }

        return formattedBytes.toString().trim();
    }

    private static Map<Character, String> getUnitBytesMap(final long bytes, final boolean shortNames) {
        final Map<Character, String> UNIT_MAP = new HashMap<>();

        if (shortNames) {
            UNIT_MAP.put('k', getKbFromBytes(bytes) + " KB");
            UNIT_MAP.put('m', getMbFromBytes(bytes) + " MB");
            UNIT_MAP.put('g', getGbFromBytes(bytes) + " GB");
        } else {
            UNIT_MAP.put('k', getKbFromBytes(bytes) + " kilobajtów");
            UNIT_MAP.put('m', getMbFromBytes(bytes) + " megabajtów");
            UNIT_MAP.put('g', getGbFromBytes(bytes) + " gigabajtów");
        }

        return UNIT_MAP;
    }

    private static Map<Character, String> getUnitKilobytesMap(final long kilobytes, final boolean shortNames) {
        final Map<Character, String> UNIT_MAP = new HashMap<>();

        if (shortNames) {
            UNIT_MAP.put('k', kilobytes + " KB");
            UNIT_MAP.put('m', getMbFromKb(kilobytes) + " MB");
            UNIT_MAP.put('g', getGbFromKb(kilobytes) + " GB");
        } else {
            UNIT_MAP.put('k', kilobytes + " kilobajtów");
            UNIT_MAP.put('m', getMbFromKb(kilobytes) + " megabajtów");
            UNIT_MAP.put('g', getGbFromKb(kilobytes) + " gigabajtów");
        }

        return UNIT_MAP;
    }
}