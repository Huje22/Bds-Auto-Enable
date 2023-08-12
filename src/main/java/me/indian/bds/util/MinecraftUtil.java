package me.indian.bds.util;

public class MinecraftUtil {

    public static String colorize(final String msg) {
        return msg.replaceAll("&", "§");
    }

    public static String tellrawToAllMessage(final String msg) {
        return MinecraftUtil.colorize("tellraw @a {\"rawtext\":[{\"text\":\"" + msg + "\"}]}");
    }

    public static String kickCommand(final String who, final String reason) {
        return "kick " + who + " " + colorize(reason);
    }

}
