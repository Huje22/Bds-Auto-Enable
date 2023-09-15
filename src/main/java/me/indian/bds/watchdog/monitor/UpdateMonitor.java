package me.indian.bds.watchdog.monitor;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.Config;
import me.indian.bds.config.sub.version.VersionManagerConfig;
import me.indian.bds.logger.LogState;
import me.indian.bds.logger.Logger;
import me.indian.bds.manager.VersionManager;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.MathUtil;
import me.indian.bds.watchdog.WatchDog;

import java.util.Timer;
import java.util.TimerTask;

public class UpdateMonitor {

    private final BDSAutoEnable bdsAutoEnable;
    private final VersionManager versionManager;
    private final Logger logger;
    private final Config config;
    private final VersionManagerConfig versionManagerConfig;
    private ServerProcess serverProcess;
    private WatchDog watchDog;
    private String prefix;
    private boolean running;

    public UpdateMonitor(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.versionManager = bdsAutoEnable.getVersionManager();
        this.logger = this.bdsAutoEnable.getLogger();
        this.config = this.bdsAutoEnable.getConfig();
        this.versionManagerConfig = this.config.getVersionManagerConfig();
        this.running = false;
    }

    public void initUpdateModule(final WatchDog watchDog, final ServerProcess serverProcess) {
        this.watchDog = watchDog;
        this.prefix = this.watchDog.getWatchDogPrefix();
        this.serverProcess = serverProcess;
    }


    public void checkForUpdate() {
        if (!this.versionManagerConfig.isCheckVersion()) {
            return;
        }
        if (this.running) {
            return;
        }
        this.running = true;

        System.out.println("OKEY0");

        final long hours = MathUtil.hoursToMillis(this.versionManagerConfig.getVersionCheckFrequency());

        final Timer timer = new Timer("Update Monitor", true);

        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                final String current = UpdateMonitor.this.versionManagerConfig.getVersion();
                final String latest = UpdateMonitor.this.bdsAutoEnable.getVersionManager().getLatestVersion();

                if (!current.equals(latest) && !latest.equals("")) {
                    UpdateMonitor.this.serverProcess.tellrawToAllAndLogger(UpdateMonitor.this.prefix,
                            "&aDostępna jest nowa wersja, aktualna to&b " + current + " &a najnowsza to&b " + latest,
                            LogState.INFO);

                    if (UpdateMonitor.this.versionManagerConfig.isAutoUpdate()) {
                        UpdateMonitor.this.serverProcess.tellrawToAllAndLogger(UpdateMonitor.this.prefix,
                                "&aWłączona jest&b Auto Aktualizacja&a po pobraniu najnowszej wersji zostaniecie wyrzuceni a server zmieni wersje!",
                                LogState.ALERT);
                        UpdateMonitor.this.autoUpdate(latest);
                    }
                }
                this.cancel();
            }
        };

        timer.scheduleAtFixedRate(timerTask, hours, hours);
    }

    private void autoUpdate(final String version) {
        if (!this.versionManager.hasVersion(version)) {
            this.versionManager.downloadServerFiles(version);
        }

        this.serverProcess.tellrawToAllAndLogger(
                this.prefix,
                "&aWersja &b" + version + "&a będzie właśnie ładowana!",
                LogState.ALERT
        );
        this.serverProcess.setCanRun(false);
        this.serverProcess.sendToConsole("stop");

        try {
            this.serverProcess.getProcess().waitFor();
        } catch (final InterruptedException exception) {
            this.logger.error(exception);
        }

        this.serverProcess.kickAllPlayers(this.prefix + " &aAktualizowanie servera....");
        this.versionManager.loadVersion(version);
        this.serverProcess.setCanRun(true);
        this.serverProcess.startProcess();
    }
}