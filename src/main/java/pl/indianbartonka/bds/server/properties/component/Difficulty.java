package pl.indianbartonka.bds.server.properties.component;

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
            case 0 -> PEACEFUL;
            case 1 -> EASY;
            case 2 -> NORMAL;
            case 3 -> HARD;
            default -> throw new IllegalArgumentException("Unknown difficulty ID: " + difficultyId);
        };
    }

    public static Difficulty getByName(final String difficultyName) throws NullPointerException {
        return switch (difficultyName.toLowerCase()) {
            case "peaceful" -> PEACEFUL;
            case "easy" -> EASY;
            case "normal" -> NORMAL;
            case "hard" -> HARD;
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