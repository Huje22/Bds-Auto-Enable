package me.indian.bds.command.defaults;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;

public class RestartCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;

    public RestartCommand(final BDSAutoEnable bdsAutoEnable) {
        super("restart", "restartuje server");
        this.bdsAutoEnable= bdsAutoEnable;
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (!isOp) {
            this.sendMessage("&cPotrzebujesz wyższych uprawnień");
            return true;
        }

        int seconds = 10;

        if(args.length == 1){
            try {
                seconds = Integer.parseInt(args[0]);
            } catch (final NumberFormatException ignored) {
            }
            this.bdsAutoEnable.getWatchDog().getAutoRestartModule().restart(true, seconds);

        } else {
            this.bdsAutoEnable.getWatchDog().getAutoRestartModule().restart(true, seconds);
        }
        return true;
    }
}
