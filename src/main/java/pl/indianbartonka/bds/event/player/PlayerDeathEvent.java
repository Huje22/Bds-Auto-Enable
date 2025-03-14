package pl.indianbartonka.bds.event.player;

import pl.indianbartonka.bds.event.Event;
import pl.indianbartonka.bds.player.PlayerStatistics;
import pl.indianbartonka.bds.player.position.Position;

public class PlayerDeathEvent extends Event {

    private final PlayerStatistics player;
    private final String deathMessage, killerName, usedItemName;
    private final Position deathPosition;

    public PlayerDeathEvent(final PlayerStatistics player, final String deathMessage, final Position deathPosition, final String killerName, final String usedItemName) {
        this.player = player;
        this.deathMessage = deathMessage;
        this.deathPosition = deathPosition;
        this.killerName = killerName;
        this.usedItemName = usedItemName;
    }

    public PlayerStatistics getPlayer() {
        return this.player;
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