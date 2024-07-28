package me.indian.bds.event.player;

import me.indian.bds.event.ResponsibleEvent;
import me.indian.bds.player.PlayerStatistics;
import me.indian.bds.player.position.Position;

public class PlayerChatEvent extends ResponsibleEvent {

    private final PlayerStatistics player;
    private final String message;
    private final Position playerPosition;
    private final boolean appHandled, muted;

    public PlayerChatEvent(final PlayerStatistics player, final String message, final Position playerPosition, final boolean muted, final boolean appHandled) {
        this.player = player;
        this.message = message;
        this.playerPosition = playerPosition;
        this.appHandled = appHandled;
        this.muted = muted;
    }

    public PlayerStatistics getPlayer() {
        return this.player;
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