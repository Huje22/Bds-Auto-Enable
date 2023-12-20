package me.indian.bds.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class ZipUtil {

    private ZipUtil() {
    }

    public static void zipFolder(final String sourceFolderPath, final String zipFilePath) throws Exception {
        final File sourceFolder = new File(sourceFolderPath);
        try (final FileOutputStream fos = new FileOutputStream(zipFilePath);
             final ZipOutputStream zos = new ZipOutputStream(fos)) {
            addFolderToZip(sourceFolder, sourceFolder.getName(), zos);
        }
    }

    public static void zipFiles(final List<String> srcFiles, final String zipFilePath) throws Exception {
        try (final FileOutputStream fos = new FileOutputStream(zipFilePath);
             final ZipOutputStream zipOut = new ZipOutputStream(fos)) {
            for (final String srcFile : srcFiles) {
                final File fileToZip = new File(srcFile);
                if (!fileToZip.exists()) continue;
                try (final FileInputStream fis = new FileInputStream(fileToZip)) {
                    final ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                    zipOut.putNextEntry(zipEntry);
                    final byte[] bytes = new byte[1024];
                    int length;
                    while ((length = fis.read(bytes)) >= 0) {
                        zipOut.write(bytes, 0, length);
                    }
                }
            }
        }
    }

    public static void unzipFile(final String zipFilePath, final String targetDirectory, final boolean deleteOnEnd) throws Exception {
        unzipFile(zipFilePath, targetDirectory, deleteOnEnd, null);
    }

    public static void unzipFile(final String zipFilePath, final String targetDirectory, final boolean deleteOnEnd, final List<String> skipFiles) throws Exception {
        final Path path = Path.of(zipFilePath);
        try (final ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(path))) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                final String entryName = zipEntry.getName();
                final File outputFile = new File(targetDirectory + File.separator + entryName);
                if (entryName.contains(File.separator + ".git")) continue;
                if (outputFile.exists() && skipFiles != null && skipFiles.contains(outputFile.getAbsolutePath())) {
                    System.out.println("Omijam plik " + outputFile.getAbsolutePath());
                    continue;
                }

                if (zipEntry.isDirectory()) {
                    outputFile.mkdirs();
                } else {
                    try (final FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                        final byte[] buffer = new byte[1024];
                        int length;
                        while ((length = zipInputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }
                    }
                }
            }
            if (deleteOnEnd) {
                Files.deleteIfExists(path);
            }
        }
    }

    private static void addFolderToZip(final File folder, final String folderName, final ZipOutputStream zos) throws Exception {
        final File[] files = folder.listFiles();
        if (files != null) {
            for (final File file : files) {
                if (file.isDirectory()) {
                    addFolderToZip(file, folderName + File.separator + file.getName(), zos);
                } else {
                    addFileToZip(file, folderName, zos);
                }
            }
        }
    }

    private static void addFileToZip(final File file, final String folderName, final ZipOutputStream zos) throws Exception {
        final byte[] buffer = new byte[1024];
        try (final FileInputStream fis = new FileInputStream(file)) {
            final String entryPath = folderName + File.separator + file.getName();
            zos.putNextEntry(new ZipEntry(entryPath));
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();
        }
    }
}
