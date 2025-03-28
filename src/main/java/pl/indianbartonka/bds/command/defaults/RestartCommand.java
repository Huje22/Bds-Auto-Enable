package pl.indianbartonka.bds.command.defaults;

import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.command.Command;

public class RestartCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;

    public RestartCommand(final BDSAutoEnable bdsAutoEnable) {
        super("restart", "restartuje server");
        this.bdsAutoEnable = bdsAutoEnable;

        this.addOption("[seconds]");
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (!isOp) {
            this.sendMessage("&cPotrzebujesz wyższych uprawnień");
            this.deniedSound();
            return true;
        }

        int seconds = 10;

        if (args.length == 1) {
            try {
                seconds = Integer.parseInt(args[0]);
            } catch (final NumberFormatException ignored) {
            }
        }

        if (!this.bdsAutoEnable.getWatchDog().getAutoRestartModule().restart(true, seconds, "Użycie komendy")) {
            this.sendMessage("&cServer jest już w trakcje restartu!");
        }
        return true;
    }
}
