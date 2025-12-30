package pl.indianbartonka.bds.config.sub.watchdog;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class BackupConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Czy robić backupy?"})
    private boolean enabled = false;

    @Comment({""})
    @Comment({"Usuwać stare backupy?"})
    private boolean deleteOldBackups = true;

    @Comment({""})
    @Comment({"Maksymalna dozwolona liczba backupów"})
    @Comment({"Jeśli ustawione na -1 albo 0 jedynym ograniczeniem będzie pamięć systemu"})
    private int maxBackups = -1;

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

    public boolean isDeleteOldBackups() {
        return this.deleteOldBackups;
    }

    public int getMaxBackups() {
        return this.maxBackups;
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
