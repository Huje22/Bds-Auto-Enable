package me.indian.bds.server.properties;

import me.indian.bds.server.properties.Difficulty;
import me.indian.bds.server.properties.ServerProperties;
import me.indian.bds.server.properties.ServerMovementAuth;
import me.indian.bds.server.properties.CompressionAlgorithm;
import me.indian.bds.server.properties.PlayerPermissionLevel;

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

    public static StoreServerProperties fromServerProperties(ServerProperties serverProperties) {
        return new StoreServerProperties(
                serverProperties.getViewDistance(),
                serverProperties.getServerPort(),
                serverProperties.getServerPortV6(),
                serverProperties.getMaxThreads(),
                serverProperties.getPlayerIdleTimeout(),
                serverProperties.getServerName(),
                serverProperties.getServerMovementAuth(),
                serverProperties.getServerBuildRadiusRatio(),
                serverProperties.isClientSideChunkGenerationEnabled(),
                serverProperties.getTickDistance(),
                serverProperties.isTexturePackRequired(),
                serverProperties.getCompressionAlgorithm(),
                serverProperties.isAllowCheats(),
                serverProperties.getDifficulty(),
                serverProperties.getPlayerPermissionLevel(),
                serverProperties.isCorrectPlayerMovement(),
                serverProperties.getMaxPlayers(),
                serverProperties.isOnlineMode(),
                serverProperties.isEmitServerTelemetry()
        );
    }
}
