package pl.indianbartonka.bds.server.properties.component;

public enum ServerMovementAuth {

    DEFAULT("default"),
    CLIENT_AUTH("client-auth"),
    SERVER_AUTH("server-auth"),
    SERVER_AUTH_REWIND("server-auth-with-rewind");

    private final String authName;

    ServerMovementAuth(final String authName) {
        this.authName = authName;
    }

    public static ServerMovementAuth getByName(final String authName) throws NullPointerException {
        return switch (authName) {
            case "client-auth" -> CLIENT_AUTH;
            case "server-auth" -> SERVER_AUTH;
            case "server-auth-with-rewind" -> SERVER_AUTH_REWIND;
            case "default" -> DEFAULT;
            default -> throw new IllegalArgumentException("Unknown Server Movement Auth name: " + authName);
        };
    }

    public String getAuthName() {
        return this.authName;
    }
}