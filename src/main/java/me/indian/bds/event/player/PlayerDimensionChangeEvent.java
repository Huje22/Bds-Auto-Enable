package me.indian.bds.event.player;

import me.indian.bds.event.Event;
import me.indian.bds.event.Position;
import me.indian.bds.player.PlayerStatistics;
import me.indian.bds.util.Dimension;

public class PlayerDimensionChangeEvent extends Event {

    private final PlayerStatistics player;
    private final Dimension dimensionFrom, dimensionTo;
    private final Position fromPosition, toPosition;

    public PlayerDimensionChangeEvent(final PlayerStatistics player, final Dimension dimensionFrom, final Dimension dimensionTo, final Position fromPosition, final Position toPosition) {
        this.player = player;
        this.dimensionFrom = dimensionFrom;
        this.dimensionTo = dimensionTo;
        this.fromPosition = fromPosition;
        this.toPosition = toPosition;
    }

    public PlayerStatistics getPlayer() {
        return this.player;
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