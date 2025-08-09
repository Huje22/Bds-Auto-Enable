package pl.indianbartonka.bds.command.defaults.info;

import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.command.Command;

public class TPSCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;

    public TPSCommand(final BDSAutoEnable bdsAutoEnable) {
        super("tps", "Pokazuje TPS servera");
        this.bdsAutoEnable = bdsAutoEnable;
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (!this.bdsAutoEnable.getServerProcess().isEnabled()) {
            this.sendMessage("&4Proces servera jest wyłączony");
            return false;
        }

        this.bdsAutoEnable.getServerProcess().sendToConsole("scriptevent bds:tps");
        return false;
    }
}