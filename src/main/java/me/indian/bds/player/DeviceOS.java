package me.indian.bds.player;

public enum DeviceOS {
    UNKNOWN("unknown", 0),
    ANDROID("android", 1),
    IOS("ios", 2),
    MAC_OS("mac_os", 3),
    FIRE_OS("fire_os", 4),
    GEARVR("gear_vr", 5),
    HOLOLENS("hololens", 6),
    WIN_32("windows32", 7),
    WIN("windows", 8),
    DEDICATED("dedicated", 9),
    TVOS("tvos", 10),
    PLAYSTATION("playstation", 11),
    NINTENDO("nintendo", 12),
    XBOX("xbox", 13),
    WINDOWS_PHONE("windows_phone", 14),
    LINUX("linux", 15);

    private final String name;
    private final int id;

    DeviceOS(final String name, final int id) {
        this.name = name;
        this.id = id;
    }

    public static DeviceOS getByName(final String name) {
        for (final DeviceOS deviceOS : DeviceOS.values()) {
            if (deviceOS.getPlatformName().equalsIgnoreCase(name)) {
                return deviceOS;
            }
        }
        return UNKNOWN;
    }

    public static DeviceOS getByID(final int id) {
        for (final DeviceOS deviceOS : DeviceOS.values()) {
            if (deviceOS.getPlatformID() == id) {
                return deviceOS;
            }
        }
        return UNKNOWN;
    }

    public String getPlatformName() {
        return this.name;
    }

    public int getPlatformID() {
        return this.id;
    }
}
