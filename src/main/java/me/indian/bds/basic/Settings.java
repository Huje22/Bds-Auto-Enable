package me.indian.bds.basic;

import me.indian.bds.Main;
import me.indian.bds.config.Config;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.SystemOs;

import java.util.Arrays;
import java.util.Scanner;

public class Settings {


    public static String filePath;
    public static String name;
    public static SystemOs os;
    public static boolean wine;
    private final Logger logger;
    private final Config config;
    private final String jarPath;
    private boolean watchdog;
    private String worldName;
    private boolean backup;

    public Settings(final Config config) {
        this.jarPath = Main.getJarPath();
        this.logger = Main.getLogger();
        this.config = config;
    }


    public void loadSettings(final Scanner scanner) {
        if (!config.isFirstRun()) {
            logger.info("Zastosować wcześnejsze ustawienia? (true/false) (Enter = true) ");

            final String input = scanner.nextLine();
            boolean ustawienia = Boolean.parseBoolean(input.isEmpty() ? "true" : input);

            if (ustawienia) {
                os = config.getSystemOs();
                logger.info("System: " + os);

                name = config.getFileName();
                logger.info("FileName: " + name);

                wine = config.isWine();
                logger.info("IsWine: " + wine);

                filePath = config.getFilePath();
                logger.info("FilePath: " + filePath);

                watchdog = config.isWatchdog();
                logger.info("Watchdog: " + watchdog);

                backup = config.isBackup();
                logger.info("Backup: " + backup);

                worldName = config.getWorldName();
                logger.info("WorldName: " + worldName);

                logger.info("Kliknij enter przycisk aby kontunować");
                scanner.nextLine();

            } else {
                logger.info("Zaczynamy od nowa");
                init(scanner);
            }
            System.out.println();
        } else {
            init(scanner);
        }
    }

    private void init(final Scanner scanner) {
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
            logger.info("Uzyć wine? (true/false) (Enter = true): ");

            input = scanner.nextLine();
            wine = Boolean.parseBoolean(input.isEmpty() ? "true" : input);

            logger.info("Użycie wine ustawione na: " + wine);
            System.out.println();
        } else {
            wine = false;
        }

        logger.info("Podaj ścieżkę do plików servera (Domyślnie " + jarPath + "): ");
        input = scanner.nextLine();
        filePath = input.isEmpty() ? jarPath : input;
        System.out.println();


        logger.info("Włączyć WatchDog? (true/false) (Enter = true): ");
        input = scanner.nextLine();
        watchdog = Boolean.parseBoolean(input.isEmpty() ? "true" : input);
        logger.info("WatchDog ustawiony na: " + watchdog);
        System.out.println();

        if (watchdog) {
            logger.info("Podaj nazwę folderu świata (Domyślnie " + config.getWorldName() + "):");
            input = scanner.nextLine();
            worldName = input.isEmpty() ? "Bedrock level" : input;
            logger.info("Podany folder świata: " + worldName);
            System.out.println();

            logger.info("Włączyć Backupy? (true/false) (Enter = true): ");
            input = scanner.nextLine();
            backup = Boolean.parseBoolean(input.isEmpty() ? "true" : input);
            logger.info("Backupy ustawione na: " + backup);
            System.out.println();

        } else {
            worldName = "Bedrock level";
            backup = false;
        }

        config.setSystemOs(os);
        config.setFileName(name);
        config.setWine(wine);
        config.setFilePath(filePath);
        config.setWatchdog(watchdog);

        config.setWorldName(worldName);
        config.setBackup(backup);


        logger.info("Podane informacje:");
        logger.info("System: " + config.getSystemOs());
        logger.info("Nazwa pliku: " + config.getFileName());
        logger.info("Wine: " + config.isWine());
        logger.info("Ścieżka do plików servera: " + filePath);

        if (watchdog) {
            logger.info("World Name: " + config.getWorldName());


        }
        logger.info("Kliknij enter przycisk aby kontunować");
        scanner.nextLine();
    }
}