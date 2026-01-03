package pl.indianbartonka.bds.command.defaults.info;

import java.util.List;
import java.util.concurrent.TimeUnit;
import pl.indianbartonka.bds.command.Command;
import pl.indianbartonka.bds.player.PlayerStatistics;
import pl.indianbartonka.bds.util.StatusUtil;
import pl.indianbartonka.util.Cooldown;
import pl.indianbartonka.util.DateUtil;

public class StatsCommand extends Command {

    private final Cooldown cooldown;

    public StatsCommand() {
        super("stats", "Aktualne statystyki servera minecraft i maszyny");
        this.cooldown = new Cooldown("stats");

        this.addAlliases(List.of("status"));
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        final PlayerStatistics player = this.getPlayer();

        if (player != null) {
            final String playerName = player.getPlayerName();

            if (this.cooldown.hasCooldown(playerName)) {
                this.sendMessage("&cMusisz odczekać:&b " + DateUtil.formatTimeDynamic(this.cooldown.getRemainingTime(playerName)));
                return true;
            }

            this.cooldown.cooldown(playerName, 30, TimeUnit.SECONDS);
        }

        StatusUtil.getMainStats(false).forEach(this::sendMessage);
        return true;
    }
}