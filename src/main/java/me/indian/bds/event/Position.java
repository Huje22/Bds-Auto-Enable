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
            return new Position(Double.parseDouble(matcher.group(1)),
                    Double.parseDouble(matcher.group(2)),
                    Double.parseDouble(matcher.group(3)));
        }
        return null;
    }
}