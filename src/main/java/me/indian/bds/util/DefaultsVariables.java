package me.indian.bds.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.SystemOS;
import me.indian.bds.config.AppConfig;
import me.indian.bds.logger.Logger;

public final class DefaultsVariables {

    private static AppConfig APPCONFIG;
    private static Logger LOGGER;
    private static boolean WINE;

    public static void init(final BDSAutoEnable bdsAutoEnable) {
        APPCONFIG = bdsAutoEnable.getAppConfigManager().getAppConfig();
        LOGGER = bdsAutoEnable.getLogger();
        WINE = wineCheck();
    }

    public static SystemOS getSystem() {
        final String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return SystemOS.WINDOWS;
        } else if (os.contains("nix") || os.contains("nux")) {
            return SystemOS.LINUX;
        }
//        else if (os.contains("mac")) {
//            return "Mac";
//        }
        else {
            return SystemOS.UNSUPPORTED;
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
        return APPCONFIG.getFilesPath() + File.separator + "worlds" + File.separator;
    }

    public static boolean hasWine() {
        return WINE;
    }

    public static boolean isPolisTimeZone() {
        return ZoneId.systemDefault().equals(DateUtil.POLISH_ZONE);
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
            LOGGER.debug("Nie znaleziono&1 WINE&r (Nie potrzebujesz go)");
        }
        return false;
    }
}