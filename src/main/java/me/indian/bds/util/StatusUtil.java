package me.indian.bds.util;

import com.sun.management.OperatingSystemMXBean;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.Defaults;
import me.indian.bds.config.Config;
import me.indian.bds.logger.Logger;
import me.indian.bds.manager.server.StatsManager;
import me.indian.bds.server.ServerProcess;

public final class StatusUtil {

    private static final List<String> status = new ArrayList<>();
    private static final File file = new File(File.separator);
    private static BDSAutoEnable bdsAutoEnable;
    private static Logger logger;
    private static ServerProcess serverProcess;
    private static StatsManager statsManager;
    private static Config config;

    public static void init(final BDSAutoEnable bdsAutoEnable) {
        StatusUtil.bdsAutoEnable = bdsAutoEnable;
        StatusUtil.logger = bdsAutoEnable.getLogger();
        StatusUtil.serverProcess = bdsAutoEnable.getServerProcess();
        StatusUtil.statsManager = bdsAutoEnable.getServerManager().getStatsManager();
        StatusUtil.config = bdsAutoEnable.getConfig();

    }

    public static List<String> getStatus(final boolean forDiscord) {
        status.clear();
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

        final String rom = "Dostępny: " + MathUtil.bytesToGB(availableDiskSpace()) + " GB " + MathUtil.getMbFromBytesGb(availableDiskSpace()) + " MB";
        final String maxRom = "Całkowity: " + MathUtil.bytesToGB(maxDiskSpace()) + " GB " + MathUtil.getMbFromBytesGb(maxDiskSpace()) + " MB";

        status.add("> **Statystyki maszyny**");
        status.add("Pamięć RAM: `" + freeComputerMemory + " / " + maxComputerMemory + "`");
        status.add("Pamięć ROM: `" + rom + " / " + maxRom + "`");
        status.add("Strefa czasowa maszyny: `" + ZoneId.systemDefault() +"`");
        status.add("");
        status.add("> **Statystyki servera**");
        status.add("Ostatnie TPS: `" + bdsAutoEnable.getServerManager().getLastTPS() + "`");
        status.add("Pamięć RAM: `" + usedServerMemory + "`");
        status.add("Czas działania: `" + DateUtil.formatTime(System.currentTimeMillis() - serverProcess.getStartTime()) + "`");

        if(config.getWatchDogConfig().getAutoRestartConfig().isEnabled()) {
            status.add("Następny restart za za: `" + DateUtil.formatTime(bdsAutoEnable.getWatchDog().getAutoRestartModule().calculateMillisUntilNextRestart()) + "`");
        }
        if(config.getWatchDogConfig().getBackupConfig().isBackup()) {
            status.add("Następny backup za: `" + DateUtil.formatTime(bdsAutoEnable.getWatchDog().getBackupModule().calculateMillisUntilNextBackup()) + "`");
        }

        status.add("");
        status.add("> **Statystyki aplikacji**");
        status.add("Czas działania: `" + DateUtil.formatTime(System.currentTimeMillis() - bdsAutoEnable.getStartTime()) + "`");
        status.add("Pamięć RAM: `" + usedAppMemory + " / " + committedAppMemory + " / " + maxAppMemory + "`");
        status.add("Aktualna liczba wątków: `" + Thread.activeCount() + "/" + ThreadUtil.getThreadsCount() + "`");
        status.add("Użycje cpu: `" + MathUtil.format((processCpuLoad * 100), 2) + "`% (Bugged jakieś)");

        if (!forDiscord) status.replaceAll(s -> s.replaceAll("`", "").replaceAll("\\*", "").replaceAll(">", ""));

        return status;
    }

    public static List<String> getTopPlayTime(final boolean forDiscord, final int top) {
        final Map<String, Long> playTimeMap = statsManager.getPlayTime();
        final List<String> topPlayTime = new ArrayList<>();

        final List<Map.Entry<String, Long>> sortedEntries = playTimeMap.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(top)
                .toList();

        int place = 1;
        for (final Map.Entry<String, Long> entry : sortedEntries) {
            topPlayTime.add(place + ". **" + entry.getKey() + "**: `" + DateUtil.formatTimeWithoutMillis(entry.getValue()) + "`");
            place++;
        }

        if (!forDiscord) topPlayTime.replaceAll(s -> s.replaceAll("`", "").replaceAll("\\*", "").replaceAll(">", ""));

        return topPlayTime;
    }

    public static List<String> getTopDeaths(final boolean forDiscord, final int top) {
        final Map<String, Long> deathsMap = statsManager.getDeaths();
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

    public static long availableDiskSpace() {
        return (file.exists() ? file.getUsableSpace() : 0);
    }

    public static long maxDiskSpace() {
        return (file.exists() ? file.getTotalSpace() : 0);
    }

    public static long getServerRamUsage() {
        if (!serverProcess.isEnabled()) return 0;
        try {
            switch (Defaults.getSystem()) {
                case WINDOWS -> {
                    return getMemoryUsageWindows(serverProcess.getProcess().pid());
                }
                case LINUX -> {
                    return getMemoryUsageLinux(serverProcess.getProcess().pid());
                }
            }
        } catch (final Exception exception) {
            logger.debug("Nie można uzyskać używanego ramu przez server dla systemu&1 " + Defaults.getSystem() , exception);
        }
        return -1;
    }

    public static long getAvailableRam() {
        return ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalMemorySize();
    }

    public static long getFreeRam() {
        return ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getFreeMemorySize();
    }

    private static long getMemoryUsageWindows(final long pid) throws IOException {
        final Process process = Runtime.getRuntime().exec("tasklist /NH /FI \"PID eq " + pid + "\"");
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(".exe")) {
                    final String[] tokens = line.split("\\s+");
                    if (tokens.length > 4) {
                        final String memoryStr = tokens[4].replaceAll("\\D", "");
                        return Long.parseLong(memoryStr);
                    }
                }
            }
        }
        return -1;
    }

    private static long getMemoryUsageLinux(final long pid) throws IOException {
        final Process process = Runtime.getRuntime().exec("ps -p " + pid + " -o rss=");
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            final String line = reader.readLine();
            return line != null ? Long.parseLong(line) : -1;
        }
    }
}