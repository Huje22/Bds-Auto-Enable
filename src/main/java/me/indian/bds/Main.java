package me.indian.bds;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import me.indian.bds.basic.Settings;
import me.indian.bds.config.Config;
import me.indian.bds.logger.impl.Logger;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.watchdog.WatchDog;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static me.indian.bds.basic.Settings.filePath;
import static me.indian.bds.basic.Settings.name;
import static me.indian.bds.basic.Settings.os;
import static me.indian.bds.basic.Settings.wine;

public class Main {


    private static Main instance;
    private static Scanner scanner;
    private static WatchDog watchDog;
    private static Logger logger;
    private static ExecutorService service;
    private static Config config;
    private static ProcessBuilder processBuilder;
    private static Process process;
    private static String jarPath;
    private static String finalFilePath;


    public Main() {
        instance = this;
        scanner = new Scanner(System.in);
        jarPath = getJarPath();
        logger = new Logger();
        config = ConfigManager.create(Config.class, (it) -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile("BDS-Auto-Enable-settings.yml");
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });

        service = Executors.newScheduledThreadPool(ThreadUtil.getThreadsCount(), new ThreadUtil("BDS Auto Enable"));
    }

    public static void main(String[] args) {
        new Main();
        new Settings(config).loadSettings(scanner);
        service.execute(() -> {
            watchDog = new WatchDog(config);
            watchDog.backup();

            finalFilePath = filePath + File.separator + name;

            final File file = new File(finalFilePath);
            if (file.exists()) {
                logger.info("Odnaleziono " + name);
            } else {
                logger.critical("Nie można odnaleźć pliku " + name + " na ścieżce " + filePath);
                return;
            }
            if (config.isFirstRun()) {
                config.setFirstRun(false);
            }
        });
        config.save();
        startProcess();
    }



    public static String getJarPath() {
        return System.getProperty("user.dir");
    }

    private static boolean isProcessRunning() {
        try {
            String command = "";

            switch (os) {
                case LINUX:
                    command = "pgrep -f " + name;
                    break;
                case WINDOWS:
                    command = "tasklist /NH /FI \"IMAGENAME eq " + name + "\"";
                    break;
                default:
                    logger.critical("Musisz podać odpowiedni system");
                    System.exit(0);
            }

            final Process process = Runtime.getRuntime().exec(command);
            process.waitFor();

            final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
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

    private static void startProcess() {
        if (isProcessRunning()) {
            logger.info("Proces " + name + " jest już uruchomiony.");
        } else {
            logger.info("Proces " + name + " nie jest uruchomiony. Uruchamianie...");

            try {
                String command;
                switch (os) {
                    case LINUX:
                        if (wine) {
                            command = "wine";
                        } else {
                            command = "LD_LIBRARY_PATH=.";
                        }
                        processBuilder = new ProcessBuilder(command, finalFilePath);
                        break;
                    case WINDOWS:
                        processBuilder = new ProcessBuilder(finalFilePath);
                        break;
                    default:
                        logger.critical("Musisz podać odpowiedni system");
                        shutdown();
                }

                final ProcessBuilder builder = processBuilder.inheritIO();
                logger.info(builder);

                process = processBuilder.start();


                final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info("Test" + line);
                }

                process.waitFor();
                startProcess();

            } catch (final Exception exception) {
                logger.critical("Nie można uruchomic procesu");
                logger.critical(exception);
                exception.printStackTrace();
                shutdown();
            }
        }
    }


    private static void shutdown() {
        watchDog.backup();
        config.save();
        service.shutdown();
        scanner.close();

        System.exit(0);
    }

    public static Logger getLogger() {
        return logger;
    }
}