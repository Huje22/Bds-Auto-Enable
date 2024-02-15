package me.indian.bds.event.player;

import me.indian.bds.event.Event;

public class PlayerDimensionChangeEvent extends Event {

    private final String playerName, dimensionFrom, dimensionTo;

    public PlayerDimensionChangeEvent(final String playerName, final String dimensionFrom, final String dimensionTo) {
        this.playerName = playerName;
        this.dimensionFrom = dimensionFrom;
        this.dimensionTo = dimensionTo;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public String getDimensionFrom() {
        return this.dimensionFrom;
    }

    public String getDimensionTo() {
        return this.dimensionTo;
    }
}
