package me.indian.bds.util.system;

public enum SystemOS {

    WINDOWS,
    LINUX,
    //    MAC,
    UNSUPPORTED;

    SystemOS() {
    }

    public static SystemOS getSystem() {
        final String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return SystemOS.WINDOWS;
        } else if (os.contains("nix") || os.contains("nux")) {
            return SystemOS.LINUX;
//        } else if (os.contains("mac")) {
//            return SystemOS.MAC;
        } else {
            return SystemOS.UNSUPPORTED;
        }
    }

    public static String getFullyOsName() {
        return System.getProperty("os.name");
    }
}