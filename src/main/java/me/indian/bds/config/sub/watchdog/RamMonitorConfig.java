package me.indian.bds.config.sub.watchdog;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class RamMonitorConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Czy powiadomić o niskiej ilości ram aplikacji?"})
    private boolean app = true;

    @Comment({""})
    @Comment({"Czy powiadomić o niskiej ilości ram maszyny?"})
    private boolean machine = true;

    @Comment({""})
    @Comment({"Czas sprawdzania stanu ram aplikacji w sekundach"})
    private int checkAppTime = 5;

    @Comment({""})
    @Comment({"Czas sprawdzania stanu ram maszyny w sekundach"})
    private int checkMachineTime = 10;

    @Comment({""})
    @Comment({"Aletry na kanale czatu discord"})
    private boolean discordAlters = true;


    public boolean isApp() {
        return this.app;
    }

    public boolean isMachine() {
        return this.machine;
    }

    public int getCheckAppTime() {
        return this.checkAppTime;
    }

    public int getCheckMachineTime() {
        return this.checkMachineTime;
    }

    public boolean isDiscordAlters() {
        return this.discordAlters;
    }
}
