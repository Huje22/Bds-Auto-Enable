package me.indian.bds.config.sub;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Header;

@Header("################################################################")
@Header("#           Ustawienia Poleceń                                 #")
@Header("#                  Tych z !                                    #")
@Header("################################################################")

public class CommandsConfig extends OkaeriConfig {

    @Comment({})
    @Comment({"czy !setting ma być dla każdego"})
    private boolean settingsForAll = true;


    public boolean isSettingsForAll() {
        return this.settingsForAll;
    }
}