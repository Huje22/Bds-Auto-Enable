package pl.indianbartonka.bds.event.player;

import pl.indianbartonka.bds.event.Event;
import pl.indianbartonka.bds.player.PlayerStatistics;
import pl.indianbartonka.bds.player.position.Dimension;
import pl.indianbartonka.bds.player.position.Position;

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