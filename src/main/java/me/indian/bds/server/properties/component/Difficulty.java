package me.indian.bds.server.properties.component;

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

    public static Difficulty getById(final int difficultyId) throws NullPointerException {
        return switch (difficultyId) {
            case 0 -> Difficulty.PEACEFUL;
            case 1 -> Difficulty.EASY;
            case 2 -> Difficulty.NORMAL;
            case 3 -> Difficulty.HARD;
            default -> throw new IllegalArgumentException("Unknown difficulty ID: " + difficultyId);
        };
    }

    public static Difficulty getByName(final String difficultyName) throws NullPointerException {
        return switch (difficultyName.toLowerCase()) {
            case "peaceful" -> Difficulty.PEACEFUL;
            case "easy" -> Difficulty.EASY;
            case "normal" -> Difficulty.NORMAL;
            case "hard" -> Difficulty.HARD;
            default -> throw new IllegalArgumentException("Unknown difficulty name: " + difficultyName);
        };
    }

    public String getDifficultyName() {
        return this.difficultyName;
    }

    public int getDifficultyId() {
        return this.difficultyId;
    }
}