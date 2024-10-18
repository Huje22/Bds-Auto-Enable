package pl.indianbartonka.bds.event.player;

import pl.indianbartonka.bds.event.Event;
import pl.indianbartonka.bds.player.PlayerStatistics;

public class PlayerJoinEvent extends Event {

    private final PlayerStatistics player;

    public PlayerJoinEvent(final PlayerStatistics player) {
        this.player = player;
    }

    public PlayerStatistics getPlayer() {
        return this.player;
    }
}