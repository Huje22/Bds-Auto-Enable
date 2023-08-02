package me.indian.bds.files;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.Config;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.ConsoleColors;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class ServerProperties {

    private final BDSAutoEnable bdsAutoEnable;
    private final Properties properties;
    private final Logger logger;
    private final Config config;

    public ServerProperties(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.properties = new Properties();
        this.config = this.bdsAutoEnable.getConfig();
        this.logger = this.bdsAutoEnable.getLogger();

    }

    public void loadProperties() {
        try {
            final InputStream input = Files.newInputStream(Paths.get(this.config.getFilesPath() + "/server.properties"));
            this.properties.clear();
            this.properties.load(input);
        } catch (IOException e) {
            this.logger.critical(ConsoleColors.RED + "Wystąpił krytyczny błąd podczas ładowania " + ConsoleColors.GREEN + "server.properties" + ConsoleColors.RESET);
            this.bdsAutoEnable.getServerProcess().shutdown(false);
            throw new RuntimeException(e);
        }
    }

    public String getWorldName() {
        return properties.getProperty("level-name");
    }

    public Properties getProperties() {
        return this.properties;
    }
}