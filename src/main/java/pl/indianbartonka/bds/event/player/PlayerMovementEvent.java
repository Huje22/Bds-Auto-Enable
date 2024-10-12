package pl.indianbartonka.bds.event.player;

import pl.indianbartonka.bds.event.Event;
import pl.indianbartonka.bds.player.PlayerStatistics;
import pl.indianbartonka.bds.player.position.Position;

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
