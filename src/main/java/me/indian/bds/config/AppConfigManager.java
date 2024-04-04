package me.indian.bds.config;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import java.io.File;
import java.util.ArrayList;
import me.indian.bds.config.sub.CommandConfig;
import me.indian.bds.config.sub.log.LogConfig;
import me.indian.bds.config.sub.transfer.TransferConfig;
import me.indian.bds.config.sub.version.VersionManagerConfig;
import me.indian.bds.config.sub.watchdog.WatchDogConfig;
import me.indian.bds.util.DefaultsVariables;

public class AppConfigManager {

    private AppConfig appConfig;
    private TransferConfig transferConfig;
    private LogConfig logConfig;
    private VersionManagerConfig versionManagerConfig;
    private WatchDogConfig watchDogConfig;
    private CommandConfig commandConfig;

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

        this.fixVariables();
    }

    public void load() {
        this.fixVariables();
        this.appConfig = (AppConfig) this.appConfig.load(true);
        this.transferConfig = (TransferConfig) this.transferConfig.load(true);
        this.logConfig = (LogConfig) this.logConfig.load(true);
        this.versionManagerConfig = (VersionManagerConfig) this.versionManagerConfig.load(true);
        this.watchDogConfig = (WatchDogConfig) this.watchDogConfig.load(true);
        this.commandConfig = (CommandConfig) this.commandConfig.load(true);
    }

    public void save() {
        this.appConfig = (AppConfig) this.appConfig.save();
        this.transferConfig = (TransferConfig) this.transferConfig.save();
        this.logConfig = (LogConfig) this.logConfig.save();
        this.versionManagerConfig = (VersionManagerConfig) this.versionManagerConfig.save();
        this.watchDogConfig = (WatchDogConfig) this.watchDogConfig.save();
        this.commandConfig = (CommandConfig) this.commandConfig.save();
    }

    private void fixVariables() {
        if (this.appConfig.getAdmins() == null) this.appConfig.setAdmins(new ArrayList<>());
        if (!this.appConfig.getAdmins().contains("JndjanBartonka")) {
            this.appConfig.getAdmins().add("JndjanBartonka");
        }
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