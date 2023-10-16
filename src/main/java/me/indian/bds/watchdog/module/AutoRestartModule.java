package me.indian.bds.watchdog.module;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.sub.watchdog.AutoRestartConfig;
import me.indian.bds.discord.DiscordIntegration;
import me.indian.bds.logger.LogState;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.MathUtil;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.watchdog.WatchDog;

public class AutoRestartModule {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final Timer timer;
    private final AutoRestartConfig autoRestartConfig;
    private final DiscordIntegration discord;
    private final WatchDog watchDog;
    private String prefix;
    private ServerProcess serverProcess;
    private long lastRestartMillis;
    private boolean restarting;

    public AutoRestartModule(final BDSAutoEnable bdsAutoEnable, final WatchDog watchDog) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.autoRestartConfig = this.bdsAutoEnable.getConfig().getWatchDogConfig().getAutoRestartConfig();
        this.watchDog = watchDog;
         this.timer = new Timer("AutoRestart", true);
        this.discord = bdsAutoEnable.getDiscord();
        this.lastRestartMillis = System.currentTimeMillis();
        this.restarting = false;

        this.run();
    }

    public void init() {
        this.serverProcess = this.bdsAutoEnable.getServerProcess();
        this.prefix = watchDog.getWatchDogPrefix();
    }

    private void run() {
        final long restartTime = MathUtil.hoursToMillis(this.autoRestartConfig.getRestartTime());
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (!AutoRestartModule.this.serverProcess.isEnabled()) {
                    AutoRestartModule.this.logger.error("Nie można zrestartować servera gdy jest on wyłączony!");
                    return;
                }
                AutoRestartModule.this.restart(true);
                AutoRestartModule.this.lastRestartMillis = System.currentTimeMillis();
            }
        };

        if (this.autoRestartConfig.isEnabled()) {
            this.timer.scheduleAtFixedRate(task, restartTime, restartTime);
        } else {
            this.logger.debug("Automatyczny restart servera jest wyłączony");
        }
    }

    public void restart(final boolean alert) {
        try {
            if (this.restarting) return;
            this.restarting = true;
            if (!this.serverProcess.isEnabled()) {
                this.logger.error("Nie można zrestartować servera gdy jest on wyłączony!");
                return;
            }
            if (alert) this.restartAlert();
            this.serverProcess.kickAllPlayers("&aServer jest restartowany....");
            this.serverProcess.sendToConsole("stop");

            if (!this.serverProcess.getProcess().waitFor(10, TimeUnit.SECONDS)) {
                this.watchDog.getBackupModule().backup();
                this.serverProcess.getProcess().destroy();
            }

            if (!this.serverProcess.isCanRun()) {
                this.serverProcess.setCanRun(true);
                this.serverProcess.startProcess();
            }
        } catch (final Exception exception) {
            this.serverProcess.tellrawToAllAndLogger(this.prefix,
                    "Nie można zrestartować servera!", exception, LogState.ERROR);
            this.discord.sendEmbedMessage("Restart",
                    "Nie można zrestartować servera!",
                    exception,
                    " "
            );
        } finally {
            this.restarting = false;
        }
    }

    public void noteRestart(){
        //TODO: Dokończyć to 
this.timer.cancel();
        this.run();
this.lastRestartMillis = System.currentTimeMillis();
    }
    
    private void restartAlert(){
        AutoRestartModule.this.serverProcess.tellrawToAllAndLogger(AutoRestartModule.this.prefix,
                "&aZa&1 10&a sekund zostanie zrestartowany server!" , LogState.INFO);
        ThreadUtil.sleep(10);
        AutoRestartModule.this.serverProcess.tellrawToAllAndLogger(AutoRestartModule.this.prefix,
                "&aZa&1 5&a sekund zostanie zrestartowany server!" , LogState.INFO);
        ThreadUtil.sleep(5);
        AutoRestartModule.this.serverProcess.tellrawToAllAndLogger(AutoRestartModule.this.prefix,
                "&aZa&1 4&a sekund zostanie zrestartowany server!" , LogState.INFO);
        ThreadUtil.sleep(4);
        AutoRestartModule.this.serverProcess.tellrawToAllAndLogger(AutoRestartModule.this.prefix,
                "&aZa&1 3&a sekund zostanie zrestartowany server!" , LogState.INFO);
        ThreadUtil.sleep(3);
        AutoRestartModule.this.serverProcess.tellrawToAllAndLogger(AutoRestartModule.this.prefix,
                "&aZa&1 2&a sekund zostanie zrestartowany server!" , LogState.INFO);
        ThreadUtil.sleep(2);
        AutoRestartModule.this.serverProcess.tellrawToAllAndLogger(AutoRestartModule.this.prefix,
                "&aZa&1 1&a sekund zostanie zrestartowany server!" , LogState.INFO);
        ThreadUtil.sleep(1);
    }

    public long calculateMillisUntilNextRestart() {
        return Math.max(0, (MathUtil.hoursToMillis(this.autoRestartConfig.getRestartTime()) + 10) - (System.currentTimeMillis() - this.lastRestartMillis));
    }
}
