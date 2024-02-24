package me.indian.bds.event.player;

import me.indian.bds.event.Event;

public class PlayerDeathEvent extends Event {

    private final String playerName, deathMessage, killerName, usedItemName;

    public PlayerDeathEvent(final String playerName, final String deathMessage, final String killerName, final String usedItemName) {
        this.playerName = playerName;
        this.deathMessage = deathMessage;
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
}