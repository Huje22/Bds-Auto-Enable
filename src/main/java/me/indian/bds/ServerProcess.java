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
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerProcess {

    private WatchDog watchDog;
    private final Logger logger;
    private final Config config;
    private final ExecutorService service;
    private final Settings settings;
    private final String finalFilePath;
    private ProcessBuilder processBuilder;
    private Process process;
    private PrintWriter writer;

    public ServerProcess(final BDSAutoEnable bdsAutoEnable) {
        this.config = bdsAutoEnable.getConfig();
        this.logger = bdsAutoEnable.getLogger();
        this.service = Executors.newScheduledThreadPool(ThreadUtil.getThreadsCount(), new ThreadUtil("Server process"));
        this.settings = bdsAutoEnable.getSettings();
        this.finalFilePath = this.config.getFilesPath() + File.separator + this.config.getFileName();
    }

    public void initWatchDog(final WatchDog watchDog){
        this.watchDog = watchDog;
    }

    private boolean isProcessRunning() {
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
                    System.exit(0);
            }

            final Process checkProcessIsRunning = Runtime.getRuntime().exec(command);
            checkProcessIsRunning.waitFor();

            final BufferedReader reader = new BufferedReader(new InputStreamReader(checkProcessIsRunning.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty() && !line.equalsIgnoreCase("INFO: No tasks are running which match the specified criteria.")) {
                    return true;
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void startProcess() {
        this.service.execute(() -> {
            if (isProcessRunning()) {
                this.logger.info("Proces " + this.settings.getName() + " jest już uruchomiony.");
                System.exit(0);
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
                            this.shutdown();
                    }
                    this.process = processBuilder.start();
                    this.logger.info("Uruchomiono proces (nadal może on sie wyłączyć)");

                    new ThreadUtil("Console", this::readConsoleOutput).newThread().start();
                    new ThreadUtil("Console", this::writeConsoleInput).newThread().start();

                    this.logger.alert("Proces zakończony z kodem: " + this.process.waitFor());
                    ThreadUtil.sleep(5);
                    this.startProcess();

                } catch (final Exception exception) {
                    this.logger.critical("Nie można uruchomic procesu");
                    this.logger.critical(exception);
                    exception.printStackTrace();
                    shutdown();
                }
            }
        });
    }

    public void readConsoleOutput() {
        try {
            final InputStream inputStream = this.process.getInputStream();
            final Scanner console = new Scanner(inputStream);

            while (console.hasNextLine()) {
                if (!console.hasNext()) continue;
                final String line = console.nextLine();
                System.out.println(line);
                this.logger.consoleToFile(line);
            }
        } catch (final Exception exception) {
            this.logger.critical(exception);
            exception.printStackTrace();
        }
    }

    public void writeConsoleInput() {
        try {
            final OutputStream outputStream = this.process.getOutputStream();
            this.writer = new PrintWriter(outputStream);

            final Scanner console = new Scanner(System.in);
            while (true) {
                final String input = console.nextLine();
                if (input.equalsIgnoreCase("stop")) {
                    this.sendCommandToConsole(MinecraftUtil.colorize("say &4Zamykanie servera..."));
                } else if (input.equalsIgnoreCase("backup")) {
                    this.logger.info("Tworzenie backupa!");
                    this.watchDog.forceBackup();
                }
                this.writer.println(input);
                this.writer.flush();
            }
        } catch (final Exception exception) {
            this.logger.critical(exception);
            exception.printStackTrace();
        }
    }

    public void sendCommandToConsole(String command) {
        this.writer.println(command);
        this.writer.flush();
    }

    public void shutdown() {
        this.watchDog.forceBackup();
        this.config.save();
        this.service.shutdown();
        this.writer.close();
        this.endServerProcess();
        this.logger.alert("Wyłączono");
        System.exit(0);
    }

    public void endServerProcess() {
        if (this.process != null && this.process.isAlive()) {
            this.service.execute(() -> {
                try {
                    this.watchDog.forceBackup();
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
}
