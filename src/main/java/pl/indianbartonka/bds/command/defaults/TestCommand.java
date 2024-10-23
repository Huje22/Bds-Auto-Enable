package pl.indianbartonka.bds.command.defaults;

import pl.indianbartonka.bds.command.Command;

public class TestCommand extends Command {

    public TestCommand() {
        super("test", " tescik");
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (!isOp) return false;

        this.sendMessage(String.valueOf(this.player.getPlatformType()));
        this.sendMessage(String.valueOf(this.player.getMemoryTier()));
        this.sendMessage(String.valueOf(this.player.getMaxRenderDistance()));
        this.sendMessage(String.valueOf(this.player));
        return false;
    }
}
