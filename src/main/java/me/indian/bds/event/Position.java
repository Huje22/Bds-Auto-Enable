package me.indian.bds.event;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;

public record Position(double x, double y, double z) {

    @Nullable
    public static Position parsePosition(final String positionString) {
        final Pattern pattern = Pattern.compile("X:(.+) Y:(.+) Z:(.+)");
        final Matcher matcher = pattern.matcher(positionString);

        if (matcher.find()) {
            final double x = Double.parseDouble(matcher.group(1));
            final double y = Double.parseDouble(matcher.group(2));
            final double z = Double.parseDouble(matcher.group(3));
            return new Position(x, y, z);
        }
        return null;
    }
}