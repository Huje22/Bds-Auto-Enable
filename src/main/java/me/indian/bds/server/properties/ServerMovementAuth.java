package me.indian.bds.server.properties;

public enum ServerMovementAuth {
    CLIENT("client-auth"),
    SERVER("server-auth"),
    SERVER_REWIND("server-auth-with-rewind");

    private final String name;

    ServerMovementAuth(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}