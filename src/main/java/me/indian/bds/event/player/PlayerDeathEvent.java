package me.indian.bds.event.player;

import me.indian.bds.event.Event;

public class PlayerDeathEvent extends Event {

    private final String playerName, deathMessage;

    public PlayerDeathEvent(final  String playerName, final String deathMessage){
        this.playerName = playerName;
        this.deathMessage = deathMessage;
    }
    
    public String getPlayerName() {
        return this.playerName;
    }

    public String getDeathMessage() {
        return this.deathMessage;
    }
}