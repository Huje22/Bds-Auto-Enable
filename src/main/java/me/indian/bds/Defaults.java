package me.indian.bds;

import me.indian.bds.config.Config;
import me.indian.bds.util.SystemOs;

import java.io.File;

public class Defaults {

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
        if (config.getSystem() == SystemOs.LINUX) {
            return "bedrock_server";
        }
        if (config.getSystem() == SystemOs.WINDOWS) {
            return "bedrock_server.exe";
        }
        return "";
    }

    public static String getJarPath() {
        return System.getProperty("user.dir");
    }


    public static String getWorldsPath() {
        return config.getFilesPath() + File.separator + "worlds" + File.separator;
    }
}
