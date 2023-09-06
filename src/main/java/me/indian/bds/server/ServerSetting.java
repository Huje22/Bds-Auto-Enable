package me.indian.bds.server;

public enum ServerSetting {

    difficulty("difficulty"),
    allowCheats("allow-cheats");

    private final String name;

    ServerSetting(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
