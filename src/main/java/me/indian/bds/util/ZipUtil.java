package me.indian.bds.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class ZipUtil {


    public static void zipFolder(final String sourceFolderPath, final String zipFilePath) throws IOException {
        final File sourceFolder = new File(sourceFolderPath);
        try (final FileOutputStream fos = new FileOutputStream(zipFilePath);
             final ZipOutputStream zos = new ZipOutputStream(fos)) {
            addFolderToZip(sourceFolder, sourceFolder.getName(), zos);
        }
    }

    public static void zipFiles(final List<String> srcFiles, final String zipFilePath) throws IOException {
        final FileOutputStream fos = new FileOutputStream(zipFilePath);
        final ZipOutputStream zipOut = new ZipOutputStream(fos);

        for (final String srcFile : srcFiles) {
            final File fileToZip = new File(srcFile);
            if (!fileToZip.exists()) continue;
            final FileInputStream fis = new FileInputStream(fileToZip);
            final ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);

            final byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
        }
        zipOut.close();
        fos.close();
    }

    private void unzipFile(final String zipFilePatch, final String targetDirectory) throws IOException {
        try (final ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(Paths.get(zipFilePatch)))) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                final String entryName = zipEntry.getName();
                final File outputFile = new File(targetDirectory, entryName);
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
        }
    }

    public static void unzipFile(final String targetDirectory, final String zipFilePath, final List<String> skipFiles) throws IOException {
        try (final ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(Paths.get(zipFilePath)))) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                final String entryName = zipEntry.getName();
                if (skipFiles.contains(entryName)) continue;
                final File outputFile = new File(targetDirectory, entryName);
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
        }
    }

    private static void addFolderToZip(final File folder, final String folderName, final ZipOutputStream zos) throws IOException {
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

    private static void addFileToZip(final File file, final String folderName, final ZipOutputStream zos) throws IOException {
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
