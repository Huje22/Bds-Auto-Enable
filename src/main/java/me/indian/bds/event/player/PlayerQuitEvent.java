package me.indian.bds.event.player;

import me.indian.bds.event.Event;
import me.indian.bds.player.PlayerStatistics;

public class PlayerQuitEvent extends Event {

    private final PlayerStatistics player;

    public PlayerQuitEvent(final PlayerStatistics player) {
        this.player = player;
    }

    public PlayerStatistics getPlayer() {
        return this.player;
    }
}