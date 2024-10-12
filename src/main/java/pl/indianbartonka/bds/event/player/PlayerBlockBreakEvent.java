package pl.indianbartonka.bds.event.player;

import pl.indianbartonka.bds.event.Event;
import pl.indianbartonka.bds.player.PlayerStatistics;
import pl.indianbartonka.bds.player.position.Position;

public class PlayerBlockBreakEvent extends Event {

    private final PlayerStatistics player;
    private final String blockID;
    private final Position blockPosition;

    public PlayerBlockBreakEvent(final PlayerStatistics player, final String blockID, final Position blockPosition) {
        this.player = player;
        this.blockID = blockID;
        this.blockPosition = blockPosition;
    }

    public PlayerStatistics getPlayer() {
        return this.player;
    }

    public String getBlockID() {
        return this.blockID;
    }

    public Position getBlockPosition() {
        return this.blockPosition;
    }
}