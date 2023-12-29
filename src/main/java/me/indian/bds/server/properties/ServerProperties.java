package me.indian.bds.server.properties;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.logger.Logger;
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
            System.exit(0);
        }
    }

    private void saveProperties() {
        try (final FileWriter writer = new FileWriter(this.propertiesFile)) {
            this.properties.store(writer, null);
        } catch (final Exception exception) {
            this.logger.critical("&cWystąpił krytyczny błąd podczas zapisywania&a server.properties", exception);
            System.exit(0);
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
                System.exit(0);
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
            this.logger.debug("", exception);
            return true;
        }
    }

    public String getWorldName() {
        try {
            return this.properties.getProperty("level-name");
        } catch (final Exception exception) {
            this.logger.debug("", exception);
            this.setWorldName("Bedrock level");
            return "Bedrock level";
        }
    }

    public void setWorldName(final String name) {
        this.properties.setProperty("level-name", name);
        this.reloadServerProperties();
    }

    public String getMOTD() {
        try {
            return this.properties.getProperty("server-name");
        } catch (final Exception exception) {
            this.logger.debug("", exception);
            this.setMOTD("BDS-Auto-Enable Server");
            return "BDS-Auto-Enable Server";
        }
    }

    public void setMOTD(final String name) {
        this.properties.setProperty("server-name", name);
        this.reloadServerProperties();
    }

    public CompressionAlgorithm getCompressionAlgorithm() {
        try {
            final String algorithm = this.properties.getProperty("compression-algorithm");
            switch (algorithm.toLowerCase()) {
                case "zlib" -> {
                    return CompressionAlgorithm.ZLIB;
                }
                case "snappy" -> {
                    return CompressionAlgorithm.SNAPPY;
                }

            }
        } catch (final Exception exception) {
            this.logger.debug("", exception);
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
            int difficulty2 = -1;
            try {
                difficulty2 = Integer.parseInt(difficulty1);
            } catch (final NumberFormatException ignored) {
            }

            if (difficulty1.equalsIgnoreCase("peaceful") || difficulty2 == 0) {
                return Difficulty.PEACEFUL;
            } else if (difficulty1.equalsIgnoreCase("easy") || difficulty2 == 1) {
                return Difficulty.EASY;
            } else if (difficulty1.equalsIgnoreCase("normal") || difficulty2 == 2) {
                return Difficulty.NORMAL;
            } else if (difficulty1.equalsIgnoreCase("hard") || difficulty2 == 3) {
                return Difficulty.HARD;
            }

        } catch (final Exception exception) {
            this.logger.debug("", exception);
            this.setDifficulty(Difficulty.NORMAL);
        }
        return Difficulty.NORMAL;
    }

    public void setDifficulty(final Difficulty difficulty) {
        this.properties.setProperty("difficulty", difficulty.getDifficultyName());
        this.reloadServerProperties();
    }

    public PlayerPermissionLevel getDefaultPlayerPermissionLevel() {
        try {
            final String permissionLevel = this.properties.getProperty("default-player-permission-level");
            int level = -1;
            try {
                level = Integer.parseInt(permissionLevel);
            } catch (final NumberFormatException ignored) {
            }

            if (permissionLevel.equalsIgnoreCase("VISITOR") || level == 0) {
                return PlayerPermissionLevel.VISITOR;
            } else if (permissionLevel.equalsIgnoreCase("MEMBER") || level == 1) {
                return PlayerPermissionLevel.MEMBER;
            } else if (permissionLevel.equalsIgnoreCase("OPERATOR") || level == 2) {
                return PlayerPermissionLevel.OPERATOR;
            } else {
                return PlayerPermissionLevel.MEMBER;
            }

        } catch (final Exception exception) {
            this.logger.debug("", exception);
            this.setDefaultPlayerPermissionLevel(PlayerPermissionLevel.MEMBER);
            return PlayerPermissionLevel.MEMBER;
        }
    }

    public void setDefaultPlayerPermissionLevel(final PlayerPermissionLevel level) {
        this.properties.setProperty("default-player-permission-level", level.getPermissionName());
        this.reloadServerProperties();
    }

    public int getServerPort() {
        try {
            return Integer.parseInt(this.properties.getProperty("server-port"));
        } catch (final NumberFormatException exception) {
            this.logger.debug("", exception);
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
            this.logger.debug("", exception);
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
            this.logger.debug("", exception);
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
            this.logger.debug("", exception);
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
            this.logger.debug("", exception);
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
            this.logger.debug("", exception);
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
            this.logger.debug("", exception);
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
            this.logger.debug("", exception);
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
            this.logger.debug("", exception);
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
            this.logger.debug("", exception);
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
            this.logger.debug("", exception);
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
            this.logger.debug("", exception);
            this.setServerAuthoritativeBlockBreaking(false);
            return false;
        }
    }

    public void setServerAuthoritativeBlockBreaking(final boolean serverAuthoritativeBlockBreaking) {
        this.properties.setProperty("server-authoritative-block-breaking", String.valueOf(serverAuthoritativeBlockBreaking));
        this.reloadServerProperties();
    }

    public ServerMovementAuth getServerAuthoritativeMovement() {
        try {
            final String serverMovement = this.properties.getProperty("server-authoritative-movement");
            switch (serverMovement) {
                case "client-auth" -> {
                    return ServerMovementAuth.CLIENT_AUTH;
                }
//            case "server-auth" -> {
//              return ServerMovementAuth.SERVER;
//            }
                case "server-auth-with-rewind" -> {
                    return ServerMovementAuth.SERVER_AUTH_REWIND;
                }
                default -> {
                    return ServerMovementAuth.SERVER_AUTH;
                }
            }
        } catch (final Exception exception) {
            this.logger.debug("", exception);
            this.setServerAuthoritativeMovement(ServerMovementAuth.SERVER_AUTH);
            return ServerMovementAuth.SERVER_AUTH;
        }
    }

    public void setServerAuthoritativeMovement(final ServerMovementAuth serverAuthoritativeMovement) {
        this.properties.setProperty("server-authoritative-movement", serverAuthoritativeMovement.getAuthName());
        this.reloadServerProperties();
    }

    public boolean isCorrectPlayerMovement() {
        try {
            return Boolean.parseBoolean(this.properties.getProperty("correct-player-movement"));
        } catch (final Exception exception) {
            this.logger.debug("", exception);
            this.setCorrectPlayerMovement(false);
            return false;
        }
    }

    public void setCorrectPlayerMovement(final boolean clientSide) {
        this.properties.setProperty("correct-player-movement", String.valueOf(clientSide));
        this.reloadServerProperties();
    }

public void setFromStored(final StoreServerProperties storedProperties) {
        this.setViewDistance(storedProperties.viewDistance());
        this.setServerPort(storedProperties.serverPort());
        this.setServerPortV6(storedProperties.serverPortV6());
        this.setMaxThreads(storedProperties.maxThreads());
        this.setPlayerIdleTimeout(storedProperties.playerIdleTimeout());
        this.setServerName(storedProperties.serverName());
        this.setServerMovementAuth(storedProperties.serverMovementAuth());
        this.setServerBuildRadiusRatio(storedProperties.serverBuildRadiusRatio());
        this.setClientSideChunkGenerationEnabled(storedProperties.clientSideChunkGenerationEnabled());
        this.setTickDistance(storedProperties.tickDistance());
        this.setTexturePackRequired(storedProperties.texturepackRequired());
        this.setCompressionAlgorithm(storedProperties.compressionAlgorithm());
        this.setAllowCheats(storedProperties.allowCheats());
        this.setDifficulty(storedProperties.difficulty());
        this.setPlayerPermissionLevel(storedProperties.playerPermissionLevel());
        this.setCorrectPlayerMovement(storedProperties.correctPlayerMovement());
        this.setMaxPlayers(storedProperties.maxPlayers());
        this.setOnlineMode(storedProperties.onlineMode());
        this.setEmitServerTelemetry(storedProperties.emitServerTelemetry());
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
                ", serverAuthoritativeMovement='" + this.getServerAuthoritativeMovement() + '\'' +
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
                ", correctPlayerMovement=" + this.isCorrectPlayerMovement() +
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
