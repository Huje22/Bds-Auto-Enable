package pl.indianbartonka.bds.command.defaults.info;

import java.util.List;
import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.command.Command;
import pl.indianbartonka.bds.player.PlayerStatistics;
import pl.indianbartonka.bds.server.ServerManager;
import pl.indianbartonka.bds.server.stats.StatsManager;
import pl.indianbartonka.util.DateUtil;

public class PlayerListCommand extends Command {

    private final ServerManager serverManager;

    public PlayerListCommand(final BDSAutoEnable bdsAutoEnable) {
        super("list", "Zaawansowana lista graczy");
        this.serverManager = bdsAutoEnable.getServerManager();
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        final StatsManager statsManager = this.serverManager.getStatsManager();

        final List<PlayerStatistics> playerList = this.serverManager.getOnlinePlayers().stream().map(statsManager::getPlayer).toList();

        if (playerList.isEmpty()){
            this.sendMessage("&cNikogo nie ma na serwerze");
            return true;
        }

        for (final PlayerStatistics playerStatistics : playerList){
            if (playerStatistics != null){
                this.sendMessage("&c -- &b" + playerStatistics.getPlayerName());
                this.sendMessage("&c - &a" + playerStatistics.getPlatformType() + "&4 |&e " + playerStatistics.getLastKnownInputMode());
                this.sendMessage("&c - &aCzas gry:&b " + DateUtil.formatTimeDynamic(playerStatistics.getPlaytime()));
                this.sendMessage("&c - &aŚmierci:&b " + playerStatistics.getDeaths());
                this.sendMessage("");
            }
        }

        return true;
    }
}
