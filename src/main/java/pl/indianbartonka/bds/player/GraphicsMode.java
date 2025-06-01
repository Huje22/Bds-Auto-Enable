package pl.indianbartonka.bds.player;

public enum GraphicsMode {
    DEFERRED("Deffered"),
    FANCY("Fancy"),
    RAY_TRACED("RayTraced"),
    SIMPLE("Simple"),
    UNKNOWN("Unknown");

    private final String name;

    GraphicsMode(final String name) {
        this.name = name;
    }

    public static GraphicsMode getByName(final String mode) {
        for (final GraphicsMode graphicsMode : GraphicsMode.values()) {
            if (graphicsMode.name.equalsIgnoreCase(mode)) {
                return graphicsMode;
            }
        }
        return UNKNOWN;
    }
}