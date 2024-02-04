package me.indian.bds.event.player;

import me.indian.bds.event.Event;

public class PlayerUnMuteEvent extends Event {
    private final String playerName;

    public PlayerUnMuteEvent(final String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return this.playerName;
    }
}
