package me.indian.bds;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import me.indian.bds.basic.Defaults;
import me.indian.bds.basic.Settings;
import me.indian.bds.config.Config;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.watchdog.WatchDog;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;


public class BDSAutoEnable {

    private final Scanner scanner;
    private final Logger logger;
    private final Config config;
    private final Settings settings;
    private final ServerProcess serverProcess;
    private  WatchDog watchDog;
    private String runDate;

    public BDSAutoEnable() {
        this.initRunDate();
        this.scanner = new Scanner(System.in);
        this.config = ConfigManager.create(Config.class, (it) -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile("BDS-Auto-Enable/config.yml");
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });
        Defaults.init(this);
        this.logger = new Logger(this);
        this.settings = new Settings(this);
        this.serverProcess = new ServerProcess(this);
        this.watchDog = new WatchDog(this);
        this.serverProcess.initWatchDog(this.watchDog);
        this.init();
    }


    public static void main(String[] args) {
        new BDSAutoEnable();
    }

    public void init() {
        this.settings.loadSettings(this.scanner);
        this.watchDog.forceBackup();
        this.watchDog.backup();
        final String finalFilePath = this.settings.getFilePath() + File.separator + this.settings.getName();
        final File file = new File(finalFilePath);
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
            this.scanner.close();
            this.serverProcess.shutdown();
        }));
        this.serverProcess.startProcess();
    }

    private void initRunDate() {
        final LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Warsaw"));
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.runDate = now.format(formatter).replaceAll(":", "-");
    }

    public String getRunDate() {
        return this.runDate;
    }

    public Config getConfig() {
        return config;
    }

    public Logger getLogger() {
        return logger;
    }

    public Settings getSettings() {
        return this.settings;
    }

    public ServerProcess getServerProcess() {
        return this.serverProcess;
    }

    public WatchDog getWatchDog() {
        return this.watchDog;
    }
}