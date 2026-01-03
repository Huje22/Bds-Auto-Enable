package pl.indianbartonka.bds.command.defaults;

import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.command.Command;
import pl.indianbartonka.bds.player.PlayerStatistics;

public class TestCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;

    public TestCommand(final BDSAutoEnable bdsAutoEnable) {
        super("test", " tescik");
        this.bdsAutoEnable = bdsAutoEnable;
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (!isOp) return false;

        final PlayerStatistics player = this.getPlayer();

        if (player != null) {
            this.sendMessage(String.valueOf(player.getPlatformType()));
            this.sendMessage(String.valueOf(player.getMemoryTier()));
            this.sendMessage(String.valueOf(player.getMaxRenderDistance()));
            this.sendMessage(String.valueOf(player));
        }

        for (int i = 0; i < 45; i++) {
            this.bdsAutoEnable.getServerProcess().sendToConsole("help " + i);
        }

        return false;
    }
}
