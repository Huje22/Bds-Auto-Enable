package me.indian.bds.command.defaults;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.event.Position;

public class TPSCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;

    public TPSCommand(final BDSAutoEnable bdsAutoEnable) {
        super("tps", "Pokazuje TPS servera");
        this.bdsAutoEnable = bdsAutoEnable;
    }

    @Override
    public boolean onExecute(final String[] args, Position position, final boolean isOp) {
        if (!this.bdsAutoEnable.getServerProcess().isEnabled()) {
            this.sendMessage("&4Proces servera jest wyłączony");
            return false;
        }

        this.bdsAutoEnable.getServerProcess().sendToConsole("scriptevent bds:tps");
        return false;
    }
}