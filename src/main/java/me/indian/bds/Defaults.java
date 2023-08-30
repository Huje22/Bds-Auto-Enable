package me.indian.bds;

import me.indian.bds.config.Config;

import java.io.File;

public final class Defaults {

    private static Config config;

    public static void init(final BDSAutoEnable bdsAutoEnable) {
        config = bdsAutoEnable.getConfig();
    }

    public static String getSystem() {
        final String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return "Windows";
        } else if (os.contains("nix") || os.contains("nux")) {
            return "Linux";
        }
//        else if (os.contains("mac")) {
//            return "Mac";
//        }
        else {
            return "Nie wspierany system";
        }
    }

    public static String getDefaultFileName() {
        switch (config.getSystem()) {
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
        return System.getProperty("user.dir") + File.separator + "BDS-Auto-Enable";
    }

    public static String getWorldsPath() {
        return config.getFilesPath() + File.separator + "worlds" + File.separator;
    }
}
