package me.indian.bds.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.AppConfigManager;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.server.stats.StatsManager;

public final class PlayerStatsUtil {

    private static final List<String> STATUS = new ArrayList<>();
    private static final File FILE = new File(File.separator);
    private static BDSAutoEnable BDSAUTOENABLE;
    private static Logger LOGGER;
    private static ServerProcess SERVERPROCESS;
    private static StatsManager STATSMANAGER;
    private static AppConfigManager APPCONFIGMANAGER;

    private PlayerStatsUtil() {
    }

    public static void init(final BDSAutoEnable bdsAutoEnable) {
        PlayerStatsUtil.BDSAUTOENABLE = bdsAutoEnable;
        PlayerStatsUtil.LOGGER = bdsAutoEnable.getLogger();
        PlayerStatsUtil.SERVERPROCESS = bdsAutoEnable.getServerProcess();
        PlayerStatsUtil.STATSMANAGER = bdsAutoEnable.getServerManager().getStatsManager();
        PlayerStatsUtil.APPCONFIGMANAGER = bdsAutoEnable.getAppConfigManager();
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
            topPlayTime.add(place + ". **" + entry.getKey() + "**: `" + DateUtil.formatTime(entry.getValue(), List.of('d', 'h', 'm', 's')) + "`");
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