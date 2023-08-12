package me.indian.bds;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import me.indian.bds.config.Config;
import me.indian.bds.file.ServerProperties;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.util.VersionManager;
import me.indian.bds.watchdog.WatchDog;

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
    private final ServerProperties serverProperties;
    private final VersionManager versionManager;
    private final String projectVersion;
    private WatchDog watchDog;
    private String runDate;

    public BDSAutoEnable() {
        this.projectVersion = "1.0.0-Dev";
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
        this.serverProperties = new ServerProperties(this);
        this.settings = new Settings(this);
        this.serverProcess = new ServerProcess(this);
        this.versionManager = new VersionManager(this);

        this.init();
    }

    public static void main(final String[] args) {
        new BDSAutoEnable();
    }

    public void init() {
        this.logger.alert("Numer wersji projektu: " + this.projectVersion);
        this.settings.loadSettings(this.scanner);
        this.watchDog = new WatchDog(this);
        this.serverProcess.initWatchDog(this.watchDog);
        this.watchDog.forceBackup();
        this.watchDog.backup();
        this.versionManager.loadVersion();
        this.config.save();

        Runtime.getRuntime().addShutdownHook(new ThreadUtil("Shutdown", () -> {
            this.logger.alert("Wykonuje się przed zakończeniem programu...");
            this.config.save();
            this.scanner.close();
            this.serverProcess.instantShutdown();
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

    public String getProjectVersion() {
        return this.projectVersion;
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

    public VersionManager getVersionManager() {
        return this.versionManager;
    }

    public ServerProperties getServerProperties() {
        return this.serverProperties;
    }

    public WatchDog getWatchDog() {
        return this.watchDog;
    }
}