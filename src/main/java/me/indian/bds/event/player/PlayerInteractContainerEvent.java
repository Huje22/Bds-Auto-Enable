package me.indian.bds.event.player;

import me.indian.bds.event.Event;
import me.indian.bds.event.Position;

public class PlayerInteractContainerEvent extends Event {

    private final String playerName, blockID;
    private final Position blockPosition;

    public PlayerInteractContainerEvent(final String playerName, final String blockID, final Position blockPosition) {
        this.playerName = playerName;
        this.blockID = blockID;
        this.blockPosition = blockPosition;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public String getBlockID() {
        return this.blockID;
    }

    public Position getBlockPosition() {
        return this.blockPosition;
    }
}