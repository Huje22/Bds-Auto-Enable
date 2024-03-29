package me.indian.bds.event.player;

import me.indian.bds.event.Event;
import me.indian.bds.event.Position;

public class PlayerInteractEntityWithContainerEvent extends Event {

    private final String playerName, entityID;
    private final Position entityPosition;

    public PlayerInteractEntityWithContainerEvent(final String playerName, final String entityID, final Position entityPosition) {
        super();
        this.playerName = playerName;
        this.entityID = entityID;
        this.entityPosition = entityPosition;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public String getEntityID() {
        return this.entityID;
    }

    public Position getEntityPosition() {
        return this.entityPosition;
    }
}