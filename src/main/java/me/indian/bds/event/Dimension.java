package me.indian.bds.event;

public enum Dimension {
    OVERWORLD("minecraft:overworld"),
    NETHER("minecraft:nether"),
    END("minecraft:end");

    private final String dimensionID;

    Dimension(final String dimensionID) {
        this.dimensionID = dimensionID;
    }

    public String getDimensionID() {
        return this.dimensionID;
    }

    public static Dimension getByID(final String dimensionID) {
        for (final Dimension dimension : values()) {
            if (dimensionID.contains(dimension.dimensionID)) {
                return dimension;
            }
        }
        throw new IllegalArgumentException("Unknown dimension ID: " + dimensionID);
    }
}
