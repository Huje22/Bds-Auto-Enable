package me.indian.bds.config.sub;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Header;

@Header("################################################################")
@Header("#           Ustawienia Poleceń                                 #")
@Header("#                  Tych z !                                    #")
@Header("################################################################")

public class CommandConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Czy !setting ma być dla każdego"})
    @CustomKey("SettingsForAll")
    private boolean settingsForAll = true;

    public boolean isSettingsForAll() {
        return this.settingsForAll;
    }
}