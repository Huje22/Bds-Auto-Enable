package me.indian.bds;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;
import me.indian.bds.config.Config;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.SystemOs;

public final class Defaults {

    private static Config config;
    private static Logger logger;
    private static boolean wine;

    public static void init(final BDSAutoEnable bdsAutoEnable) {
        config = bdsAutoEnable.getConfig();
        logger = bdsAutoEnable.getLogger();
        wine = wineCheck();
    }

    public static SystemOs getSystem() {
        final String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return SystemOs.WINDOWS;
        } else if (os.contains("nix") || os.contains("nux")) {
            return SystemOs.LINUX;
        }
//        else if (os.contains("mac")) {
//            return "Mac";
//        }
        else {
            return SystemOs.UNSUPPORTED;
        }
    }

    public static String getDefaultFileName() {
        switch (getSystem()) {
            case LINUX -> {
                return "bedrock_server";
            }
            case WINDOWS -> {
                return "bedrock_server.exe";
            }
            default -> {
                return "";
            }
        }
    }

    public static String getJarDir() {
        return System.getProperty("user.dir");
    }

    public static String getAppDir() {
        return System.getProperty("user.dir") + File.separator + "BDS-Auto-Enable" + File.separator;
    }

    public static String getWorldsPath() {
        return config.getFilesPath() + File.separator + "worlds" + File.separator;
    }

    public static boolean hasWine() {
        return wine;
    }

    public static boolean isPolisTimeZone() {
        return ZoneId.systemDefault().equals(ZoneId.of("Europe/Warsaw"));
    }

    public static boolean isJavaLoverThan17() {
        return Double.parseDouble(System.getProperty("java.version").substring(0, 3)) < 17.0;
    }

    private static boolean wineCheck() {
        try {
            final Process process = Runtime.getRuntime().exec("wine --version");
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("wine-")) {
                        return true;
                    }
                }
            }
            if (!process.waitFor(30, TimeUnit.MILLISECONDS)) process.destroy();
        } catch (final Exception exception) {
            logger.debug("Nie znaleziono&1 WINE&r (Nie potrzebujesz go)");
        }
        return false;
    }
}