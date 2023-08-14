package me.indian.bds.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    public static String getFixedDate() {
        final LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Warsaw"));
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter).replace(":", "-");
    }

    public static String getDate() {
        final LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Warsaw"));
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

    public static long localDateToLong(final LocalDate localDate) {
        return localDate.toEpochDay();
    }


    public static LocalDate longToLocalDate(final long days) {
        return LocalDate.ofEpochDay(days);
    }
}
