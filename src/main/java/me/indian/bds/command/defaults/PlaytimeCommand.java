package me.indian.bds.command.defaults;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.command.CommandSender;
import me.indian.bds.server.ServerStats;
import me.indian.bds.util.DateUtil;
import me.indian.bds.util.StatusUtil;

public class PlaytimeCommand extends Command {

    private final ServerStats serverStats;

    public PlaytimeCommand(final BDSAutoEnable bdsAutoEnable) {
        super("playtime", "Top 10 graczy z największym czasem gry");
        this.serverStats = bdsAutoEnable.getServerManager().getStatsManager().getServerStats();
    }

    @Override
    public boolean onExecute(final CommandSender sender, final String[] args, final boolean isOp) {
        this.sendMessage("&a---------------------");
        for (final String s : StatusUtil.getTopPlayTime(false, 10)) {
            this.sendMessage(s);
        }

        this.sendMessage("&aŁączny czas działania servera: &b"
                + DateUtil.formatTime(this.serverStats.getTotalUpTime(), "days hours minutes seconds "));
        this.sendMessage("&a---------------------");
        return true;
    }
}