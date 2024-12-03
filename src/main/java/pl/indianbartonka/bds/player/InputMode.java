package pl.indianbartonka.bds.player;

public enum InputMode {

    GAMEPAD("Gamepad"),
    KEYBOARD_AND_MOUSE("KeyboardAndMouse"),
    MOTION_CONTROLLER("MotionController"),
    TOUCH("Touch"),
    UNKNOWN("Unknown");

    private final String mode;

    InputMode(final String mode) {
        this.mode = mode;
    }

    public static InputMode getByName(final String mode) {
        for (final InputMode platformType : InputMode.values()) {
            if (platformType.mode.equalsIgnoreCase(mode)) {
                return platformType;
            }
        }
        return UNKNOWN;
    }

    public String getMode() {
        return this.mode;
    }
}
