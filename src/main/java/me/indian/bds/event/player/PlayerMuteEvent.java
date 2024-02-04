package me.indian.bds.event.player;

import me.indian.bds.event.Event;

public class PlayerMuteEvent extends Event {
    private final String playerName;

    public PlayerMuteEvent(final String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return this.playerName;
    }
}
