package me.indian.bds.server.properties;

public enum ServerMovementAuth {

    CLIENT_AUTH("client-auth"),
    SERVER_AUTH("server-auth"),
    SERVER_AUTH_REWIND("server-auth-with-rewind");;

    private final String authName;

    ServerMovementAuth(final String authName) {
        this.authName = authName;
    }

    public String getAuthName() {
        return this.authName;
    }
}