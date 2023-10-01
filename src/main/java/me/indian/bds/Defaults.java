package me.indian.bds;

import me.indian.bds.config.Config;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.SystemOs;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

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

    public static boolean hasWine() {
        return wine;
    }
}
