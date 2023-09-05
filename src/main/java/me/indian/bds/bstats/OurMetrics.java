package me.indian.bds.bstats;

import me.indian.bds.BDSAutoEnable;

public class OurMetrics {

    public OurMetrics(final BDSAutoEnable bdsAutoEnable) {
        final Metrics metrics = new Metrics(bdsAutoEnable);
        metrics.addCustomChart(new Metrics.SimplePie("bds_loaded_version", () -> bdsAutoEnable.getConfig().getVersion()));
    }
}
