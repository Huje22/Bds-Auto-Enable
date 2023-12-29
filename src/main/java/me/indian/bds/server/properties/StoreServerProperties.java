package me.indian.bds.server.properties;

import org.jetbrains.annotations.Nullable;

public record StoreServerProperties(
        int viewDistance,
        int serverPort,
        int serverPortV6,
        int maxThreads,
        int playerIdleTimeout,
        String serverName,
        ServerMovementAuth serverMovementAuth,
        double serverBuildRadiusRatio,
        boolean clientSideChunkGenerationEnabled,
        int tickDistance,
        boolean texturepackRequired,
        CompressionAlgorithm compressionAlgorithm,
        boolean allowCheats,
        Difficulty difficulty,
        PlayerPermissionLevel playerPermissionLevel,
        boolean correctPlayerMovement,
        int maxPlayers,
        boolean onlineMode,
        boolean emitServerTelemetry
) {

    @Nullable
    public static StoreServerProperties fromServerProperties(final ServerProperties serverProperties) {
        if (!serverProperties.propertiesExists()) return null;
        return new StoreServerProperties(
                serverProperties.getViewDistance(),
                serverProperties.getServerPort(),
                serverProperties.getServerPortV6(),
                serverProperties.getMaxThreads(),
                serverProperties.getPlayerIdleTimeout(),
                serverProperties.getMOTD(),
                serverProperties.getServerMovementAuth(),
                serverProperties.getServerBuildRadiusRatio(),
                serverProperties.isClientSideChunkGeneration(),
                serverProperties.getTickDistance(),
                serverProperties.isTexturePackRequired(),
                serverProperties.getCompressionAlgorithm(),
                serverProperties.isAllowCheats(),
                serverProperties.getDifficulty(),
                serverProperties.getPlayerPermissionLevel(),
                serverProperties.isCorrectPlayerMovement(),
                serverProperties.getMaxPlayers(),
                serverProperties.isOnlineMode(),
                serverProperties.isServerTelemetry()
        );
    }
}