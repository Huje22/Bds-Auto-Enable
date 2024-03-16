package me.indian.bds.watchdog.module;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.sub.watchdog.AutoRestartConfig;
import me.indian.bds.event.server.ServerRestartEvent;
import me.indian.bds.logger.LogState;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.MathUtil;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.watchdog.WatchDog;
import org.jetbrains.annotations.Nullable;

public class AutoRestartModule {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final AutoRestartConfig autoRestartConfig;
    private final WatchDog watchDog;
    private final Timer timer;
    private final ExecutorService service;
    private TimerTask task;
    private String prefix;
    private ServerProcess serverProcess;
    private long lastRestartMillis;
    private boolean restarting;

    public AutoRestartModule(final BDSAutoEnable bdsAutoEnable, final WatchDog watchDog) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.autoRestartConfig = this.bdsAutoEnable.getAppConfigManager().getWatchDogConfig().getAutoRestartConfig();
        this.watchDog = watchDog;
        this.timer = new Timer("AutoRestart", true);
        this.service = Executors.newSingleThreadExecutor(new ThreadUtil("Restart"));
        this.lastRestartMillis = System.currentTimeMillis();
        this.restarting = false;

        this.run();
    }

    public void init() {
        this.serverProcess = this.bdsAutoEnable.getServerProcess();
        this.prefix = this.watchDog.getWatchDogPrefix();
    }

    private void run() {
        final long restartTime = MathUtil.hoursTo(this.autoRestartConfig.getRestartTime(), TimeUnit.MILLISECONDS);
        this.task = new TimerTask() {
            @Override
            public void run() {
                if (!AutoRestartModule.this.serverProcess.isEnabled()) {
                    AutoRestartModule.this.logger.error("Nie można zrestartować servera gdy jest on wyłączony!");
                    return;
                }
                AutoRestartModule.this.restart(true, 10);
                AutoRestartModule.this.lastRestartMillis = System.currentTimeMillis();
            }
        };

        if (this.autoRestartConfig.isEnabled()) {
            this.timer.schedule(this.task, restartTime, restartTime);
        } else {
            this.logger.debug("Automatyczny restart servera jest wyłączony");
        }
    }


    public void restart(final boolean alert, final int seconds) {
        this.restart(alert, seconds, null);
    }

    public void restart(final boolean alert, final int seconds, @Nullable final String reason) {
        if (this.restarting) return;
        this.restarting = true;
        this.service.execute(() -> {
            try {
                if (!this.serverProcess.isEnabled()) {
                    this.logger.error("Nie można zrestartować servera gdy jest on wyłączony!");
                    return;
                }
                this.serverProcess.tellrawToAllAndLogger(this.prefix,
                        "&aPrzygotowanie do&b restartu&a servera",
                        LogState.WARNING);
                this.watchDog.saveAndResume();
                if (alert) this.restartAlert(seconds);


                if (!this.bdsAutoEnable.getAppConfigManager().getTransferConfig().isEnable()) {
                    this.serverProcess.kickAllPlayers(this.prefix + " &aServer jest restartowany....");
                }

                this.serverProcess.sendToConsole("stop");
                this.bdsAutoEnable.getEventManager().callEvent(new ServerRestartEvent(reason));

                if (!this.serverProcess.waitFor(10, TimeUnit.SECONDS)) {
                    this.watchDog.getBackupModule().backup();
                    this.serverProcess.destroyProcess();
                }

                if (!this.serverProcess.isCanRun()) {
                    this.serverProcess.setCanRun(true);
                    this.serverProcess.startProcess();
                }
            } catch (final Exception exception) {
                this.serverProcess.tellrawToAllAndLogger(this.prefix,
                        "Nie można zrestartować servera!", exception, LogState.ERROR);
            } finally {
                this.restarting = false;
            }
        });
    }

    public void noteRestart() {
        if (!this.autoRestartConfig.isEnabled()) return;
        this.task.cancel();
        this.run();
        this.lastRestartMillis = System.currentTimeMillis();
    }

    private void restartAlert(final int seconds) {
        for (int i = seconds; i >= 0; i--) {
            AutoRestartModule.this.serverProcess.tellrawToAllAndLogger(AutoRestartModule.this.prefix,
                    "&aZa&1 " + i + "&a sekund server zostanie zrestartowany!", LogState.INFO);
            ThreadUtil.sleep(1);
        }
    }

    public long calculateMillisUntilNextRestart() {
        return Math.max(0, (MathUtil.hoursTo(this.autoRestartConfig.getRestartTime(), TimeUnit.MILLISECONDS) + 10) - (System.currentTimeMillis() - this.lastRestartMillis));
    }
}