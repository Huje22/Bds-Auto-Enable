package me.indian.bds;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import me.indian.bds.basic.Settings;
import me.indian.bds.config.Config;
import me.indian.bds.logger.Logger;
import me.indian.bds.logger.ServerLogType;
import me.indian.bds.util.ConsoleColors;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.watchdog.WatchDog;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
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
    private static String finalFilePath;


    public Main() {
        instance = this;
        scanner = new Scanner(System.in);
        config = ConfigManager.create(Config.class, (it) -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile("BDS-Auto-Enable/config.yml");
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });
        logger = new Logger(config);
        service = Executors.newScheduledThreadPool(ThreadUtil.getThreadsCount(), new ThreadUtil("BDS Auto Enable"));
    }


    public static void main(String[] args) {
        new Main();
        new Settings(config).loadSettings(scanner);
        watchDog = new WatchDog(config);
        watchDog.backup();
        watchDog.forceBackup();
        finalFilePath = filePath + File.separator + name;
        final File file = new File(finalFilePath);
        if (file.exists()) {
            logger.info("Odnaleziono " + name);
        } else {
            logger.critical("Nie można odnaleźć pliku " + name + " na ścieżce " + filePath);
            System.exit(0);
        }

        config.save();

        Runtime.getRuntime().addShutdownHook(new ThreadUtil("Shutdown", () -> {
            logger.alert("Wykonuje się przed zakończeniem programu...");
            watchDog.forceBackup();
            config.save();
            service.shutdown();
            scanner.close();
            process.destroy();
        }));

        startProcess();
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

    private static void startProcess() {
        service.execute(() ->{
        if (isProcessRunning()) {
            logger.info("Proces " + name + " jest już uruchomiony.");
        } else {
            logger.info("Proces " + name + " nie jest uruchomiony. Uruchamianie...");

            try {
                switch (os) {
                    case LINUX:
                        if (wine) {
                            processBuilder = new ProcessBuilder("wine", finalFilePath);
                        } else {
                            processBuilder = new ProcessBuilder("./" + name);
                            processBuilder.environment().put("LD_LIBRARY_PATH", ".");
                            processBuilder.directory(new File(filePath));
                        }
                        break;
                    case WINDOWS:
                        processBuilder = new ProcessBuilder(finalFilePath);
                        break;
                    default:
                        logger.critical("Musisz podać odpowiedni system");
                        shutdown();
                }

                if (Settings.serverLogType == ServerLogType.FILE) {
                    final File logFile = logger.getLogFile();
                    processBuilder.redirectOutput(logFile);
                    processBuilder.redirectError(logFile);
                } else if (Settings.serverLogType == ServerLogType.CONSOLE) {
                    processBuilder.inheritIO();
                }

                process = processBuilder.start();
                logger.info("Uruchomiono proces (nadal może on sie wyłączyć)");

                new ThreadUtil("Exit scanner", () ->{
                final Scanner exit = new Scanner(System.in);
                logger.alert("Wpisz: " + ConsoleColors.GREEN + "end" + ConsoleColors.RESET + " aby zakończyć działanie programu");
                String input;
                do {
                    input = exit.nextLine();
                    logger.info("Wprowadzono: " + input);
                } while (!input.equalsIgnoreCase("end"));
                logger.info("Wyłączanie...");
                    shutdown();
                }).newThread().start();

                InputStream stdout = process.getInputStream();
                logger.debug(stdout);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = stdout.read(buffer)) != -1) {
                    String output = new String(buffer, 0, bytesRead);
                    logger.info(output);
                }

                logger.alert("Proces zakończony z kodem: " + process.waitFor());
                startProcess();

            } catch (final Exception exception) {
                logger.critical("Nie można uruchomic procesu");
                logger.critical(exception);
                exception.printStackTrace();
                shutdown();
            }
        }
        });
    }


    private static void shutdown() {
        watchDog.forceBackup();
        config.save();
        service.shutdown();
        scanner.close();
        process.destroy();

        System.exit(0);
    }


    public static Config getConfig() {
        return config;
    }

    public static Logger getLogger() {
        return logger;
    }
}