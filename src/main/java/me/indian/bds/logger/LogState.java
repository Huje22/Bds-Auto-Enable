package me.indian.bds.logger;

public enum LogState {
    NONE("&r"),
    INFO("&1"),
    ALERT("&c"),
    CRITICAL("&4"),
    ERROR("&8"),
    DEBUG("&8"),
    WARNING("&f");

    private final String colorCode;

    LogState(final String colorCode) {
        this.colorCode = colorCode;
    }

    public String getColorCode() {
        return this.colorCode;
    }
}
