package me.indian.bds.command.defaults;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.event.player.PlayerMuteEvent;
import me.indian.bds.event.player.PlayerUnMuteEvent;
import me.indian.bds.player.PlayerStatistics;
import me.indian.bds.server.ServerManager;

public class MuteCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;
    private final ServerManager serverManager;

    public MuteCommand(final BDSAutoEnable bdsAutoEnable) {
        super("mute", "Wycisz/odcisz kogoś na czacie i na discord");
        this.bdsAutoEnable = bdsAutoEnable;
        this.serverManager = this.bdsAutoEnable.getServerManager();

        this.addOption("<player>");
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (!isOp) {
            this.sendMessage("&cPotrzebujesz wyższych uprawnień");
            return true;
        }
        if (args.length == 0) return false;

        final boolean appHandling = this.bdsAutoEnable.getWatchDog().getPackModule().isAppHandledMessages();
        final PlayerStatistics playerStatistics = this.serverManager.getStatsManager().getPlayer(args[0]);

        if (playerStatistics == null) {
            this.sendMessage("&aNie odnaleziono gracza o nicku&b " + args[0]);
            return true;
        }

        if (!appHandling) {
            this.sendMessage("&aAplikacja nie utrzymuję wiadomości czatu, gracz nadal będzie mógł pisać na czacie Minecraft");
        }

        if (this.serverManager.isMuted(playerStatistics.getXuid())) {
            this.unMute(playerStatistics);
        } else {
            this.mute(playerStatistics);
        }
        return true;
    }


    private void mute(final PlayerStatistics playerStatistics) {
        this.serverManager.mute(playerStatistics.getXuid());
        this.sendMessage("&aGracz&b " + playerStatistics.getPlayerName() + "&a został dodany do listy wyciszonych");
        this.bdsAutoEnable.getEventManager().callEvent(new PlayerMuteEvent(playerStatistics));
    }

    private void unMute(final PlayerStatistics playerStatistics) {
        this.serverManager.unMute(playerStatistics.getXuid());
        this.sendMessage("&aGracz&b " + playerStatistics.getPlayerName() + "&a został usunięty z listy wyciszonych");
        this.bdsAutoEnable.getEventManager().callEvent(new PlayerUnMuteEvent(playerStatistics));
    }
}