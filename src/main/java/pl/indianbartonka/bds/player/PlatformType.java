package pl.indianbartonka.bds.player;

public enum PlatformType {

    UNKNOWN("unknown"),
    Console("Console"),
    Desktop("Desktop"),
    Mobile("Mobile");

    private final String name;

    PlatformType(final String name) {
        this.name = name;
    }

    public static PlatformType getByName(final String name) {
        for (final PlatformType platformType : PlatformType.values()) {
            if (platformType.getPlatformName().equalsIgnoreCase(name)) {
                return platformType;
            }
        }
        return UNKNOWN;
    }

    public String getPlatformName() {
        return this.name;
    }
}
