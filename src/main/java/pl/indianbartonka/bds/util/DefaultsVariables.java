package pl.indianbartonka.bds.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;
import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.config.AppConfig;
import pl.indianbartonka.util.DateUtil;
import pl.indianbartonka.util.logger.Logger;
import pl.indianbartonka.util.system.SystemUtil;

public final class DefaultsVariables {

    public static boolean wine;
    public static boolean box64;
    private static AppConfig APPCONFIG;
    private static Logger LOGGER;

    private DefaultsVariables() {
    }

    public static void init(final BDSAutoEnable bdsAutoEnable) {
        APPCONFIG = bdsAutoEnable.getAppConfigManager().getAppConfig();
        LOGGER = bdsAutoEnable.getLogger();
        wine = wineCheck();
        box64 = box64();
    }

    public static String getDefaultFileName() {
        return switch (SystemUtil.getSystem()) {
            case LINUX -> (APPCONFIG.isWine() ? "bedrock_server.exe" : "bedrock_server");
            case WINDOWS -> "bedrock_server.exe";
            default -> "";
        };
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
                    if (line.contains("wine-")) return true;
                }
            }
            if (!process.waitFor(30, TimeUnit.MILLISECONDS)) process.destroy();
        } catch (final Exception exception) {
            LOGGER.debug("Nie znaleziono&1 WINE&r (Nie potrzebujesz go)");
        }
        return false;
    }

    private static boolean box64() {
        try {
            final Process process = Runtime.getRuntime().exec("box64 --version");
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("Box64")) return true;
                }
            }
            if (!process.waitFor(30, TimeUnit.MILLISECONDS)) process.destroy();
        } catch (final Exception exception) {
            LOGGER.debug("Nie znaleziono&1 Box64");
        }
        return false;
    }
}
