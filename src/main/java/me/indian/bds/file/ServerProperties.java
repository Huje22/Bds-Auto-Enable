package me.indian.bds.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.Config;
import me.indian.bds.logger.Logger;

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
            this.properties.clear();
            this.properties.load(Files.newInputStream(Paths.get(this.config.getFilesPath() + "/server.properties")));
        } catch (final IOException exception) {
            this.logger.critical("&cWystąpił krytyczny błąd podczas ładowania &aserver.properties");
            this.logger.critical(exception);
            System.exit(0);
        }
    }

    private void saveProperties() {
        try {
            this.properties.store(Files.newOutputStream(Paths.get(this.config.getFilesPath() + "/server.properties")), null);
        } catch (final IOException exception) {
            this.logger.critical("&cWystąpił krytyczny błąd podczas zapisywania&a server.properties");
            this.logger.critical(exception);
            System.exit(0);
        }
    }

    public void reloadServerProperties() {
        this.saveProperties();
        this.loadProperties();
    }

    public String getWorldName() {
        try {
            return this.properties.getProperty("level-name");
        } catch (final Exception exception) {
            return "Bedrock level";
        }
    }

    public int getServerPort() {
        try {
            return Integer.parseInt(this.properties.getProperty("server-port"));
        } catch (final Exception exception) {
            return 19132;
        }
    }

    public int getServerPortV6() {
        try {
            return Integer.parseInt(this.properties.getProperty("server-portv6"));
        } catch (final Exception exception) {
            return 19133;
        }
    }

    public int getMaxThreads() {
        try {
            return Integer.parseInt(this.properties.getProperty("max-threads"));
        } catch (final Exception exception) {
            return 8;
        }
    }
    
    public int getMaxPlayers() {
        try {
            return Integer.parseInt(this.properties.getProperty("max-players"));
        } catch (final Exception exception) {
            return 10;
        }
    }

    public boolean isClientSideChunkGeneration() {
        try {
            return Boolean.parseBoolean(this.properties.getProperty("client-side-chunk-generation-enabled"));
        } catch (final Exception exception) {
            return true;
        }
    }

    public void setServerPort(final int port) {
        this.properties.setProperty("server-port", String.valueOf(port));
        this.reloadServerProperties();
    }

    public void setServerPortV6(final int port) {
        this.properties.setProperty("server-portv6", String.valueOf(port));
        this.reloadServerProperties();
    }

    public void setMaxThreads(final int threads) {
        this.properties.setProperty("max-threads", String.valueOf(threads));
        this.reloadServerProperties();
    }

    public void setClientSideChunkGeneration(final boolean clientSide) {
        this.properties.setProperty("client-side-chunk-generation-enabled", String.valueOf(clientSide));
        this.reloadServerProperties();
    }

    public Properties getProperties() {
        return this.properties;
    }
}
