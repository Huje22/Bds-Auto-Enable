package me.indian.bds.server;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.Config;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.MathUtil;

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

    public boolean isOnlineMode() {
        try {
            return Boolean.parseBoolean(this.properties.getProperty("online-mode"));
        } catch (final Exception exception) {
            return true;
        }
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

    public int getDifficulty() {
        try {
            return Integer.parseInt(this.properties.getProperty("difficulty"));
        } catch (final NumberFormatException exception) {
            this.setDifficulty(2);
            return 2;
        }
    }

    public void setDifficulty(final int port) {
        this.properties.setProperty("difficulty", String.valueOf(MathUtil.getCorrectNumber(port, 0, 3)));
        this.reloadServerProperties();
    }


    public int getServerPort() {
        try {
            return Integer.parseInt(this.properties.getProperty("server-port"));
        } catch (final NumberFormatException exception) {
            this.setServerPort(19132);
            return 19132;
        }
    }

    public void setServerPort(final int port) {
        this.properties.setProperty("server-port", String.valueOf(port));
        this.reloadServerProperties();
    }

    public int getServerPortV6() {
        try {
            return Integer.parseInt(this.properties.getProperty("server-portv6"));
        } catch (final NumberFormatException exception) {
            this.setServerPortV6(19133);
            return 19133;
        }
    }

    public void setServerPortV6(final int port) {
        this.properties.setProperty("server-portv6", String.valueOf(port));
        this.reloadServerProperties();
    }

    public int getMaxThreads() {
        try {
            return Integer.parseInt(this.properties.getProperty("max-threads"));
        } catch (final NumberFormatException exception) {
            this.setMaxThreads(8);
            return 8;
        }
    }

    public void setMaxThreads(final int threads) {
        this.properties.setProperty("max-threads", String.valueOf(Math.max(threads, 0)));
        this.reloadServerProperties();
    }

    public int getMaxPlayers() {
        try {
            return Integer.parseInt(this.properties.getProperty("max-players"));
        } catch (final NumberFormatException exception) {
            this.setMaxPlayers(10);
            return 10;
        }
    }

    public void setMaxPlayers(final int players) {
        this.properties.setProperty("max-players", String.valueOf(Math.max(players, 1)));
        this.reloadServerProperties();
    }

    public int getViewDistance() {
        try {
            return Integer.parseInt(this.properties.getProperty("view-distance"));
        } catch (final NumberFormatException exception) {
            this.setViewDistance(32);
            return 32;
        }
    }

    public void setViewDistance(final int tickDistance) {
        this.properties.setProperty("view-distance", String.valueOf(Math.max(tickDistance, 5)));
        this.reloadServerProperties();
    }


    public int getTickDistance() {
        try {
            return Integer.parseInt(this.properties.getProperty("tick-distance"));
        } catch (final NumberFormatException exception) {
            this.setTickDistance(4);
            return 4;
        }
    }

    public void setTickDistance(final int tickDistance) {
        this.properties.setProperty("tick-distance", String.valueOf(MathUtil.getCorrectNumber(tickDistance , 4, 12)));
        this.reloadServerProperties();
    }

    public boolean isAllowCheats() {
        try {
            return Boolean.parseBoolean(this.properties.getProperty("allow-cheats"));
        } catch (final Exception exception) {
            this.setAllowCheats(true);
            return true;
        }
    }

    public void setAllowCheats(final boolean allowCheats) {
        this.properties.setProperty("allow-cheats", String.valueOf(allowCheats));
        this.reloadServerProperties();
    }

    public int getPlayerIdleTimeout() {
        try {
            return Integer.parseInt(this.properties.getProperty("player-idle-timeout"));
        } catch (final NumberFormatException exception) {
            this.setPlayerIdleTimeout(30);
            return 30;
        }
    }

    public void setPlayerIdleTimeout(final int minutes) {
        this.properties.setProperty("player-idle-timeout", String.valueOf(Math.max(0, minutes)));
        this.reloadServerProperties();
    }


    public boolean isTexturePackRequired() {
        try {
            return Boolean.parseBoolean(this.properties.getProperty("texturepack-required"));
        } catch (final Exception exception) {
            this.setTexturePackRequired(false);
            return false;
        }
    }

    public void setTexturePackRequired(final boolean texturePackRequired) {
        this.properties.setProperty("texturepack-required", String.valueOf(texturePackRequired));
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
