package me.indian.bds.watchdog;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.Config;
import me.indian.bds.logger.LogState;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.MinecraftUtil;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.watchdog.module.BackupModule;
import me.indian.bds.watchdog.module.PackModule;

public class WatchDog {

    private final BackupModule backupModule;
    private final PackModule packModule;
    private final Config config;
    private final String watchDogPrefix;
    private final ServerProcess serverProcess;

    public WatchDog(final BDSAutoEnable bdsAutoEnable) {
        this.backupModule = new BackupModule(bdsAutoEnable);
        this.packModule = new PackModule(bdsAutoEnable);
        this.config = bdsAutoEnable.getConfig();
        this.watchDogPrefix = "&b[&3WatchDog&b]";
        this.serverProcess = bdsAutoEnable.getServerProcess();
    }

    public BackupModule getBackupModule() {
        return this.backupModule;
    }

    public PackModule getPackModule() {
        return this.packModule;
    }

    public void init(final ServerProcess serverProcess) {
        this.backupModule.initBackupModule(this, serverProcess);
        this.packModule.initBackupModule(this);
    }

    public void saveWorld() {
        final double lastSave = (this.config.getLastBackupTime() / 4.0);
        MinecraftUtil.tellrawToAllAndLogger(this.watchDogPrefix, "&aZapisywanie świata, prosze czekać około:&b " + lastSave + "&b sekund", LogState.INFO);
        this.serverProcess.sendToConsole("save hold");
        ThreadUtil.sleep((int) lastSave);
    }

    public void saveResume() {
        this.serverProcess.sendToConsole("save resume");
    }

    public void saveAndResume() {
        this.saveWorld();
        this.saveResume();
        ThreadUtil.sleep(2);
    }

    public String getWatchDogPrefix() {
        return this.watchDogPrefix;
    }
}
