package me.indian.bds.server.properties.component;

public enum Gamemode {

    SURVIVAL("survival", 0),
    CREATIVE("creative", 1),
    ADVENTURE("adventure", 2),
    SPECTATOR("spectator", 3);

    private final String name;
    private final int id;

    Gamemode(final String name, final int id) {
        this.name = name;
        this.id = id;
    }

    public static Gamemode getByName(final String name) throws NullPointerException {
        return switch (name.toLowerCase()) {
            case "survival" -> SURVIVAL;
            case "creative" -> CREATIVE;
            case "adventure" -> ADVENTURE;
            case "spectator" -> SPECTATOR;
            default -> throw new IllegalArgumentException("Unknown gamemode name:" + name);
        };
    }

    public static Gamemode getByID(final int id) throws NullPointerException {
        return switch (id) {
            case 0 -> SURVIVAL;
            case 1 -> CREATIVE;
            case 2 -> ADVENTURE;
            case 3 -> SPECTATOR;
            default -> throw new IllegalArgumentException("Unknown gamemode ID:" + id);
        };
    }

    public String getName() {
        return this.name;
    }

    public int getId() {
        return this.id;
    }
}
