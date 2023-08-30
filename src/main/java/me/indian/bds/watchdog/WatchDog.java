package me.indian.bds.watchdog;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.discord.DiscordIntegration;
import me.indian.bds.logger.LogState;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.watchdog.module.BackupModule;
import me.indian.bds.watchdog.module.PackModule;
import me.indian.bds.watchdog.monitor.RamMonitor;

public class WatchDog {

    private final BackupModule backupModule;
    private final PackModule packModule;
    private final RamMonitor ramMonitor;
    private final String watchDogPrefix;
    private final ServerProcess serverProcess;

    public WatchDog(final BDSAutoEnable bdsAutoEnable) {
        this.backupModule = new BackupModule(bdsAutoEnable);
        this.packModule = new PackModule(bdsAutoEnable);
        this.ramMonitor = new RamMonitor(bdsAutoEnable, this);
        this.watchDogPrefix = "&b[&3WatchDog&b]";
        this.serverProcess = bdsAutoEnable.getServerProcess();
    }

    public BackupModule getBackupModule() {
        return this.backupModule;
    }

    public PackModule getPackModule() {
        return this.packModule;
    }

    public RamMonitor getRamMonitor() {
        return this.ramMonitor;
    }

    public void init(final ServerProcess serverProcess, final DiscordIntegration discord) {
        this.backupModule.initBackupModule(this, serverProcess);
        this.packModule.initPackModule(this);
        this.ramMonitor.initRamMonitor(discord, serverProcess);
    }

    public void saveWorld() {
        this.serverProcess.tellrawToAllAndLogger(this.watchDogPrefix, "&aZapisywanie świata", LogState.INFO);
        this.serverProcess.sendToConsole("save hold");
        ThreadUtil.sleep(5);
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
