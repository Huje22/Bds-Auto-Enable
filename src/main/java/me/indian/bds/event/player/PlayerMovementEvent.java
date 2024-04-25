package me.indian.bds.event.player;

import me.indian.bds.event.Event;
import me.indian.bds.player.position.Position;
import me.indian.bds.player.PlayerStatistics;

public class PlayerMovementEvent extends Event {

    private final PlayerStatistics player;
    private final Position position;

    public PlayerMovementEvent(final PlayerStatistics player, final Position position) {
        this.player = player;
        this.position = position;
    }

    public PlayerStatistics getPlayer() {
        return this.player;
    }

    public Position getPosition() {
        return this.position;
    }
}
