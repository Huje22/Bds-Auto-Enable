package me.indian.bds.logger;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.Config;
import me.indian.bds.util.ConsoleColors;
import me.indian.bds.util.DateUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class Logger {

    private final BDSAutoEnable bdsAutoEnable;
    private final Config config;
    private File logFile;
    private String prefix;
    private LogState logState;
    private PrintStream printStream;

    public Logger(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.config = this.bdsAutoEnable.getConfig();
        this.logState = LogState.NONE;
        this.updatePrefix();
        this.initializeLogFile();
    }

    private void updatePrefix() {
        final String logStateColor = this.logState.getColorCode();
        this.prefix = ConsoleColors.DARK_GRAY + DateUtil.getDate() + ConsoleColors.BRIGHT_GREEN + " BDS " + ConsoleColors.BRIGHT_BLUE + "Auto Enabled " +
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
                if (!logsDir.mkdir()) logsDir.mkdirs();
            }
            this.logFile = new File(logsDir, "ServerLog-" + this.bdsAutoEnable.getRunDate() + ".log");
            final FileOutputStream fileOutputStream = new FileOutputStream(logFile, true);
            this.printStream = new PrintStream(fileOutputStream);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void alert(final Object log) {
        this.logState = LogState.ALERT;
        this.logToFile(log);
        System.out.println(prefix + log);
    }

    public void critical(final Object log) {
        this.logState = LogState.CRITICAL;
        this.logToFile(log);
        System.out.println(prefix + log);
    }

    public void error(final Object log) {
        this.logState = LogState.ERROR;
        this.logToFile(log);
        System.out.println(prefix + log);
    }

    public void warning(final Object log) {
        this.logState = LogState.WARNING;
        this.logToFile(log);
        System.out.println(prefix + log);
    }

    public void info(final Object log) {
        this.logState = LogState.INFO;
        this.logToFile(log);
        System.out.println(prefix + log);
    }

    public void debug(final Object log) {
        if (config.isDebug()) {
            this.logState = LogState.DEBUG;
            this.logToFile(log);
            System.out.println(prefix + log);
        }
    }

    public void instantLogToFile(final Object log) {
        if (this.printStream != null) {
            this.printStream.println(log);
        }
    }

    public void logToFile(final Object log) {
        this.updatePrefix();
        if (this.printStream != null) {
            this.printStream.println(DateUtil.getDate() + " [" + Thread.currentThread().getName() + "] " + this.logState + " " + log);
        }
    }

    public File getLogFile() {
        return this.logFile;
    }
}
