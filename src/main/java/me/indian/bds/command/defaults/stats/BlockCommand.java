package me.indian.bds.command.defaults.stats;

import java.util.ArrayList;
import java.util.Collections;
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
        super("block", "Top 10 graczy z największym wykopaniem i postawieniem bloków");
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

        final List<String> topBlocks = new ArrayList<>();

        final List<Map.Entry<String, Long>> sortedBrokenEntries = brokenMap.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(top)
                .toList();

        final List<Map.Entry<String, Long>> sortedPlacedEntries = placedMap.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(top)
                .toList();


        topBlocks.add(String.format("%-20s  %-12s  %-12s", "NICK", "WYKOPANE", "POSTAWIONE"));

        for (int i = 0; i < top && i < sortedBrokenEntries.size() && i < sortedPlacedEntries.size(); i++) {
            final Map.Entry<String, Long> brokenEntry = sortedBrokenEntries.get(i);
            final Map.Entry<String, Long> placedEntry = sortedPlacedEntries.get(i);

            topBlocks.add(String.format("%-20s  %-12s  %-12s",
                    brokenEntry.getKey(), brokenEntry.getValue(), placedEntry.getValue()));

        }

        if (!forDiscord) {
            topBlocks.replaceAll(s -> s.replaceAll("`", "").replaceAll("\\*", "").replaceAll(">", ""));
        }

        return topBlocks;
    }
}