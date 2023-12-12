package me.indian.bds.logger;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.AppConfig;
import me.indian.bds.discord.DiscordIntegration;
import me.indian.bds.discord.DiscordLogChannelType;
import me.indian.bds.util.DateUtil;
import me.indian.bds.util.DefaultsVariables;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class Logger {

    private final BDSAutoEnable bdsAutoEnable;
    private final AppConfig appConfig;
    private File logFile;
    private String prefix;
    private LogState logState;
    private PrintStream printStream;

    public Logger(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.appConfig = this.bdsAutoEnable.getAppConfigManager().getAppConfig();
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
        final File logsDir = new File(DefaultsVariables.getAppDir() + "logs");
        if (!logsDir.exists()) {
            if (!logsDir.mkdir()) if (logsDir.mkdirs()) {
                throw new RuntimeException("Nie można utworzyć miejsca na logi");
            }
        }

        try {
            this.logFile = new File(logsDir, "ServerLog-" + this.bdsAutoEnable.getRunDate() + ".log");
            final FileOutputStream fileOutputStream = new FileOutputStream(this.logFile, true);
            this.printStream = new PrintStream(fileOutputStream);
        } catch (final Exception exception) {
            this.error("Nie można utworzyć&1 PrintStreamu&r aby zapisywać logi do pliku ", exception);
        }
    }

    public void print() {
        this.print("");
    }

    public void print(final Object log) {
        this.logState = LogState.NONE;
        if (this.printStream != null) {
            this.printStream.println(ConsoleColors.removeColors(log));
        }

        System.out.println(ConsoleColors.convertMinecraftColors(log));
    }

    public void print(final Object log, final Throwable throwable) {
        this.print(log);
        this.logThrowableToFile(throwable);
    }

    public void print(final Object log, final DiscordIntegration discord, final DiscordLogChannelType channelType) {
        this.print(log, null, discord, channelType);
    }

    public void print(final Object log, final Throwable throwable, final DiscordIntegration discord, final DiscordLogChannelType channelType) {
        this.print(log, throwable);

        if (discord == null) {
            this.debug("Podana integracja z discord jest&c nullem!");
            return;
        }

        switch (channelType) {
            case CHAT -> discord.sendMessage(log.toString(), throwable);
            case CONSOLE -> discord.writeConsole(log.toString(), throwable);
            default -> this.debug("Nieznany typ kanału discord&1 " + channelType);
        }
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
        if (this.appConfig.isDebug()) {
            this.logState = LogState.DEBUG;
            this.updatePrefix();
            this.logToFile(log);
            System.out.println(ConsoleColors.convertMinecraftColors(this.prefix + log));
        }
    }

    public void debug(final Object log, final Throwable throwable) {
        if (this.appConfig.isDebug()) {
            this.debug(log);
            this.logThrowableToFile(throwable);
        }
    }

    public void logByState(final Object log, final LogState logState) {
        this.logByState(log , null , logState);
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

    public void logThrowableToFile(final Throwable throwable) {
        if (this.printStream != null && throwable != null) {
            throwable.printStackTrace();
            throwable.printStackTrace(this.printStream);
        }
    }

    public File getLogFile() {
        return this.logFile;
    }
}