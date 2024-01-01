package me.indian.bds.version;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.sub.version.VersionManagerConfig;
import me.indian.bds.logger.LogState;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.MathUtil;

public class VersionUpdater {

    private final BDSAutoEnable bdsAutoEnable;
    private final VersionManager versionManager;
    private final Logger logger;
    private final VersionManagerConfig versionManagerConfig;
    private final ServerProcess serverProcess;
    private final String prefix;
    private boolean running, updating;

    public VersionUpdater(final BDSAutoEnable bdsAutoEnable, final VersionManager versionManager) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.versionManager = versionManager;
        this.logger = this.bdsAutoEnable.getLogger();
        this.serverProcess = this.bdsAutoEnable.getServerProcess();
        this.versionManagerConfig = this.bdsAutoEnable.getAppConfigManager().getVersionManagerConfig();
        this.prefix = "&b[&3VersionUpdater&b]";
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

        final long hours = MathUtil.hoursTo(this.versionManagerConfig.getVersionCheckFrequency(), TimeUnit.MILLISECONDS);
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
                        VersionUpdater.this.updateToLatest();
                    }
                }
            }
        };

        timer.scheduleAtFixedRate(timerTask, MathUtil.minutesTo(1, TimeUnit.MILLISECONDS), hours);
    }

    public void updateToLatest() {
        if (this.updating) {
            this.logger.error("Server jest już aktualizowany!!");
            return;
        }
        this.updating = true;
        try {
            final String version = this.versionManager.getLatestVersion();
            if (this.versionManagerConfig.getVersion().equalsIgnoreCase(version)) return;
            this.bdsAutoEnable.getDiscordHelper().getDiscordJDA().sendServerUpdateMessage(version);
            if (!this.versionManager.hasVersion(version)) {
                this.versionManager.downloadServerFiles(version);
            }

            this.serverProcess.tellrawToAllAndLogger(
                    this.prefix,
                    "&aWersja &1" + version + "&a będzie właśnie ładowana!",
                    LogState.ALERT
            );
            this.serverProcess.setCanRun(false);

            if (this.serverProcess.isEnabled()) {
                this.serverProcess.kickAllPlayers(this.prefix + " &aAktualizowanie servera....");
                this.serverProcess.sendToConsole("stop");
                try {
                    this.serverProcess.getProcess().waitFor();
                } catch (final InterruptedException exception) {
                    this.logger.error("", exception);
                }
            }

            this.versionManager.loadVersion(version);
            this.serverProcess.setCanRun(true);
            this.serverProcess.startProcess();
        } catch (final Exception exception) {
            this.logger.critical("Wystąpił krytyczny błąd przy próbie aktualizacji sevrera do najnowszej wersji", exception);
        } finally {
            this.updating = false;
        }
    }
}