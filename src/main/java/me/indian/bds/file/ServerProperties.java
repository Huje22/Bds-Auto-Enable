package me.indian.bds.file;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.Config;
import me.indian.bds.logger.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class ServerProperties {

    private final Properties properties;
    private final Logger logger;
    private final Config config;

    public ServerProperties(final BDSAutoEnable bdsAutoEnable) {
        this.properties = new Properties();
        this.config = bdsAutoEnable.getConfig();
        this.logger = bdsAutoEnable.getLogger();
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
            this.setWorldName("Bedrock level");
            return "Bedrock level";
        }
    }

    public void setWorldName(final String name) {
        this.properties.setProperty("level-name", name);
        this.reloadServerProperties();
    }

    public int getServerPort() {
        try {
            return Integer.parseInt(this.properties.getProperty("server-port"));
        } catch (final Exception exception) {
            this.setServerPort(19132);
            return 19132;
        }
    }

    public int getServerPortV6() {
        try {
            return Integer.parseInt(this.properties.getProperty("server-portv6"));
        } catch (final Exception exception) {
            this.setServerPortV6(19133);
            return 19133;
        }
    }

    public int getMaxThreads() {
        try {
            return Integer.parseInt(this.properties.getProperty("max-threads"));
        } catch (final Exception exception) {
            this.setMaxThreads(8);
            return 8;
        }
    }

    public int getMaxPlayers() {
        try {
            return Integer.parseInt(this.properties.getProperty("max-players"));
        } catch (final Exception exception) {
            this.setMaxPlayers(10);
            return 10;
        }
    }

    public void setMaxPlayers(final int players) {
        this.properties.setProperty("max-players", String.valueOf(players));
        this.reloadServerProperties();
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

    public boolean isClientSideChunkGeneration() {
        try {
            return Boolean.parseBoolean(this.properties.getProperty("client-side-chunk-generation-enabled"));
        } catch (final Exception exception) {
            this.setClientSideChunkGeneration(true);
            return true;
        }
    }

    public void setClientSideChunkGeneration(final boolean clientSide) {
        this.properties.setProperty("client-side-chunk-generation-enabled", String.valueOf(clientSide));
        this.reloadServerProperties();
    }

    public Properties getProperties() {
        return this.properties;
    }
}
