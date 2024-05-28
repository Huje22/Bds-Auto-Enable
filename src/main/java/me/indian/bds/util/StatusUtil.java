package me.indian.bds.util;

import com.sun.management.OperatingSystemMXBean;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.AppConfigManager;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerManager;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.server.stats.ServerStats;
import me.indian.bds.server.stats.StatsManager;
import me.indian.bds.util.system.SystemUtil;
import me.indian.bds.watchdog.WatchDog;

public final class StatusUtil {

    private static final List<String> STATUS = new ArrayList<>();
    private static final File FILE = new File(File.separator);
    private static BDSAutoEnable BDSAUTOENABLE;
    private static Logger LOGGER;
    private static ServerProcess SERVERPROCESS;
    private static StatsManager STATSMANAGER;
    private static AppConfigManager APPCONFIGMANAGER;

    private StatusUtil() {
    }

    public static void init(final BDSAutoEnable bdsAutoEnable) {
        StatusUtil.BDSAUTOENABLE = bdsAutoEnable;
        StatusUtil.LOGGER = bdsAutoEnable.getLogger();
        StatusUtil.SERVERPROCESS = bdsAutoEnable.getServerProcess();
        StatusUtil.STATSMANAGER = bdsAutoEnable.getServerManager().getStatsManager();
        StatusUtil.APPCONFIGMANAGER = bdsAutoEnable.getAppConfigManager();
    }

    public static List<String> getMainStats(final boolean markdown) {
        STATUS.clear();

        final WatchDog watchDog = BDSAUTOENABLE.getWatchDog();
        final ServerStats serverStats = STATSMANAGER.getServerStats();

        final MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        final OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        final double processCpuLoad = operatingSystemMXBean.getCpuLoad();

        final long computerRam = getAvailableRam();
        final long computerFreeRam = getFreeRam();
        final long computerRamUsed = getUsedRam();

        final String usedServerMemory = "Użyte " + MathUtil.formatKiloBytesDynamic(getServerRamUsage(), true);

        final String usedComputerMemory = "Użyte " + MathUtil.formatBytesDynamic(computerRamUsed, true);
        final String maxComputerMemory = "Całkowity " + MathUtil.formatBytesDynamic(computerRam, true);
        final String freeComputerMemory = "Wolny " + MathUtil.formatBytesDynamic(computerFreeRam, true);

        final String usedAppMemory = "Użyte " + MathUtil.bytesToMB(heapMemoryUsage.getUsed()) + " MB";
        final String committedAppMemory = "Przydzielone " + MathUtil.bytesToMB(heapMemoryUsage.getCommitted()) + " MB";
        final String maxAppMemory = "Dostępne " + MathUtil.bytesToMB(heapMemoryUsage.getMax()) + " MB";

        final String usedRom = "Użyty: " + MathUtil.formatBytesDynamic(usedDiskSpace(), true);
        final String rom = "Dostępny: " + MathUtil.formatBytesDynamic(availableDiskSpace(), true);
//        final String maxRom = "Całkowity: " + MathUtil.bytesToGB(maxDiskSpace()) + " GB " + MathUtil.getMbFromBytesGb(maxDiskSpace()) + " MB";

        STATUS.add("> **Statystyki maszyny**");
        STATUS.add("Pamięć RAM: `" + usedComputerMemory + " / " + maxComputerMemory + "` (`" + freeComputerMemory + "`)");
        STATUS.add("Pamięć ROM: `" + usedRom + " / " + rom + "`");

        STATUS.add("");
        STATUS.add("> **Statystyki servera**");
        STATUS.add("Ostatnie TPS: `" + BDSAUTOENABLE.getServerManager().getLastTPS() + "`");
        STATUS.add("Pamięć RAM: `" + usedServerMemory + "` (`" + freeComputerMemory + "`)");
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
        STATUS.add("Aktualna liczba wątków: `" + ThreadUtil.getThreadsCount() + "/" + ThreadUtil.getPeakThreadsCount() + "` ");
        STATUS.add("Użycje cpu: `" + MathUtil.format((processCpuLoad * 100), 2) + "`% (Bugged jakieś)");

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
        stringBuilder.append("Ram: ").append(MathUtil.formatBytesDynamic(ram + MathUtil.kilobytesToBytes(getServerRamUsage()), true));

        return stringBuilder.toString();
    }

    public static long availableDiskSpace() {
        return (FILE.exists() ? FILE.getUsableSpace() : 0);
    }

    public static long maxDiskSpace() {
        return (FILE.exists() ? FILE.getTotalSpace() : 0);
    }

    public static long usedDiskSpace() {
        return (maxDiskSpace() - availableDiskSpace());
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

    public static long getUsedRam() {
        return getAvailableRam() - getFreeRam();
    }

    public static long getAvailableRam() {
        return ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalMemorySize();
    }

    public static long getFreeRam() {
        return ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getFreeMemorySize();
    }
}
