package me.indian.bds.event.player;

import me.indian.bds.event.Event;
import me.indian.bds.event.Position;

public class PlayerInteractEntityWithContainerEvent extends Event {

    private final String playerInteract, entityID;
    private final Position blockPosition;

    public PlayerInteractEntityWithContainerEvent(final String playerInteract, final String entityID, final Position blockPosition) {
        super();
        this.playerInteract = playerInteract;
        this.entityID = entityID;
        this.blockPosition = blockPosition;
    }

    public String getPlayerInteract() {
        return this.playerInteract;
    }

    public String getEntityID() {
        return this.entityID;
    }

    public Position getBlockPosition() {
        return this.blockPosition;
    }
}
