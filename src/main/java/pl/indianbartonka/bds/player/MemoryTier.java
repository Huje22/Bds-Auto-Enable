package pl.indianbartonka.bds.player;

public enum MemoryTier {
    UNDETERMINED(-1),
    SUPER_LOW(0),
    LOW(1),
    MID(2),
    HIGH(3),
    SUPER_HIGH(4);

    private final int tier;

    MemoryTier(final int tier) {
        this.tier = tier;
    }

    public static MemoryTier getMemoryTier(final int tier) {
        for (final MemoryTier memoryTier : values()) {
            if (memoryTier.tier == tier) return memoryTier;
        }

        return UNDETERMINED;
    }

    public int getTier() {
        return this.tier;
    }
}

