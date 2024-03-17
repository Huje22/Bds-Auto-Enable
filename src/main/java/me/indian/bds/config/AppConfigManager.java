package me.indian.bds.config;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import java.io.File;
import me.indian.bds.config.sub.CommandConfig;
import me.indian.bds.config.sub.transfer.TransferConfig;
import me.indian.bds.config.sub.log.LogConfig;
import me.indian.bds.config.sub.version.VersionManagerConfig;
import me.indian.bds.config.sub.watchdog.WatchDogConfig;
import me.indian.bds.util.DefaultsVariables;

public class AppConfigManager {

    private final AppConfig appConfig;
    private final TransferConfig transferConfig;
    private final LogConfig logConfig;
    private final VersionManagerConfig versionManagerConfig;
    private final WatchDogConfig watchDogConfig;
    private final CommandConfig commandConfig;

    public AppConfigManager() {
        final String configDir = DefaultsVariables.getAppDir() + File.separator + "config" + File.separator;

        this.appConfig = ConfigManager.create(AppConfig.class, (it) -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile(configDir + "config.yml");
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });

        this.transferConfig = ConfigManager.create(TransferConfig.class, (it) -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile(configDir + "Transfer.yml");
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });

        this.logConfig = ConfigManager.create(LogConfig.class, (it) -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile(configDir + "Log.yml");
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });

        this.versionManagerConfig = ConfigManager.create(VersionManagerConfig.class, (it) -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile(configDir + "VersionManager.yml");
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });

        this.watchDogConfig = ConfigManager.create(WatchDogConfig.class, (it) -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile(configDir + "WatchDog.yml");
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });

        this.commandConfig = ConfigManager.create(CommandConfig.class, (it) -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile(configDir + "Commands.yml");
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });
    }

    public void load() {
        this.appConfig.load();
        this.transferConfig.load();
        this.logConfig.load();
        this.versionManagerConfig.load();
        this.watchDogConfig.load();
    }

    public void save() {
        this.appConfig.save();
        this.transferConfig.save();
        this.logConfig.save();
        this.versionManagerConfig.save();
        this.watchDogConfig.save();
    }

    public AppConfig getAppConfig() {
        return this.appConfig;
    }

    public TransferConfig getTransferConfig() {
        return this.transferConfig;
    }

    public LogConfig getLogConfig() {
        return this.logConfig;
    }

    public VersionManagerConfig getVersionManagerConfig() {
        return this.versionManagerConfig;
    }

    public WatchDogConfig getWatchDogConfig() {
        return this.watchDogConfig;
    }

    public CommandConfig getCommandsConfig() {
        return this.commandConfig;
    }
}