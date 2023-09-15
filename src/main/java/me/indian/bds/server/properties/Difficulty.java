package me.indian.bds.server.properties;

public enum Difficulty {

    PEACEFUL("peaceful", 0),
    EASY("easy", 1),
    NORMAL("normal", 2),
    HARD("hard", 3);

    private final String name;
    private final int id;


    Difficulty(final String name, final int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public int getId() {
        return this.id;
    }
}
