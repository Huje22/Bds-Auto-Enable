package me.indian.bds.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.server.stats.StatsManager;

public final class PlayerStatsUtil {

    private static StatsManager STATSMANAGER;

    private PlayerStatsUtil() {
    }

    public static void init(final BDSAutoEnable bdsAutoEnable) {
        PlayerStatsUtil.STATSMANAGER = bdsAutoEnable.getServerManager().getStatsManager();
    }

    public static List<String> getTopPlayTime(final boolean markdown, final int top) {
        final Map<String, Long> playTimeMap = STATSMANAGER.getPlayTime();
        final List<String> topPlayTime = new ArrayList<>();

        final List<Map.Entry<String, Long>> sortedEntries = playTimeMap.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(top)
                .toList();

        int place = 1;
        for (final Map.Entry<String, Long> entry : sortedEntries) {
            topPlayTime.add(place + ". **" + entry.getKey() + "**: `" + DateUtil.formatTimeDynamic(entry.getValue()) + "`");
            place++;
        }

        if (!markdown) topPlayTime.replaceAll(s -> s.replaceAll("`", "").replaceAll("\\*", "").replaceAll(">", ""));

        return topPlayTime;
    }

    public static List<String> getTopDeaths(final boolean markdown, final int top) {
        final Map<String, Long> deathsMap = STATSMANAGER.getDeaths();
        final List<String> topDeaths = new ArrayList<>();

        final List<Map.Entry<String, Long>> sortedEntries = deathsMap.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(top)
                .toList();

        int place = 1;
        for (final Map.Entry<String, Long> entry : sortedEntries) {
            topDeaths.add(place + ". **" + entry.getKey() + "**: `" + entry.getValue() + "`");
            place++;
        }

        if (!markdown) topDeaths.replaceAll(s -> s.replaceAll("`", "").replaceAll("\\*", "").replaceAll(">", ""));

        return topDeaths;
    }

    public static List<String> getTopBlockBroken(final boolean markdown, final int top) {
        final Map<String, Long> brokenMap = STATSMANAGER.getBlockBroken();
        final List<String> topBroken = new ArrayList<>();

        final List<Map.Entry<String, Long>> sortedEntries = brokenMap.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(top)
                .toList();

        int place = 1;
        for (final Map.Entry<String, Long> entry : sortedEntries) {
            topBroken.add(place + ". **" + entry.getKey() + "**: `" + entry.getValue() + "`");
            place++;
        }

        if (!markdown) topBroken.replaceAll(s -> s.replaceAll("`", "").replaceAll("\\*", "").replaceAll(">", ""));

        return topBroken;
    }

    public static List<String> getTopBlockPlaced(final boolean markdown, final int top) {
        final Map<String, Long> placedMap = STATSMANAGER.getBlockPlaced();
        final List<String> topPlaced = new ArrayList<>();

        final List<Map.Entry<String, Long>> sortedEntries = placedMap.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(top)
                .toList();

        int place = 1;
        for (final Map.Entry<String, Long> entry : sortedEntries) {
            topPlaced.add(place + ". **" + entry.getKey() + "**: `" + entry.getValue() + "`");
            place++;
        }

        if (!markdown) topPlaced.replaceAll(s -> s.replaceAll("`", "").replaceAll("\\*", "").replaceAll(">", ""));

        return topPlaced;
    }
}