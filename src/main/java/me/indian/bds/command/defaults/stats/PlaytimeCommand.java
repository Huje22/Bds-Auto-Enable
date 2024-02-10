package me.indian.bds.command.defaults.stats;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.server.ServerStats;
import me.indian.bds.util.DateUtil;
import me.indian.bds.util.StatusUtil;
import me.indian.bds.watchdog.module.PackModule;

import java.util.List;

public class PlaytimeCommand extends Command {

    private final ServerStats serverStats;
    private final PackModule packModule;

    public PlaytimeCommand(final BDSAutoEnable bdsAutoEnable) {
        super("playtime", "Top 10 graczy z największym czasem gry");
        this.serverStats = bdsAutoEnable.getServerManager().getStatsManager().getServerStats();
        this.packModule = bdsAutoEnable.getWatchDog().getPackModule();
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (!this.packModule.isLoaded()) {
            this.sendMessage("&cPaczka &b" + this.packModule.getPackName() + "&c nie jest załadowana!");
            return true;
        }

        this.sendMessage("&a---------------------");
        StatusUtil.getTopPlayTime(false, 10).forEach(this::sendMessage);
        this.sendMessage("&aŁączny czas działania servera: &b"
                + DateUtil.formatTime(this.serverStats.getTotalUpTime(), List.of('d', 'h', 'm', 's')));
        this.sendMessage("&a---------------------");
        return true;
    }
}
