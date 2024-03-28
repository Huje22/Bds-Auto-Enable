package me.indian.bds.command.defaults;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.event.Position;

public class RestartCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;

    public RestartCommand(final BDSAutoEnable bdsAutoEnable) {
        super("restart", "restartuje server");
        this.bdsAutoEnable = bdsAutoEnable;

        this.addOption("[seconds]");
    }

    @Override
    public boolean onExecute(final String[] args, Position position, final boolean isOp) {
        if (!isOp) {
            this.sendMessage("&cPotrzebujesz wyższych uprawnień");
            return true;
        }

        int seconds = 10;

        if (args.length == 1) {
            try {
                seconds = Integer.parseInt(args[0]);
            } catch (final NumberFormatException ignored) {
            }
            if (!this.bdsAutoEnable.getWatchDog().getAutoRestartModule().restart(true, seconds, "Użycie komendy")) {
                this.sendMessage("&cServer jest już w trakcje restartu!");
            }
        } else {
            if (!this.bdsAutoEnable.getWatchDog().getAutoRestartModule().restart(true, seconds, "Użycie komendy")) {
                this.sendMessage("&cServer jest już w trakcje restartu!");
            }
        }
        return true;
    }
}
