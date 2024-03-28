package me.indian.bds.command.defaults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.event.Position;
import me.indian.bds.server.stats.ServerStats;
import me.indian.bds.server.stats.StatsManager;
import me.indian.bds.util.DateUtil;
import me.indian.bds.util.StatusUtil;
import me.indian.bds.watchdog.module.pack.PackModule;

public class TopCommand extends Command {

    private final StatsManager statsManager;
    private final ServerStats serverStats;
    private final PackModule packModule;

    public TopCommand(final BDSAutoEnable bdsAutoEnable) {
        super("top", "Topka graczy w różnych kategoriach");
        this.statsManager = bdsAutoEnable.getServerManager().getStatsManager();
        this.serverStats = this.statsManager.getServerStats();
        this.packModule = bdsAutoEnable.getWatchDog().getPackModule();

        this.addOption("playtime", "Top 10 graczy z największą ilością przegranego czasu");
        this.addOption("deaths", "Top 10 graczy z największą ilością śmierci");
        this.addOption("block", "Top 10 wykopanych i postawionych bloków");
    }


    @Override
    public boolean onExecute(final String[] args, Position position, final boolean isOp) {
        if (!this.packModule.isLoaded()) {
            this.sendMessage("&cPaczka &b" + this.packModule.getPackName() + "&c nie jest załadowana!");
            return true;
        }

        if (args.length == 0) {
            this.buildHelp();
            return true;
        }

        if (args[0].equalsIgnoreCase("playtime")) {
            this.sendMessage("&a---------------------");
            StatusUtil.getTopPlayTime(false, 10).forEach(this::sendMessage);
            this.sendMessage("&aŁączny czas działania servera: &b"
                    + DateUtil.formatTime(this.serverStats.getTotalUpTime(), List.of('d', 'h', 'm', 's')));
            this.sendMessage("&a---------------------");

            return true;
        }

        if (args[0].equalsIgnoreCase("deaths")) {
            this.sendMessage("&a---------------------");
            StatusUtil.getTopDeaths(false, 10).forEach(this::sendMessage);
            this.sendMessage("&a---------------------");
            return true;
        }


        if (args[0].equalsIgnoreCase("block")) {
            this.sendMessage("&a---------------------");
            this.getTopBlock(false).forEach(this::sendMessage);
            this.sendMessage("&a---------------------");
            return true;
        }

        return false;
    }


    public List<String> getTopBlock(final boolean forDiscord) {
        final Map<String, Long> brokenMap = this.statsManager.getBlockBroken();
        final Map<String, Long> placedMap = this.statsManager.getBlockPlaced();
        final Map<String, Long> combinedMap = new HashMap<>(brokenMap);

        final List<String> topBlocks = new ArrayList<>();

        placedMap.forEach((key, value) ->
                combinedMap.merge(key, value, Long::sum)
        );

        final List<Map.Entry<String, Long>> sortedCombinedEntries = combinedMap.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(10)
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