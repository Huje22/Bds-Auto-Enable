package pl.indianbartonka.bds.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.extension.Extension;
import pl.indianbartonka.bds.extension.ExtensionManager;
import pl.indianbartonka.bds.player.PlayerStatistics;
import pl.indianbartonka.bds.server.ServerManager;
import pl.indianbartonka.bds.util.DefaultsVariables;
import pl.indianbartonka.util.MessageUtil;
import pl.indianbartonka.util.system.SystemOS;
import pl.indianbartonka.util.system.SystemUtil;

public class AppMetrics {

    private final BDSAutoEnable bdsAutoEnable;
    private final ExtensionManager extensionManager;
    private final ServerManager serverManager;
    private final IMetrics metrics;

    public AppMetrics(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.extensionManager = bdsAutoEnable.getExtensionManager();
        this.serverManager = bdsAutoEnable.getServerManager();
        this.metrics = new IMetrics(bdsAutoEnable);
        this.init();
    }

    //Z jakiegoś powodu to nie działa, nie chce mi się nad tym siedzieć

    private void init() {
        final SystemOS systemOS = SystemUtil.getSystem();

        for (final Map.Entry<String, Extension> entry : this.extensionManager.getExtensions().entrySet()) {
            this.sendExtensionData(entry.getValue().getName());
        }

        this.metrics.addCustomChart(new IMetrics.SimplePie("runtime_environment", () -> {
            if (systemOS == SystemOS.WINDOWS) return "Windows";

            final List<String> runtimeLayers = new ArrayList<>();

            final boolean wine = this.bdsAutoEnable.getAppConfigManager().getAppConfig().isWine();
            final boolean box64 = DefaultsVariables.box64;
            final boolean box86 = DefaultsVariables.box86;

            if (box64) runtimeLayers.add("box64");
            if (box86) runtimeLayers.add("box86");
            if (wine) runtimeLayers.add("wine");

            return MessageUtil.stringListToString(runtimeLayers, " + ");
        }));

        this.metrics.addCustomChart(new IMetrics.AdvancedPie("player_platform", () -> {
            final Map<String, Integer> valueMap = new HashMap<>();

            for (final String player : this.serverManager.getOnlinePlayers()) {
                final PlayerStatistics playerStatistics = this.serverManager.getStatsManager().getPlayer(player);
                if (playerStatistics != null) {
                    final String platform = playerStatistics.getPlatformType().getPlatformName();
                    if (!valueMap.containsKey(platform)) {
                        valueMap.put(platform, 1);
                    } else {
                        valueMap.put(platform, valueMap.get(platform) + 1);
                    }
                }
            }
            return valueMap;
        }));

        this.metrics.addCustomChart(new IMetrics.AdvancedPie("player_controller", () -> {
            final Map<String, Integer> valueMap = new HashMap<>();

            for (final String player : this.serverManager.getOnlinePlayers()) {
                final PlayerStatistics playerStatistics = this.serverManager.getStatsManager().getPlayer(player);
                if (playerStatistics != null) {
                    final String controller = playerStatistics.getLastKnownInputMode().getMode();
                    if (!valueMap.containsKey(controller)) {
                        valueMap.put(controller, 1);
                    } else {
                        valueMap.put(controller, valueMap.get(controller) + 1);
                    }
                }
            }
            return valueMap;
        }));

        this.metrics.addCustomChart(new IMetrics.AdvancedPie("player_memorytier", () -> {
            final Map<String, Integer> valueMap = new HashMap<>();

            for (final String player : this.serverManager.getOnlinePlayers()) {
                final PlayerStatistics playerStatistics = this.serverManager.getStatsManager().getPlayer(player);
                if (playerStatistics != null) {
                    final String memoryTier = playerStatistics.getMemoryTier().name();
                    if (!valueMap.containsKey(memoryTier)) {
                        valueMap.put(memoryTier, 1);
                    } else {
                        valueMap.put(memoryTier, valueMap.get(memoryTier) + 1);
                    }
                }
            }
            return valueMap;
        }));
    }

    private void sendExtensionData(final String extensionName) {
        this.metrics.addCustomChart(new IMetrics.DrilldownPie("extensions", () -> {
            final Map<String, Map<String, Integer>> valmap = new HashMap<>();
            final Map<String, Integer> assets = new HashMap<>();
            final Extension extension = this.extensionManager.getExtension(extensionName);
            if (extension != null) {
                final String version = extension.getVersion();
                assets.put(version, 1);
                valmap.put(extensionName, assets);
            }
            return valmap;
        }));
    }
}