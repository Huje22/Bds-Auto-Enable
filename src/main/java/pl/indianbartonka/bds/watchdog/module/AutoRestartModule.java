package pl.indianbartonka.bds.watchdog.module;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.Nullable;
import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.config.sub.transfer.LobbyConfig;
import pl.indianbartonka.bds.config.sub.watchdog.AutoRestartConfig;
import pl.indianbartonka.bds.event.server.ServerAlertEvent;
import pl.indianbartonka.bds.event.server.ServerRestartEvent;
import pl.indianbartonka.bds.server.ServerProcess;
import pl.indianbartonka.bds.util.ServerUtil;
import pl.indianbartonka.bds.watchdog.WatchDog;
import pl.indianbartonka.util.DateUtil;
import pl.indianbartonka.util.MathUtil;
import pl.indianbartonka.util.ThreadUtil;
import pl.indianbartonka.util.logger.LogState;
import pl.indianbartonka.util.logger.Logger;

public class AutoRestartModule {

    private static boolean playersOnRestart = false;
    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final AutoRestartConfig autoRestartConfig;
    private final WatchDog watchDog;
    private final Timer timer;
    private final ExecutorService service;
    private TimerTask task;
    private String prefix;
    private ServerProcess serverProcess;
    private long lastPlanedRestartMillis;
    private boolean restarting, lastRestartDone;

    public AutoRestartModule(final BDSAutoEnable bdsAutoEnable, final WatchDog watchDog) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.autoRestartConfig = this.bdsAutoEnable.getAppConfigManager().getWatchDogConfig().getAutoRestartConfig();
        this.watchDog = watchDog;
        this.timer = new Timer("AutoRestart", true);
        this.service = Executors.newSingleThreadExecutor(new ThreadUtil("Restart"));
        this.lastPlanedRestartMillis = System.currentTimeMillis();
        this.restarting = false;

        this.run();
    }

    public void init() {
        this.serverProcess = this.bdsAutoEnable.getServerProcess();
        this.prefix = this.watchDog.getWatchDogPrefix();
    }

    private void run() {
        final long restartTime = DateUtil.hoursTo(this.autoRestartConfig.getRestartTime(), TimeUnit.MILLISECONDS);

        this.task = new TimerTask() {

            @Override
            public void run() {
                if (!AutoRestartModule.this.serverProcess.isEnabled()) {
                    AutoRestartModule.this.logger.error("Nie można zrestartować servera gdy jest on wyłączony!");
                    return;
                }

                final boolean players = !AutoRestartModule.this.bdsAutoEnable.getServerManager().getOnlinePlayers().isEmpty();

                if (!players && !playersOnRestart) {
                    AutoRestartModule.this.logger.alert("Brak graczy&c....");
                    AutoRestartModule.this.lastRestartDone = false;
                    AutoRestartModule.this.lastPlanedRestartMillis = System.currentTimeMillis();
                    return;
                }

                playersOnRestart = players;

                if (AutoRestartModule.this.restart(true, 10)) {
                    AutoRestartModule.this.bdsAutoEnable.getEventManager().callEvent(new ServerAlertEvent("Server jest restartowany tak jak co " + AutoRestartModule.this.autoRestartConfig.getRestartTime() + " godziny", LogState.INFO));
                }

                AutoRestartModule.this.lastRestartDone = true;
                AutoRestartModule.this.lastPlanedRestartMillis = System.currentTimeMillis();
            }
        };

        if (this.autoRestartConfig.isEnabled()) {
            this.timer.schedule(this.task, restartTime, restartTime);
        } else {
            this.logger.debug("Automatyczny restart servera jest wyłączony");
        }
    }

    public boolean restart(final boolean alert, final int seconds) {
        return this.restart(alert, seconds, null);
    }

    public boolean restart(final boolean alert, final int seconds, @Nullable final String reason) {
        if (this.restarting) return false;
        this.restarting = true;
        this.service.execute(() -> {
            try {
                if (!this.serverProcess.isEnabled()) {
                    this.logger.error("Nie można zrestartować servera gdy jest on wyłączony!");
                    return;
                }

                final LobbyConfig lobbyConfig = this.bdsAutoEnable.getAppConfigManager().getTransferConfig().getLobbyConfig();

                if (lobbyConfig.isEnable()) {
                    ServerUtil.tellrawToAll("&2Zaraz zostaniecie przeniesieni na server&b lobby");
                }

                ServerUtil.titleToAll("&cServer zostanie zrestartowany", "&bZa&a " + seconds + "&e sekund");
                ServerUtil.playSoundToAll("mob.wither.break_block");
                ServerUtil.tellrawToAllAndLogger(this.prefix, "&aPrzygotowanie do&b restartu&a servera", LogState.WARNING);

                if (alert) this.restartAlert(seconds);

                ServerUtil.tellrawToAllAndLogger(this.prefix, "&aPierw zapiszemy świat!", LogState.INFO);
                this.watchDog.saveAndResume();

                ServerUtil.playSoundToAll("mob.wither.death");

                if (!lobbyConfig.isEnable()) {
                    ServerUtil.kickAllPlayers(this.prefix + " &aServer jest restartowany....");
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
                ServerUtil.tellrawToAllAndLogger(this.prefix, "Nie można zrestartować servera!", exception, LogState.ERROR);
            } finally {
                this.restarting = false;
            }
        });
        return true;
    }

    public void noteRestart() {
        if (!this.autoRestartConfig.isEnabled()) return;
        this.task.cancel();
        this.run();
        this.lastPlanedRestartMillis = System.currentTimeMillis();
    }

    private void restartAlert(final int seconds) {
        int nextSeconds = seconds;
        for (int i = seconds; i >= 1; i--) {
            if (i < 10 || i == nextSeconds) {
                nextSeconds = MathUtil.getCorrectNumber((i - 5), 0, seconds);

                ServerUtil.actionBarToAll("&aZa&b " + i + "&a sekund server zostanie zrestartowany!");
                ServerUtil.tellrawToAllAndLogger(AutoRestartModule.this.prefix,
                        "&aZa&b " + i + "&a sekund server zostanie zrestartowany!", LogState.INFO);

            }
            ThreadUtil.sleep(1);
        }
    }

    public long calculateMillisUntilNextRestart() {
        return Math.max(0, (DateUtil.hoursTo(this.autoRestartConfig.getRestartTime(), TimeUnit.MILLISECONDS) + 10) - (System.currentTimeMillis() - this.lastPlanedRestartMillis));
    }

    public boolean isLastRestartDone() {
        return this.lastRestartDone;
    }
}
