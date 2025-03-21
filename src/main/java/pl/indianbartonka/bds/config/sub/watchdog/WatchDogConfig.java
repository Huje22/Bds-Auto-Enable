package pl.indianbartonka.bds.config.sub.watchdog;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Header;

@Header("################################################################")
@Header("#           Ustawienia WatchDog                                #")
@Header("################################################################")

public class WatchDogConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Backups"})
    @CustomKey("Backup")
    private BackupConfig backupConfig = new BackupConfig();

    @Comment({""})
    @Comment({"RamMonitor"})
    @CustomKey("RamMonitor")
    private RamMonitorConfig ramMonitorConfig = new RamMonitorConfig();

    @Comment({""})
    @Comment({"AutoRestart"})
    @CustomKey("AutoRestart")
    private AutoRestartConfig autoRestartConfig = new AutoRestartConfig();


    public BackupConfig getBackupConfig() {
        return this.backupConfig;
    }

    public RamMonitorConfig getRamMonitorConfig() {
        return this.ramMonitorConfig;
    }

    public AutoRestartConfig getAutoRestartConfig() {
        return this.autoRestartConfig;
    }
}