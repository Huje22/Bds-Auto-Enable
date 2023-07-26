package me.indian.bds.util;

public class MinecraftUtil {

    public static String colorize(final String msg) {
        return msg.replaceAll("&", "§");
    }

    public static String tellrawToAllMessage(final String msg) {
        return MinecraftUtil.colorize("tellraw @a {\"rawtext\":[{\"text\":\"" + msg + "\"}]}");
    }

}
