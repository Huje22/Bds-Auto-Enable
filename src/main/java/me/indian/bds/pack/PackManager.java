package me.indian.bds.pack;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.pack.loader.BehaviorPackLoader;
import me.indian.bds.pack.loader.ResourcePackLoader;

public class PackManager {

    private final ResourcePackLoader resourcePackLoader;
    private final BehaviorPackLoader behaviorPackLoader;

    public PackManager(final BDSAutoEnable bdsAutoEnable) {
        this.resourcePackLoader = new ResourcePackLoader(bdsAutoEnable);
        this.behaviorPackLoader = new BehaviorPackLoader(bdsAutoEnable);
    }

    public ResourcePackLoader getResourcePackLoader() {
        return this.resourcePackLoader;
    }

    public BehaviorPackLoader getBehaviorPackLoader() {
        return this.behaviorPackLoader;
    }
}