package me.indian.bds.server.properties;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.stream.Stream;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.properties.component.CompressionAlgorithm;
import me.indian.bds.server.properties.component.Difficulty;
import me.indian.bds.server.properties.component.PlayerPermissionLevel;
import me.indian.bds.server.properties.component.ServerMovementAuth;
import me.indian.bds.util.DefaultsVariables;
import me.indian.bds.util.MathUtil;
import me.indian.bds.version.VersionManager;

public class ServerProperties {

    private final BDSAutoEnable bdsAutoEnable;
    private final Properties properties;
    private final Logger logger;
    private final File propertiesFile;

    public ServerProperties(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.properties = new Properties();
        this.logger = this.bdsAutoEnable.getLogger();
        this.propertiesFile = new File(this.bdsAutoEnable.getAppConfigManager().getAppConfig().getFilesPath() + File.separator + "server.properties");
    }

    public void loadProperties() {
        this.checkForProperties();
        try {
            this.properties.clear();
            this.properties.load(Files.newInputStream(this.propertiesFile.toPath()));
        } catch (final IOException exception) {
            this.logger.critical("&cWystąpił krytyczny błąd podczas ładowania &aserver.properties", exception);
            System.exit(5);
        }
    }

    private void saveProperties() {
        try (final FileWriter writer = new FileWriter(this.propertiesFile)) {
            this.properties.store(writer, null);
        } catch (final Exception exception) {
            this.logger.critical("&cWystąpił krytyczny błąd podczas zapisywania&a server.properties", exception);
            System.exit(5);
        }
    }

    private void checkForProperties() {
        if (!this.propertiesFile.exists()) {
            final VersionManager manager = this.bdsAutoEnable.getVersionManager();
            if (manager != null) {
                manager.setLoaded(false);
                manager.loadVersion();
            } else {
                this.logger.critical("&cNie można odnaleźć pliku&a server.properties");
                System.exit(6);
            }
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
            this.logger.logThrowable(exception);
            this.setOnlineMode(true);
            return true;
        }
    }

    public void setOnlineMode(final boolean onlineMode) {
        this.properties.setProperty("online-mode", String.valueOf(onlineMode));
        this.reloadServerProperties();
    }

    public String getWorldName() {
        try {
            return this.properties.getProperty("level-name");
        } catch (final Exception exception) {
            this.logger.logThrowable(exception);
            this.setWorldName("Bedrock level");
            return "Bedrock level";
        }
    }

    public void setWorldName(final String name) {
        if (name == null) return;
        this.properties.setProperty("level-name", name);
        this.reloadServerProperties();
    }

    public String getRealWorldName() {
        final String levelNamePatch = DefaultsVariables.getWorldsPath() + this.getWorldName() + File.separator + "levelname.txt";
        final Path filePath = Paths.get(levelNamePatch);
        String name = "";

        if (Files.exists(filePath)) {
            try (final Stream<String> stream = Files.lines(filePath)) {
                name = stream.findFirst().orElse("");
            } catch (final IOException exception) {
                this.logger.logThrowable(exception);
            }
        }

        return name;
    }

    public String getMOTD() {
        try {
            return this.properties.getProperty("server-name");
        } catch (final Exception exception) {
            this.logger.logThrowable(exception);
            this.setMOTD("BDS-Auto-Enable Server");
            return "BDS-Auto-Enable Server";
        }
    }

    public void setMOTD(final String name) {
        if (name == null) return;
        this.properties.setProperty("server-name", name);
        this.reloadServerProperties();
    }

    public CompressionAlgorithm getCompressionAlgorithm() {
        try {
            return CompressionAlgorithm.getByName(this.properties.getProperty("compression-algorithm"));
        } catch (final NullPointerException exception) {
            this.logger.logThrowable(exception);
            this.setCompressionAlgorithm(CompressionAlgorithm.ZLIB);
        }
        return CompressionAlgorithm.ZLIB;
    }

    public void setCompressionAlgorithm(final CompressionAlgorithm compressionAlgorithm) {
        this.properties.setProperty("compression-algorithm", compressionAlgorithm.getAlgorithmName());
        this.reloadServerProperties();
    }

    public Difficulty getDifficulty() {
        try {
            final String difficulty1 = this.properties.getProperty("difficulty");
            try {
                return Difficulty.getById(Integer.parseInt(difficulty1));
            } catch (final NumberFormatException ignored) {
            }
            return Difficulty.getByName(difficulty1);
        } catch (final NullPointerException exception) {
            this.logger.logThrowable(exception);
            this.setDifficulty(Difficulty.NORMAL);
        }
        return Difficulty.NORMAL;
    }

    public void setDifficulty(final Difficulty difficulty) {
        this.properties.setProperty("difficulty", difficulty.getDifficultyName());
        this.reloadServerProperties();
    }

    public PlayerPermissionLevel getPlayerPermissionLevel() {
        try {
            final String permissionLevel = this.properties.getProperty("default-player-permission-level");
            try {
                return PlayerPermissionLevel.getByLevel(Integer.parseInt(permissionLevel));
            } catch (final NumberFormatException ignored) {
            }
            return PlayerPermissionLevel.getByName(permissionLevel);
        } catch (final NullPointerException exception) {
            this.logger.logThrowable(exception);
            this.setPlayerPermissionLevel(PlayerPermissionLevel.MEMBER);
            return PlayerPermissionLevel.MEMBER;
        }
    }

    public void setPlayerPermissionLevel(final PlayerPermissionLevel level) {
        this.properties.setProperty("default-player-permission-level", level.getPermissionName());
        this.reloadServerProperties();
    }

    public int getServerPort() {
        try {
            return Integer.parseInt(this.properties.getProperty("server-port"));
        } catch (final NumberFormatException exception) {
            this.logger.logThrowable(exception);
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
            this.logger.logThrowable(exception);
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
            this.logger.logThrowable(exception);
            this.setMaxThreads(8);
            return 8;
        }
    }

    public void setMaxThreads(final int threads) {
        this.properties.setProperty("max-threads", String.valueOf(MathUtil.getCorrectNumber(threads, 0, 12)));
        this.reloadServerProperties();
    }

    public int getMaxPlayers() {
        try {
            return Integer.parseInt(this.properties.getProperty("max-players"));
        } catch (final NumberFormatException exception) {
            this.logger.logThrowable(exception);
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
            this.logger.logThrowable(exception);
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
            this.logger.logThrowable(exception);
            this.setTickDistance(4);
            return 4;
        }
    }

    public void setTickDistance(final int tickDistance) {
        this.properties.setProperty("tick-distance", String.valueOf(MathUtil.getCorrectNumber(tickDistance, 4, 12)));
        this.reloadServerProperties();
    }

    public boolean isAllowCheats() {
        try {
            return Boolean.parseBoolean(this.properties.getProperty("allow-cheats"));
        } catch (final Exception exception) {
            this.logger.logThrowable(exception);
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
            this.logger.logThrowable(exception);
            this.setPlayerIdleTimeout(30);
            return 30;
        }
    }

    public void setPlayerIdleTimeout(final int minutes) {
        this.properties.setProperty("player-idle-timeout", String.valueOf(Math.max(0, minutes)));
        this.reloadServerProperties();
    }

    public boolean isServerTelemetry() {
        try {
            return Boolean.parseBoolean(this.properties.getProperty("emit-server-telemetry"));
        } catch (final Exception exception) {
            this.logger.logThrowable(exception);
            this.setServerTelemetry(false);
            return false;
        }
    }

    public void setServerTelemetry(final boolean telemetry) {
        this.properties.setProperty("emit-server-telemetry", String.valueOf(telemetry));
        this.reloadServerProperties();
    }

    public boolean isTexturePackRequired() {
        try {
            return Boolean.parseBoolean(this.properties.getProperty("texturepack-required"));
        } catch (final Exception exception) {
            this.logger.logThrowable(exception);
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
            this.logger.logThrowable(exception);
            this.setClientSideChunkGeneration(true);
            return true;
        }
    }

    public void setClientSideChunkGeneration(final boolean clientSide) {
        this.properties.setProperty("client-side-chunk-generation-enabled", String.valueOf(clientSide));
        this.reloadServerProperties();
    }

    public double getServerBuildRadiusRatio() {
        try {
            return Double.parseDouble(this.properties.getProperty("server-build-radius-ratio"));
        } catch (final NullPointerException | NumberFormatException exception) {
            this.setServerBuildRadiusRatio(-1.0);
            return -1.0;
        }
    }

    public void setServerBuildRadiusRatio(final double ratio) {
        if (ratio <= -1.0) {
            this.properties.setProperty("server-build-radius-ratio", "Disabled");
        } else {
            this.properties.setProperty("server-build-radius-ratio", String.valueOf(MathUtil.getCorrectNumber(ratio, 0.0, 1.0)));
        }
        this.reloadServerProperties();
    }

    public boolean isServerAuthoritativeBlockBreaking() {
        try {
            return Boolean.parseBoolean(this.properties.getProperty("server-authoritative-block-breaking"));
        } catch (final Exception exception) {
            this.logger.logThrowable(exception);
            this.setServerAuthoritativeBlockBreaking(false);
            return false;
        }
    }

    public void setServerAuthoritativeBlockBreaking(final boolean serverAuthoritativeBlockBreaking) {
        this.properties.setProperty("server-authoritative-block-breaking", String.valueOf(serverAuthoritativeBlockBreaking));
        this.reloadServerProperties();
    }

    public ServerMovementAuth getServerMovementAuth() {
        try {
            return ServerMovementAuth.getByName(this.properties.getProperty("server-authoritative-movement"));
        } catch (final Exception exception) {
            this.logger.logThrowable(exception);
            this.setServerMovementAuth(ServerMovementAuth.SERVER_AUTH);
            return ServerMovementAuth.SERVER_AUTH;
        }
    }

    public void setServerMovementAuth(final ServerMovementAuth serverAuthoritativeMovement) {
        this.properties.setProperty("server-authoritative-movement", serverAuthoritativeMovement.getAuthName());
        this.reloadServerProperties();
    }

    public boolean propertiesExists() {
        return this.propertiesFile.exists();
    }

    public Properties getProperties() {
        return this.properties;
    }

    @Override
    public String toString() {
        return "ServerProperties(" +
                " viewDistance=" + this.getViewDistance() +
//                ", levelSeed='" + this.getLevelSeed() + '\'' +
//                ", disablePersona=" + this.isDisablePersona() +
//                ", disablePlayerInteraction=" + this.isDisablePlayerInteraction() +
//                ", blockNetworkIdsAreHashes=" + this.isBlockNetworkIdsAreHashes() +
//                ", levelName='" + this.getLevelName() + '\'' +
//                ", gamemode='" + this.getGamemode() + '\'' +
                ", serverPort=" + this.getServerPort() +
                ", serverPortV6=" + this.getServerPortV6() +
                ", maxThreads=" + this.getMaxThreads() +
//                ", enableLanVisibility=" + this.isEnableLanVisibility() +
                ", playerIdleTimeout=" + this.getPlayerIdleTimeout() +
                ", serverName='" + this.getMOTD() + "&r'" +
//                ", playerMovementDistanceThreshold=" + this.getPlayerMovementDistanceThreshold() +
                ", serverAuthoritativeMovement='" + this.getServerMovementAuth() + '\'' +
                ", serverBuildRadiusRatio=" + this.getServerBuildRadiusRatio() +
                ", clientSideChunkGenerationEnabled=" + this.isClientSideChunkGeneration() +
                ", tickDistance=" + this.getTickDistance() +
                ", texturepackRequired=" + this.isTexturePackRequired() +
//                ", compressionThreshold=" + this.getCompressionThreshold() +
                ", compressionAlgorithm='" + this.getCompressionAlgorithm() + '\'' +
//                ", forceGamemode=" + this.isForceGamemode() +
//                ", playerMovementScoreThreshold=" + this.getPlayerMovementScoreThreshold() +
                ", allowCheats=" + this.isAllowCheats() +
                ", difficulty='" + this.getDifficulty() + '\'' +
                ", playerPermissionLevel='" + this.getPlayerPermissionLevel() + '\'' +
//                ", chatRestriction='" + this.getChatRestriction() + '\'' +
                ", maxPlayers=" + this.getMaxPlayers() +
                ", onlineMode=" + this.isOnlineMode() +
                ", emitServerTelemetry=" + this.isServerTelemetry() +
//                ", disableCustomSkins=" + this.isDisableCustomSkins() +
//                ", allowList=" + this.isAllowList() +
//                ", contentLogFileEnabled=" + this.isContentLogFileEnabled() +
                ')';
    }
}