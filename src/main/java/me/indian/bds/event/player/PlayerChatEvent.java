package me.indian.bds.event.player;

import me.indian.bds.event.Position;
import me.indian.bds.event.ResponsibleEvent;

public class PlayerChatEvent extends ResponsibleEvent {

    private final String playerName, message;
    private final Position playerPosition;
    private final boolean appHandled, muted;

    public PlayerChatEvent(final String playerName, final String message, final Position playerPosition, final boolean muted, final boolean appHandled) {
        this.playerName = playerName;
        this.message = message;
        this.playerPosition = playerPosition;
        this.appHandled = appHandled;
        this.muted = muted;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public String getMessage() {
        return this.message;
    }

    public Position getPlayerPosition() {
        return this.playerPosition;
    }

    public boolean isAppHandled() {
        return this.appHandled;
    }

    public boolean isMuted() {
        return this.muted;
    }
}