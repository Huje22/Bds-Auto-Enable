package me.indian.bds.command.defaults.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.server.manager.stats.StatsManager;
import me.indian.bds.watchdog.module.PackModule;

public class BlockCommand extends Command {

    private final StatsManager statsManager;
    private final PackModule packModule;

    public BlockCommand(final BDSAutoEnable bdsAutoEnable) {
        super("blocks", "Top 10 graczy z największym wykopaniem i postawieniem bloków");
        this.statsManager = bdsAutoEnable.getServerManager().getStatsManager();
        this.packModule = bdsAutoEnable.getWatchDog().getPackModule();
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (!this.packModule.isLoaded()) {
            this.sendMessage("&cPaczka &b" + this.packModule.getPackName() + "&c nie jest załadowana!");
            return true;
        }

        this.sendMessage("&a---------------------");
        this.getTopBlock(false, 10).forEach(this::sendMessage);
        this.sendMessage("&a---------------------");
        return true;
    }


    public List<String> getTopBlock(final boolean forDiscord, final int top) {
        final Map<String, Long> brokenMap = this.statsManager.getBlockBroken();
        final Map<String, Long> placedMap = this.statsManager.getBlockPlaced();
        final Map<String, Long> combinedMap = new HashMap<>(brokenMap);

        final List<String> topBlocks = new ArrayList<>();

        placedMap.forEach((key, value) ->
                combinedMap.merge(key, value, Long::sum)
        );

        final List<Map.Entry<String, Long>> sortedCombinedEntries = combinedMap.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(25)
                .toList();

        topBlocks.add(String.format("%-20s  %-12s  %-12s", "NICK", "WYKOPANE", "POSTAWIONE"));

        int place = 1;
        for (final Map.Entry<String, Long> entry : sortedCombinedEntries) {
            topBlocks.add(place + ". " + String.format("%-20s  %-12s  %-12s",
                    entry.getKey(), brokenMap.getOrDefault(entry.getKey(), 0L), placedMap.getOrDefault(entry.getKey(), 0L)));
            place++;

        }
        if (!forDiscord) {
            topBlocks.replaceAll(s -> s.replaceAll("`", "").replaceAll("\\*", "").replaceAll(">", ""));
        }

        return topBlocks;
    }
}