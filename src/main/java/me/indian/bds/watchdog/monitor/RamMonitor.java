package me.indian.bds.watchdog.monitor;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.Timer;
import java.util.TimerTask;
import me.indian.bds.discord.DiscordIntegration;
import me.indian.bds.logger.LogState;
import me.indian.bds.util.MathUtil;
import me.indian.bds.util.MinecraftUtil;
import me.indian.bds.util.StatusUtil;
import me.indian.bds.watchdog.WatchDog;

public class RamMonitor {

    private final Timer timer;
    private final String prefix;
    private DiscordIntegration discord;
    private boolean running = false;

    public RamMonitor(final WatchDog watchDog) {
        this.timer = new Timer("RamMonitor", true);
        this.prefix = watchDog.getWatchDogPrefix();
    }

    public void initRamMonitor(final DiscordIntegration discord) {
        this.discord = discord;
    }

    public void monitRamUsage() {
        if (this.running) return;
        this.running = true;
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                final MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
                final long freeMem = MathUtil.bytesToMB(heapMemoryUsage.getMax() - heapMemoryUsage.getUsed());
                if (MathUtil.bytesToMB(heapMemoryUsage.getUsed()) >= ((long) (MathUtil.bytesToMB(heapMemoryUsage.getMax()) * 0.80))) {
                    MinecraftUtil.tellrawToAllAndLogger(RamMonitor.this.prefix,
                            "&cAplikacija używa&b 80%&c dostępnej dla niej pamięci&b RAM&4!!!" + "&d(&c Wolne:&b " + freeMem + " &aMB&d )",
                            LogState.CRITICAL);
                    MinecraftUtil.tellrawToAllAndLogger(RamMonitor.this.prefix,
                            "&cWiększe użycje może to prowadzić do crashy aplikacij a w tym servera&4!!",
                            LogState.CRITICAL);
                    MinecraftUtil.tellrawToAllAndLogger(RamMonitor.this.prefix,
                            "&cServer używa:&b " + MathUtil.kilobytesToMb(StatusUtil.getServerRamUsage()) + " &aMB &d(&aTen ram to ram używany przez proces servera&d)",
                            LogState.ALERT);

                    RamMonitor.this.discord.sendServerFire();
                }
            }
        };
        this.timer.scheduleAtFixedRate(task, 0, 5000);
    }
}