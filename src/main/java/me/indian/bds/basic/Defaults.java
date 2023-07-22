package me.indian.bds.basic;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.Config;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.SystemOs;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class Defaults {

    private static final Config config = BDSAutoEnable.getConfig();
    private static final Logger logger = BDSAutoEnable.getLogger();
    private static final Properties properties = new Properties();


    public static String getSystem() {
      final   String os = System.getProperty("os.name").toLowerCase();

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

    public static String getWorldName() {
        try {
            final InputStream input = Files.newInputStream(Paths.get(config.getFilesPath() + "/" + "server.properties"));
            properties.load(input);
            return properties.getProperty("level-name");
        } catch (final Exception e) {
            logger.critical("Nie udało znaleźć sie nazwy pliku świata");
            throw new RuntimeException(e);
        }
    }

    public static String getWorldsPath() {
        return config.getFilesPath() + File.separator + "worlds" + File.separator;
    }
}
