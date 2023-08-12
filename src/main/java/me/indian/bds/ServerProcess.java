package me.indian.bds;

import me.indian.bds.config.Config;
import me.indian.bds.exception.BadThreadException;
import me.indian.bds.logger.Logger;
import me.indian.bds.manager.PlayerManager;
import me.indian.bds.util.ConsoleColors;
import me.indian.bds.util.MinecraftUtil;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.watchdog.WatchDog;
import me.indian.bds.watchdog.module.BackupModule;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerProcess {

    private WatchDog watchDog;
    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final Config config;
    private final PlayerManager playerManager;
    private final ExecutorService processService;
    private final ExecutorService consoleService;
    private final String finalFilePath;
    private final String prefix;
    private ProcessBuilder processBuilder;
    private Process process;
    private PrintWriter writer;
    private String lastLine;

    public ServerProcess(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.config = this.bdsAutoEnable.getConfig();
        this.playerManager = this.bdsAutoEnable.getPlayerManager();
        this.processService = Executors.newScheduledThreadPool(2, new ThreadUtil("Server process"));
        this.consoleService = Executors.newScheduledThreadPool(2, new ThreadUtil("Console"));
        this.finalFilePath = this.config.getFilesPath() + File.separator + this.config.getFileName();
        this.prefix = "&b[&3ServerProcess&b] ";

    }

    public void initWatchDog(final WatchDog watchDog){
        this.watchDog = watchDog;
    }

    private boolean isProcessRunning() throws RuntimeException {
        BufferedReader reader = null;
        try {
            String command = "";
            switch (this.config.getSystemOs()) {
                case LINUX:
                    command = "pgrep -f " + this.config.getFileName();
                    break;
                case WINDOWS:
                    command = "tasklist /NH /FI \"IMAGENAME eq " + this.config.getFileName() + "\"";
                    break;
                default:
                    this.logger.critical("Musisz podać odpowiedni system");
                    this.instantShutdown();
            }

            final Process checkProcessIsRunning = Runtime.getRuntime().exec(command);

            reader = new BufferedReader(new InputStreamReader(checkProcessIsRunning.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty() && !line.equalsIgnoreCase("INFO: No tasks are running which match the specified criteria.")) {
                    return true;
                }
            }
            checkProcessIsRunning.waitFor();
        } catch (final IOException | InterruptedException exception) {
            this.logger.critical(exception);
            exception.printStackTrace();
            this.instantShutdown();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException ioException) {
                    throw new RuntimeException(ioException);
                }
            }
        }
        return false;
    }

    public void startProcess() {
        this.processService.execute(() -> {
            if (isProcessRunning()) {
                this.logger.info("Proces " + this.config.getFileName() + " jest już uruchomiony.");
                this.instantShutdown();
            } else {
                this.logger.info("Proces " + this.config.getFileName() + " nie jest uruchomiony. Uruchamianie...");

                try {
                    switch (this.config.getSystemOs()) {
                        case LINUX:
                            if (this.config.isWine()) {
                                this.processBuilder = new ProcessBuilder("wine", this.finalFilePath);
                            } else {
                                this.processBuilder = new ProcessBuilder("./" + this.config.getFileName());
                                this.processBuilder.environment().put("LD_LIBRARY_PATH", ".");
                                this.processBuilder.directory(new File(this.config.getFilesPath()));
                            }
                            break;
                        case WINDOWS:
                            this.processBuilder = new ProcessBuilder(this.finalFilePath);
                            break;
                        default:
                            this.logger.critical("Musisz podać odpowiedni system");
                            this.instantShutdown();
                    }
                    this.playerManager.clearPlayers();
                    this.process = this.processBuilder.start();
                    this.logger.info("Uruchomiono proces (nadal może on sie wyłączyć)");

                    this.consoleService.execute(this::readConsoleOutput);
                    this.consoleService.execute(this::writeConsoleInput);

                    this.logger.alert("Proces zakończony z kodem: " + this.process.waitFor());
                    ThreadUtil.sleep(5);
                    this.startProcess();

                } catch (final Exception exception) {
                    this.logger.critical("Nie można uruchomic procesu");
                    this.logger.critical(exception);
                    exception.printStackTrace();
                    this.instantShutdown();
                }
            }
        });
    }

    private void readConsoleOutput() {
        final Scanner consoleOutput = new Scanner(this.process.getInputStream());
        try {
            while (consoleOutput.hasNextLine()) {
                if (!consoleOutput.hasNext()) continue;
                final String line = consoleOutput.nextLine();
                if (this.containsNotAllowedToLog(line) || line.isEmpty()) continue;
                this.lastLine = line;
                System.out.println(line);
                this.logger.instantLogToFile(line);
                this.playerManager.updatePlayerList(line);
            }
        } catch (final Exception exception) {
            this.logger.critical(exception);
            exception.printStackTrace();
            consoleOutput.close();
            ThreadUtil.sleep(1);
            this.readConsoleOutput();
        }
    }

    private void writeConsoleInput() {
        final Scanner consoleInput = new Scanner(System.in);
        try {
            this.writer = new PrintWriter(this.process.getOutputStream());
            while (consoleInput.hasNextLine()) {
                final String input = consoleInput.nextLine();
                if (input.equalsIgnoreCase("stop")) {
                    this.sendToConsole(MinecraftUtil.tellrawToAllMessage(this.prefix + "&4Zamykanie servera..."));
                    if (!this.playerManager.getOnlinePlayers().isEmpty()) {
                        for (final String name : this.playerManager.getOnlinePlayers()) {
                            this.sendToConsole(MinecraftUtil.kickCommand(name, this.prefix + "&cKtoś wykonał &astop &c w konsoli servera , \n co skutkuje  restartem"));
                        }
                    }
                    ThreadUtil.sleep(2);
                    this.sendToConsole("stop");
                } else if (input.equalsIgnoreCase("backup")) {
                    this.watchDog.getBackupModule().forceBackup();
                } else if (input.equalsIgnoreCase(".end")) {
                    this.endServerProcess(true);
                } else if (input.equalsIgnoreCase("version")) {
                    this.logger.info("Versija minecraft: " + this.config.getVersion());
                    this.logger.info("Versija BDS-Auro-Enable:" + this.bdsAutoEnable.getProjectVersion());
                    final List<String> players = this.playerManager.getOnlinePlayers();
                    if (!players.isEmpty()) {
                        for (final String name : players) {
                            this.sendToConsole(MinecraftUtil.tellrawToAllMessage("&aVersija minecraft:&b " + this.config.getVersion()));
                            this.sendToConsole(MinecraftUtil.tellrawToAllMessage("&aVersija BDS-Auro-Enable:&b " + this.bdsAutoEnable.getProjectVersion()));
                        }
                    }
                } else {
                    this.sendToConsole(input);
                }
            }
        } catch (final Exception exception) {
            this.logger.critical(exception);
            exception.printStackTrace();
            consoleInput.close();
            this.writer.close();
        }
    }

    public String commandAndResponse(final String command) throws BadThreadException {
        final String threadName = Thread.currentThread().getName();
        if (threadName.contains("Console") || threadName.contains("Server process")) {
            throw new BadThreadException("Nie możesz wykonac tego na tym wątku!");
        }
        this.sendToConsole(command);
        ThreadUtil.sleep(1);
        return this.lastLine == null ? "null" : this.lastLine;
    }


    public void sendToConsole(final String command) {
        this.writer.println(command);
        this.writer.flush();
    }

    public void instantShutdown() {
        this.logger.alert("Wyłączanie...");
        if (this.consoleService != null && !this.consoleService.isTerminated()) {
            this.logger.info("Zatrzymywanie wątków konsoli");
            try {
                this.consoleService.shutdown();
                ThreadUtil.sleep(2);
                this.logger.info("Zatrzymano wątki konsoli");
            } catch (final Exception exception) {
                this.logger.error("Nie udało się zarzymać wątków konsoli");
                exception.printStackTrace();
            }
        }

        if (this.processService != null && !this.processService.isTerminated()) {
            this.logger.info("Zatrzymywanie wątków procesu servera");
            try {
                this.processService.shutdown();
                ThreadUtil.sleep(2);
                this.logger.info("Zatrzymano wątki procesu servera");
            } catch (final Exception exception) {
                this.logger.error("Nie udało się zarzymać wątków procesu servera");
                exception.printStackTrace();
            }
        }
        if (!this.playerManager.getOnlinePlayers().isEmpty()) {
            for (final String name : this.playerManager.getOnlinePlayers()) {
                this.sendToConsole(MinecraftUtil.kickCommand(name, this.prefix + "&cServer jest zamykany"));
            }
            ThreadUtil.sleep(3);
        }

        if (this.writer != null) {
            this.logger.info("Zatrzymywanie writera");
            try {
                this.writer.close();
                this.logger.info("Zatrzymano writer");
            } catch (final Exception exception) {
                this.logger.error("Błąd podczas zamykania writera");
                exception.printStackTrace();
            }
        }

        if (this.process != null && this.process.isAlive()) {
            this.logger.info("Niszczenie procesu servera");
            try {
                this.process.destroy();
                this.logger.info("Zniszczeno proces servera");
            } catch (final Exception exception) {
                this.logger.error("Nie udało się zniszczyć procesu servera");
                exception.printStackTrace();
            }
        }

        this.logger.info("Zapisywanie configu...");
        try {
            this.config.save();
            this.logger.info("Zapisano config");
        } catch (final Exception exception) {
            this.logger.critical("Nie można zapisać configu");
            exception.printStackTrace();
        }

        Runtime.getRuntime().halt(129);
    }

    public void endServerProcess(final boolean backup) {
        if (this.process != null && this.process.isAlive()) {
            this.processService.execute(() -> {
                try {
                    if (backup) {
                        this.logger.warning("Wyłączanie servera , prosze poczekac , pierw zostanie utworzony backup");
                        this.sendToConsole(MinecraftUtil.tellrawToAllMessage(this.prefix + "&aWyłączanie servera , prosze poczekac pierw zostanie utworzony backup"));
                        this.watchDog.getBackupModule().forceBackup();
                    }
                    while (!BackupModule.isBackuping()){
                        final String done = "Backup zrobiony!";
                        this.sendToConsole(MinecraftUtil.tellrawToAllMessage(this.prefix + "&a" + done));
                        this.logger.info(done);
                        this.instantShutdown();
                    }
                } catch (final Exception e) {
                    this.logger.critical(e);
                    e.printStackTrace();
                }
            });
        }
    }

    private boolean containsNotAllowedToLog(final String msg) {
        for (final String s : this.config.getNoLogInfo()) {
            if (msg.toLowerCase().contains(s.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public Process getProcess() {
        return this.process;
    }
}
