package me.indian.bds.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class DateUtil {

    public static String getFixedDate() {
        final LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter).replace(":", "-");
    }

    public static String getDate() {
        final LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

    public static String getTimeHM() {
        final LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return now.format(formatter);
    }

    public static String getTimeHMS() {
        final LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return now.format(formatter);
    }

    public static long localDateToLong(final LocalDate localDate) {
        return localDate.toEpochDay();
    }

    public static LocalDate longToLocalDate(final long days) {
        return LocalDate.ofEpochDay(days);
    }


    //TODO: Zwracać tylko long + w formatTime dodać boolean: days, hours, i resztę 
    
    public static String formatDays(final long millis) {
        final long totalSeconds = millis / 1000;
        final long totalMinutes = totalSeconds / 60;
        final long totalHours = totalMinutes / 60;
        final long days = totalHours / 24;

        return days + " dni";
    }

    public static String formatHours(final long millis) {
        final long totalSeconds = millis / 1000;
        final long totalMinutes = totalSeconds / 60;
        final long totalHours = totalMinutes / 60;
        final long hours = totalHours % 24;

        return hours + " godzin";
    }

    public static String formatMinutes(final long millis) {
        final long totalSeconds = millis / 1000;
        final long totalMinutes = totalSeconds / 60;
        final long minutes = totalMinutes % 60;

        return minutes + " minut";
    }

    public static String formatSeconds(final long millis) {
        final long totalSeconds = millis / 1000;
        final long seconds = totalSeconds % 60;

        return seconds + " sekund";
    }

    public static String formatTimeWithoutMillis(final long millis) {
        String formattedTime = "";

        formattedTime += formatDays(millis) + " ";
        formattedTime += formatHours(millis) + " ";
        formattedTime += formatMinutes(millis) + " ";
        formattedTime += formatSeconds(millis) + " ";

        return formattedTime;
    }

    public static String formatTime(final long millis) {
        String formattedTime = "";

        formattedTime += formatDays(millis) + " ";
        formattedTime += formatHours(millis) + " ";
        formattedTime += formatMinutes(millis) + " ";
        formattedTime += formatSeconds(millis) + " ";
        formattedTime += millis % 1000 + " milisekund";

        return formattedTime;
    }
}
