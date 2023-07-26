package me.indian.bds;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import me.indian.bds.basic.Defaults;
import me.indian.bds.basic.Settings;
import me.indian.bds.config.Config;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.MinecraftColor;
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


public class BDSAutoEnable {


    private final Scanner scanner;
    private final Logger logger;
    private final Config config;
    private final ExecutorService service;
    private final Settings settings;
    private WatchDog watchDog;
    private ProcessBuilder processBuilder;
    private Process process;
    private PrintWriter writer;
    private String finalFilePath;


    public BDSAutoEnable() {
        this.scanner = new Scanner(System.in);
        this.config = ConfigManager.create(Config.class, (it) -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile("BDS-Auto-Enable/config.yml");
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });
        this.logger = new Logger(this.config);
        this.service = Executors.newScheduledThreadPool(ThreadUtil.getThreadsCount(), new ThreadUtil("BDS Auto Enable"));
        this.settings = new Settings(this);
        Defaults.init(this);
        this.init();
    }


    public static void main(String[] args) {
        new BDSAutoEnable();
    }

    public void init() {
        this.settings.loadSettings(this.scanner);
        this.watchDog = new WatchDog(this);
        this.watchDog.backup();
        this.watchDog.forceBackup();
        this.finalFilePath = this.settings.getFilePath() + File.separator + this.settings.getName();
        final File file = new File(this.finalFilePath);
        if (file.exists()) {
            this.logger.info("Odnaleziono " + this.settings.getName());
        } else {
            this.logger.critical("Nie można odnaleźć pliku " + this.settings.getName() + " na ścieżce " + this.settings.getFilePath());
            System.exit(0);
        }

        this.config.save();

        Runtime.getRuntime().addShutdownHook(new ThreadUtil("Shutdown", () -> {
            this.logger.alert("Wykonuje się przed zakończeniem programu...");
            this.watchDog.forceBackup();
            this.config.save();
            this.service.shutdown();
            this.scanner.close();
            this.process.destroy();
        }));

        this.startProcess();
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

    private void startProcess() {
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
                    this.writeConsoleInput();

                    this.logger.alert("Proces zakończony z kodem: " + this.process.waitFor());
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
                if (line.equalsIgnoreCase("Quit correctly") || line.equalsIgnoreCase("crash")) {
                    this.restartServerProcess();
                }
            }
        } catch (final Exception exception) {
            this.logger.critical(exception);
            exception.printStackTrace();
        }
    }

    public void writeConsoleInput() {
        try {
            final OutputStream outputStream = process.getOutputStream();
            this.writer = new PrintWriter(outputStream);

            final Scanner console = new Scanner(System.in);
            while (true) {
                final String input = console.nextLine();
                if (input.equalsIgnoreCase("stop")) {
                    this.sendCommandToConsole(MinecraftColor.colorize("say &4Zamykanie servera..."));
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

    private void shutdown() {
        this.watchDog.forceBackup();
        this.config.save();
        this.service.shutdown();
        this.scanner.close();
        endServerProcess();

        this.logger.alert("Wyłączono");
        System.exit(0);
    }

    private void endServerProcess() {
        if (this.process != null && this.process.isAlive()) {
            this.service.execute(() -> {
                try {
                    this.logger.info("Czekanie 10sekund..");
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

    private void restartServerProcess() {
        if (this.process != null && this.process.isAlive()) {
            this.service.execute(() -> {
                try {
                    int timeoutInSeconds = 10;
                    boolean processCompleted = process.waitFor(timeoutInSeconds, java.util.concurrent.TimeUnit.SECONDS);
                    logger.info("Czekanie na zakończnie procesu servera..");
                    if (!processCompleted) {
                        process.destroy();
                        logger.alert("Zakończono proces servera");
                    }
                    this.startProcess();
                } catch (final Exception e) {
                    this.logger.critical(e);
                    e.printStackTrace();
                }
            });
        } else {
            this.startProcess();
        }
    }

    public Config getConfig() {
        return config;
    }

    public Logger getLogger() {
        return logger;
    }
}