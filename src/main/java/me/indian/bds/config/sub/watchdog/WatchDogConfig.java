package me.indian.bds.config.sub.watchdog;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;

public class WatchDogConfig extends OkaeriConfig {


    @Comment({""})
    @Comment({"Backups"}) @CustomKey("backup")
    private BackupConfig backupConfig = new BackupConfig();

    @Comment({""})
    @Comment({"RamMonitor"}) @CustomKey("ramMonitor")
    private RamMonitorConfig ramMonitorConfig = new RamMonitorConfig();


    public BackupConfig getBackup() {
        return this.backupConfig;
    }

    public RamMonitorConfig getRamMonitor() {
        return this.ramMonitorConfig;
    }
}
