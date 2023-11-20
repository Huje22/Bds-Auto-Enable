package me.indian.bds.config;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import java.io.File;



public class ConfigManager{

private final Config config;

//TODO: Dokończyć to
  
public ConfigManager(){
this.init()
}

private void init(){

  final String appDir = Defaults.getAppDir();
  
  this.config = ConfigManager.create(Config.class, (it) -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile(appDir + "config.yml");
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });
}



  
}
