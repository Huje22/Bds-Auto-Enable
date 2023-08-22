package me.indian.bds.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.Config;
import me.indian.bds.discord.DiscordIntegration;
import me.indian.bds.logger.LogState;
import me.indian.bds.logger.Logger;
import me.indian.bds.manager.PlayerManager;
import me.indian.bds.util.MinecraftUtil;
import me.indian.bds.util.StatusUtil;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.watchdog.WatchDog;

public class ServerProcess {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final Config config;
    private final DiscordIntegration discord;
    private final PlayerManager playerManager;
    private final ExecutorService processService;
    private final String prefix;
    private String finalFilePath;
    private ProcessBuilder processBuilder;
    private Process process;
    private PrintWriter writer;
    private String lastLine;
    private long startTime;
    private WatchDog watchDog;

    public ServerProcess(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.config = this.bdsAutoEnable.getConfig();
        this.discord = this.bdsAutoEnable.getDiscord();
        this.playerManager = this.bdsAutoEnable.getPlayerManager();
        this.processService = Executors.newScheduledThreadPool(ThreadUtil.getThreadsCount(), new ThreadUtil("Server process"));
        this.prefix = "&b[&3ServerProcess&b] ";
    }

    public void initWatchDog(final WatchDog watchDog) {
        this.watchDog = watchDog;
    }

    private boolean isProcessRunning() {
        try {
            String command = "";
            switch (this.config.getSystem()) {
                case LINUX -> command = "pgrep -f " + this.config.getFileName();
                case WINDOWS -> command = "tasklist /NH /FI \"IMAGENAME eq " + this.config.getFileName() + "\"";
                default -> {
                    this.logger.critical("Musisz podać odpowiedni system");
                    this.instantShutdown();
                }
            }

            final Process checkProcessIsRunning = Runtime.getRuntime().exec(command);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(checkProcessIsRunning.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty() && !line.equalsIgnoreCase("INFO: No tasks are running which match the specified criteria.")) {
                    return true;
                }
            }
            checkProcessIsRunning.waitFor();
            reader.close();
        } catch (final IOException | InterruptedException exception) {
            this.logger.critical(exception);
            exception.printStackTrace();
            this.instantShutdown();
        }
        return false;
    }

    public void startProcess() {
        this.finalFilePath = this.config.getFilesPath() + File.separator + this.config.getFileName();
        this.processService.execute(() -> {
            if (isProcessRunning()) {
                this.logger.info("Proces " + this.config.getFileName() + " jest już uruchomiony.");
                this.instantShutdown();
            } else {
                this.logger.info("Proces " + this.config.getFileName() + " nie jest uruchomiony. Uruchamianie...");
                try {
                    switch (this.config.getSystem()) {
                        case LINUX -> {
                            if (this.config.isWine()) {
                                this.processBuilder = new ProcessBuilder("wine", this.finalFilePath);
                            } else {
                                this.processBuilder = new ProcessBuilder("./" + this.config.getFileName());
                                this.processBuilder.environment().put("LD_LIBRARY_PATH", ".");
                                this.processBuilder.directory(new File(this.config.getFilesPath()));
                            }
                        }
                        case WINDOWS -> this.processBuilder = new ProcessBuilder(this.finalFilePath);
                        default -> {
                            this.logger.critical("Musisz podać odpowiedni system");
                            this.instantShutdown();
                        }
                    }
                    this.playerManager.clearPlayers();
                    this.process = this.processBuilder.start();
                    this.startTime = System.currentTimeMillis();
                    this.logger.info("Uruchomiono proces ");
                    this.discord.sendEnabledMessage();


                    final Thread output = new ThreadUtil("Console-Output").newThread(this::readConsoleOutput);
                    final Thread input = new ThreadUtil("Console-Input").newThread(this::writeConsoleInput);

                    output.start();
                    input.start();

                    this.logger.alert("Proces zakończony z kodem: " + this.process.waitFor());
                    output.interrupt();
                    input.interrupt();
                    this.discord.sendDisabledMessage();
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
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (!consoleOutput.hasNext()) continue;
                } catch (final IllegalStateException stateException) {
                    break;
                }

                final String line = consoleOutput.nextLine();
                if (line.isEmpty()) continue;
                if (!this.containsNotAllowedToFileLog(line)) {
                    this.logger.instantLogToFile(line);
                }
                if (!this.containsNotAllowedToConsoleLog(line)) {
                    System.out.println(line);
                    this.lastLine = line;
                    this.playerManager.initFromLog(line);
                }
            }

        } catch (final Exception exception) {
            this.logger.critical(exception);
            exception.printStackTrace();
            consoleOutput.close();
        } finally {
            Thread.currentThread().interrupt();
        }
    }

    private void writeConsoleInput() {
        final Scanner consoleInput = new Scanner(System.in);
        try {
            this.writer = new PrintWriter(this.process.getOutputStream());
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (!consoleInput.hasNext()) continue;
                } catch (final IllegalStateException stateException) {
                    break;
                }

                final String input = consoleInput.nextLine();
                switch (input.toLowerCase()) {
                    case "stop" -> {
                        MinecraftUtil.tellrawToAllAndLogger(this.prefix, "&4Zamykanie servera...", LogState.ALERT);
                        if (!this.playerManager.getOnlinePlayers().isEmpty()) {
                            for (final String name : this.playerManager.getOnlinePlayers()) {
                                this.sendToConsole(MinecraftUtil.kickCommand(name, this.prefix + "&cKtoś wykonał &astop &c w konsoli servera , \n co skutkuje  restartem"));
                            }
                        }
                        if (!Thread.currentThread().isInterrupted()) ThreadUtil.sleep(2);
                        this.sendToConsole("stop");
                    }
                    case "version" -> {
                        MinecraftUtil.tellrawToAllAndLogger(this.prefix, "&aVersija minecraft:&b " + this.config.getVersion(), LogState.INFO);
                        MinecraftUtil.tellrawToAllAndLogger(this.prefix, "&aVersija BDS-Auro-Enable:&b " + this.bdsAutoEnable.getProjectVersion(), LogState.INFO);
                    }
                    case "backup" -> this.watchDog.getBackupModule().forceBackup();
                    case ".end" -> this.endServerProcess(true);
                    case "test" -> {
                        final Map<Thread, StackTraceElement[]> allThreads = Thread.getAllStackTraces();

                        for (final Map.Entry<Thread, StackTraceElement[]> entry : allThreads.entrySet()) {
                            final Thread thread = entry.getKey();

                            System.out.println("Thread ID: " + thread.getId());
                            System.out.println("Thread Name: " + thread.getName());
                            System.out.println("Thread State: " + thread.getState());
                            System.out.println("Thread is Alive: " + thread.isAlive());
                            System.out.println("-----------------------------");
                        }
                    }
                    case "stats" -> {
                        for (final String s : StatusUtil.getStatus(false)) {
                            this.logger.info(s);
                        }
                    }
                    default -> this.sendToConsole(input);
                }
            }
        } catch (final Exception exception) {
            this.logger.critical(exception);
            exception.printStackTrace();
            consoleInput.close();
            this.writer.close();
        }
    }

    public void sendToConsole(final String command) {
        this.writer.println(command);
        this.writer.flush();
    }

    public String commandAndResponse(final String command)  {
        final String threadName = Thread.currentThread().getName();
        if (threadName.contains("Console") || threadName.contains("Server process")) {
            throw new IllegalThreadStateException("Nie możesz wykonac tego na tym wątku!");
        }
        this.sendToConsole(command);
        ThreadUtil.sleep(1);
        return this.lastLine == null ? "null" : this.lastLine;
    }

    public void instantShutdown() {
        this.logger.alert("Wyłączanie...");
        this.discord.sendDisablingMessage();
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

        if (this.process != null && this.process.isAlive()) this.watchDog.saveAndResume();

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
                this.discord.sendDestroyedMessage();
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
        this.discord.disableBot();
        Runtime.getRuntime().halt(129);
    }

    public void endServerProcess(final boolean backup) {
        if (this.process != null && this.process.isAlive()) {
            this.processService.execute(() -> {
                try {
                    if (backup) {
                        MinecraftUtil.tellrawToAllAndLogger(this.prefix, "&aWyłączanie servera , prosze poczekac pierw zostanie utworzony backup", LogState.ALERT);
                        this.watchDog.getBackupModule().forceBackup();
                    }
                    while (this.watchDog.getBackupModule().isBackuping()) {
                        //it so bugged without this , I don't know why
//                        this.logger.info("");
                    }
                    ThreadUtil.sleep(1);
                    while (!this.watchDog.getBackupModule().isBackuping()) {
                        MinecraftUtil.tellrawToAllAndLogger(this.prefix, "&aBackup zrobiony!", LogState.INFO);
                        ThreadUtil.sleep(1);
                        this.instantShutdown();
                    }
                } catch (final Exception exception) {
                    this.logger.critical(exception);
                    exception.printStackTrace();
                }
            });
        }
    }

    private boolean containsNotAllowedToFileLog(final String msg) {
        for (final String s : this.config.getNoLog().getFile()) {
            if (msg.toLowerCase().contains(s.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsNotAllowedToConsoleLog(final String msg) {
        for (final String s : this.config.getNoLog().getConsole()) {
            if (msg.toLowerCase().contains(s.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public Process getProcess() {
        return this.process;
    }
}
