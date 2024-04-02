package me.indian.bds.player;

public enum Controller {
    UNKNOWN("UNKNOWN", 0),
    MOUSE("MOUSE", 1),
    TOUCH("TOUCH", 2),
    GAME_PAD("GAME_PAD", 3),
    MOTION_CONTROLLER("MOTION_CONTROLLER", 4);

    private final String name;
    private final int id;

    Controller(final String name, final int id) {
        this.name = name;
        this.id = id;
    }

    public static Controller getByName(final String typeName) {
        for (final Controller type : Controller.values()) {
            if (type.getName().equalsIgnoreCase(typeName)) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public static Controller getById(final int id) {
        for (final Controller type : Controller.values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public String getName() {
        return this.name;
    }

    public int getId() {
        return this.id;
    }
}