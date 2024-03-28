package me.indian.bds.event.player;

import me.indian.bds.util.Dimension;
import me.indian.bds.event.Event;
import me.indian.bds.event.Position;

public class PlayerDimensionChangeEvent extends Event {

    private final String playerName;
    private final Dimension dimensionFrom, dimensionTo;
    private final Position fromPosition, toPosition;

    public PlayerDimensionChangeEvent(final String playerName, final Dimension dimensionFrom, final Dimension dimensionTo, final Position fromPosition, final Position toPosition) {
        this.playerName = playerName;
        this.dimensionFrom = dimensionFrom;
        this.dimensionTo = dimensionTo;
        this.fromPosition = fromPosition;
        this.toPosition = toPosition;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public Dimension getDimensionFrom() {
        return this.dimensionFrom;
    }

    public Dimension getDimensionTo() {
        return this.dimensionTo;
    }

    public Position getFromPosition() {
        return this.fromPosition;
    }

    public Position getToPosition() {
        return this.toPosition;
    }
}