package me.indian.bds.util;

public final class MinecraftUtil {

    private MinecraftUtil() {

    }

    public static String colorize(final String msg) {
        return (msg == null ? "" : msg.replaceAll("&", "§"));
    }

    public static String fixMessage(final String message) {
        return fixMessage(message, false);
    }

    public static String fixMessage(final String message, final boolean newLines) {
        if (message.isEmpty()) return "";
        String msg2 = message.replaceAll("\\\\", "")
                .replaceAll("[\\uE000-\\uE0EA]", "?")
                .replaceAll("\\$", "?")
                .replaceAll("ঋ", "?")
                .replaceAll("ༀ", "?")
                .replaceAll("", "?");

        if (!newLines) msg2 = msg2.replaceAll("\\r\\n|\\r|\\n", " ");

        return msg2;
    }

    public static String fixPlayerName(final String playerName) {
        if (playerName.contains(" ")) {
            return "\"" + playerName + "\"";
        }
        return playerName;
    }
}