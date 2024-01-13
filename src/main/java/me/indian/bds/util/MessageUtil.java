package me.indian.bds.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public final class MessageUtil {

    private static final Random random = new Random();
    private static final char[] CHARS = {'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P', 'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9', '@', '#', '*'};

    private MessageUtil() {
    }

    public static String generateCode(final int length) {
        String code = "";
        for (int i = 0; i < length; i++) {
            code += CHARS[random.nextInt(CHARS.length)];
        }

        return code;
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

    public static String buildMessageFromArgs(final String[] args) {
        return buildMessageFromArgs(args, null);
    }

    public static String buildMessageFromArgs(final String[] args, final String[] includeArgs) {
        if (args == null) return "";
        String message = "";
        for (final String arg : args) {
            if (includeArgs != null && Arrays.stream(includeArgs).anyMatch(s -> s.equals(arg))) continue;
            message = message.concat(arg + " ");
        }
        return message.trim();
    }

    public static String[] stringToArgs(final String input) {
        return input.split("\\s+");
    }

    public static String[] removeFirstArgs(final String[] args) {
        return removeArgs(args, 1);
    }

    public static String[] removeArgs(final String[] args, final int startFrom) {
        if (args == null) return new String[]{};
        final String[] newArgs = new String[args.length - startFrom];
        System.arraycopy(args, startFrom, newArgs, 0, newArgs.length);

        return newArgs;
    }

    public static String listToSpacedString(final List<String> lista) {
        return stringListToString(lista, "\n");
    }

    public static String stringListToString(final List<String> list, String split) {
        if (split == null) split = " ";
        if (list == null || list.isEmpty()) return "";

        return String.join(split, list);
    }

    public static <T> String objectListToString(final List<T> list, String split) {
        if (split == null) split = " ";
        if (list == null || list.isEmpty()) return "";

        return String.join(split, list.stream().map(Object::toString).toArray(String[]::new));
    }

    public static <E extends Enum<E>> String enumSetToString(final EnumSet<E> enumSet, String split) {
        if (split == null) split = " ";
        if (enumSet == null || enumSet.isEmpty()) return "";

        return String.join(split, enumSet.stream().map(Enum::toString).toArray(String[]::new));
    }

    public static List<String> stringToStringList(final String text, String split) {
        if (split == null) split = " ";
        if (text == null || text.isEmpty()) return new ArrayList<>();

        return Arrays.asList(text.split(split));
    }

    public static String getStackTraceAsString(final Throwable throwable) {
        if (throwable == null) return "";
        final StringBuilder stackTraceBuilder = new StringBuilder();
        stackTraceBuilder.append(throwable.getMessage()).append("\n");
        for (final StackTraceElement element : throwable.getStackTrace()) {
            stackTraceBuilder.append(element.toString()).append("\n");
        }
        return stackTraceBuilder.toString();
    }
}