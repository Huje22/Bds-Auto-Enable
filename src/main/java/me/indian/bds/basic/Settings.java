package me.indian.bds.basic;

import me.indian.bds.Main;
import me.indian.bds.config.Config;
import me.indian.bds.logger.Logger;
import me.indian.bds.logger.ServerLogType;
import me.indian.bds.util.ScannerUtil;
import me.indian.bds.util.SystemOs;

import java.util.Arrays;
import java.util.Scanner;

public class Settings {


    public static String filePath;
    public static String name;
    public static SystemOs os;
    public static ServerLogType serverLogType;
    public static boolean wine;
    private final Logger logger;
    private final Config config;
    private boolean watchdog;
    private String worldName;
    private boolean backup;

    public Settings(final Config config) {
        this.logger = Main.getLogger();
        this.config = config;
    }


    public void loadSettings(final Scanner scanner) {
        if (!this.config.isFirstRun()) {
            logger.info("Zastosować wcześnejsze ustawienia? (true/false) (Enter = true) ");

            final String input = scanner.nextLine();
            boolean ustawienia = Boolean.parseBoolean(input.isEmpty() ? "true" : input);

            if (ustawienia) {
                this.currentSettings(scanner);
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
        ScannerUtil scannerUtil = new ScannerUtil(logger, scanner);

        final String enter = "[Enter = Domyślnie]";

        ServerLogType serverLogType;
        try {
            serverLogType = ServerLogType.valueOf(
                    scannerUtil.addQuestion(
                            (defaultValue) -> {
                                logger.info("Gdzie zapisywać wyjście konsoli (Domyślnie: " + defaultValue + "): " + enter);
                                logger.info("Dostępne wyjścia: " + Arrays.toString(ServerLogType.values()));
                            },
                            String.valueOf(ServerLogType.FILE),
                            (input) -> logger.info("System ustawiony na: " + input.toUpperCase())
                    ).toUpperCase());
        } catch (IllegalArgumentException exception) {
            logger.error("Nie znane wyjście konsoli, ustawiono domyślnie na: FILE");
            serverLogType = ServerLogType.FILE;
        }
        this.config.setServerLogType(serverLogType);

        SystemOs system;
        try {
            system = SystemOs.valueOf(
                    scannerUtil.addQuestion(
                            (defaultValue) -> {
                                logger.info("Podaj system (Wykryty system: " + defaultValue + "): " + enter);
                                logger.info("Obsługiwane systemy: " + Arrays.toString(SystemOs.values()));
                            },
                            Defaults.getSystem(),
                            (input) -> logger.info("System ustawiony na: " + input.toUpperCase())
                    ).toUpperCase());
        } catch (IllegalArgumentException exception) {
            logger.error("Podano nie znany system , ustawiono domyślnie na: LINUX");
            system = SystemOs.LINUX;
        }
        this.config.setSystemOs(system);
        this.config.setFileName(scannerUtil.addQuestion(
                (defaultValue) -> logger.info("Podaj nazwę pliku (Domyślnie: " + defaultValue + "): " + enter),
                Defaults.getDefaultFileName(),
                (input) -> logger.info("Nazwa pliku ustawiona na: " + input)
        ));
        if (this.config.getSystemOs() == SystemOs.LINUX) {
            if (this.config.getFileName().contains(".exe")) {
                logger.alert("W tym wypadku będzie potrzebne WINE");
                this.config.setWine(true);
            } else {
                this.config.setWine(false);
            }
        }
        this.config.setFilesPath(scannerUtil.addQuestion(
                (defaultValue) -> logger.info("Podaj ścieżkę do plików servera (Domyślnie: " + defaultValue + "): " + enter),
                Defaults.getJarPath(),
                (input) -> logger.info("Ścieżke do plików servera ustawiona na: " + input)
        ));
        this.config.setWatchdog(scannerUtil.addQuestion(
                (defaultValue) -> logger.info("Włączyć Watchdog (Domyślnie: " + defaultValue + ")? " + enter),
                true,
                (input) -> logger.info("Watchdog ustawiony na: " + input)
        ));
        if (this.config.isWatchdog()) {
            this.config.setBackup(scannerUtil.addQuestion(
                    (defaultValue) -> logger.info("Włączyć Backupy (Domyślnie: " + defaultValue + ")? " + enter),
                    true,
                    (input) -> logger.info("Backupy ustawione na: " + input)
            ));
            this.config.setWorldName(scannerUtil.addQuestion(
                    (defaultValue) -> logger.info("Podaj nazwę folderu świata (Domyślnie: " + defaultValue + "): " + enter),
                    "Bedrock level",
                    (input) -> logger.info("Podany folder świata: " + input)
            ));
        } else {
            this.config.setWorldName("Bedrock level");
            this.config.setBackup(false);
        }
        this.currentSettings(scanner);
    }

    private void currentSettings(Scanner scanner) {
        serverLogType = this.config.getServerLogType();
        logger.info("Wyjście konsoli: " + serverLogType);

        os = this.config.getSystemOs();
        logger.info("System: " + os);

        name = this.config.getFileName();
        logger.info("FileName: " + name);

        wine = this.config.isWine();
        logger.info("IsWine: " + wine);

        filePath = this.config.getFilesPath();
        logger.info("FilePath: " + filePath);

        watchdog = this.config.isWatchdog();
        logger.info("Watchdog: " + watchdog);

        backup = this.config.isBackup();
        logger.info("Backup: " + backup);

        worldName = this.config.getWorldName();
        logger.info("WorldName: " + worldName);

        logger.info("Kliknij enter przycisk aby kontunować");
        scanner.nextLine();

        if (config.isFirstRun()) {
            config.setFirstRun(false);
        }

        config.save();
    }
}