package me.indian.bds.util;

import com.sun.management.OperatingSystemMXBean;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.Config;
import me.indian.bds.server.ServerProcess;

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


    public static List<String> getStatus(boolean forDiscord) {
        status.clear();
        final MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();

        final long memorySize = getAvailableRam();
        final String usedServerMemory = "Użyte " + MathUtil.kilobytesToMb(getServerRamUsage()) + " MB";
        final String maxComputerMemory = "Ram maszyny " + getFormattedRam();


        final String usedAppMemory = "Użyte " + MathUtil.bytesToMB(heapMemoryUsage.getUsed()) + " MB";
        final String committedAppMemory = "Przydzielone " + MathUtil.bytesToMB(heapMemoryUsage.getCommitted()) + " MB";
        final String maxAppMemory = "Dostępne " + MathUtil.bytesToMB(heapMemoryUsage.getMax()) + " MB";

        status.add("> **Statystyki servera**");
        status.add("Pamięc RAM `" + usedServerMemory + " / " + maxComputerMemory + "`");
        status.add("Czas działania `" + DateUtil.formatTime(System.currentTimeMillis() - serverProcess.getStartTime()) + "`");
        status.add("");
        status.add("> **Statystyki aplikacji**");
        status.add("Czas działania `" + DateUtil.formatTime(System.currentTimeMillis() - bdsAutoEnable.getStartTime()) + "`");
        status.add("Pamięc RAM `" + usedAppMemory + " / " + committedAppMemory + " / " + maxAppMemory + "`");
        status.add("Aktualna liczba wątków: `" + Thread.activeCount() + "/" + ThreadUtil.getThreadsCount() + "`");

        if (!forDiscord) status.replaceAll(s -> s.replaceAll("`", "").replaceAll("\\*", "").replaceAll(">", ""));

        return status;
    }

    public static long availableDiskSpace() {
        if (file.exists()) {
            return file.getUsableSpace();
        }
        return 0;
    }

    public static long getServerRamUsage() {
        if (!serverProcess.getProcess().isAlive()) return 0;
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

    public static String getFormattedRam() {
        final long memorySize = getAvailableRam();
        return MathUtil.bytesToGB(memorySize) + "GB " +
                MathUtil.bytesToMB(memorySize - (MathUtil.bytesToGB(memorySize) * 1024 * 1024 * 1024)) + "MB";
    }

    private static long getMemoryUsageWindows(long pid) throws IOException {
        final Process process = Runtime.getRuntime().exec("tasklist /NH /FI \"PID eq " + pid + "\"");
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(".exe")) {
                    final String[] tokens = line.split("\\s+");
                    if (tokens.length > 4) {
                        String memoryStr = tokens[4].replaceAll("\\D", "");
                        return Long.parseLong(memoryStr);
                    }
                }
            }
        }
        return -1;
    }

    private static long getMemoryUsageLinux(long pid) throws IOException {
        final Process process = Runtime.getRuntime().exec("ps -p " + pid + " -o rss=");
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line = reader.readLine();
            return line != null ? Long.parseLong(line) : -1;
        }
    }
}