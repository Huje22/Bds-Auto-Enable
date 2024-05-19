package me.indian.bds.util;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.AppConfig;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.system.SystemUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

public final class DefaultsVariables {

    public static boolean WINE;
    private static AppConfig APPCONFIG;
    private static Logger LOGGER;

    private DefaultsVariables() {
    }

    public static void init(final BDSAutoEnable bdsAutoEnable) {
        APPCONFIG = bdsAutoEnable.getAppConfigManager().getAppConfig();
        LOGGER = bdsAutoEnable.getLogger();
        WINE = wineCheck();
    }

    public static String getDefaultFileName() {
        return switch (SystemUtil.getSystem()) {
            case LINUX ->
                    (APPCONFIG.isWine() ? (isLeviLamina() ? "bedrock_server_mod.exe" : "bedrock_server.exe") : "bedrock_server");
            case WINDOWS -> (isLeviLamina() ? "bedrock_server_mod.exe" : "bedrock_server.exe");
            default -> "";
        };
    }

    public static boolean isLeviLamina() {
        return new File(APPCONFIG.getFilesPath() + File.separator + "bedrock_server_mod.exe").exists();
    }

    public static String getJarDir() {
        return System.getProperty("user.dir");
    }

    public static String getUserHome() {
        return System.getProperty("user.home");
    }

    public static String getAppDir() {
        return System.getProperty("user.dir") + File.separator + "BDS-Auto-Enable" + File.separator;
    }

    public static String getLogsDir() {
        return getAppDir() + "logs";
    }

    public static String getWorldsPath() {
        return APPCONFIG.getFilesPath() + File.separator + "worlds" + File.separator;
    }

    public static boolean isPolisTimeZone() {
        return ZoneId.systemDefault().equals(DateUtil.POLISH_ZONE);
    }

    public static boolean isJavaLoverThan17() {
        return Double.parseDouble(System.getProperty("java.specification.version")) < 17.0;
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
