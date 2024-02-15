package me.indian.bds.metrics;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.extension.Extension;
import me.indian.bds.extension.ExtensionManager;

import java.util.HashMap;
import java.util.Map;

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