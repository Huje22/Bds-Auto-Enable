package me.indian.bds.config.sub.watchdog;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class AutoRestartConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Czy włączyć restartowanie sie servera?"})
    private boolean enabled = true;
    @Comment({""})
    @Comment({"Co ile godzin ma się on restartować?"})
    private int restartTime = 4;


    public boolean isEnabled() {
        return this.enabled;
    }

    public int getRestartTime() {
        return this.restartTime;
    }
}