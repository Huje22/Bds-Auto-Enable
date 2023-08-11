package me.indian.bds;

import me.indian.bds.basic.Settings;
import me.indian.bds.config.Config;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.ConsoleColors;
import me.indian.bds.util.MinecraftUtil;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.watchdog.WatchDog;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerProcess {

    private WatchDog watchDog;
    private final Logger logger;
    private final Config config;
    private final ExecutorService processService;
    private final ExecutorService consoleService;
    private final Settings settings;
    private final String finalFilePath;
    private final String prefix;
    private ProcessBuilder processBuilder;
    private Process process;
    private PrintWriter writer;

    public ServerProcess(final BDSAutoEnable bdsAutoEnable) {
        this.config = bdsAutoEnable.getConfig();
        this.logger = bdsAutoEnable.getLogger();
        this.processService = Executors.newScheduledThreadPool(2, new ThreadUtil("Server process"));
        this.consoleService = Executors.newScheduledThreadPool(2, new ThreadUtil("Console"));
        this.settings = bdsAutoEnable.getSettings();
        this.finalFilePath = this.config.getFilesPath() + File.separator + this.config.getFileName();
        this.prefix = "&b[&3ServerProcess&b] ";
    }

    public void initWatchDog(final WatchDog watchDog){
        this.watchDog = watchDog;
    }

    private boolean isProcessRunning() {
        BufferedReader reader = null;
        try {
            String command = "";
            switch (this.settings.getOs()) {
                case LINUX:
                    command = "pgrep -f " + this.settings.getFileName();
                    break;
                case WINDOWS:
                    command = "tasklist /NH /FI \"IMAGENAME eq " + this.settings.getFileName() + "\"";
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
        final Scanner console = new Scanner(this.process.getInputStream());
        try {
            while (console.hasNextLine()) {
                if (!console.hasNext()) continue;
                final String line = console.nextLine();
                if (this.containsNotAllowedToLog(line) || line.isEmpty()) continue;
                System.out.println(line);
                this.logger.instantLogToFile(line);
            }
        } catch (final Exception exception) {
            this.logger.critical(exception);
            exception.printStackTrace();
            console.close();
            ThreadUtil.sleep(1);
            this.readConsoleOutput();
        }
    }

    private void writeConsoleInput() {
        final Scanner console = new Scanner(System.in);
        try {
            this.writer = new PrintWriter(this.process.getOutputStream());
            while (!Thread.currentThread().isInterrupted()) {
                final String input = console.nextLine();
                if (input.equalsIgnoreCase("stop")) {
                    this.sendToConsole(MinecraftUtil.colorize("say &4Zamykanie servera..."));
                    ThreadUtil.sleep(3);
                    this.sendToConsole("stop");
                } else if (input.equalsIgnoreCase("backup")) {
                    this.logger.info("Tworzenie backupa!");
                    this.watchDog.forceBackup();
                } else if (input.equalsIgnoreCase(".end")) {
                    this.endServerProcess(true);
                } else {
                    this.sendToConsole(input);
                }
            }
        } catch (final NoSuchElementException noSuchElementException) {
            this.logger.critical(noSuchElementException);
            noSuchElementException.printStackTrace();
            console.close();
            this.writer.close();
            ThreadUtil.sleep(1);
            Thread.currentThread().interrupt();
        }
    }

    public void sendToConsole(String command) {
        this.writer.println(command);
        this.writer.flush();
    }

    public void instantShutdown() {
        this.logger.alert("Wyłączanie...");
        if (this.process != null && this.process.isAlive()) this.process.destroy();
        if (this.consoleService != null && !this.consoleService.isTerminated()) this.consoleService.shutdown();
        if (this.writer != null) this.writer.close();
        if (this.processService != null && !this.processService.isTerminated()) this.processService.shutdown();
        this.config.save();
        ThreadUtil.sleep(5);
        System.exit(1);
    }

    public void endServerProcess(final boolean backup) {
        if (this.process != null && this.process.isAlive()) {
            this.processService.execute(() -> {
                final int endTime = (int) this.watchDog.getLastBackupTime() + 2;
                this.logger.warning("Wyłączanie servera , prosze poczekac " + ConsoleColors.GREEN + endTime + ConsoleColors.RESET + " sekund....");
                this.sendToConsole(MinecraftUtil.tellrawToAllMessage(this.prefix + "&aWyłączanie servera , prosze poczekac&b " + endTime + "&a sekund...."));
                try {
                    if (backup) {
                        this.watchDog.forceBackup();
                    }
                    ThreadUtil.sleep(endTime);
                    this.process.destroy();
                    this.consoleService.shutdown();
                    this.writer.close();
                    this.config.save();
                    this.processService.shutdown();
                    this.logger.alert("Zakończono proces servera");
                    System.exit(1);
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
