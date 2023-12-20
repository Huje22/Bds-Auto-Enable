package me.indian.bds.util.system;

public enum SystemArch {

    //May need improvements

    AMD_X32(),
    AMD_X64(),
    ARM(),
    UNKNOWN();

    SystemArch() {
    }

    public static SystemArch getCurrentArch() {
        final String osArch = System.getProperty("os.arch").toLowerCase();

        if (osArch.contains("amd64") || osArch.contains("x86_64")) return AMD_X64;
        if (osArch.contains("arm")) return ARM;
        if (osArch.contains("x86")) return AMD_X32;

        return UNKNOWN;
    }

    public static String getFullyArchCode() {
        return System.getProperty("os.arch");
    }
}