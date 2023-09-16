package me.indian.bds.server.properties;

public enum Difficulty {

    PEACEFUL("peaceful", 0),
    EASY("easy", 1),
    NORMAL("normal", 2),
    HARD("hard", 3);

    private final String difficultyName;
    private final int difficultyId;

    Difficulty(final String difficultyName, final int difficultyId) {
        this.difficultyName = difficultyName;
        this.difficultyId = difficultyId;
    }

    public String getDifficultyName() {
        return this.difficultyName;
    }

    public int getDifficultyId() {
        return this.difficultyId;
    }
}
