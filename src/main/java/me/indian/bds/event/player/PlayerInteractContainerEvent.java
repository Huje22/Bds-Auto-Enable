package me.indian.bds.event.player;

import me.indian.bds.event.Event;
import me.indian.bds.player.position.Position;
import me.indian.bds.player.PlayerStatistics;

public class PlayerInteractContainerEvent extends Event {

    private final PlayerStatistics player;
    private final String blockID;
    private final Position blockPosition;

    public PlayerInteractContainerEvent(final PlayerStatistics player, final String blockID, final Position blockPosition) {
        this.player = player;
        this.blockID = blockID;
        this.blockPosition = blockPosition;
    }

    public PlayerStatistics getPlayer() {
        return this.player;
    }

    public String getBlockID() {
        return this.blockID;
    }

    public Position getBlockPosition() {
        return this.blockPosition;
    }
}