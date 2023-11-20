package me.indian.bds.config;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import java.io.File;

public class ConfigManager{

  private final Config config;
  private final DiscordConfig discordConfig;
  private final LogConfig logConfig;
  private final RestApiConfig restApiConfig;
  private final VersionManagerConfig versionManagerConfig;
  private final WatchDogConfig watchDogConfig;
  private final AutoMessagesConfig autoMessagesConfig;

//TODO: Dokończyć to
  
public ConfigManager(){
  final String appDir = Defaults.getAppDir() + File.separator + "configs" ;
  
  this.config = ConfigManager.create(Config.class, (it) -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile(appDir + "config.yml");
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });
  
  this.discordConfig = ConfigManager.create(DiscordConfig.class, (it) -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile(appDir + "Discord.yml");
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

    this.restApiConfig = ConfigManager.create(RestApiConfig.class, (it) -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile(appDir + "RestApi.yml");
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });

    this.versionManagerConfig  = ConfigManager.create(VersionManagerConfig.class, (it) -> {
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

  this.autoMessagesConfig = ConfigManager.create(AutoMessagesConfig.class, (it) -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile(appDir + "AutoMessages.yml");
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        }); 
}
}
