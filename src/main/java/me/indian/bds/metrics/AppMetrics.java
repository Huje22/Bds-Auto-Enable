package me.indian.bds.metrics;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.extension.Extension;
import me.indian.bds.extension.ExtensionLoader;

import java.util.HashMap;
import java.util.Map;

public class AppMetrics {

    private final BDSAutoEnable bdsAutoEnable;
    private final ExtensionLoader extensionLoader;
    private final IMetrics metrics;

    public AppMetrics(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.extensionLoader = this.bdsAutoEnable.getExtensionLoader();
        this.metrics = new IMetrics(this.bdsAutoEnable);
        this.init();
    }

    private void init() {


        for (final Extension extension : this.extensionLoader.getExtensions()) {
            this.sendExtensionData(extension.getName());
        }
    }

    private void sendExtensionData(final String extensionName) {
        this.metrics.addCustomChart(new IMetrics.DrilldownPie("extensions", () -> {
            final Map<String, Map<String, Integer>> valmap = new HashMap<>();
            final Map<String, Integer> assets = new HashMap<>();
            final Extension extension = this.extensionLoader.getExtension(extensionName);
            if (extension != null) {
                final String version = extension.getVersion();
                assets.put(version, 1);
                valmap.put(extensionName, assets);
            }
            return valmap;
        }));
    }
}