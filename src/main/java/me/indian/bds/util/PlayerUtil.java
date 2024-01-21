package me.indian.bds.util;

public final class PlayerUtil {

    private PlayerUtil() {

    }

    public static String getOS(final int deviceOS) {
        return switch (deviceOS) {
            case 1 -> "ANDROID";
            case 2 -> "IOS";
            case 3 -> "MAC";
            case 4 -> "FIRE";
            case 5 -> "GEARVR";
            case 6 -> "HOLOLENS";
            case 7, 8 -> "WINDOWS";
            case 9 -> "DEDICATED";
            case 10 -> "TVOS";
            case 11 -> "PLAYSTATION";
            case 12 -> "NINTENDO";
            case 13 -> "XBOX";
            case 14 -> "WINPHONE";
            case 15 -> "LINUX";
            default -> "UNKNOWN" + " (" + deviceOS + ")";
        };
    }

}
