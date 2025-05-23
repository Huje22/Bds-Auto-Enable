package pl.indianbartonka.bds.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;
import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.config.AppConfig;
import pl.indianbartonka.util.DateUtil;
import pl.indianbartonka.util.IndianUtils;
import pl.indianbartonka.util.logger.Logger;
import pl.indianbartonka.util.system.SystemUtil;

public final class DefaultsVariables {

    public static boolean wine;
    public static boolean box64;
    public static boolean box86;
    private static AppConfig APPCONFIG;
    private static Logger LOGGER;

    private DefaultsVariables() {
    }

    public static void init(final BDSAutoEnable bdsAutoEnable) {
        APPCONFIG = bdsAutoEnable.getAppConfigManager().getAppConfig();
        LOGGER = bdsAutoEnable.getLogger();
        wine = IndianUtils.wineCheck();
        box64 = IndianUtils.box64Check();
        box86 = IndianUtils.box86Check();
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
}
