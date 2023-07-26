package me.indian.bds.basic;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.Config;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.ScannerUtil;
import me.indian.bds.util.SystemOs;

import java.util.Arrays;
import java.util.Scanner;

public class Settings {


    private final Logger logger;
    private final Config config;
    private String filePath;
    private String name;
    private SystemOs os;
    private boolean wine;
    private boolean watchdog;
    private boolean backup;

    public Settings(final BDSAutoEnable bdsAutoEnable) {
        this.logger = bdsAutoEnable.getLogger();
        this.config = bdsAutoEnable.getConfig();
    }

    public String getFilePath() {
        return this.filePath;
    }

    public String getName() {
        return this.name;
    }

    public SystemOs getOs() {
        return this.os;
    }

    public boolean isWine() {
        return this.wine;
    }

    private boolean isWatchdog() {
        return this.watchdog;
    }

    private boolean isBackup() {
        return this.backup;
    }

    public void loadSettings(final Scanner scanner) {
        if (!this.config.isFirstRun()) {
            this.logger.info("Zastosować wcześnejsze ustawienia? (true/false) (Enter = true) ");

            final String input = scanner.nextLine();
            final boolean ustawienia = Boolean.parseBoolean(input.isEmpty() ? "true" : input);

            if (ustawienia) {
                this.currentSettings(scanner);
            } else {
                this.logger.info("Zaczynamy od nowa");
                this.init(scanner);
            }
        } else {
            this.init(scanner);
        }
    }

    private void init(final Scanner scanner) {
        final ScannerUtil scannerUtil = new ScannerUtil(logger, scanner);

        final String enter = "[Enter = Domyślnie]";
        SystemOs system;
        try {
            system = SystemOs.valueOf(
                    scannerUtil.addQuestion(
                            (defaultValue) -> {
                                this.logger.info("Podaj system (Wykryty system: " + defaultValue + "): " + enter);
                                this.logger.info("Obsługiwane systemy: " + Arrays.toString(SystemOs.values()));
                            },
                            Defaults.getSystem(),
                            (input) -> this.logger.info("System ustawiony na: " + input.toUpperCase())
                    ).toUpperCase());
        } catch (IllegalArgumentException exception) {
            this.logger.error("Podano nie znany system , ustawiono domyślnie na: LINUX");
            system = SystemOs.LINUX;
        }
        this.config.setSystemOs(system);
        this.config.setFileName(scannerUtil.addQuestion(
                (defaultValue) -> this.logger.info("Podaj nazwę pliku (Domyślnie: " + defaultValue + "): " + enter),
                Defaults.getDefaultFileName(),
                (input) -> this.logger.info("Nazwa pliku ustawiona na: " + input)
        ));
        if (this.config.getSystemOs() == SystemOs.LINUX) {
            if (this.config.getFileName().contains(".exe")) {
                this.logger.alert("W tym wypadku będzie potrzebne WINE");
                this.config.setWine(true);
            } else {
                this.config.setWine(false);
            }
        }
        this.config.setFilesPath(scannerUtil.addQuestion(
                (defaultValue) -> this.logger.info("Podaj ścieżkę do plików servera (Domyślnie: " + defaultValue + "): " + enter),
                Defaults.getJarPath(),
                (input) -> this.logger.info("Ścieżke do plików servera ustawiona na: " + input)
        ));
        this.config.setWatchdog(scannerUtil.addQuestion(
                (defaultValue) -> this.logger.info("Włączyć Watchdog (Domyślnie: " + defaultValue + ")? " + enter),
                true,
                (input) -> this.logger.info("Watchdog ustawiony na: " + input)
        ));
        if (this.config.isWatchdog()) {
            this.config.setBackup(scannerUtil.addQuestion(
                    (defaultValue) -> this.logger.info("Włączyć Backupy (Domyślnie: " + defaultValue + ")? " + enter),
                    true,
                    (input) -> this.logger.info("Backupy ustawione na: " + input)
            ));
        } else {
            this.config.setBackup(false);
        }
        this.currentSettings(scanner);
    }

    private void currentSettings(final Scanner scanner) {
        os = this.config.getSystemOs();
        this.logger.info("System: " + os);

        name = this.config.getFileName();
        this.logger.info("FileName: " + name);

        wine = this.config.isWine();
        this.logger.info("IsWine: " + wine);

        filePath = this.config.getFilesPath();
        this.logger.info("FilePath: " + filePath);

        watchdog = this.config.isWatchdog();
        this.logger.info("Watchdog: " + watchdog);

        backup = this.config.isBackup();
        this.logger.info("Backup: " + backup);

        if(backup) {
            this.logger.info("WorldName: " + Defaults.getWorldName());
        }

        this.logger.info("Kliknij enter przycisk aby kontunować");
        scanner.nextLine();

        if (this.config.isFirstRun()) {
            this.config.setFirstRun(false);
        }
        this.config.save();
    }
}