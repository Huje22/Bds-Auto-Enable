package me.indian.bds.server.properties;

public enum ServerMovementAuth {
    CLIENT("client-auth"),
    SERVER("server-auth"),
    SERVER_REWIND("server-auth-with-rewind");

    private final String authName;

    ServerMovementAuth(final String authName) {
        this.authName = authName;
    }

    public String getAuthName() {
        return this.authName;
    }
}