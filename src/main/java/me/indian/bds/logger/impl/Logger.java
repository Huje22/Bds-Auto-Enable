package me.indian.bds.logger.impl;


import me.indian.bds.logger.AutoRestartLogger;
import me.indian.bds.logger.LogState;
import me.indian.bds.util.ConsoleColors;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger implements AutoRestartLogger {
    private String prefix;
    private LogState logState;
    private String date;

    public Logger() {
        logState = LogState.NONE;
        upDateDate();
        updatePrefix();
    }

    private void upDateDate() {
        final LocalDateTime now = LocalDateTime.now();
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        date = now.format(formatter) + " ";
    }

    private void updatePrefix() {
        String logStateColor = logState.getColorCode();
        prefix = ConsoleColors.DARK_GRAY + date + ConsoleColors.BRIGHT_GREEN + "BDS " + ConsoleColors.BRIGHT_BLUE + "Auto Enabled " +
                ConsoleColors.BRIGHT_GREEN + "[" + ConsoleColors.BRIGHT_GRAY +
                Thread.currentThread().getName()
                + ConsoleColors.BRIGHT_GREEN + "]" +
                " " + logStateColor + logState.name().toUpperCase()
                + ConsoleColors.RESET + " ";
    }

    public void alert(Object log) {
        logState = LogState.ALERT;
        updatePrefix();
        System.out.println(prefix + log);
    }

    public void critical(Object log) {
        logState = LogState.CRITICAL;
        updatePrefix();
        System.out.println(prefix + log);
    }

    public void error(Object log) {
        logState = LogState.ERROR;
        updatePrefix();
        System.out.println(prefix + log);
    }

    public void warning(Object log) {
        logState = LogState.WARNING;
        updatePrefix();
        System.out.println(prefix + log);
    }

    public void info(Object log) {
        logState = LogState.INFO;
        updatePrefix();
        System.out.println(prefix + log);
    }
}
