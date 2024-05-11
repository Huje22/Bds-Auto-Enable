package me.indian.bds.watchdog.monitor;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.Timer;
import java.util.TimerTask;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.sub.watchdog.RamMonitorConfig;
import me.indian.bds.logger.LogState;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.MathUtil;
import me.indian.bds.util.ServerUtil;
import me.indian.bds.util.StatusUtil;
import me.indian.bds.watchdog.WatchDog;

public class RamMonitor {

    private final BDSAutoEnable bdsAutoEnable;
    private final Timer ramMonitorTimer;
    private final Logger logger;
    private final String prefix;
    private final RamMonitorConfig ramMonitorConfig;
    private boolean running;

    public RamMonitor(final BDSAutoEnable bdsAutoEnable, final WatchDog watchDog) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.ramMonitorTimer = new Timer("RamMonitorTimer", true);
        this.logger = this.bdsAutoEnable.getLogger();
        this.prefix = watchDog.getWatchDogPrefix();
        this.ramMonitorConfig = this.bdsAutoEnable.getAppConfigManager().getWatchDogConfig().getRamMonitorConfig();
        this.running = false;

    }

    public void monitRamUsage() {
        if (this.running) return;
        this.running = true;
        final TimerTask appRamMonitor = new TimerTask() {
            @Override
            public void run() {
                final MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
                final long freeMem = MathUtil.bytesToMB(heapMemoryUsage.getMax() - heapMemoryUsage.getUsed());
                if (MathUtil.bytesToMB(heapMemoryUsage.getUsed()) >= ((long) (MathUtil.bytesToMB(heapMemoryUsage.getMax()) * 0.80))) {
                    ServerUtil.tellrawToAllAndLogger(RamMonitor.this.prefix,
                            "&cAplikacja używa&b 80%&c dostępnej dla niej pamięci&b RAM&4!!!" + "&d(&c Wolne:&b " + freeMem + " &aMB&d )",
                            LogState.CRITICAL);
                   ServerUtil.tellrawToAllAndLogger(RamMonitor.this.prefix,
                            "&cWiększe użycje może to prowadzić do crashy aplikacji a w tym servera&4!!",
                            LogState.CRITICAL);
                }
            }
        };

        final TimerTask machineRamMonitor = new TimerTask() {
            @Override
            public void run() {
                final long computerRam = StatusUtil.getAvailableRam();
                final long computerFreeRam = StatusUtil.getFreeRam();

                final long computerFreeRamGb = MathUtil.bytesToGB(computerFreeRam);
                final String freeComputerMemory = "&eWolne:&a " + MathUtil.formatBytesDynamic(computerFreeRam, true);
                final String maxComputerMemory = "&eCałkowite:&a " + MathUtil.formatBytesDynamic(computerRam, true);

                if (computerFreeRamGb < 1) {
                    ServerUtil.tellrawToAllAndLogger(RamMonitor.this.prefix,
                            "&cMaszyna posiada mniej niż&b 1GB&c wolej pamięci ram!",
                            LogState.ALERT);
                    ServerUtil.tellrawToAllAndLogger(RamMonitor.this.prefix,
                            freeComputerMemory + " / " + maxComputerMemory,
                            LogState.ALERT);
                }
            }
        };

        if (this.ramMonitorConfig.isMachine()) {
            this.ramMonitorTimer.scheduleAtFixedRate(machineRamMonitor, 0, MathUtil.secondToMillis(this.ramMonitorConfig.getCheckMachineTime()));
        } else {
            this.logger.debug("Monitorowanie ramu maszyny jest wyłączone");
        }

        if (this.ramMonitorConfig.isApp()) {
            this.ramMonitorTimer.scheduleAtFixedRate(appRamMonitor, 0, MathUtil.secondToMillis(this.ramMonitorConfig.getCheckAppTime()));
        } else {
            this.logger.debug("Monitorowanie ramu aplikacji jest wyłączone");
        }
    }
}