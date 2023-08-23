package me.indian.bds.util;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.server.ServerProcess;

public class StatusUtil {

    private static final List<String> status = new ArrayList<>();
    private static final File file = new File("/");
    private static ServerProcess serverProcess;
    private static BDSAutoEnable bdsAutoEnable;


    public static void init(final BDSAutoEnable bdsAutoEnable, final ServerProcess serverProcess) {
        StatusUtil.bdsAutoEnable = bdsAutoEnable;
        StatusUtil.serverProcess = serverProcess;
    }


    public static List<String> getStatus(boolean forDiscord) {
        status.clear();
        final MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        final String usedMemory = "Użyte " + MathUtil.bytesToMB(heapMemoryUsage.getUsed()) + " MB";
        final String committedMemory = "Przydzielone " + MathUtil.bytesToMB(heapMemoryUsage.getCommitted()) + " MB";
        final String maxMemory = "Dostępne " + MathUtil.bytesToMB(heapMemoryUsage.getMax()) + " MB";

        status.add("**Statystyki servera**");
        status.add("Czas działania `" + DateUtil.formatTime(System.currentTimeMillis() - serverProcess.getStartTime()) + "`");
        status.add("**Statystyki aplikacij**");
        status.add("Czas działania `" + DateUtil.formatTime(System.currentTimeMillis() - bdsAutoEnable.getStartTime()) + "`");
        status.add("Pamięc RAM `" + usedMemory + " / " + committedMemory + " / " + maxMemory + "`");
        status.add("Aktualna liczba wątków: `" + Thread.activeCount() + "/" + ThreadUtil.getThreadsCount() + "`");

        if (!forDiscord) status.replaceAll(s -> s.replaceAll("`", "").replaceAll("\\*", ""));

        return status;
    }

    public static long availableGbSpace() {
        if (file.exists()) {
            return file.getUsableSpace() / (1024 * 1024 * 1024);
        }
        return 0;
    }

    public static long availableMbSpace() {
        if (file.exists()) {
            return file.getUsableSpace() / (1024 * 1024);
        }
        return 0;
    }
}