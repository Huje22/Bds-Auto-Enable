package me.indian.bds.metrics;

import java.util.HashMap;
import java.util.Map;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.extension.Extension;
import me.indian.bds.extension.ExtensionManager;

public class AppMetrics {

    private final BDSAutoEnable bdsAutoEnable;
    private final ExtensionManager extensionManager;
    private final IMetrics metrics;

    public AppMetrics(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.extensionManager = this.bdsAutoEnable.getExtensionManager();
        this.metrics = new IMetrics(this.bdsAutoEnable);
        this.init();
    }
    //Z jakiegoś powodu to nie działa, nie chce mi się nad tym siedzieć

    private void init() {
        for (final Map.Entry<String, Extension> entry : this.extensionManager.getExtensions().entrySet()) {
            this.sendExtensionData(entry.getValue().getName());
        }
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