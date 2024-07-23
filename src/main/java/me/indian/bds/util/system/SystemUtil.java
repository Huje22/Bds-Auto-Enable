package me.indian.bds.util.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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

    public static String getFullOsNameWithDistribution() {
        return switch (getSystem()) {
            case WINDOWS, UNSUPPORTED -> getFullyOsName();
            case LINUX -> getFullyOsName() + " (" + getLinuxDistribution() + ")";
        };
    }

    public static SystemArch getCurrentArch() {
        final String osArch = System.getProperty("os.arch").toLowerCase();

        return switch (osArch) {
            case "amd64", "x86_64" -> SystemArch.AMD_X64;
            case "x86", "i386", "i486", "i586", "i686" -> SystemArch.AMD_X32;
            case "aarch64", "arm", "arm32", "armv7", "armv8" -> SystemArch.ARM;
            default -> SystemArch.UNKNOWN;
        };
    }

    public static String getFullyArchCode() {
        return System.getProperty("os.arch");
    }

    public static String getLinuxDistribution() {
        final String releaseFilePath = "/etc/os-release";

        try {
            final Path path = Paths.get(releaseFilePath);
            final List<String> lines = Files.readAllLines(path);

            for (final String line : lines) {
                if (line.startsWith("PRETTY_NAME=")) {
                    return line.substring(12, line.length() - 1).replaceAll("\"", "");
                }
            }
        } catch (final IOException ioException) {
            ioException.printStackTrace();
        }

        return "Nieznana";
    }

    public static long getRamUsageByPid(final long pid) throws IOException {
        return switch (getSystem()) {
            case WINDOWS -> getMemoryUsageWindows(pid);
            case LINUX -> getMemoryUsageLinux(pid);
            default -> throw new UnsupportedOperationException("Nie można pozyskać ilość ram dla wspieranego systemu");
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
