package me.indian.bds.server.properties;

public enum PlayerPermissionLevel {

    VISITOR(0, "VISITOR"),
    MEMBER(1, "MEMBER"),
    OPERATOR(2, "OPERATOR");

    private final int permissionLevel;
    private final String permissionName;

    PlayerPermissionLevel(final int permissionLevel, final String permissionName) {
        this.permissionLevel = permissionLevel;
        this.permissionName = permissionName;
    }

    public int getPermissionLevel() {
        return this.permissionLevel;
    }

    public String getPermissionName() {
        return this.permissionName;
    }
}
