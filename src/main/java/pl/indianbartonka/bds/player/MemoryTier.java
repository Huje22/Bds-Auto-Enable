package pl.indianbartonka.bds.player;

public enum MemoryTier {
    UNDETERMINED(0),
    SUPER_LOW(1),
    LOW(2),
    MID(3),
    HIGH(4),
    SUPER_HIGH(5);

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

