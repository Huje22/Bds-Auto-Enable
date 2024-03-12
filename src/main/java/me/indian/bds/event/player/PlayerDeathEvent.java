package me.indian.bds.event.player;

import me.indian.bds.event.Event;
import me.indian.bds.event.Position;

public class PlayerDeathEvent extends Event {

    private final String playerName, deathMessage, killerName, usedItemName;
    private final Position deathPosition;

    public PlayerDeathEvent(final String playerName, final String deathMessage, final Position deathPosition, final String killerName, final String usedItemName) {
        this.playerName = playerName;
        this.deathMessage = deathMessage;
        this.deathPosition = deathPosition;
        this.killerName = killerName;
        this.usedItemName = usedItemName;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public String getDeathMessage() {
        return this.deathMessage;
    }

    public String getKillerName() {
        return this.killerName;
    }

    public String getUsedItemName() {
        return this.usedItemName;
    }

    public Position getDeathPosition() {
        return this.deathPosition;
    }
}