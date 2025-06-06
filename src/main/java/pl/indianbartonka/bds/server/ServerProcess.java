package pl.indianbartonka.bds.server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.config.AppConfigManager;
import pl.indianbartonka.bds.config.sub.transfer.LobbyConfig;
import pl.indianbartonka.bds.event.EventManager;
import pl.indianbartonka.bds.event.server.ServerAlertEvent;
import pl.indianbartonka.bds.event.server.ServerClosedEvent;
import pl.indianbartonka.bds.event.server.ServerConsoleCommandEvent;
import pl.indianbartonka.bds.shutdown.ShutdownHandler;
import pl.indianbartonka.bds.util.DefaultsVariables;
import pl.indianbartonka.bds.util.ServerUtil;
import pl.indianbartonka.bds.watchdog.WatchDog;
import pl.indianbartonka.util.BedrockQuery;
import pl.indianbartonka.util.ThreadUtil;
import pl.indianbartonka.util.logger.LogState;
import pl.indianbartonka.util.logger.Logger;
import pl.indianbartonka.util.system.SystemOS;
import pl.indianbartonka.util.system.SystemUtil;

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
        this.consoleOutputService = Executors.newCachedThreadPool(new ThreadUtil("Console Output"));
        this.prefix = "&b[&3ServerProcess&b] ";
        this.system = SystemUtil.getSystem();
        this.eventManager = this.bdsAutoEnable.getEventManager();
        this.lastConsoleLine = "";
        this.pid = -1;
        this.startTime = 0;
        this.canRun = true;
        this.canWriteConsoleOutput = true;
    }

    public void init() {
        this.watchDog = this.bdsAutoEnable.getWatchDog();
        this.fileName = DefaultsVariables.getDefaultFileName();
    }

    /**
     * Metoda która patrzy czy proces o nazwie pliku servera jest już aktywny
     *
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
                    this.processBuilder = this.buildStartCommand();

                    if (ShutdownHandler.isShutdownHookCalled()) return;

                    this.canWriteConsoleOutput = true;
                    this.watchDog.getPackModule().getPackInfo();
                    this.process = this.processBuilder.start();
                    this.startTime = System.currentTimeMillis();

                    if (!this.appConfigManager.getAppConfig().isQuestions()) {
                        this.bdsAutoEnable.getSettings().currentSettings(false);
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
     * Buduje i zwraca obiekt {@link ProcessBuilder}, który uruchamia aplikację
     * w zależności od systemu operacyjnego i konfiguracji aplikacji.
     * <p>
     * Obsługiwane są systemy: LINUX oraz WINDOWS.
     * Dla systemu Linux metoda wspiera dodatkowo uruchamianie przy pomocy WINE oraz BOX64.
     *
     * @return obiekt {@link ProcessBuilder} skonfigurowany do uruchomienia aplikacji
     *         zgodnie z aktualnym systemem i konfiguracją. W przypadku braku wsparcia
     *         dla systemu lub brakującego WINE – aplikacja zostanie zakończona.
     */
    private ProcessBuilder buildStartCommand() {
        ProcessBuilder processBuilder = null;
        switch (this.system) {
            case LINUX -> {
                final List<String> firstArgs = new LinkedList<>();
                final boolean wine = this.appConfigManager.getAppConfig().isWine();
                final boolean box64 = DefaultsVariables.box64;

                if (!wine && !box64) {
                    processBuilder = new ProcessBuilder("./" + this.fileName);
                    processBuilder.environment().put("LD_LIBRARY_PATH", ".");
                    processBuilder.directory(new File(this.appConfigManager.getAppConfig().getFilesPath()));

                    return processBuilder;
                }

                if (box64) firstArgs.add("box64");

                if (wine) {
                    if (!DefaultsVariables.wine) {
                        this.logger.critical("^#cNIE POSIADASZ ^#1WINE^#C!");
                        System.exit(-1);
                        return null;
                    }
                    firstArgs.add("wine");
                }

                firstArgs.add("./" + this.fileName);
                processBuilder = new ProcessBuilder(firstArgs);
            }

            case WINDOWS -> processBuilder = new ProcessBuilder(this.finalFilePath);
            default -> {
                this.logger.critical("Twój system jest nie wspierany");
                System.exit(21);
            }
        }

        return processBuilder;
    }

    /**
     * Metoda aby zatrzymać server z oczekiwaniem i zapisaniem świata , najlepiej używać jej tylko wtedy gdy trzeba użyć samego "stop" aby zatrzymać server
     */
    public void disableServer() throws InterruptedException {
        this.watchDog.saveAndResume();
        this.sendToConsole("stop");
        this.logger.alert("Oczekiwanie na zamknięcie servera");
        this.process.waitFor();
        ThreadUtil.sleep(1);
    }

    /**
     * Metoda dzięki której konsola servera BDS wypisywana jest do konsoli aplikacji
     * Metody wykonywane są z consoleOutputService aby nie obciążać jednego wątku wykonywaniem tylu akcji na raz
     */
    private void readConsoleOutput() {
        try (final Scanner consoleOutput = new Scanner(new BufferedInputStream(this.process.getInputStream()), StandardCharsets.UTF_8).useDelimiter("\\A")) {
            try {
                while (this.canWriteConsoleOutput) {
                    if (!consoleOutput.hasNext()) continue;
                    final String line = consoleOutput.nextLine();
                    if (line.isEmpty()) continue;

                    this.serverManager.initFromLog(line);

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
            }
        }
    }

    /**
     * Metoda do wysyłania poleceń do konsoli servera BDS
     *
     * @param command Komenda do serveru
     */
    public void sendToConsole(final String command) {
        if (command.isEmpty()) return;
        try {
            if (!this.isEnabled()) {
                this.logger.debug("Nie udało wysłać się wiadomości  (" + command + ") do konsoli ponieważ, Process jest&c nullem&r albo nie jest aktywny");
                return;
            }

            final OutputStream outputStream = this.process.getOutputStream();

            if (outputStream == null) {
                this.logger.critical("Nie udało wysłać się wiadomości (" + command + ") do konsoli ponieważ,&b OutputStream&r servera jest&c nullem&r!");
                return;
            }

            this.someChangesForCommands(command);

            outputStream.write((command + "\n").getBytes());
            outputStream.flush();

            this.eventManager.callEventsWithResponse(new ServerConsoleCommandEvent(command));
            this.logger.debug("Wysłano &b" + command.replaceAll("\n", "\\\\n"));
        } catch (final Exception exception) {
            this.logger.error("Wystąpił błąd podczas próby wysłania polecenia (" + command + ") do konsoli", exception);
        }
    }

    /**
     * Metoda do wysyłania poleceń do konsoli servera BDS i uzyskania ostatniej linij z konsoli , może być opóźnione
     *
     * @param command Komenda do serveru
     * @return ostatnia linia z konsoli
     */
    public String commandAndResponse(final String command) {
        final Thread thread = Thread.currentThread();

        if (BDSAutoEnable.isImportantThread()) {
            throw new UnsupportedOperationException("Nie możesz wykonać tego na tym wątku! (" + thread.getName() + ")");
        }

        if (thread.isInterrupted()) {
            throw new RuntimeException("Ten wątek (" + thread.getName() + ") został przerwany, nie można na nim wykonać tej metody.");
        }

        this.sendToConsole(command);
        ThreadUtil.sleep(1);
        return this.lastConsoleLine == null ? "null" : this.lastConsoleLine;
    }

    /**
     * Metoda do zatrzymania bezpiecznie servera wywoływana przez shutdown hook
     */
    public void instantShutdown() {
        this.logger.alert("Wyłączanie...");
        this.setCanRun(false);
        this.bdsAutoEnable.setAppWindowName("Wyłączanie...");

        if (!this.appConfigManager.getTransferConfig().getLobbyConfig().isEnable()) {
            ServerUtil.kickAllPlayers(this.prefix + "&cServer jest zamykany");
        } else {
            ServerUtil.tellrawToAll("&2Zaraz zostaniecie przeniesieni na server&b lobby");
        }

        try {
            this.logger.info("Zamykanie servera...");
            if (this.isEnabled()) this.disableServer();
        } catch (final InterruptedException exception) {
            this.logger.critical("&4Nie udało się zamknąć procesu servera ,zrób to ręcznie!");
        }

        ThreadUtil.sleep(3);
        this.bdsAutoEnable.getServerManager().getStatsManager().saveAllData();

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

        this.bdsAutoEnable.setAppWindowName("Wyłączono");
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
     * Jeśli ktoś użył by 'this.process.getInputStream()' to może popsuć całą konsole
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

    public void destroyForciblyProcess() {
        this.process.destroyForcibly();
    }

    private void someChangesForCommands(final String command) {
        if (command.equalsIgnoreCase("stop")) {
            if (!this.isEnabled()) return;
            ServerUtil.tellrawToAllAndLogger(this.prefix, "&4Zamykanie servera...", LogState.ALERT);

            final LobbyConfig lobbyConfig = this.appConfigManager.getTransferConfig().getLobbyConfig();
            if (lobbyConfig.isEnable()) {
                final String address = lobbyConfig.getAddress();
                final int port = lobbyConfig.getPort();
                final BedrockQuery query = BedrockQuery.create(address, port);

                for (final String player : this.serverManager.getOnlinePlayers()) {
                    if (query.online()) {
                        ServerUtil.tellrawToPlayer(player, lobbyConfig.getTransferringMessage());
                        ThreadUtil.sleep(1);
                        ServerUtil.transferPlayer(player, address, port);
                    } else {
                        ServerUtil.kickAllPlayers(lobbyConfig.getServerOffline());
                        break;
                    }
                }
            } else {
                ServerUtil.kickAllPlayers(this.prefix + "&cKtoś wykonał&a stop &c w konsoli servera , co skutkuje  restartem");
            }

            if (!Thread.currentThread().isInterrupted()) ThreadUtil.sleep(2);
        }
    }

    private void handleExitCode(final int exitCode) {
        switch (exitCode) {
            case 0 -> this.logger.info("&eProces servera zakończył się pomyślnie");

            case 1 -> {
                this.logger.alert("Proces serwera zakończył się kodem wyjściowym 1, prawdopodobnie został zamknięty z poziomu terminala.");
                this.eventManager.callEvent(new ServerAlertEvent("Proces serwera zakończył się kodem wyjściowym 1, prawdopodobnie został zamknięty z poziomu terminala.", LogState.ALERT));
            }

            case 130 -> {
                this.logger.alert("Proces serwera zakończył się kodem wyjściowym 130, prawdopodobnie został zamknięty za pomocą sygnału SIGINT. Nie jest to najlepszym wyjściem dla BDS");
                this.eventManager.callEvent(new ServerAlertEvent("Proces serwera zakończył się kodem wyjściowym 130, prawdopodobnie został zamknięty za pomocą sygnału SIGINT.",
                        "Nie jest to najlepszym wyjściem dla BDS", LogState.ALERT));
            }

            case -1073740791 -> {
                this.logger.critical("&cKod&b " + exitCode + "&c zazwyczaj występuje gdy jakiś behavior w skryptach ma&1 &nimport \".\"");
                this.logger.alert("Za&1 30&r sekund spróbujemy znów uruchomić proces servera a ty spróbuj to&l naprawić");

                this.eventManager.callEvent(new ServerAlertEvent("Kod " + exitCode + " zazwyczaj występuje gdy jakiś behavior w skryptach ma import \".\"",
                        "Za 30 sekund spróbujemy znów uruchomić proces servera a ty spróbuj to naprawić", LogState.CRITICAL));
                ThreadUtil.sleep(30);
            }

            default -> {
                this.logger.alert("Wystąpił nieznany kod wyjściowy (" + exitCode + "), prosimy o zgłoszenie tego oraz opisanie kroków, które zostały wykonane, abyśmy mogli go zdiagnozować.");
                this.eventManager.callEvent(new ServerAlertEvent("Wystąpił nieznany kod wyjściowy (" + exitCode + "), prosimy o zgłoszenie tego oraz opisanie kroków, które zostały wykonane, abyśmy mogli go zdiagnozować.", LogState.ALERT));
            }
        }
    }

    private boolean containsNotAllowedToFileLog(final String message) {
        return this.contains(message, this.appConfigManager.getLogConfig().getNoFile());
    }

    private boolean containsNotAllowedToConsoleLog(final String message) {
        return this.contains(message, this.appConfigManager.getLogConfig().getNoConsole());
    }

    private boolean containsAllowedToAlert(final String message) {
        return this.contains(message, this.appConfigManager.getLogConfig().getAlertOn());
    }

    private boolean contains(final String message, final List<String> list) {
        for (final String element : list) {
            if (message.toLowerCase().contains(element.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
