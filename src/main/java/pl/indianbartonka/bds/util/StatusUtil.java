package pl.indianbartonka.bds.util;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.config.AppConfigManager;
import pl.indianbartonka.bds.server.ServerManager;
import pl.indianbartonka.bds.server.ServerProcess;
import pl.indianbartonka.bds.server.stats.ServerStats;
import pl.indianbartonka.bds.server.stats.StatsManager;
import pl.indianbartonka.bds.watchdog.WatchDog;
import pl.indianbartonka.bds.watchdog.monitor.RamMonitor;
import pl.indianbartonka.util.DateUtil;
import pl.indianbartonka.util.MathUtil;
import pl.indianbartonka.util.MemoryUnit;
import pl.indianbartonka.util.ThreadUtil;
import pl.indianbartonka.util.logger.Logger;
import pl.indianbartonka.util.system.SystemUtil;

public final class StatusUtil {

    private static final List<String> STATUS = new ArrayList<>();
    private static BDSAutoEnable BDSAUTOENABLE;
    private static Logger LOGGER;
    private static WatchDog WATCH_DOG;
    private static ServerProcess SERVERPROCESS;
    private static StatsManager STATSMANAGER;
    private static AppConfigManager APPCONFIGMANAGER;

    private StatusUtil() {
    }

    public static void init(final BDSAutoEnable bdsAutoEnable) {
        StatusUtil.BDSAUTOENABLE = bdsAutoEnable;
        StatusUtil.LOGGER = bdsAutoEnable.getLogger();
        StatusUtil.WATCH_DOG = bdsAutoEnable.getWatchDog();
        StatusUtil.SERVERPROCESS = bdsAutoEnable.getServerProcess();
        StatusUtil.STATSMANAGER = bdsAutoEnable.getServerManager().getStatsManager();
        StatusUtil.APPCONFIGMANAGER = bdsAutoEnable.getAppConfigManager();
    }

    public static List<String> getMainStats(final boolean markdown) {
        STATUS.clear();

        final WatchDog watchDog = BDSAUTOENABLE.getWatchDog();
        final RamMonitor ramMonitor = watchDog.getRamMonitor();
        final ServerStats serverStats = STATSMANAGER.getServerStats();

        final MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        final OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        final double processCpuLoad = operatingSystemMXBean.getCpuLoad();

        final String usedServerMemory = "Użyte " + MathUtil.formatKilobytesDynamic(getServerRamUsage(), true);

        final String usedComputerMemory = "Użyte " + MathUtil.formatBytesDynamic(SystemUtil.getUsedRam(), true);
        final String maxComputerMemory = "Całkowity " + MathUtil.formatBytesDynamic(SystemUtil.getMaxRam(), true);
        final String freeComputerMemory = "Wolny " + MathUtil.formatBytesDynamic(SystemUtil.getFreeRam(), true);

        final String usedAppMemory = "Użyte " + MemoryUnit.BYTES.to(heapMemoryUsage.getUsed(), MemoryUnit.MEGABYTES) + " MB";
        final String committedAppMemory = "Przydzielone " + MemoryUnit.BYTES.to(heapMemoryUsage.getCommitted(), MemoryUnit.MEGABYTES) + " MB";
        final String maxAppMemory = "Dostępne " + MemoryUnit.BYTES.to(heapMemoryUsage.getMax(), MemoryUnit.MEGABYTES) + " MB";

        final String usedRom = "Użyty: " + MathUtil.formatBytesDynamic(SystemUtil.getUsedMainDiskSpace(), true);
        final String rom = "Dostępny: " + MathUtil.formatBytesDynamic(SystemUtil.getFreeMainDiskSpace(), true);
//        final String maxRom = "Całkowity: " + MathUtil.bytesToGB(maxDiskSpace()) + " GB " + MathUtil.getMbFromBytesGb(maxDiskSpace()) + " MB";

        STATUS.add("> **Statystyki maszyny**");
        STATUS.add("Pamięć RAM: `" + usedComputerMemory + " / " + maxComputerMemory + "` (`" + freeComputerMemory + "`)");
        STATUS.add("Pamięć ROM: `" + usedRom + " / " + rom + "`");

        STATUS.add("");
        STATUS.add("> **Statystyki servera**");
        STATUS.add("Ostatnie TPS: `" + BDSAUTOENABLE.getServerManager().getLastTPS() + "`");
        STATUS.add("Pamięć RAM: `" + usedServerMemory + "` (`" + freeComputerMemory + "`)");
        STATUS.add("Średnie użycie ramu: `" + MathUtil.formatKilobytesDynamic(ramMonitor.getAverageServerRamUsage(), true) + "` (" + ramMonitor.getAverageServerRamUsageListSize() + ")");
        if (APPCONFIGMANAGER.getWatchDogConfig().getAutoRestartConfig().isEnabled()) {
            STATUS.add("Następny restart za: `" + DateUtil.formatTimeDynamic(watchDog.getAutoRestartModule().calculateMillisUntilNextRestart()) + "`");
        }
        if (APPCONFIGMANAGER.getWatchDogConfig().getBackupConfig().isEnabled()) {
            STATUS.add("Następny backup za: `" + DateUtil.formatTimeDynamic(watchDog.getBackupModule().calculateMillisUntilNextBackup()) + "`");
        }
        STATUS.add("Czas działania: `" + DateUtil.formatTimeDynamic(System.currentTimeMillis() - SERVERPROCESS.getStartTime()) + "`");
        STATUS.add("Łączny czas działania servera: `" + DateUtil.formatTimeDynamic(serverStats.getTotalUpTime()) + "`");

        STATUS.add("");
        STATUS.add("> **Statystyki aplikacji**");
        STATUS.add("Czas działania: `" + DateUtil.formatTimeDynamic(System.currentTimeMillis() - BDSAUTOENABLE.getStartTime()) + "`");
        STATUS.add("Pamięć RAM: `" + usedAppMemory + " / " + committedAppMemory + " / " + maxAppMemory + "`");
        STATUS.add("Średnie użycie ramu: `" + MathUtil.formatBytesDynamic(ramMonitor.getAverageAppRamUsage(), true) + "` (" + ramMonitor.getAverageAppRamUsageListSize() + ")");
        STATUS.add("Aktualna liczba wątków: `" + ThreadUtil.getThreadsCount() + "/" + ThreadUtil.getPeakThreadsCount() + "` ");
        STATUS.add("Użycje cpu: `" + MathUtil.formatDecimal((processCpuLoad * 100), 2) + "`% (Bugged jakieś)");

        if (!markdown) STATUS.replaceAll(s -> s.replaceAll("`", "&b").replaceAll("\\*", "&a").replaceAll("> ", "&l"));

        return STATUS;
    }

    public static String getShortStatus() {
        final StringBuilder stringBuilder = new StringBuilder();

        final ServerManager serverManager = BDSAUTOENABLE.getServerManager();
        final MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        final long ram = heapMemoryUsage.getUsed() + getServerRamUsage();

        stringBuilder.append(BDSAUTOENABLE.getProjectVersion()).append(" | ");
        stringBuilder.append(BDSAUTOENABLE.getVersionManager().getLoadedVersion()).append(" | ");
        stringBuilder.append("TPS: ").append(serverManager.getLastTPS()).append(" | ");
        stringBuilder.append("Online: ").append(serverManager.getOnlinePlayers().size()).append("/").append(BDSAUTOENABLE.getServerProperties().getMaxPlayers()).append(" | ");
        stringBuilder.append("Ram: ").append(MathUtil.formatBytesDynamic(ram + MemoryUnit.KILOBYTES.to(getServerRamUsage(), MemoryUnit.BYTES), true));

        return stringBuilder.toString();
    }

    public static long getServerRamUsage() {
        if (!SERVERPROCESS.isEnabled()) return 0;
        try {
            return SystemUtil.getRamUsageByPid(SERVERPROCESS.getPID());
        } catch (final Exception exception) {
            LOGGER.debug("Nie można uzyskać używanego ramu przez server dla systemu&1 " + SystemUtil.getSystem(), exception);
        }
        return -1;
    }
}
