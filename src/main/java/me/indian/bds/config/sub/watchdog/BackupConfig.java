package me.indian.bds.config.sub.watchdog;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class BackupConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Czy robić backupy?"})
    private boolean enabled = false;
    @Comment({""})
    @Comment({"Co ile min robić backup?"})
    private int backupFrequency = 60;
    @Comment({""})
    @Comment({"Nie zmieniaj tego!"})
    private double lastBackupTime = 20;

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public int getBackupFrequency() {
        return this.backupFrequency;
    }

    public void setBackupFrequency(final int backupFrequency) {
        this.backupFrequency = backupFrequency;
    }

    public double getLastBackupTime() {
        return this.lastBackupTime;
    }

    public void setLastBackupTime(final double lastBackupTime) {
        this.lastBackupTime = lastBackupTime;
    }
}
