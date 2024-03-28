package me.indian.bds.event.player;

import me.indian.bds.event.Event;
import me.indian.bds.event.Position;

public class PlayerInteractEntityWithContainerEvent extends Event {

    private final String playerName, entityID;
    private final Position blockPosition;

    public PlayerInteractEntityWithContainerEvent(final String playerName, final String entityID, final Position blockPosition) {
        super();
        this.playerName = playerName;
        this.entityID = entityID;
        this.blockPosition = blockPosition;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public String getEntityID() {
        return this.entityID;
    }

    public Position getBlockPosition() {
        return this.blockPosition;
    }
}
