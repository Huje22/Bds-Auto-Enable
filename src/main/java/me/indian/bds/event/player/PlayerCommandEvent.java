package me.indian.bds.event.player;

import me.indian.bds.event.Event;
import me.indian.bds.event.Position;
import me.indian.bds.player.PlayerStatistics;

public class PlayerCommandEvent extends Event {

    private final PlayerStatistics player;
    private final String command;
    private final Position position;
    private final boolean isOp;

    public PlayerCommandEvent(final PlayerStatistics player, final String command, final Position position, final boolean isOp) {
        this.player = player;
        this.command = command;
        this.position = position;
        this.isOp = isOp;
    }

    public PlayerStatistics getPlayer() {
        return this.player;
    }

    public String getCommand() {
        return this.command;
    }

    public Position getPosition() {
        return this.position;
    }

    public boolean isOp() {
        return this.isOp;
    }
}