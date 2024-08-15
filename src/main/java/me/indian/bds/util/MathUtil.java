package me.indian.bds.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class MathUtil {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat();

    static {
        final DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
        DECIMAL_FORMAT.setDecimalFormatSymbols(decimalFormatSymbols);
    }

    private MathUtil() {
    }

    // Utility methods for clamping numbers
    public static int getCorrectNumber(final int number, final int min, final int max) {
        return Math.max(min, Math.min(max, number));
    }

    public static double getCorrectNumber(final double number, final double min, final double max) {
        return Math.max(min, Math.min(max, number));
    }


    // Number formatting
    public static double format(final double decimal, final int format) {
        DECIMAL_FORMAT.setMaximumFractionDigits(format);
        return Double.parseDouble(DECIMAL_FORMAT.format(decimal));
    }

    // Data conversion methods
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

    // Methods to extract remaining units
    public static long getRemainingGbFromBytes(final long bytes) {
        return (bytes % (1024L * 1024 * 1024 * 1024)) / (1024 * 1024 * 1024);
    }

    public static long getRemainingMbFromBytes(final long bytes) {
        return (bytes % (1024 * 1024 * 1024)) / (1024 * 1024);
    }

    public static long getRemainingKbFromBytes(final long bytes) {
        return (bytes % (1024 * 1024)) / 1024;
    }

    public static long getRemainingGbFromKb(final long kb) {
        return (kb % (1024L * 1024 * 1024)) / (1024 * 1024);
    }

    public static long getRemainingMbFromKb(final long kb) {
        return (kb % (1024 * 1024)) / 1024;
    }

    public static long getBytesFromKb(final long kb) {
        return kb * 1024;
    }

    // Formatting methods
    public static String formatKiloBytesDynamic(final long kilobytes, final boolean shortNames) {
        if (kilobytes == 0) return "N/A";

        final List<Character> unitsPattern = new ArrayList<>();
        final long gb = kilobytesToGb(kilobytes);
        final long mb = getRemainingMbFromKb(kilobytes);

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
        final long gb = bytesToGB(bytes);
        final long mb = getRemainingMbFromBytes(bytes);
        final long kb = getRemainingKbFromBytes(bytes);

        if (gb > 0) unitsPattern.add('g');
        if (mb > 0) {
            unitsPattern.add('m');
        } else {
            if (kb > 0) {
                unitsPattern.add('k');
            } else {
                unitsPattern.add('b');
            }
        }

        return formatBytes(bytes, unitsPattern, shortNames);
    }

    public static String formatKilobytes(final long kilobytes, final List<Character> unitsPattern, final boolean shortNames) {
        final StringBuilder formattedKilobytes = new StringBuilder();
        final Map<Character, String> unitMap = getUnitKilobytesMap(kilobytes, shortNames);

        for (final char unit : unitsPattern) {
            if (unitMap.containsKey(unit)) {
                formattedKilobytes.append(unitMap.get(unit)).append(" ");
            }
        }

        return formattedKilobytes.toString().trim();
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

    // Helper methods to map units to strings
    private static Map<Character, String> getUnitBytesMap(final long bytes, final boolean shortNames) {
        final Map<Character, String> UNIT_MAP = new HashMap<>();

        if (shortNames) {
            UNIT_MAP.put('k', getRemainingKbFromBytes(bytes) + " KB");
            UNIT_MAP.put('m', getRemainingMbFromBytes(bytes) + " MB");
            UNIT_MAP.put('g', getRemainingGbFromBytes(bytes) + " GB");
        } else {
            UNIT_MAP.put('k', getRemainingKbFromBytes(bytes) + " kilobajtów");
            UNIT_MAP.put('m', getRemainingMbFromBytes(bytes) + " megabajtów");
            UNIT_MAP.put('g', getRemainingGbFromBytes(bytes) + " gigabajtów");
        }

        return UNIT_MAP;
    }

    private static Map<Character, String> getUnitKilobytesMap(final long kilobytes, final boolean shortNames) {
        final Map<Character, String> UNIT_MAP = new HashMap<>();

        if (shortNames) {
            UNIT_MAP.put('k', kilobytes + " KB");
            UNIT_MAP.put('m', getRemainingMbFromKb(kilobytes) + " MB");
            UNIT_MAP.put('g', getRemainingGbFromKb(kilobytes) + " GB");
        } else {
            UNIT_MAP.put('k', kilobytes + " kilobajtów");
            UNIT_MAP.put('m', getRemainingMbFromKb(kilobytes) + " megabajtów");
            UNIT_MAP.put('g', getRemainingGbFromKb(kilobytes) + " gigabajtów");
        }

        return UNIT_MAP;
    }
}