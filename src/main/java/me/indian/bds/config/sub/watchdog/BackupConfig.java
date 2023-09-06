package me.indian.bds.config.sub.watchdog;

import eu.okaeri.configs.OkaeriConfig;

public class BackupConfig extends OkaeriConfig {

    private boolean backup = true;
    private int backupFrequency = 60;
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
