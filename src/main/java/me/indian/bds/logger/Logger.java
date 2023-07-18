package me.indian.bds.logger;

import me.indian.bds.config.Config;
import me.indian.bds.util.ConsoleColors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    private final Config config;
    private File logFile;
    private String prefix;
    private LogState logState;
    private String date;
    private PrintStream printStream;

    public Logger(final Config config) {
        this.config = config;
        this.logState = LogState.NONE;
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
        final String logStateColor = this.logState.getColorCode();
        prefix = ConsoleColors.DARK_GRAY + date + ConsoleColors.BRIGHT_GREEN + "BDS " + ConsoleColors.BRIGHT_BLUE + "Auto Enabled " +
                ConsoleColors.BRIGHT_GREEN + "[" + ConsoleColors.BRIGHT_GRAY +
                Thread.currentThread().getName()
                + ConsoleColors.BRIGHT_GREEN + "]" +
                " " + logStateColor + this.logState.name().toUpperCase()
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


    public void alert(Object log) {
        this.logState = LogState.ALERT;
        this.logToFile(log);
        System.out.println(prefix + log);
    }

    public void critical(Object log) {
        this.logState = LogState.CRITICAL;
        this.logToFile(log);
        System.out.println(prefix + log);
    }

    public void error(Object log) {
        this.logState = LogState.ERROR;
        this.logToFile(log);
        System.out.println(prefix + log);
    }

    public void warning(Object log) {
        this.logState = LogState.WARNING;
        this.logToFile(log);
        System.out.println(prefix + log);
    }

    public void info(Object log) {
        this.logState = LogState.INFO;
        this.logToFile(log);
        System.out.println(prefix + log);
    }

    public void debug(Object log) {
        if (config.isDebug()) {
            this.logState = LogState.DEBUG;
            this.logToFile(log);
            System.out.println(prefix + log);
        }
    }

    public void logToFile(Object log) {
        this.upDateDate();
        this.updatePrefix();
        if (this.printStream != null) {
            this.printStream.println(date + " [" + Thread.currentThread().getName() + "] " + this.logState + " " + log);
        }
    }

    public File getLogFile() {
        return this.logFile;
    }
}
