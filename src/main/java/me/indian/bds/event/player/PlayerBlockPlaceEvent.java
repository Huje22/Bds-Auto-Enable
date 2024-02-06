package me.indian.bds.event.player;

import me.indian.bds.event.Event;

public class PlayerBlockPlaceEvent extends Event {

    private final String playerName, blockID, blockPosition;

    public PlayerBlockPlaceEvent(final String playerName, final String blockID, final String blockPosition) {
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

    public String getBlockPosition() {
        return this.blockPosition;
    }
}