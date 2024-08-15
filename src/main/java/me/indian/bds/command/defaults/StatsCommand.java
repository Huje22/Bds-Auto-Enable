package me.indian.bds.command.defaults;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.indian.bds.command.Command;
import me.indian.bds.util.DateUtil;
import me.indian.bds.util.StatusUtil;

public class StatsCommand extends Command {

    private final Map<String, Long> cooldown;

    public StatsCommand() {
        super("stats", "Aktualne statystyki servera minecraft i maszyny");
        this.cooldown = new HashMap<>();

        this.addAlliases(List.of("status"));
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (this.player != null) {
            final String playerName = this.player.getPlayerName();
            final long cooldownTime = DateUtil.secondToMillis(90);

            if (!this.cooldown.containsKey(playerName) || System.currentTimeMillis() - this.cooldown.get(playerName) > cooldownTime) {
                this.cooldown.put(playerName, System.currentTimeMillis());
            } else {
                final long playerCooldown = this.cooldown.getOrDefault(playerName, 0L);
                final long remainingTime = (playerCooldown + cooldownTime) - System.currentTimeMillis();

                this.sendMessage("&cMusisz odczekać:&b " + DateUtil.formatTimeDynamic(remainingTime));
                return true;
            }
        }

        StatusUtil.getMainStats(false).forEach(this::sendMessage);
        return true;
    }
}