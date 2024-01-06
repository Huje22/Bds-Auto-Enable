package me.indian.bds.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class DateUtil {

    public static final ZoneId POLISH_ZONE = ZoneId.of("Europe/Warsaw");

    private DateUtil() {
    }

    public static String getFixedDate() {
        final LocalDateTime now = LocalDateTime.now(POLISH_ZONE);
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter).replace(":", "-");
    }

    public static String getDate() {
        final LocalDateTime now = LocalDateTime.now(POLISH_ZONE);
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

    public static String getTimeHM() {
        final LocalDateTime now = LocalDateTime.now(POLISH_ZONE);
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return now.format(formatter);
    }

    public static String getTimeHMS() {
        final LocalDateTime now = LocalDateTime.now(POLISH_ZONE);
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return now.format(formatter);
    }

    public static long localDateToLong(final LocalDate localDate) {
        return localDate.toEpochDay();
    }

    public static LocalDate longToLocalDate(final long days) {
        return LocalDate.ofEpochDay(days);
    }

    public static long formatDays(final long millis) {
        final long totalSeconds = millis / 1000;
        final long totalMinutes = totalSeconds / 60;
        final long totalHours = totalMinutes / 60;

        return totalHours / 24;
    }

    public static long formatHours(final long millis) {
        final long totalSeconds = millis / 1000;
        final long totalMinutes = totalSeconds / 60;
        final long totalHours = totalMinutes / 60;

        return totalHours % 24;
    }

    public static long formatMinutes(final long millis) {
        final long totalSeconds = millis / 1000;
        final long totalMinutes = totalSeconds / 60;

        return totalMinutes % 60;
    }

    public static long formatSeconds(final long millis) {
        return (millis / 1000) % 60;
    }

    public static String formatTime(final long millis, final String times) {
        String formattedTime = "";

        if (times.toLowerCase().contains("days".toLowerCase())) formattedTime += formatDays(millis) + " dni ";
        if (times.toLowerCase().contains("hours".toLowerCase())) formattedTime += formatHours(millis) + " godzin ";
        if (times.toLowerCase().contains("minutes".toLowerCase())) formattedTime += formatMinutes(millis) + " minut ";
        if (times.toLowerCase().contains("seconds".toLowerCase())) formattedTime += formatSeconds(millis) + " sekund ";
        if (times.toLowerCase().contains("millis".toLowerCase())) formattedTime += millis % 1000 + " milisekund";

        return formattedTime;
    }
}
