package me.indian.bds.event.player;

import me.indian.bds.event.Event;

public class PlayerJoinEvent extends Event {

    private final String playerName;

    public PlayerJoinEvent(final String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return this.playerName;
    }
}