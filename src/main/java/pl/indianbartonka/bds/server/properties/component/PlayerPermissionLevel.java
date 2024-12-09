package pl.indianbartonka.bds.server.properties.component;

public enum PlayerPermissionLevel {

    VISITOR(0, "visitor"),
    MEMBER(1, "member"),
    OPERATOR(2, "operator");

    private final int permissionLevel;
    private final String permissionName;

    PlayerPermissionLevel(final int permissionLevel, final String permissionName) {
        this.permissionLevel = permissionLevel;
        this.permissionName = permissionName;
    }

    public static PlayerPermissionLevel getByName(final String permissionName) throws NullPointerException {
        return switch (permissionName.toUpperCase()) {
            case "VISITOR" -> VISITOR;
            case "MEMBER" -> MEMBER;
            case "OPERATOR" -> OPERATOR;
            default -> throw new IllegalArgumentException("Unknown Player Permission Level name: " + permissionName);
        };
    }

    public static PlayerPermissionLevel getByLevel(final int permissionLevel) {
        return switch (permissionLevel) {
            case 0 -> VISITOR;
            case 1 -> MEMBER;
            case 2 -> OPERATOR;
            default -> throw new IllegalArgumentException("Unknown Player Permission Level: " + permissionLevel);
        };
    }

    public int getPermissionLevel() {
        return this.permissionLevel;
    }

    public String getPermissionName() {
        return this.permissionName;
    }
}