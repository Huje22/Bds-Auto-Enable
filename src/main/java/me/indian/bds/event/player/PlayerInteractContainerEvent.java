package me.indian.bds.event.player;

import me.indian.bds.event.Event;
import me.indian.bds.event.Position;

public class PlayerInteractContainerEvent extends Event {

    private final String playerInteract, blockID;
    private final Position blockPosition;

    public PlayerInteractContainerEvent(final String playerInteract, final String blockID, final Position blockPosition) {
        this.playerInteract = playerInteract;
        this.blockID = blockID;
        this.blockPosition = blockPosition;
    }

    public String getPlayerInteract() {
        return this.playerInteract;
    }

    public String getBlockID() {
        return this.blockID;
    }

    public Position getBlockPosition() {
        return this.blockPosition;
    }
}