package me.indian.bds.util;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.Config;
import me.indian.bds.logger.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class VersionManager {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final Config config;
    private final List<String> importantFiles;
    private final File versionFolder;
    private final List<String> availableVersions;

    public VersionManager(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.config = this.bdsAutoEnable.getConfig();
        this.importantFiles = new ArrayList<>();
        this.versionFolder = new File("BDS-Auto-Enable/versions");
        this.availableVersions = new ArrayList<>();
        this.loadVersionsInfo();


        this.importantFiles.add("config");
        this.importantFiles.add("allowlist.json");
        this.importantFiles.add("server.properties");
        this.importantFiles.add("permissions.json");
    }

    private void loadVersionsInfo() {
        this.availableVersions.clear();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(this.versionFolder.getPath()))) {
            for (Path path : directoryStream) {
                if (Files.isRegularFile(path)) {
                    final String name = String.valueOf(path.getFileName());
                    System.out.println("Plik: " + name);
                    if (name.endsWith(".zip")) {
                        this.availableVersions.add(name.replaceAll(".zip", ""));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(this.availableVersions);
    }

    public void loadVersion(final String version) {
        if (!this.versionFolder.exists()) {
            if (this.versionFolder.mkdirs()) {
                this.logger.info("Utworzono mnieisce na wersjie");
            } else {
                this.logger.error("Nie można utworzyć mnieisca na wersjie");
                return;
            }
        }

        final File verFile = new File(this.versionFolder.getPath() + File.separator + version + ".zip");
        if (!verFile.exists()) {
            this.logger.info("Nie znaleziono wersji " + ConsoleColors.BLUE + version + ConsoleColors.RESET);
            this.downloadServerFiles(version);
        }
        try {
            this.logger.info("Ładowanie wersji: " + version);

            final int versionSize = this.getSize(version);
            if (!(versionSize <= -1) && versionSize != Files.size(verFile.toPath())) {
                this.logger.critical("Wielkość versij nie jest zgodna!");
                this.downloadServerFiles(version);
            }

            ZipUtil.unzipFile(verFile.getAbsolutePath(), this.config.getFilesPath(), this.importantFiles);
            this.logger.info("Załadowano versie: " + version);
        } catch (final IOException exception) {
            this.logger.error("Nie można załadować wersji: " + version);
            this.logger.critical(exception);
            throw new RuntimeException(exception);

        }
        this.config.setLoaded(true);
        this.config.setVersion(version);
        this.config.save();
//        this.bdsAutoEnable.getServerProcess().instantShutdown();
    }

    public void loadVersion() {
        final File bedrockFile = new File(this.config.getFilesPath() + File.separator + this.config.getFileName());
        if (!bedrockFile.exists()) {
            this.logger.critical("Nie można odnaleźć pliku " + this.config.getFileName() + " na ścieżce " + this.config.getFilesPath());
            this.config.setLoaded(false);
            this.config.save();
        }

        if (!this.config.isLoaded()) {
            this.loadVersion(this.config.getVersion());
        }
        this.bdsAutoEnable.getServerProperties().loadProperties();
    }

    public void downloadServerFiles(final String version) {
        try {
            final long startTime = System.currentTimeMillis();
            final URL url = new URL(this.getServerDownloadUrl(version));
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            final int response = connection.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                this.logger.info("Pobieranie wersji: " + ConsoleColors.BLUE + version + ConsoleColors.RESET);
                final int fileSize = connection.getContentLength();
                final InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                final FileOutputStream outputStream = new FileOutputStream(this.versionFolder.getPath() + File.separator + version + ".zip");

                final byte[] buffer = new byte[1024];
                int bytesRead;
                long totalBytesRead = 0;

                int tempProgres = -1;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    final int progress = Math.toIntExact((totalBytesRead * 100) / fileSize);

                    if (progress != tempProgres) {
                        tempProgres = progress;
                        this.logger.info("Pobrano w: " + progress + "%");
                    }
                }

                inputStream.close();
                outputStream.close();

                this.logger.info("Pobrano w " + ConsoleColors.GREEN + ((System.currentTimeMillis() - startTime) / 1000.0) + ConsoleColors.RESET + " sekund");
            } else {
                this.logger.error("Kod odpowiedzi strony: " + response);
                this.logger.error("Prawdopodobnie nie ma takiej wersij jak: " + version);
                this.bdsAutoEnable.getServerProcess().instantShutdown();
            }
        } catch (final IOException ioException) {
            this.logger.error("Wystąpił błąd podczas próby pobrania wersji " + version);
            this.logger.critical(ioException);
            ioException.printStackTrace();
        }
    }

    private int getSize(final String version) {
        try {
            final HttpURLConnection connection = (HttpURLConnection) new URL(this.getServerDownloadUrl(version)).openConnection();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return connection.getContentLength();
            } else {
                return -1;
            }
        } catch (final IOException ioException) {
            this.logger.error("Wystąpił błąd podczas próby pobrania wersji " + version);
            this.logger.critical(ioException);
            ioException.printStackTrace();
        }
        return -1;
    }

    private String getServerDownloadUrl(final String version) {
        switch (this.config.getSystemOs()) {
            case LINUX:
                if (this.config.isWine()) {
                    return "https://minecraft.azureedge.net/bin-win/bedrock-server-" + version + ".zip";
                } else {
                    return "https://minecraft.azureedge.net/bin-linux/bedrock-server-" + version + ".zip";
                }
            case WINDOWS:
                return "https://minecraft.azureedge.net/bin-win/bedrock-server-" + version + ".zip";
            default:
                throw new RuntimeException("Nieprawidłowy system");
        }
    }

    public List<String> getAvailableVersions() {
        return this.availableVersions;
    }
}