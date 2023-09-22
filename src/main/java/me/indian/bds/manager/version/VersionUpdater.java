package me.indian.bds.manager.version;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.sub.version.VersionManagerConfig;
import me.indian.bds.logger.LogState;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.MathUtil;

import java.util.Timer;
import java.util.TimerTask;

public class VersionUpdater {

    private final BDSAutoEnable bdsAutoEnable;
    private final VersionManager versionManager;
    private final Logger logger;
    private final VersionManagerConfig versionManagerConfig;
    private final ServerProcess serverProcess;
    private String prefix;
    private boolean running;

    public VersionUpdater(final BDSAutoEnable bdsAutoEnable, final VersionManager versionManager) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.versionManager = versionManager;
        this.logger = this.bdsAutoEnable.getLogger();
        this.serverProcess = this.bdsAutoEnable.getServerProcess();
        this.versionManagerConfig = this.bdsAutoEnable.getConfig().getVersionManagerConfig();
        this.running = false;
    }

    public void checkForUpdate() {
        if (!this.versionManagerConfig.isCheckVersion()) {
            this.logger.debug("Sprawdzanie najnowszej wersji jest wyłączone");
            return;
        }
        if (this.running) {
            return;
        }
        this.running = true;

        final long hours = MathUtil.hoursToMillis(this.versionManagerConfig.getVersionCheckFrequency());
        final Timer timer = new Timer("Version Updater", true);

        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                final String current = VersionUpdater.this.versionManagerConfig.getVersion();
                final String latest = VersionUpdater.this.bdsAutoEnable.getVersionManager().getLatestVersion();

                if (!current.equals(latest) && !latest.equals("")) {
                    VersionUpdater.this.serverProcess.tellrawToAllAndLogger(VersionUpdater.this.prefix,
                            "&aDostępna jest nowa wersja, aktualna to&b " + current + " &a najnowsza to&b " + latest,
                            LogState.INFO);

                    if (VersionUpdater.this.versionManagerConfig.isAutoUpdate()) {
                        VersionUpdater.this.serverProcess.tellrawToAllAndLogger(VersionUpdater.this.prefix,
                                "&aWłączona jest&b Auto Aktualizacja&a po pobraniu najnowszej wersji zostaniecie wyrzuceni a server zmieni wersje!",
                                LogState.ALERT);
                        VersionUpdater.this.autoUpdate(latest);
                    }
                }
            }
        };

        timer.scheduleAtFixedRate(timerTask, 0, hours);
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
            if(this.serverProcess.getProcess() == null){
                this.autoUpdate(version);
                return;
            }
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