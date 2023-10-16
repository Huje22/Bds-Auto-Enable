package me.indian.bds.config.sub.watchdog;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class BackupConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Czy robić backupy?"})
    private boolean backup = false;
    @Comment({""})
    @Comment({"Co ile min robić backup?"})
    private int backupFrequency = 60;
    @Comment({""})
    @Comment({"Nie zmieniaj tego!"})
    private double lastBackupTime = 20;

    public boolean isBackup() {
        return this.backup;
    }

    public void setBackup(final boolean backup) {
        this.backup = backup;
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
