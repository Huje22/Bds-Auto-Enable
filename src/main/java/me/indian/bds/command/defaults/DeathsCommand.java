package me.indian.bds.command.defaults;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.StatusUtil;

public class DeathsCommand extends Command {

    private final ServerProcess serverProcess;

    public DeathsCommand(final BDSAutoEnable bdsAutoEnable) {
        super("deaths", "Top 10 graczy z największą ilością śmierci");
        this.serverProcess = bdsAutoEnable.getServerProcess();
    }

    @Override
    public boolean onExecute(final String player, final String[] args, final boolean isOp) {
        this.serverProcess.tellrawToPlayer(player, "&a---------------------");
        for (final String s : StatusUtil.getTopDeaths(false, 10)) {
            this.serverProcess.tellrawToPlayer(player, s);
        }
        this.serverProcess.tellrawToPlayer(player, "&a---------------------");
        return true;
    }
}
