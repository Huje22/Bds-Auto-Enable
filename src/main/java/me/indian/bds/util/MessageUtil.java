package me.indian.bds.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MessageUtil {

    public static String colorize(final String msg) {
        return msg.replaceAll("&", "§");
    }

    public static String buildMessageFromArgs(final String[] args) {
        String message = "";
        for (final String arg : args) {
            message = message.concat(arg + " ");
        }
        message = message.trim();
        return message;
    }

    public static String buildMessageFromArgs(final String[] args, final String includeArg) {
        String message = "";
        for (final String arg : args) {
            if (arg.equals(includeArg)) continue;
            message = message.concat(arg + " ");
        }
        message = message.trim();
        return message;
    }

    public static String listToSpacedString(final List<String> lista) {
        if (lista == null) {
            return "";
        }
        return String.join("\n", lista);
    }

    public static String stringListToString(final List<String> lista, String split) {
        if (split == null) {
            split = " ";
        }

        if (lista == null) {
            return "";
        }
        return String.join(split, lista);
    }

    public static String objectListToString(final List<Object> lista, String split) {
        String string = "";
        if (split == null) {
            split = " ";
        }

        if (lista == null) {
            return "";
        }

        for (final Object object : lista) {
            string += object.toString() + split;
        }

        return string;
    }

    public static List<String> stringToStringList(final String text, String split) {
        if (split == null) {
            split = " ";
        }

        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(text.split(split));
    }
}