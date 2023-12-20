package me.indian.bds.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.AppConfigManager;
import me.indian.bds.discord.DiscordIntegration;
import me.indian.bds.discord.component.Footer;
import me.indian.bds.exception.BadThreadException;
import me.indian.bds.logger.LogState;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.manager.ServerManager;
import me.indian.bds.server.properties.ServerProperties;
import me.indian.bds.util.DefaultsVariables;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.util.system.SystemOS;
import me.indian.bds.watchdog.WatchDog;

public class ServerProcess {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final AppConfigManager appConfigManager;
    private final DiscordIntegration discord;
    private final ServerManager serverManager;
    private final ExecutorService processService;
    private final Lock cmdLock, cmdResponseLock;
    private final String prefix;
    private final SystemOS system;
    private String finalFilePath, fileName;
    private ProcessBuilder processBuilder;
    private Process process;
    private String lastLine;
    private long startTime;
    private WatchDog watchDog;
    private boolean canRun;

    public ServerProcess(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.appConfigManager = this.bdsAutoEnable.getAppConfigManager();
        this.discord = this.bdsAutoEnable.getDiscord();
        this.serverManager = this.bdsAutoEnable.getServerManager();
        this.processService = Executors.newScheduledThreadPool(5, new ThreadUtil("Server process"));
        this.cmdLock = new ReentrantLock();
        this.cmdResponseLock = new ReentrantLock();
        this.prefix = "&b[&3ServerProcess&b] ";
        this.system = SystemOS.getSystem();
        this.canRun = true;
    }

    public void init() {
        this.watchDog = this.bdsAutoEnable.getWatchDog();
        this.fileName = DefaultsVariables.getDefaultFileName();
    }

    public boolean isProcessRunning() {
        try {
            String command = "";
            switch (this.system) {
                case LINUX -> command = "pgrep -f " + this.fileName;
                case WINDOWS -> command = "tasklist /NH /FI \"IMAGENAME eq " + this.fileName + "\"";
                default -> {
                    this.logger.critical("Musisz podać odpowiedni system");
                    System.exit(0);
                }
            }

            final Process checkProcessIsRunning = Runtime.getRuntime().exec(command);
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(checkProcessIsRunning.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isEmpty() && !line.equalsIgnoreCase("INFO: No tasks are running which match the specified criteria.")) {
                        return true;
                    }
                }
                checkProcessIsRunning.waitFor();
            }
        } catch (final IOException | InterruptedException exception) {
            this.logger.critical("Nie można sprawdzić czy proces jest aktywny", exception);
            this.discord.sendEmbedMessage("ServerProcess",
                    "Nie można sprawdzić czy proces jest aktywny",
                    exception,
                    new Footer(exception.getLocalizedMessage()));
            System.exit(0);
        }
        return false;
    }

    public void startProcess() {
        if (!this.canRun) {
            this.logger.debug("Nie można uruchomić procesu ponieważ&b canRun&r jest ustawione na:&b " + false);
            return;
        }
        this.finalFilePath = this.appConfigManager.getAppConfig().getFilesPath() + File.separator + this.fileName;
        this.processService.execute(() -> {
            if (this.isProcessRunning()) {
                this.logger.info("Proces " + this.fileName + " jest już uruchomiony.");
                System.exit(0);
            } else {
                this.logger.debug("Proces " + this.fileName + " nie jest uruchomiony. Uruchamianie...");
                try {
                    switch (this.system) {
                        case LINUX -> {
                            if (this.appConfigManager.getAppConfig().isWine()) {
                                if (!DefaultsVariables.WINE) {
                                    this.logger.critical("^#cNIE POSIADASZ ^#1WINE^#C!");
                                    System.exit(0);
                                    return;
                                }

                                this.processBuilder = new ProcessBuilder("wine", this.finalFilePath);
                            } else {
                                this.processBuilder = new ProcessBuilder("./" + this.fileName);
                                this.processBuilder.environment().put("LD_LIBRARY_PATH", ".");
                                this.processBuilder.directory(new File(this.appConfigManager.getAppConfig().getFilesPath()));
                            }
                        }
                        case WINDOWS -> this.processBuilder = new ProcessBuilder(this.finalFilePath);
                        default -> {
                            this.logger.critical("Musisz podać odpowiedni system");
                            System.exit(0);
                        }
                    }
                    this.watchDog.getPackModule().getPackInfo();
                    this.colorizeMOTD();
                    this.process = this.processBuilder.start();
                    this.startTime = System.currentTimeMillis();
                    this.logger.info("Uruchomiono proces servera ");
                    this.discord.sendProcessEnabledMessage();

                    this.logger.debug("&bPID&r procesu servera to&1 " + this.process.pid());

                    final Thread output = new ThreadUtil("Console-Output").newThread(this::readConsoleOutput);

                    output.start();

                    this.logger.alert("Proces zakończony z kodem: " + this.process.waitFor());
                    this.watchDog.getAutoRestartModule().noteRestart();
                    this.serverManager.clearPlayers();
                    this.serverManager.getStatsManager().saveAllData();
                    output.interrupt();
                    this.discord.sendDisabledMessage();
                    this.startProcess();
                } catch (final Exception exception) {
                    this.logger.critical("Nie można uruchomić procesu", exception);
                    this.discord.sendEmbedMessage("ServerProcess",
                            "Nie można uruchomić procesu",
                            exception,
                            new Footer(exception.getLocalizedMessage()));

                    System.exit(0);
                }
            }
        });
    }

    private void colorizeMOTD() {
        final ServerProperties serverProperties = this.bdsAutoEnable.getServerProperties();
        final String motd = MessageUtil.fixMessage(MessageUtil.colorize(serverProperties.getMOTD()));

        serverProperties.setMOTD(motd);
    }

    private void readConsoleOutput() {
        try (final Scanner consoleOutput = new Scanner(this.process.getInputStream())) {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    if (!consoleOutput.hasNext()) continue;
                    final String line = consoleOutput.nextLine();
                    if (line.isEmpty()) continue;
                    if (!this.containsNotAllowedToFileLog(line)) {
                        this.logger.instantLogToFile(line);
                    }
                    if (!this.containsNotAllowedToConsoleLog(line)) {
                        System.out.println(line);
                        this.lastLine = line;
                        this.serverManager.initFromLog(line);
                    }
                    if (!this.containsNotAllowedToDiscordConsoleLog(line)) {
                        this.discord.writeConsole(line);
                    }
                }
            } catch (final Exception exception) {
                this.logger.critical("Czytanie konsoli uległo awarii , powoduje to wyłączenie aplikacji ", exception);
                this.discord.sendEmbedMessage("ServerProcess",
                        "Czytanie konsoli uległo awarii , powoduje to wyłączenie aplikacji",
                        exception,
                        new Footer(exception.getLocalizedMessage()));
                this.discord.sendMessage("<owner>");

                System.exit(1);
            } finally {
                consoleOutput.close();
            }
        }
    }

    public void sendToConsole(final String command) {
        if(command.isEmpty()) return;
        this.cmdLock.lock();
        this.cmdResponseLock.lock();
        try {
            if (!this.isEnabled()) {
                this.logger.debug("Nie udało wysłać się wiadomości do konsoli ponieważ, Process jest&c nullem&r albo nie jest aktywny");
                return;
            }

            final OutputStream outputStream = this.process.getOutputStream();

            if (outputStream == null) {
                this.logger.critical("Nie udało wysłać się wiadomości do konsoli ponieważ, OutputStream servera jest&c nullem&r!");
                return;
            }

            outputStream.write((command + "\n").getBytes());
            outputStream.flush();
            this.logger.debug("Wysłano &b" + command);

        } catch (final Exception exception) {
            this.logger.error("Wystąpił błąd podczas próby wysłania polecenia do konsoli", exception);
        } finally {
            this.cmdLock.unlock();
            this.cmdResponseLock.unlock();
        }
    }

    public String commandAndResponse(final String command) {
        final Thread thread = Thread.currentThread();

        if (ThreadUtil.isImportantThread()) {
            throw new BadThreadException("Nie możesz wykonać tego na tym wątku! (" + thread.getName() + ")");
        }

        if (thread.isInterrupted()) {
            throw new RuntimeException("Ten wątek (" + thread.getName() + ") został przerwany, nie można na nim wykonać tej metody.");
        }

        this.cmdResponseLock.lock();
        this.sendToConsole(command);
        ThreadUtil.sleep(1);
        this.cmdResponseLock.unlock();
        return this.lastLine == null ? "null" : this.lastLine;
    }

    public void kickAllPlayers(final String msg) {
        if (this.serverManager.getOnlinePlayers().isEmpty()) {
            this.logger.debug("Lista graczy jest pusta");
            return;
        }
        this.serverManager.getOnlinePlayers().forEach(name -> this.kick(name, msg));
    }

    public void kick(final String who, final String reason) {
        if (this.serverManager.getOnlinePlayers().isEmpty()) {
            this.logger.debug("Lista graczy jest pusta");
            return;
        }
        this.sendToConsole("kick " + who + " " + MessageUtil.colorize(reason));
    }

    public void tellrawToAll(final String msg) {
        if (this.serverManager.getOnlinePlayers().isEmpty()) {
            this.logger.debug("Lista graczy jest pusta");
            return;
        }

        this.tellrawToPlayer("@a", msg);
    }

    public void tellrawToPlayer(final String playerName, final String msg) {
        if (this.serverManager.getOnlinePlayers().isEmpty()) {
            this.logger.debug("Lista graczy jest pusta");
            return;
        }

        final String msg2 = MessageUtil.fixMessage(msg, false).replace("\"", "\\\"");

        this.sendToConsole(MessageUtil.colorize("tellraw " + playerName + " {\"rawtext\":[{\"text\":\"" + msg2 + "\"}]}"));
    }

    public void tellrawToAllAndLogger(final String prefix, final String msg, final LogState logState) {
        this.tellrawToAllAndLogger(prefix, msg, null, logState);
    }

    public void tellrawToAllAndLogger(final String prefix, final String msg, final Throwable throwable, final LogState logState) {
        this.logger.logByState("[To Minecraft] " + msg, throwable, logState);
        if (!this.serverManager.getOnlinePlayers().isEmpty()) this.tellrawToAll(prefix + " " + msg);
    }

    public void instantShutdown() {
        this.discord.startShutdown();
        this.logger.alert("Wyłączanie...");
        this.discord.sendDisablingMessage();
        this.setCanRun(false);

        this.kickAllPlayers(this.prefix + "&cServer jest zamykany");
        ThreadUtil.sleep(3);
        this.bdsAutoEnable.getServerManager().getStatsManager().saveAllData();

        if (this.isEnabled()) {
            this.watchDog.saveAndResume();
            this.sendToConsole("stop");

            this.logger.info("Niszczenie procesu servera");
            try {
                this.process.destroy();
                this.logger.info("Zniszczono proces servera");
                this.discord.sendDestroyedMessage();
            } catch (final Exception exception) {
                this.logger.error("Nie udało się zniszczyć procesu servera", exception);
            }
        }

        this.logger.info("Zapisywanie configu...");
        try {
            this.appConfigManager.save();
            this.logger.info("Zapisano config");
        } catch (final Exception exception) {
            this.logger.critical("Nie można zapisać configu", exception);
        }

        if (this.processService != null && !this.processService.isTerminated()) {
            this.logger.info("Zatrzymywanie wątków procesu servera");
            try {
                this.processService.shutdown();
                if (this.processService.awaitTermination(10, TimeUnit.SECONDS)) {
                    this.logger.info("Zatrzymano wątki procesu servera");
                }
            } catch (final Exception exception) {
                this.logger.error("Nie udało się zatrzymać wątków procesu servera", exception);
            }
        }

        this.discord.shutdown();
    }

    public boolean isCanRun() {
        return this.canRun;
    }

    public void setCanRun(final boolean canRun) {
        this.canRun = canRun;
        //TODO: dodac opcje aby na true mogla zmienic to kalas która ustawila to na false
    }

    public long getStartTime() {
        return this.startTime;
    }

    public boolean isEnabled() {
        return (this.process != null && this.process.isAlive());
    }

    public Process getProcess() {
        return this.process;
    }

    private boolean containsNotAllowedToFileLog(final String msg) {
        for (final String s : this.appConfigManager.getLogConfig().getNoFile()) {
            if (msg.toLowerCase().contains(s.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsNotAllowedToConsoleLog(final String msg) {
        for (final String s : this.appConfigManager.getLogConfig().getNoConsole()) {
            if (msg.toLowerCase().contains(s.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsNotAllowedToDiscordConsoleLog(final String msg) {
        for (final String s : this.appConfigManager.getLogConfig().getNoDiscordConsole()) {
            if (msg.toLowerCase().contains(s.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
