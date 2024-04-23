package me.indian.bds.server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.AppConfigManager;
import me.indian.bds.config.sub.transfer.LobbyConfig;
import me.indian.bds.event.EventManager;
import me.indian.bds.event.server.ServerAlertEvent;
import me.indian.bds.event.server.ServerClosedEvent;
import me.indian.bds.event.server.ServerConsoleCommandEvent;
import me.indian.bds.exception.BadThreadException;
import me.indian.bds.logger.ConsoleColors;
import me.indian.bds.logger.LogState;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.BedrockQuery;
import me.indian.bds.util.DefaultsVariables;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.util.system.SystemOS;
import me.indian.bds.util.system.SystemUtil;
import me.indian.bds.watchdog.WatchDog;

public class ServerProcess {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final AppConfigManager appConfigManager;
    private final ServerManager serverManager;
    private final ExecutorService processService, consoleOutputService;
    private final String prefix;
    private final SystemOS system;
    private final EventManager eventManager;
    private String finalFilePath, fileName;
    private ProcessBuilder processBuilder;
    private Process process;
    private String lastConsoleLine;
    private long pid;
    private long startTime;
    private WatchDog watchDog;
    private boolean canRun, canWriteConsoleOutput;

    public ServerProcess(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.appConfigManager = this.bdsAutoEnable.getAppConfigManager();
        this.serverManager = this.bdsAutoEnable.getServerManager();
        this.processService = Executors.newFixedThreadPool(2, new ThreadUtil("Server process"));
        this.consoleOutputService = Executors.newFixedThreadPool(3 * ThreadUtil.getLogicalThreads(),
                new ThreadUtil("Console Output"));
        this.prefix = "&b[&3ServerProcess&b] ";
        this.system = SystemUtil.getSystem();
        this.eventManager = this.bdsAutoEnable.getEventManager();
        this.lastConsoleLine = "";
        this.pid = -1;
        this.canRun = true;
        this.canWriteConsoleOutput = true;
    }

    public void init() {
        this.watchDog = this.bdsAutoEnable.getWatchDog();
        this.fileName = DefaultsVariables.getDefaultFileName();
    }

    /**
     * Metoda która patrzy czy proces o nazwie pliku servera jest już aktywny
     * @return Czy proces servera jest aktywny
     */
    public boolean checkProcesRunning() {
        try {
            String command = "";
            switch (this.system) {
                case LINUX -> command = "pgrep -f " + this.fileName;
                case WINDOWS -> command = "tasklist /NH /FI \"IMAGENAME eq " + this.fileName + "\"";
                default -> {
                    this.logger.critical("Musisz podać odpowiedni system");
                    System.exit(21);
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
            System.exit(5);
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
            if (this.checkProcesRunning()) {
                this.logger.alert("&cProces&b " + this.fileName + "&c jest już uruchomiony.");
                this.logger.alert("Za&1 30&r sekund spróbujemy znów uruchomić proces servera");
                ThreadUtil.sleep(30);
                this.startProcess();
            } else {
                this.logger.debug("Proces " + this.fileName + " nie jest uruchomiony. Uruchamianie...");
                try {
                    switch (this.system) {
                        case LINUX -> {
                            if (this.appConfigManager.getAppConfig().isWine()) {
                                if (!DefaultsVariables.WINE) {
                                    this.logger.critical("^#cNIE POSIADASZ ^#1WINE^#C!");
                                    System.exit(-1);
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
                            this.logger.critical("Twój system jest nie wspierany");
                            System.exit(21);
                        }
                    }

                    this.canWriteConsoleOutput = true;
                    this.watchDog.getPackModule().getPackInfo();
                    this.process = this.processBuilder.start();
                    this.startTime = System.currentTimeMillis();

                    if (!this.appConfigManager.getAppConfig().isQuestions()) {
                        this.bdsAutoEnable.getSettings().currentSettings(this.bdsAutoEnable.getMainScanner(), false);
                    }

                    this.pid = this.process.pid();
                    this.logger.info("Uruchomiono proces servera ");
                    this.logger.debug("&bPID&r procesu servera to&1 " + this.pid);
                    this.consoleOutputService.execute(this::readConsoleOutput);

                    final int exitCode = this.process.waitFor();

                    this.logger.alert("Proces zakończony z kodem: " + exitCode);
                    this.eventManager.callEvent(new ServerClosedEvent());

                    this.canWriteConsoleOutput = false;
                    this.pid = -1;
                    this.watchDog.getAutoRestartModule().noteRestart();
                    this.serverManager.getStatsManager().saveAllData();
                    this.serverManager.restartPlayersList();
                    this.handleExitCode(exitCode);
                    this.startProcess();
                } catch (final Exception exception) {
                    this.logger.critical("Nie można uruchomić procesu", exception);
                    System.exit(5);
                }
            }
        });
    }

    /**
     * Metoda dzięki której konsola servera BDS wypisywana jest do konsoli aplikacji
     * Metody wykonywane są z consoleOutputService aby nie obciążać jednego wątku wykonywaniem tylu akcji na raz
     */
    private void readConsoleOutput() {
        //TODO: Ogarnąć też errorStream
        try (final Scanner consoleOutput = new Scanner(new BufferedInputStream(this.process.getInputStream()), StandardCharsets.UTF_8)/*.useDelimiter("\\A")*/) {
            try {
                while (this.canWriteConsoleOutput) {
                    if (!consoleOutput.hasNext()) continue;
                    final String line = consoleOutput.nextLine();
                    if (line.isEmpty()) continue;

                    this.serverManager.initFromLog(ConsoleColors.removeAnsiColors(line));

                    if (this.containsAllowedToAlert(line)) {
                        this.eventManager.callEvent(new ServerAlertEvent(line, LogState.ALERT));
                    }

                    if (!this.containsNotAllowedToFileLog(line)) {
                        this.consoleOutputService.execute(() -> this.logger.instantLogToFile(line));
                    }

                    if (!this.containsNotAllowedToConsoleLog(line)) {
                        this.consoleOutputService.execute(() -> System.out.println(line));
                        this.lastConsoleLine = line;
                    }
                }
            } catch (final Exception exception) {
                this.logger.critical("Czytanie konsoli uległo awarii , powoduje to wyłączenie aplikacji ", exception);
                System.exit(1);
            } finally {
                consoleOutput.close();
            }
        }
    }

    /**
     *  Metoda do wysyłania poleceń do konsoli servera BDS
     * @param command Komenda do serveru
     */
    public void sendToConsole(final String command) {
        if (command.isEmpty()) return;
        try {
            if (!this.isEnabled()) {
                this.logger.debug("Nie udało wysłać się wiadomości do konsoli ponieważ, Process jest&c nullem&r albo nie jest aktywny");
                return;
            }

            final OutputStream outputStream = this.process.getOutputStream();

            if (outputStream == null) {
                this.logger.critical("Nie udało wysłać się wiadomości do konsoli ponieważ,&b OutputStream&r servera jest&c nullem&r!");
                return;
            }

            this.someChangesForCommands(command);

            outputStream.write((command + "\n").getBytes());
            outputStream.flush();


            this.eventManager.callEventWithResponse(new ServerConsoleCommandEvent(command));
            this.logger.debug("Wysłano &b" + command.replaceAll("\n", "\\\\n"));
        } catch (final Exception exception) {
            this.logger.error("Wystąpił błąd podczas próby wysłania polecenia do konsoli", exception);
        }
    }

    /**
     * Metoda do wysyłania poleceń do konsoli servera BDS i uzyskania ostatniej linij z konsoli , może być opóźnione
     * @param command Komenda do serveru
     * @return ostatnia linia z konsoli
     */
    public String commandAndResponse(final String command) {
        final Thread thread = Thread.currentThread();

        if (ThreadUtil.isImportantThread())
            throw new BadThreadException("Nie możesz wykonać tego na tym wątku! (" + thread.getName() + ")");
        if (thread.isInterrupted())
            throw new RuntimeException("Ten wątek (" + thread.getName() + ") został przerwany, nie można na nim wykonać tej metody.");

        this.sendToConsole(command);
        ThreadUtil.sleep(1);
        return this.lastConsoleLine == null ? "null" : this.lastConsoleLine;
    }

    public void kickAllPlayers(final String msg) {
        if (this.serverManager.getOnlinePlayers().isEmpty()) {
            this.logger.debug("Lista graczy jest pusta");
            return;
        }
        new ArrayList<>(this.serverManager.getOnlinePlayers()).forEach(name -> this.kick(name, msg));
    }

    public void kick(final String who, final String reason) {
        if (this.serverManager.getOnlinePlayers().isEmpty()) {
            this.logger.debug("Lista graczy jest pusta");
            return;
        }
        this.sendToConsole("kick " + who + " " + MessageUtil.colorize(reason));
    }

    public void transferPlayer(final String playerName, final String address, final int port) {
        this.sendToConsole("transfer " + playerName + " " + address + " " + port);
    }

    public void transferPlayer(final String playerName, final String address) {
        this.transferPlayer(playerName, address, 19132);
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

        final String msg2 = MessageUtil.fixMessage(msg, true).replace("\"", "\\\"");

        this.sendToConsole(MessageUtil.colorize("tellraw " + playerName + " {\"rawtext\":[{\"text\":\"" + msg2 + "\"}]}"));
    }

    public void tellrawToAllAndLogger(final String prefix, final String msg, final LogState logState) {
        this.tellrawToAllAndLogger(prefix, msg, null, logState);
    }

    public void tellrawToAllAndLogger(final String prefix, final String msg, final Throwable throwable, final LogState logState) {
        this.logger.logByState("[To Minecraft] " + msg, throwable, logState);
        if (!this.serverManager.getOnlinePlayers().isEmpty()) this.tellrawToAll(prefix + " " + msg);
    }

    public void titleToPlayer(final String playerName, final String message) {
        if (this.serverManager.getOnlinePlayers().isEmpty()) return;
        this.sendToConsole("title " + playerName + " title " + MessageUtil.colorize(message));
    }

    public void titleToPlayer(final String playerName, final String message, final String subTitle) {
        if (this.serverManager.getOnlinePlayers().isEmpty()) return;
        this.sendToConsole("title " + playerName + " subtitle " + MessageUtil.colorize(subTitle));
        this.titleToPlayer(playerName, message);
    }

    public void titleToAll(final String message) {
        this.titleToPlayer("@a", message);
    }

    public void titleToAll(final String message, final String subTitle) {
        this.titleToPlayer("@a", message, subTitle);
    }

    public void actionBarToPlayer(final String playerName, final String message) {
        if (this.serverManager.getOnlinePlayers().isEmpty()) return;
        this.sendToConsole("title " + playerName + " actionbar " + MessageUtil.colorize(message));
    }

    public void actionBarToAll(final String message) {
        this.actionBarToPlayer("@a", message);
    }

    public void playSoundToPlayer(final String playerName, final String soundName) {
        this.sendToConsole("playsound " + soundName + " " + playerName);
    }

    public void playSoundToAll( final String soundName) {
        this.playSoundToPlayer("@a" ,soundName);
    }

    /**
     * Metoda do zatrzymania bezpiecznie servera wywoływana przez shutdown hook
     */
    public void instantShutdown() {
        this.logger.alert("Wyłączanie...");
        this.setCanRun(false);


        if (!this.appConfigManager.getTransferConfig().getLobbyConfig().isEnable()) {
            this.kickAllPlayers(this.prefix + "&cServer jest zamykany");
        } else {
            this.tellrawToAll("&2Zaraz zostaniecie przeniesieni na server&b lobby");
        }

        ThreadUtil.sleep(3);
        this.bdsAutoEnable.getServerManager().getStatsManager().saveAllData();

        if (this.isEnabled()) {
            this.watchDog.saveAndResume();
            this.sendToConsole("stop");
            this.logger.alert("Oczekiwanie na zamknięcie servera");

            try {
                this.process.waitFor();
                ThreadUtil.sleep(1);
                this.logger.info("&eProces servera zakończył się pomyślnie");
            } catch (final InterruptedException exception) {
                this.logger.critical("&4Nie udało się zamknąć procesu servera ,zrób to ręcznie!");
            }
        }

//        this.logger.info("Zapisywanie configu...");
//        try {
//            this.appConfigManager.save();
//            this.logger.info("Zapisano config");
//        } catch (final Exception exception) {
//            this.logger.critical("Nie można zapisać configu", exception);
//        }

        this.bdsAutoEnable.getExtensionManager().disableExtensions();

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
    }

    public boolean isCanRun() {
        return this.canRun;
    }

    public void setCanRun(final boolean canRun) {
        this.canRun = canRun;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public boolean isEnabled() {
        return (this.process != null && this.process.isAlive());
    }

    public String getLastConsoleLine() {
        return this.lastConsoleLine;
    }

    /**
     *  Jeśli ktoś użył by 'this.process.getInputStream()' to może popsuć całą konsole
     */
    public long getPID() {
        return this.pid;
    }

    public void waitFor() throws InterruptedException {
        this.process.waitFor();
    }

    public boolean waitFor(final long timeout, final TimeUnit unit) throws InterruptedException {
        return this.process.waitFor(timeout, unit);
    }

    public void destroyProcess() {
        this.process.destroy();
    }

    private void someChangesForCommands(final String command) {
        if (command.equalsIgnoreCase("stop")) {
            if (!this.isEnabled()) return;
            this.tellrawToAllAndLogger(this.prefix, "&4Zamykanie servera...", LogState.ALERT);

            final LobbyConfig lobbyConfig = this.appConfigManager.getTransferConfig().getLobbyConfig();
            if (lobbyConfig.isEnable()) {
                final String address = lobbyConfig.getAddress();
                final int port = lobbyConfig.getPort();

                final BedrockQuery query = BedrockQuery.create(address, port);

                for (final String player : new ArrayList<>(this.serverManager.getOnlinePlayers())) {
                    if (query.online()) {
                        this.tellrawToPlayer(player, lobbyConfig.getTransferringMessage());
                        ThreadUtil.sleep(1);
                        this.transferPlayer(player, address, port);
                    } else {
                        this.kickAllPlayers(lobbyConfig.getServerOffline());
                        break;
                    }
                }
            } else {
                this.kickAllPlayers(this.prefix + "&cKtoś wykonał&a stop &c w konsoli servera , co skutkuje  restartem");
            }

            if (!Thread.currentThread().isInterrupted()) ThreadUtil.sleep(2);
        }
    }

    private void handleExitCode(final int exitCode) {
        if (exitCode == -1073740791) {
            this.logger.critical("&cKod&b " + exitCode + "&c zazwyczaj występuje gdy jakiś behavior w skryptach ma&1 &nimport \".\"");
            this.logger.alert("Za&1 30&r sekund spróbujemy znów uruchomić proces servera a ty spróbuj to&l naprawić");
            ThreadUtil.sleep(30);
        }
    }

    private boolean containsNotAllowedToFileLog(final String msg) {
        for (final String noAllowed : this.appConfigManager.getLogConfig().getNoFile()) {
            if (msg.toLowerCase().contains(noAllowed.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsNotAllowedToConsoleLog(final String msg) {
        for (final String noAllowed : this.appConfigManager.getLogConfig().getNoConsole()) {
            if (msg.toLowerCase().contains(noAllowed.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAllowedToAlert(final String msg) {
        for (final String allowedForAlert : this.appConfigManager.getLogConfig().getAlertOn()) {
            if (msg.toLowerCase().contains(allowedForAlert.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
