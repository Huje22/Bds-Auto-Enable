package pl.indianbartonka.bds.watchdog;

import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.config.sub.watchdog.WatchDogConfig;
import pl.indianbartonka.bds.server.ServerProcess;
import pl.indianbartonka.bds.util.ServerUtil;
import pl.indianbartonka.bds.watchdog.module.AutoRestartModule;
import pl.indianbartonka.bds.watchdog.module.BackupModule;
import pl.indianbartonka.bds.watchdog.module.pack.PackModule;
import pl.indianbartonka.util.MathUtil;
import pl.indianbartonka.util.ThreadUtil;
import pl.indianbartonka.util.logger.LogState;

public class WatchDog {

    private final BackupModule backupModule;
    private final PackModule packModule;
    private final AutoRestartModule autoRestartModule;
    private final String watchDogPrefix;
    private final ServerProcess serverProcess;
    private final WatchDogConfig watchDogConfig;

    public WatchDog(final BDSAutoEnable bdsAutoEnable) {
        this.watchDogPrefix = "&b[&3WatchDog&b]";
        this.serverProcess = bdsAutoEnable.getServerProcess();
        this.backupModule = new BackupModule(bdsAutoEnable, this);
        this.packModule = new PackModule(bdsAutoEnable);
        this.autoRestartModule = new AutoRestartModule(bdsAutoEnable, this);
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

    public void init() {
        this.backupModule.init();
        this.autoRestartModule.init();
    }

    public void saveWorld() {
        final int time = (int) MathUtil
                .getCorrectNumber(5, (this.watchDogConfig.getBackupConfig().getLastBackupTime() / 5), 60);

        ServerUtil.tellrawToAllAndLogger(this.watchDogPrefix, "&aZapisywanie świata, będzie to trwało około&1 " + time + "&a sekund", LogState.INFO);
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
