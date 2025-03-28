package pl.indianbartonka.bds.event.player;

import pl.indianbartonka.bds.event.Event;
import pl.indianbartonka.bds.player.PlayerStatistics;
import pl.indianbartonka.bds.player.position.Position;

public class PlayerInteractEntityWithContainerEvent extends Event {

    private final PlayerStatistics player;
    private final String entityID;
    private final Position entityPosition;

    public PlayerInteractEntityWithContainerEvent(final PlayerStatistics player, final String entityID, final Position entityPosition) {
        this.player = player;
        this.entityID = entityID;
        this.entityPosition = entityPosition;
    }

    public PlayerStatistics getPlayer() {
        return this.player;
    }

    public String getEntityID() {
        return this.entityID;
    }

    public Position getEntityPosition() {
        return this.entityPosition;
    }
}