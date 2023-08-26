package me.indian.bds.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileUtil {

    public static boolean canExecute(final String filePath) {
        try {
            if (Files.isExecutable(Path.of(URLDecoder.decode(filePath.replace("/C", "C"), StandardCharsets.UTF_8)))) {
                return true;
            }
        } catch (final Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }
}
