package me.indian.bds.util.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import me.indian.bds.exception.UnSupportedSystemException;

public final class SystemUtil {

    private SystemUtil() {
    }

    public static SystemOS getSystem() {
        final String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return SystemOS.WINDOWS;
        } else if (os.contains("nix") || os.contains("nux")) {
            return SystemOS.LINUX;
//        } else if (os.contains("mac")) {
//            return SystemOS.MAC;
        } else {
            return SystemOS.UNSUPPORTED;
        }
    }

    public static String getFullyOsName() {
        return System.getProperty("os.name");
    }

    public static void clearSystemCache() throws IOException, UnSupportedSystemException {
        switch (getSystem()) {
            case LINUX -> Runtime.getRuntime().exec("sync && echo 3 > /proc/sys/vm/drop_caches");
            case WINDOWS -> {
                //TODO: Dodać sensowne wspracie dla windows
            }
            default -> throw new UnSupportedSystemException("Nie można wyczyścić pamięci cache dla nie wspieranego systemu");
        }
    }

    public static long getRamUsageByPid(final long pid) throws IOException, UnSupportedSystemException {
        return switch (getSystem()) {
            case WINDOWS -> getMemoryUsageWindows(pid);
            case LINUX -> getMemoryUsageLinux(pid);
            default -> throw new UnSupportedSystemException("Nie można pozyskać ilość ram dla wspieranego systemu");
        };
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