package me.indian.bds.util;

import com.sun.management.OperatingSystemMXBean;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.AppConfigManager;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.server.ServerStats;
import me.indian.bds.server.manager.stats.StatsManager;
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

    public static List<String> getMainStats(final boolean forDiscord) {
        STATUS.clear();

        final WatchDog watchDog = BDSAUTOENABLE.getWatchDog();
        final ServerStats serverStats = STATSMANAGER.getServerStats();

        final MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        final OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        final double processCpuLoad = operatingSystemMXBean.getProcessCpuLoad();

        final long computerRam = getAvailableRam();
        final long computerFreeRam = getFreeRam();

        final String usedServerMemory = "Użyte " + MathUtil.kilobytesToGb(getServerRamUsage()) + " GB " + MathUtil.getMbFromKilobytesGb(getServerRamUsage()) + " MB";
        final String maxComputerMemory = "Całkowity " + MathUtil.bytesToGB(computerRam) + " GB " + MathUtil.getMbFromBytesGb(computerRam) + " MB";
        final String freeComputerMemory = "Wolny " + MathUtil.bytesToGB(computerFreeRam) + " GB " + MathUtil.getMbFromBytesGb(computerFreeRam) + " MB";

        final String usedAppMemory = "Użyte " + MathUtil.bytesToMB(heapMemoryUsage.getUsed()) + " MB";
        final String committedAppMemory = "Przydzielone " + MathUtil.bytesToMB(heapMemoryUsage.getCommitted()) + " MB";
        final String maxAppMemory = "Dostępne " + MathUtil.bytesToMB(heapMemoryUsage.getMax()) + " MB";

        final String usedRom = "Użyty: " + MathUtil.bytesToGB(usedDiskSpace()) + " GB " + MathUtil.getMbFromBytesGb(usedDiskSpace()) + " MB";
        final String rom = "Dostępny: " + MathUtil.bytesToGB(availableDiskSpace()) + " GB " + MathUtil.getMbFromBytesGb(availableDiskSpace()) + " MB";
//        final String maxRom = "Całkowity: " + MathUtil.bytesToGB(maxDiskSpace()) + " GB " + MathUtil.getMbFromBytesGb(maxDiskSpace()) + " MB";

        STATUS.add("> **Statystyki maszyny**");
        STATUS.add("Pamięć RAM: `" + freeComputerMemory + " / " + maxComputerMemory + "`");
        STATUS.add("Pamięć ROM: `" + usedRom + " / " + rom + "`");

        STATUS.add("");
        STATUS.add("> **Statystyki servera**");
        STATUS.add("Ostatnie TPS: `" + BDSAUTOENABLE.getServerManager().getLastTPS() + "`");
        STATUS.add("Pamięć RAM: `" + usedServerMemory + "`");
        if (APPCONFIGMANAGER.getWatchDogConfig().getAutoRestartConfig().isEnabled()) {
            STATUS.add("Następny restart za za: `" + DateUtil.formatTime(watchDog.getAutoRestartModule().calculateMillisUntilNextRestart(), List.of('d', 'h', 'm', 's', 'i')) + "`");
        }
        if (APPCONFIGMANAGER.getWatchDogConfig().getBackupConfig().isEnabled()) {
            STATUS.add("Następny backup za: `" + DateUtil.formatTime(watchDog.getBackupModule().calculateMillisUntilNextBackup(), List.of('d', 'h', 'm', 's', 'i')) + "`");
        }
        STATUS.add("Czas działania: `" + DateUtil.formatTime(System.currentTimeMillis() - SERVERPROCESS.getStartTime(), List.of('d', 'h', 'm', 's', 'i')) + "`");
        STATUS.add("Łączny czas działania servera: `" + DateUtil.formatTime(serverStats.getTotalUpTime(), List.of('d', 'h', 'm', 's')) + "`");

        STATUS.add("");
        STATUS.add("> **Statystyki aplikacji**");
        STATUS.add("Czas działania: `" + DateUtil.formatTime(System.currentTimeMillis() - BDSAUTOENABLE.getStartTime(), List.of('d', 'h', 'm', 's', 'i')) + "`");
        STATUS.add("Pamięć RAM: `" + usedAppMemory + " / " + committedAppMemory + " / " + maxAppMemory + "`");
        STATUS.add("Aktualna liczba wątków: `" + Thread.activeCount() + "/" + ThreadUtil.getThreadsCount() + "`");
        STATUS.add("Użycje cpu: `" + MathUtil.format((processCpuLoad * 100), 2) + "`% (Bugged jakieś)");

        if (!forDiscord) STATUS.replaceAll(s -> s.replaceAll("`", "&b").replaceAll("\\*", "&a").replaceAll("> ", "&l"));

        return STATUS;
    }

    public static List<String> getTopPlayTime(final boolean forDiscord, final int top) {
        final Map<String, Long> playTimeMap = STATSMANAGER.getPlayTime();
        final List<String> topPlayTime = new ArrayList<>();

        final List<Map.Entry<String, Long>> sortedEntries = playTimeMap.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(top)
                .toList();

        int place = 1;
        for (final Map.Entry<String, Long> entry : sortedEntries) {
            topPlayTime.add(place + ". **" + entry.getKey() + "**: `" + DateUtil.formatTime(entry.getValue(), List.of('d', 'h', 'm', 's')) + "`");
            place++;
        }

        if (!forDiscord) topPlayTime.replaceAll(s -> s.replaceAll("`", "").replaceAll("\\*", "").replaceAll(">", ""));

        return topPlayTime;
    }

    public static List<String> getTopDeaths(final boolean forDiscord, final int top) {
        final Map<String, Long> deathsMap = STATSMANAGER.getDeaths();
        final List<String> topDeaths = new ArrayList<>();

        final List<Map.Entry<String, Long>> sortedEntries = deathsMap.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(top)
                .toList();

        int place = 1;
        for (final Map.Entry<String, Long> entry : sortedEntries) {
            topDeaths.add(place + ". **" + entry.getKey() + "**: `" + entry.getValue() + "`");
            place++;
        }

        if (!forDiscord) topDeaths.replaceAll(s -> s.replaceAll("`", "").replaceAll("\\*", "").replaceAll(">", ""));

        return topDeaths;
    }

    public static List<String> getTopBlockBroken(final boolean forDiscord, final int top) {
        final Map<String, Long> brokenMap = STATSMANAGER.getBlockBroken();
        final List<String> topBroken = new ArrayList<>();

        final List<Map.Entry<String, Long>> sortedEntries = brokenMap.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(top)
                .toList();

        int place = 1;
        for (final Map.Entry<String, Long> entry : sortedEntries) {
            topBroken.add(place + ". **" + entry.getKey() + "**: `" + entry.getValue() + "`");
            place++;
        }

        if (!forDiscord) topBroken.replaceAll(s -> s.replaceAll("`", "").replaceAll("\\*", "").replaceAll(">", ""));

        return topBroken;
    }

    public static List<String> getTopBlockPlaced(final boolean forDiscord, final int top) {
        final Map<String, Long> placedMap = STATSMANAGER.getBlockPlaced();
        final List<String> topPlaced = new ArrayList<>();

        final List<Map.Entry<String, Long>> sortedEntries = placedMap.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(top)
                .toList();

        int place = 1;
        for (final Map.Entry<String, Long> entry : sortedEntries) {
            topPlaced.add(place + ". **" + entry.getKey() + "**: `" + entry.getValue() + "`");
            place++;
        }

        if (!forDiscord) topPlaced.replaceAll(s -> s.replaceAll("`", "").replaceAll("\\*", "").replaceAll(">", ""));

        return topPlaced;
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

    public static long getAvailableRam() {
        return ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalMemorySize();
    }

    public static long getFreeRam() {
        return ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getFreeMemorySize();
    }
}