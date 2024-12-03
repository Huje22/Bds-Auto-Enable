package pl.indianbartonka.bds.event.player;

import pl.indianbartonka.bds.event.Event;
import pl.indianbartonka.bds.player.InputMode;
import pl.indianbartonka.bds.player.PlayerStatistics;

public class PlayerChangeInputModeEvent extends Event {

    private final PlayerStatistics playerStatistics;
    private final InputMode newInput;
    private final InputMode oldInput;

    public PlayerChangeInputModeEvent(final PlayerStatistics playerStatistics, final InputMode newInput, final InputMode oldInput) {
        this.playerStatistics = playerStatistics;
        this.newInput = newInput;
        this.oldInput = oldInput;
    }

    public PlayerStatistics getPlayerStatistics() {
        return this.playerStatistics;
    }

    public InputMode getNewInput() {
        return this.newInput;
    }

    public InputMode getOldInput() {
        return this.oldInput;
    }
}
