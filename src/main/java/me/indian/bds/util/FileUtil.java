package me.indian.bds.util;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public final class FileUtil {

    public static boolean canExecute(final String filePath) {
        try {
          return Files.isExecutable(Path.of(URLDecoder.decode(filePath.replace("/C", "C"), StandardCharsets.UTF_8)));
        } catch (final Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }

    public static boolean addExecutePerm(final String filePath) {
        try {
            final File file = new File(filePath);
            if (!file.exists()) throw new NoSuchFileException(file.toString());
            return file.setExecutable(true, false);
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