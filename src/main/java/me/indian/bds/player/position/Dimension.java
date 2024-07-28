package me.indian.bds.player.position;

import java.util.Arrays;

public enum Dimension {
    UNKNOWN("UNKNOWN"),
    OVERWORLD("minecraft:overworld"),
    NETHER("minecraft:nether"),
    END("minecraft:the_end");

    private final String dimensionID;

    Dimension(final String dimensionID) {
        this.dimensionID = dimensionID;
    }

    public static Dimension getByID(final String dimensionID) {
        return Arrays.stream(values())
                .filter(dimension -> dimension.getDimensionID().contains(dimensionID))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public String getDimensionID() {
        return this.dimensionID;
    }
}