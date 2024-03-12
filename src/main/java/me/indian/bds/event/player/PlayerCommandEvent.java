package me.indian.bds.event.player;

import me.indian.bds.event.Event;
import me.indian.bds.event.Position;

public class PlayerCommandEvent extends Event {

    private final String playerName;
    private final String command;
    private final Position position;
    private final boolean isOp;

    public PlayerCommandEvent(final String playerName, final String command, final Position position, final boolean isOp) {
        this.playerName = playerName;
        this.command = command;
        this.position = position;
        this.isOp = isOp;
    }

    public String getPlayerName() {
        return this.playerName;
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