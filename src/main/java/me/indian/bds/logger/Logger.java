package me.indian.bds.logger;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.Defaults;
import me.indian.bds.config.Config;
import me.indian.bds.discord.DiscordIntegration;
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
        this.prefix = "&8" + DateUtil.getDate() + "&a BDS&1 Auto Enable &a[&7" +
                Thread.currentThread().getName() + "&r&a] "
                + logStateColor + this.logState.name().toUpperCase() + " &r";
    }

    private void initializeLogFile() {
        try {
            final File logsDir = new File(Defaults.getAppDir() + "logs");
            if (!logsDir.exists()) {
                if (!logsDir.mkdir()) logsDir.mkdirs();
            }
            this.logFile = new File(logsDir, "ServerLog-" + this.bdsAutoEnable.getRunDate() + ".log");
            final FileOutputStream fileOutputStream = new FileOutputStream(this.logFile, true);
            this.printStream = new PrintStream(fileOutputStream);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void print(final Object log) {
         this.logState = LogState.NONE;
        if (this.printStream != null) {
            this.printStream.println(ConsoleColors.removeColors(log));
        }
        
        System.out.println(ConsoleColors.convertMinecraftColors(log));
    }
    
     public void print(final Object log, final DiscordIntegration discord) {
         this.print(log);
         if (discord == null) {
             throw new RuntimeException("Integracja z discord podana w logerze jest null");
         }
         discord.writeConsole(log.toString());
     }
    
    public void print(final Object log, final Throwable throwable) {
         this.print(log);
         this.logThrowableToFile(throwable);
        }
    
    public void print(final Object log, final Throwable throwable, final DiscordIntegration discord) {
        this.print(log);
        discord.writeConsole(log.toString(), throwable);
    }

    public void info(final Object log) {
        this.logState = LogState.INFO;
        this.updatePrefix();
        System.out.println(ConsoleColors.convertMinecraftColors(this.prefix + log));
        this.logToFile(log);
    }

    public void info(final Object log, final Throwable throwable) {
        this.info(log);
        this.logThrowableToFile(throwable);
    }

    public void warning(final Object log) {
        this.logState = LogState.WARNING;
        this.updatePrefix();
        System.out.println(ConsoleColors.convertMinecraftColors(this.prefix + log));
        this.logToFile(log);
    }

    public void warning(final Object log, final Throwable throwable) {
        this.warning(log);
        this.logThrowableToFile(throwable);
    }

    public void alert(final Object log) {
        this.logState = LogState.ALERT;
        this.updatePrefix();
        System.out.println(ConsoleColors.convertMinecraftColors(this.prefix + log));
        this.logToFile(log);
    }

    public void alert(final Object log, final Throwable throwable) {
        this.alert(log);
        this.logThrowableToFile(throwable);
    }

    public void critical(final Object log) {
        this.logState = LogState.CRITICAL;
        this.updatePrefix();
        System.out.println(ConsoleColors.convertMinecraftColors(this.prefix + log));
        this.logToFile(log);
    }

    public void critical(final Object log, final Throwable throwable) {
        this.critical(log);
        this.logThrowableToFile(throwable);
    }

    public void error(final Object log) {
        this.logState = LogState.ERROR;
        this.updatePrefix();
        System.out.println(ConsoleColors.convertMinecraftColors(this.prefix + log));
        this.logToFile(log);
    }

    public void error(final Object log, final Throwable throwable) {
        this.error(log);
        this.logThrowableToFile(throwable);
    }

    public void debug(final Object log) {
        if (this.config.isDebug()) {
            this.logState = LogState.DEBUG;
            this.updatePrefix();
            this.logToFile(log);
            System.out.println(ConsoleColors.convertMinecraftColors(this.prefix + log));
        }
    }

    public void debug(final Object log, final Throwable throwable) {
        if (this.config.isDebug()) {
            this.debug(log);
            this.logThrowableToFile(throwable);
        }
    }

    public void logByState(final Object log, final LogState logState) {
        switch (logState) {
            case NONE -> this.print(log);
            case INFO -> this.info(log);
            case ALERT -> this.alert(log);
            case CRITICAL -> this.critical(log);
            case ERROR -> this.error(log);
            case WARNING -> this.warning(log);
            case DEBUG -> this.debug(log);
        }
    }

    public void logByState(final Object log, final Throwable throwable, final LogState logState) {
        switch (logState) {
            case NONE -> this.print(log, throwable);
            case INFO -> this.info(log, throwable);
            case ALERT -> this.alert(log, throwable);
            case CRITICAL -> this.critical(log, throwable);
            case ERROR -> this.error(log, throwable);
            case WARNING -> this.warning(log, throwable);
            case DEBUG -> this.debug(log, throwable);
        }
    }

    public void instantLogToFile(final Object log) {
        if (this.printStream != null) {
            this.printStream.println(ConsoleColors.removeColors(log));
        }
    }

    private void logToFile(final Object log) {
        if (this.printStream != null) {
            this.printStream.println(DateUtil.getDate() + " [" + Thread.currentThread().getName() + "] " + this.logState + " " + ConsoleColors.removeColors(log));
        }
    }

    private void logThrowableToFile(final Throwable throwable) {
        if (this.printStream != null && throwable != null) {
            throwable.printStackTrace();
            throwable.printStackTrace(this.printStream);
        }
    }

    public File getLogFile() {
        return this.logFile;
    }
}
