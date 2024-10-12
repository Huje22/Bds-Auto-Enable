package pl.indianbartonka.bds.metrics;

import java.util.HashMap;
import java.util.Map;
import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.extension.Extension;
import pl.indianbartonka.bds.extension.ExtensionManager;

public class AppMetrics {

    private final ExtensionManager extensionManager;
    private final IMetrics metrics;

    public AppMetrics(final BDSAutoEnable bdsAutoEnable) {
        this.extensionManager = bdsAutoEnable.getExtensionManager();
        this.metrics = new IMetrics(bdsAutoEnable);
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