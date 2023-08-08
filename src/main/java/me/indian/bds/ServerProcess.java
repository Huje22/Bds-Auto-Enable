package me.indian.bds;

import me.indian.bds.basic.Settings;
import me.indian.bds.config.Config;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.MinecraftUtil;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.watchdog.WatchDog;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerProcess {

    private WatchDog watchDog;
    private final Logger logger;
    private final Config config;
    private final ExecutorService service;
    private final ExecutorService consoleService;
    private final Settings settings;
    private final String finalFilePath;
    private ProcessBuilder processBuilder;
    private Process process;
    private PrintWriter writer;

    public ServerProcess(final BDSAutoEnable bdsAutoEnable) {
        this.config = bdsAutoEnable.getConfig();
        this.logger = bdsAutoEnable.getLogger();
        this.service = Executors.newScheduledThreadPool(ThreadUtil.getThreadsCount(), new ThreadUtil("Server process"));
        this.consoleService = Executors.newScheduledThreadPool(ThreadUtil.getThreadsCount(), new ThreadUtil("Console"));
        this.settings = bdsAutoEnable.getSettings();
        this.finalFilePath = this.config.getFilesPath() + File.separator + this.config.getFileName();
    }

    public void initWatchDog(final WatchDog watchDog){
        this.watchDog = watchDog;
    }

    private boolean isProcessRunning() {
        BufferedReader reader:
        try {
            String command = "";
            switch (this.settings.getOs()) {
                case LINUX:
                    command = "pgrep -f " + this.settings.getName();
                    break;
                case WINDOWS:
                    command = "tasklist /NH /FI \"IMAGENAME eq " + this.settings.getName() + "\"";
                    break;
                default:
                    this.logger.critical("Musisz podać odpowiedni system");
                    this.shutdown(false);
            }

            final Process checkProcessIsRunning = Runtime.getRuntime().exec(command);
            checkProcessIsRunning.waitFor();

            reader = new BufferedReader(new InputStreamReader(checkProcessIsRunning.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty() && !line.equalsIgnoreCase("INFO: No tasks are running which match the specified criteria.")) {
                    return true;
                }
            }
             reader.close();
        } catch (IOException | InterruptedException exception) {
            reader.close();
            this.logger.critical(exception);
            exception.printStackTrace();
        this.shutdown(false);
        }
        return false;
    }

    public void startProcess() {
        this.service.execute(() -> {
            if (isProcessRunning()) {
                this.logger.info("Proces " + this.settings.getName() + " jest już uruchomiony.");
                this.shutdown(false);
            } else {
                this.logger.info("Proces " + this.settings.getName() + " nie jest uruchomiony. Uruchamianie...");

                try {
                    switch (this.settings.getOs()) {
                        case LINUX:
                            if (this.settings.isWine()) {
                                this.processBuilder = new ProcessBuilder("wine", finalFilePath);
                            } else {
                                this.processBuilder = new ProcessBuilder("./" + this.settings.getName());
                                this.processBuilder.environment().put("LD_LIBRARY_PATH", ".");
                                this.processBuilder.directory(new File(this.settings.getFilePath()));
                            }
                            break;
                        case WINDOWS:
                            this.processBuilder = new ProcessBuilder(finalFilePath);
                            break;
                        default:
                            this.logger.critical("Musisz podać odpowiedni system");
                            this.shutdown(false);
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
                    this.shutdown(false);
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
                this.logger.consoleToFile(line);
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
                } else if (input.equalsIgnoreCase("backup")) {
                    this.logger.info("Tworzenie backupa!");
                    this.watchDog.forceBackup();
                }
                this.sendToConsole(input);
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

    public void shutdown(final boolean backup) {
        this.service.shutdown();
        this.consoleService.shutdown();
        this.writer.close();
        this.config.save();
        this.endServerProcess(backup);
        this.logger.alert("Wyłączono");
        System.exit(0);
    }

    public void endServerProcess(final boolean backup) {
        if (this.process != null && this.process.isAlive()) {
            this.service.execute(() -> {
                try {
                    if (backup) {
                        this.watchDog.forceBackup();
                    }
                    ThreadUtil.sleep(10);
                    this.process.destroy();
                    this.logger.alert("Zakończono proces servera");
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
