package me.indian.bds.util;

import com.sun.management.OperatingSystemMXBean;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.Config;
import me.indian.bds.server.ServerProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;

public final class StatusUtil {

    private static final List<String> status = new ArrayList<>();
    private static final File file = new File("/");
    private static BDSAutoEnable bdsAutoEnable;
    private static ServerProcess serverProcess;
    private static Config config;

    public static void init(final BDSAutoEnable bdsAutoEnable, final ServerProcess serverProcess) {
        StatusUtil.bdsAutoEnable = bdsAutoEnable;
        StatusUtil.serverProcess = serverProcess;
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
        status.add("Pamięc RAM: `" + freeComputerMemory + " / " + maxComputerMemory + "`");
        status.add("Pamięc ROM: `" + rom + " / " + maxRom + "`");
        status.add("");
        status.add("> **Statystyki servera**");
        status.add("Pamięc RAM: `" + usedServerMemory + "`");
        status.add("Czas działania: `" + DateUtil.formatTime(System.currentTimeMillis() - serverProcess.getStartTime()) + "`");
        status.add("");
        status.add("> **Statystyki aplikacji**");
        status.add("Czas działania: `" + DateUtil.formatTime(System.currentTimeMillis() - bdsAutoEnable.getStartTime()) + "`");
        status.add("Pamięc RAM: `" + usedAppMemory + " / " + committedAppMemory + " / " + maxAppMemory + "`");
        status.add("Aktualna liczba wątków: `" + Thread.activeCount() + "/" + ThreadUtil.getThreadsCount() + "`");
        status.add("Użycje cpu: `" + MathUtil.format((processCpuLoad * 100) , 2)+ "`% (Bugged jakieś)");

        if (!forDiscord) status.replaceAll(s -> s.replaceAll("`", "").replaceAll("\\*", "").replaceAll(">", ""));

        return status;
    }

    public static long availableDiskSpace() {
        if (file.exists()) {
            return file.getUsableSpace();
        }
        return 0;
    }

    public static long maxDiskSpace() {
        if (file.exists()) {
            return file.getTotalSpace();
        }
        return 0;
    }

    public static long getServerRamUsage() {
        if (serverProcess.getProcess() == null || !serverProcess.getProcess().isAlive()) return 0;
        try {
            switch (config.getSystem()) {
                case WINDOWS -> {
                    return getMemoryUsageWindows(serverProcess.getProcess().pid());
                }
                case LINUX -> {
                    return getMemoryUsageLinux(serverProcess.getProcess().pid());
                }
            }
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
        return 0;
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