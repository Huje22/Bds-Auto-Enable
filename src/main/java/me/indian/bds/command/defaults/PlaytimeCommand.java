package me.indian.bds.command.defaults;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.server.ServerStats;
import me.indian.bds.util.DateUtil;
import me.indian.bds.util.StatusUtil;

public class PlaytimeCommand extends Command {

    private final ServerStats serverStats;
    private final ServerProcess serverProcess;

    public PlaytimeCommand(final BDSAutoEnable bdsAutoEnable) {
        super("playtime", "Top 10 graczy z największym czasem gry");
        this.serverStats = bdsAutoEnable.getServerManager().getStatsManager().getServerStats();
        this.serverProcess = bdsAutoEnable.getServerProcess();
    }

    @Override
    public boolean onExecute(final String player, final String[] args) {
        this.serverProcess.tellrawToPlayer(player, "&a---------------------");
        for (final String s : StatusUtil.getTopPlayTime(false, 10)) {
            this.serverProcess.tellrawToPlayer(player, s);
        }

        this.serverProcess.tellrawToPlayer(player, "&aŁączny czas działania servera: &b"
                + DateUtil.formatTime(this.serverStats.getTotalUpTime(), "days hours minutes seconds "));
        this.serverProcess.tellrawToPlayer(player, "&a---------------------");
        return false;
    }
}