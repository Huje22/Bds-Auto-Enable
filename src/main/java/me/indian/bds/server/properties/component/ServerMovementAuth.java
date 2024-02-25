package me.indian.bds.server.properties.component;

public enum ServerMovementAuth {

    CLIENT_AUTH("client-auth"),
    SERVER_AUTH("server-auth"),
    SERVER_AUTH_REWIND("server-auth-with-rewind");

    private final String authName;

    ServerMovementAuth(final String authName) {
        this.authName = authName;
    }

    public static ServerMovementAuth getByName(final String authName) throws NullPointerException {
        return switch (authName) {
            case "client-auth" -> ServerMovementAuth.CLIENT_AUTH;
            case "server-auth" -> ServerMovementAuth.SERVER_AUTH;
            case "server-auth-with-rewind" -> ServerMovementAuth.SERVER_AUTH_REWIND;
            default -> throw new NullPointerException();
        };
    }

    public String getAuthName() {
        return this.authName;
    }
}