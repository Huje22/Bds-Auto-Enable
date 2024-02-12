package me.indian.bds.config;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import java.io.File;
import me.indian.bds.config.sub.CommandConfig;
import me.indian.bds.config.sub.EventsConfig;
import me.indian.bds.config.sub.log.LogConfig;
import me.indian.bds.config.sub.version.VersionManagerConfig;
import me.indian.bds.config.sub.watchdog.WatchDogConfig;
import me.indian.bds.util.DefaultsVariables;

public class AppConfigManager {

    private final AppConfig appConfig;
    private final LogConfig logConfig;
    private final VersionManagerConfig versionManagerConfig;
    private final WatchDogConfig watchDogConfig;
    private final CommandConfig commandConfig;
    private final EventsConfig eventsConfig;

    public AppConfigManager() {
        final String appDir = DefaultsVariables.getAppDir() + File.separator + "config" + File.separator;

        this.appConfig = ConfigManager.create(AppConfig.class, (it) -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile(appDir + "config.yml");
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });

        this.logConfig = ConfigManager.create(LogConfig.class, (it) -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile(appDir + "Log.yml");
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });

        this.versionManagerConfig = ConfigManager.create(VersionManagerConfig.class, (it) -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile(appDir + "VersionManager.yml");
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });

        this.watchDogConfig = ConfigManager.create(WatchDogConfig.class, (it) -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile(appDir + "WatchDog.yml");
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });

        this.commandConfig = ConfigManager.create(CommandConfig.class, (it) -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile(appDir + "Commands.yml");
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });

        this.eventsConfig = ConfigManager.create(EventsConfig.class, (it) -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile(appDir + "Events.yml");
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });
    }

    public void load() {
        this.appConfig.load();
        this.logConfig.load();
        this.versionManagerConfig.load();
        this.watchDogConfig.load();
    }

    public void save() {
        this.appConfig.save();
        this.logConfig.save();
        this.versionManagerConfig.save();
        this.watchDogConfig.save();
    }

    public AppConfig getAppConfig() {
        return this.appConfig;
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

    public EventsConfig getEventsConfig() {
        return this.eventsConfig;
    }
}