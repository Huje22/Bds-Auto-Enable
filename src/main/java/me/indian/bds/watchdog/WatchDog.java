package me.indian.bds.watchdog;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.ServerProcess;
import me.indian.bds.config.Config;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.MinecraftUtil;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.watchdog.module.BackupModule;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WatchDog {

    private final BackupModule backupModule;
    private final Logger logger;
    private final Config config;
    private final String watchDogPrefix;
    private final ServerProcess serverProcess;


    public WatchDog(final BDSAutoEnable bdsAutoEnable) {
        this.backupModule = new BackupModule(bdsAutoEnable);
        this.logger = bdsAutoEnable.getLogger();
        this.config = bdsAutoEnable.getConfig();
        this.watchDogPrefix = "&b[&3WatchDog&b]";
        this.serverProcess = bdsAutoEnable.getServerProcess();
    }


    public BackupModule getBackupModule() {
        return this.backupModule;
    }

    public void saveWorld() {
        final double lastSave = (this.config.getLastBackupTime() / 4.0);
        this.serverProcess.sendToConsole(MinecraftUtil.tellrawToAllMessage(this.watchDogPrefix + " &aZapisywanie servera, prosze czekać około:&b " + lastSave + "&b sekund"));
       this.logger.info("Za[isywanie servera , prosze czekać około: " + lastSave + " sekund");
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
