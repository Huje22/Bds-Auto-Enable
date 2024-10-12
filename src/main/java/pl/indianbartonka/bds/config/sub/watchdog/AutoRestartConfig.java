package pl.indianbartonka.bds.config.sub.watchdog;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class AutoRestartConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Czy włączyć restartowanie sie servera?"})
    private boolean enabled = true;
    @Comment({""})
    @Comment({"Co ile godzin ma się on restartować?"})
    private int restartTime = 10;

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public int getRestartTime() {
        return this.restartTime;
    }

    public void setRestartTime(final int restartTime) {
        this.restartTime = restartTime;
    }
}
