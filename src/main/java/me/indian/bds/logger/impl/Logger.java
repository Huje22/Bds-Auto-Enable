package me.indian.bds.logger.impl;

import me.indian.bds.logger.AutoRestartLogger;
import me.indian.bds.logger.LogState;
import me.indian.bds.util.ConsoleColors;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.util.ZipUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger implements AutoRestartLogger {

    private File logFile;
    private String prefix;
    private LogState logState;
    private String date;
    private PrintStream printStream;

    public Logger() {
        logState = LogState.NONE;
        this.upDateDate();
        this.updatePrefix();
        this.initializeLogFile();
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

    private void initializeLogFile() {
        try {
            final File logsDir = new File("BDS-Auto-Enable/logs");
            if (!logsDir.exists()) {
                if (!logsDir.mkdir()) {
                    logsDir.mkdirs();
                }
            }
            logFile = new File(logsDir, "ServerLog-" + date.replaceAll(":", "-") + ".log");
            final FileOutputStream fileOutputStream = new FileOutputStream(logFile, true);
            this.printStream = new PrintStream(fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void alert(Object log) {
        logState = LogState.ALERT;
        this.logToFile(log);
        System.out.println(prefix + log);
    }

    @Override
    public void critical(Object log) {
        logState = LogState.CRITICAL;
        this.logToFile(log);
        System.out.println(prefix + log);
    }

    @Override
    public void error(Object log) {
        logState = LogState.ERROR;
        this.logToFile(log);
        System.out.println(prefix + log);
    }

    @Override
    public void warning(Object log) {
        logState = LogState.WARNING;
        this.logToFile(log);
        System.out.println(prefix + log);
    }

    @Override
    public void info(Object log) {
        logState = LogState.INFO;
        this.logToFile(log);
        System.out.println(prefix + log);
    }

    public void logToFile(Object log) {
        this.upDateDate();
        this.updatePrefix();
        if (this.printStream != null) {
            this.printStream.println(date + " [" + Thread.currentThread().getName() + "] " + logState + " " + log);
        }
    }

    public File getLogFile() {
        return this.logFile;
    }
}
