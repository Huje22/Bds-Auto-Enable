package me.indian.bds.basic;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.Config;
import me.indian.bds.file.ServerProperties;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.ConsoleColors;
import me.indian.bds.util.ScannerUtil;
import me.indian.bds.util.SystemOs;
import me.indian.bds.util.ThreadUtil;

import java.util.Arrays;
import java.util.Scanner;

public class Settings {

    private final BDSAutoEnable bdsAutoEnable;
    private final ServerProperties serverProperties;
    private final Logger logger;
    private final Config config;
    private String filePath;
    private String fileName;
    private SystemOs os;
    private boolean wine;
    private boolean backup;

    public Settings(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.serverProperties = this.bdsAutoEnable.getServerProperties();
        this.logger = this.bdsAutoEnable.getLogger();
        this.config = this.bdsAutoEnable.getConfig();
    }

    public void loadSettings(final Scanner scanner) {
        if (!this.config.isFirstRun()) {
            this.logger.info("Zastosować wcześnejsze ustawienia? (true/false) (Enter = true) ");

            final String input = scanner.nextLine();
            final boolean ustawienia = Boolean.parseBoolean(input.isEmpty() ? "true" : input);

            if (ustawienia) {
                this.serverProperties.loadProperties();
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
        final ScannerUtil scannerUtil = new ScannerUtil(scanner);
        final String enter = "[Enter = Domyślnie]";
        final long startTime = System.currentTimeMillis();

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

        this.serverProperties.loadProperties();

        this.config.setBackup(scannerUtil.addQuestion(
                (defaultValue) -> this.logger.info("Włączyć Backupy (Domyślnie: " + defaultValue + ")? " + enter),
                true,
                (input) -> this.logger.info("Backupy ustawione na: " + input)
        ));
        final int threads = scannerUtil.addQuestion(
                (defaultValue) -> {
                    this.logger.info("Liczba wątków używana przez server (Dostępna liczba wątków " + ThreadUtil.getThreadsCount() + ")? ");
                    this.logger.info("Maksymalna liczba wątków, jakie serwer będzie próbował wykorzystać, Jeśli ustawione na 0 wtedy będzie używać najwięcej jak to możliwe.");
                },
                0,
                (input) -> this.logger.info("Liczba wątków ustawiona na: " + (Integer.parseInt(input) <= -1 ? 0 : input)));

        this.serverProperties.setMaxThreads(threads <= -1 ? 0 : threads);
        this.serverProperties.setClientSideChunkGeneration(scannerUtil.addQuestion(
                (defaultValue) -> {
                    this.logger.info("Client Side Chunks (Domyślnie: " + defaultValue + ")? " + enter);
                    this.logger.info("Jeśli jest " + ConsoleColors.BLUE + "true" + ConsoleColors.RESET +
                            ", serwer poinformuje klientów, że mają możliwość generowania chunków poziomu wizualnego poza odległościami interakcji graczy.");
                },
                false,
                (input) -> this.logger.info("Client Side Chunks na: " + input)
        ));

        this.logger.info("Ukończono odpowiedzi w " + ConsoleColors.GREEN + ((System.currentTimeMillis() - startTime) / 1000.0) + ConsoleColors.RESET + " sekund");
        this.currentSettings(scanner);
    }

    private void currentSettings(final Scanner scanner) {
        this.os = this.config.getSystemOs();
        this.logger.info("System: " + this.os);

        this.fileName = this.config.getFileName();
        this.logger.info("FileName: " + this.fileName);

        this.wine = this.config.isWine();
        this.logger.info("IsWine: " + this.wine);

        this.filePath = this.config.getFilesPath();
        this.logger.info("FilePath: " + this.filePath);

        this.backup = this.config.isBackup();
        this.logger.info("Backup: " + this.backup);

        if (this.backup) {
            this.logger.info("WorldName: " + this.serverProperties.getWorldName());
        }

        this.logger.info("Liczba wątków używana przez server: " + this.serverProperties.getMaxThreads());
        this.logger.info("Czy klient generuje chunki: " + this.serverProperties.isClientSideChunkGeneration());

        this.logger.info("Kliknij enter aby kontunować");
        scanner.nextLine();

        if (this.config.isFirstRun()) {
            this.config.setFirstRun(false);
        }
        this.config.save();
    }

    public String getFilePath() {
        return this.filePath;
    }

    public String getFileName() {
        return this.fileName;
    }

    public SystemOs getOs() {
        return this.os;
    }

    public boolean isWine() {
        return this.wine;
    }

    private boolean isBackup() {
        return this.backup;
    }
}