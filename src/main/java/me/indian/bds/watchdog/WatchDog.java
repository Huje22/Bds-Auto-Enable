package me.indian.bds.watchdog;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.sub.watchdog.WatchDogConfig;
import me.indian.bds.discord.jda.DiscordJDA;
import me.indian.bds.logger.LogState;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.MathUtil;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.watchdog.module.AutoRestartModule;
import me.indian.bds.watchdog.module.BackupModule;
import me.indian.bds.watchdog.module.PackModule;
import me.indian.bds.watchdog.monitor.RamMonitor;

public class WatchDog {

    private final BackupModule backupModule;
    private final PackModule packModule;
    private final AutoRestartModule autoRestartModule;
    private final RamMonitor ramMonitor;
    private final String watchDogPrefix;
    private final ServerProcess serverProcess;
    private final WatchDogConfig watchDogConfig;

    public WatchDog(final BDSAutoEnable bdsAutoEnable) {
        this.watchDogPrefix = "&b[&3WatchDog&b]";
        this.serverProcess = bdsAutoEnable.getServerProcess();
        this.backupModule = new BackupModule(bdsAutoEnable, this);
        this.packModule = new PackModule(bdsAutoEnable, this);
        this.autoRestartModule = new AutoRestartModule(bdsAutoEnable, this);
        this.ramMonitor = new RamMonitor(bdsAutoEnable, this);
        this.watchDogConfig = bdsAutoEnable.getAppConfigManager().getWatchDogConfig();
    }

    public BackupModule getBackupModule() {
        return this.backupModule;
    }

    public PackModule getPackModule() {
        return this.packModule;
    }

    public AutoRestartModule getAutoRestartModule() {
        return this.autoRestartModule;
    }

    public RamMonitor getRamMonitor() {
        return this.ramMonitor;
    }

    public void init(final DiscordJDA discordJDA) {
        this.backupModule.init();
        this.autoRestartModule.init();
        this.ramMonitor.init(discordJDA);
    }

    public void saveWorld() {
        final int time = (int) MathUtil
                .getCorrectNumber((this.watchDogConfig.getBackupConfig().getLastBackupTime() / 5), 5, 60);

        this.serverProcess.tellrawToAllAndLogger(this.watchDogPrefix, "&aZapisywanie świata, będzie to trwało około&1 " + time + "&a sekund", LogState.INFO);
        this.serverProcess.sendToConsole("save hold");
        ThreadUtil.sleep(time);
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