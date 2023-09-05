package me.indian.bds.util;

import java.io.IOException;
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

    public static boolean renameFolder(final Path oldPath, final Path newPath) {
        try {
            Files.move(oldPath, newPath);
            return true;
        } catch (final IOException e) {
            return false;
        }
    }
}
