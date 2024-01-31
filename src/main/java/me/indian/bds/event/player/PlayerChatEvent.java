package me.indian.bds.event.player;

import me.indian.bds.event.Event;

public class PlayerChatEvent extends Event {

    private final String playerName, message;
    private final boolean appHandled, muted;

    public PlayerChatEvent(final String playerName, final String message,final boolean muted, final boolean appHandled) {
        this.playerName = playerName;
        this.message = message;
        this.appHandled = appHandled;
        this.muted = muted;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public String getMessage() {
        return this.message;
    }

    public boolean isAppHandled() {
        return this.appHandled;
    }

    public boolean isMuted() {
        return this.muted;
    }
}