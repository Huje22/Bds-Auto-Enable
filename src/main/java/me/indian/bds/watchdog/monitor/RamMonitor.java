package me.indian.bds.watchdog.monitor;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.sub.watchdog.RamMonitorConfig;
import me.indian.bds.logger.LogState;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.MathUtil;
import me.indian.bds.util.ServerUtil;
import me.indian.bds.util.StatusUtil;
import me.indian.bds.watchdog.WatchDog;

public class RamMonitor {

    private final Timer ramMonitorTimer;
    private final Logger logger;
    private final String prefix;
    private final RamMonitorConfig ramMonitorConfig;
    private final List<Long> averageAppRamUsageList, averageServerRamUsageList;
    private boolean ramMonitorRunning;

    public RamMonitor(final BDSAutoEnable bdsAutoEnable, final WatchDog watchDog) {
        this.ramMonitorTimer = new Timer("RamMonitorTimer", true);
        this.logger = bdsAutoEnable.getLogger();
        this.prefix = watchDog.getWatchDogPrefix();
        this.ramMonitorConfig = bdsAutoEnable.getAppConfigManager().getWatchDogConfig().getRamMonitorConfig();
        this.averageAppRamUsageList = new ArrayList<>();
        this.averageServerRamUsageList = new ArrayList<>();
        this.ramMonitorRunning = false;
    }

    public void init() {
        this.monitRamUsage();
        this.monitAverageRamUsage();
    }

    private void monitAverageRamUsage() {
        final long tenMinutes = MathUtil.minutesTo(10, TimeUnit.MILLISECONDS);
        final long maxEntries = 10000 * MathUtil.bytesToGB(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax());

        final TimerTask app = new TimerTask() {

            @Override
            public void run() {
                if (RamMonitor.this.averageAppRamUsageList.size() == maxEntries) {
                    RamMonitor.this.averageAppRamUsageList.clear();
                }

                RamMonitor.this.averageAppRamUsageList.add(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed());
            }
        };

        final TimerTask server = new TimerTask() {
            @Override
            public void run() {
                if (RamMonitor.this.averageServerRamUsageList.size() == maxEntries) {
                    RamMonitor.this.averageServerRamUsageList.clear();
                }

                RamMonitor.this.averageServerRamUsageList.add(StatusUtil.getServerRamUsage());
            }
        };

        this.ramMonitorTimer.scheduleAtFixedRate(app, tenMinutes, tenMinutes);
        this.ramMonitorTimer.scheduleAtFixedRate(server, tenMinutes, tenMinutes);
    }

    private void monitRamUsage() {
        if (this.ramMonitorRunning) return;
        this.ramMonitorRunning = true;
        final TimerTask appRamMonitor = new TimerTask() {
            @Override
            public void run() {
                final MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
                final long freeMem = MathUtil.bytesToMB(heapMemoryUsage.getMax() - heapMemoryUsage.getUsed());

                if (MathUtil.bytesToMB(heapMemoryUsage.getUsed()) >= (MathUtil.bytesToMB(heapMemoryUsage.getMax()) * 0.80)) {
                    ServerUtil.tellrawToAllAndLogger(RamMonitor.this.prefix,
                            "&cAplikacja używa&b 80%&c dostępnej dla niej pamięci&b RAM&4!!!" + "&d(&c Wolne:&b " + freeMem + " &aMB&d )",
                            LogState.CRITICAL);
                    ServerUtil.tellrawToAllAndLogger(RamMonitor.this.prefix,
                            "&cWiększe użycie może to prowadzić do crashy aplikacji a w tym servera&4!!",
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

    /**
     * Oblicza średnie zużycie pamięci RAM przez aplikację.
     * <p>
     * Metoda sumuje wszystkie wartości przechowywane w averageAppRamUsageList i dzieli przez liczbę elementów na liście.
     * Obiekty są dodawane do listy co 10 minut.
     *
     * @return średnie zużycie pamięci RAM przez aplikację, w bajtach.
     */
    public long getAverageAppRamUsage() {
        long sum = 0;

        for (final long value : this.averageAppRamUsageList) {
            sum += value;
        }

        final long size = this.averageAppRamUsageList.size();
        return (size == 0 ? 0 : sum / size);
    }

    /**
     * Oblicza średnie zużycie pamięci RAM przez serwer.
     * <p>
     * Metoda sumuje wszystkie wartości przechowywane w averageServerRamUsageList i dzieli przez liczbę elementów na liście.
     * Obiekty są dodawane do listy co 10 minut.
     *
     * @return średnie zużycie pamięci RAM przez serwer, w kilo bajtach.
     */
    public long getAverageServerRamUsage() {
        long sum = 0;

        for (final long value : this.averageServerRamUsageList) {
            sum += value;
        }

        final long size = this.averageServerRamUsageList.size();
        return (size == 0 ? 0 : sum / size);
    }
}