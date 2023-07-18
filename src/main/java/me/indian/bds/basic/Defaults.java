package me.indian.bds.basic;

import me.indian.bds.Main;
import me.indian.bds.config.Config;
import me.indian.bds.util.SystemOs;

import java.io.File;

public class Defaults {

    private static final Config config = Main.getConfig();


    public static String getSystem() {
        String os = System.getProperty("os.name").toLowerCase();

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
        if (config.getSystemOs() == SystemOs.LINUX) {
            return "bedrock_server";
        }
        if (config.getSystemOs() == SystemOs.WINDOWS) {
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
