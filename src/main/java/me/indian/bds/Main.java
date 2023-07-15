package me.indian.bds;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import me.indian.bds.config.Config;
import me.indian.bds.logger.impl.Logger;
import me.indian.bds.util.SystemOs;
import me.indian.bds.util.ThreadUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {


    private static Logger logger;
    private static ExecutorService service;
    private static Scanner scanner;
    private static Config config;
    private static ProcessBuilder processBuilder;
    private static String jarPath;
    private static String filePath;
    private static String finalFilePath;
    private static String name;
    private static SystemOs os;
    private static boolean wine;


    public Main() {
        jarPath = getJarPath();
        logger = new Logger();
        config = ConfigManager.create(Config.class, (it) -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile("config.yml");
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });

        service = Executors.newScheduledThreadPool(ThreadUtil.getThreadsCount(), new ThreadUtil("Auto restart"));
        scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        new Main();
        init();

        finalFilePath = filePath + File.separator + name;

        final File file = new File(finalFilePath);
        if (file.exists()) {
            logger.info("Odnaleziono " + name);
        } else {
            logger.critical("Nie można odnaleźć pliku " + name + " na ścieżce " + filePath);
            return;
        }

        config.save();
        startProcess();
    }

    private static void init() {
        String input;

        logger.info("Podaj system: ");
        logger.info("Obsługiwane systemy: " + Arrays.toString(SystemOs.values()));

        input = scanner.nextLine();
        os = input.isEmpty() ? SystemOs.LINUX : SystemOs.valueOf(input.toUpperCase());
        logger.info("System ustawiony na: " + os);
        System.out.println();

        logger.info("Podaj nazwę pliku (Domyślnie: bedrock_server.exe): ");
        input = scanner.nextLine();
        name = input.isEmpty() ? "bedrock_server.exe" : input;
        logger.info("Nazwa pliku ustawiona na: " + name);
        System.out.println();


        if (os != SystemOs.WINDOWS) {
            logger.info("Uzyć wine? (true/false): ");
            wine = scanner.nextBoolean();
            logger.info("Użycie wine ustawione na: " + wine);
            System.out.println();
        } else {
            wine = false;
        }

        logger.info("Podaj ścieżkę do pliku (Domyślnie " + jarPath + "): ");
        input = scanner.nextLine();
        filePath = input.isEmpty() ? jarPath : input;
        System.out.println();

        logger.info("Podane informacje:");
        logger.info("System: " + os);
        config.setSystemOs(os);
        logger.info("Nazwa: " + name);
        config.setName(name);
        logger.info("Wine: " + wine);
        config.setWine(wine);
        logger.info("Ścieżka do pliku: " + filePath);
        config.setFilePath(filePath);

        logger.info("Kliknij enter przycisk aby kontunować");
        scanner.nextLine();


    }


    public static String getJarPath() {
        return System.getProperty("user.dir");
    }

    public static boolean isProcessRunning() {
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

    public static void startProcess() {
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
                        exit();
                }

                final ProcessBuilder builder = processBuilder.inheritIO();
                logger.info(builder);

                final Process process = processBuilder.start();
                final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }

                process.waitFor();
                startProcess();

            } catch (final Exception exception) {
                logger.critical("Nie można uruchomic procesu");
                logger.critical(exception);
                exception.printStackTrace();
                exit();
            }
        }
    }

    public static void exit() {

        System.exit(0);
    }
}