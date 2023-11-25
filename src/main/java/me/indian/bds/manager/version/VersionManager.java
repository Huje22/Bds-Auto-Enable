package me.indian.bds.manager.version;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.util.DefaultsVariables;
import me.indian.bds.config.AppConfig;
import me.indian.bds.config.sub.version.VersionManagerConfig;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.SystemOS;
import me.indian.bds.util.ZipUtil;

public class VersionManager {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final AppConfig appConfig;
    private final VersionManagerConfig versionManagerConfig;
    private final List<String> importantFiles;
    private final File versionFolder;
    private final List<String> availableVersions;
    private final ServerProcess serverProcess;
    private final VersionUpdater versionUpdater;
    private final SystemOS system;
    
    public VersionManager(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.appConfig = this.bdsAutoEnable.getAppConfigManager().getAppConfig();
        this.versionManagerConfig = this.bdsAutoEnable.getAppConfigManager().getVersionManagerConfig();
        this.importantFiles = new ArrayList<>();
        this.versionFolder = new File(DefaultsVariables.getAppDir() + "versions");
        this.availableVersions = new ArrayList<>();
        this.serverProcess = bdsAutoEnable.getServerProcess();
        this.versionUpdater = new VersionUpdater(bdsAutoEnable, this);
        this.system = DefaultsVariables.getSystem();

        if (!this.versionFolder.exists()) {
            if (this.versionFolder.mkdirs()) {
                this.logger.info("Utworzono miejsce na wersje");
            } else {
                this.logger.error("Nie można utworzyć miejsca na wersje");
                return;
            }
        }

        this.loadVersionsInfo();

        this.importantFiles.add(this.appConfig.getFilesPath() + File.separator + "allowlist.json");
        this.importantFiles.add(this.appConfig.getFilesPath() + File.separator + "server.properties");
        this.importantFiles.add(this.appConfig.getFilesPath() + File.separator + "config" + File.separator + "default" + File.separator + "permissions.json");
        this.importantFiles.add(this.appConfig.getFilesPath() + File.separator + "permissions.json");
    }

    private void loadVersionsInfo() {
        this.availableVersions.clear();
        try (final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(this.versionFolder.getPath()))) {
            for (final Path path : directoryStream) {
                if (Files.isRegularFile(path)) {
                    final String name = String.valueOf(path.getFileName());
                    if (name.endsWith(".zip")) {
                        this.availableVersions.add(name.replaceAll(".zip", ""));
                    }
                }
            }
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }

    public void loadVersion(final String version) {
        if (this.serverProcess.isEnabled()) {
            this.logger.error("Nie można załadować innej wersji gdy server jest aktywny!!");
            return;
        }
        final File verFile = new File(this.versionFolder.getPath() + File.separator + version + ".zip");
        if (!verFile.exists()) {
            this.logger.info("Nie znaleziono wersji:&1 " + version);
            this.downloadServerFiles(version);
        }
        try {
            this.logger.info("Ładowanie wersji:&1 " + version);
            final int versionSize = this.getSize(version);
            if (!(versionSize <= -1) && versionSize != Files.size(verFile.toPath())) {
                this.logger.error("Wielkość versij nie jest zgodna!");
                this.downloadServerFiles(version);
            }
            final long startTime = System.currentTimeMillis();
            ZipUtil.unzipFile(verFile.getAbsolutePath(), this.appConfig.getFilesPath(), false, this.importantFiles);
            this.versionManagerConfig.setLoaded(true);
            this.versionManagerConfig.setVersion(version);
            this.logger.info("Załadowano versie:&1 " + version + "&r w &a" + ((System.currentTimeMillis() - startTime) / 1000.0) + "&r sekund");
        } catch (final Exception exception) {
            this.logger.critical("Nie można załadować wersji: " + version, exception);
            throw new RuntimeException(exception);
        }
        this.versionManagerConfig.save();
    }

    public void loadVersion() {
        final File serverFile = new File(this.appConfig.getFilesPath() + File.separator + DefaultsVariables.getDefaultFileName());
        if (!serverFile.exists()) {
            this.versionManagerConfig.setLoaded(false);
            this.versionManagerConfig.save();
        }

        if (!this.versionManagerConfig.isLoaded()) {
            this.loadVersion(this.versionManagerConfig.getVersion());
        }
        this.bdsAutoEnable.getServerProperties().loadProperties();
    }

    public void downloadServerFiles(final String version) {
        try {
            final long startTime = System.currentTimeMillis();
            final HttpURLConnection connection = (HttpURLConnection) new URL(this.getServerDownloadUrl(version)).openConnection();
            final int response = connection.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                this.logger.info("Pobieranie wersji: &1" + version);
                final int fileSize = connection.getContentLength();

                try (final InputStream inputStream = new BufferedInputStream(connection.getInputStream())) {
                    try (final FileOutputStream outputStream = new FileOutputStream(this.versionFolder.getPath() + File.separator + version + ".zip")) {

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
                                this.logger.info("Pobrano w:&b " + progress + "&a%");
                            }
                        }
                    }
                }

                this.logger.info("Pobrano w &a" + ((System.currentTimeMillis() - startTime) / 1000.0) + "&r sekund");
                this.loadVersionsInfo();
            } else {
                this.logger.error("Kod odpowiedzi strony: " + response);
                this.logger.error("Prawdopodobnie nie ma takiej wersji jak: " + version);
                System.exit(1);
            }
        } catch (final IOException ioException) {
            this.logger.error("Wystąpił błąd podczas próby pobrania wersji " + version, ioException);
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
            this.logger.error("Wystąpił błąd podczas próby pobrania wersji " + version, ioException);
        }
        return -1;
    }

    private String getServerDownloadUrl(final String version) {
        switch (this.system) {
            case LINUX -> {
                if (this.appConfig.isWine()) {
                    return "https://minecraft.azureedge.net/bin-win/bedrock-server-" + version + ".zip";
                } else {
                    return "https://minecraft.azureedge.net/bin-linux/bedrock-server-" + version + ".zip";
                }
            }
            case WINDOWS -> {
                return "https://minecraft.azureedge.net/bin-win/bedrock-server-" + version + ".zip";
            }
            default -> throw new RuntimeException("Niewspierany system");
        }
    }

    public String getLatestVersion() {
        try {
            final StringBuilder response = new StringBuilder();
            final URL url = new URL("https://raw.githubusercontent.com/Bedrock-OSS/BDS-Versions/main/versions.json");
            final HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");

            final int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (final BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                }
                final JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();

                switch (this.system) {
                    case WINDOWS -> {
                        return jsonObject.getAsJsonObject("windows").get("stable").getAsString();
                    }
                    case LINUX -> {
                        return jsonObject.getAsJsonObject("linux").get("stable").getAsString();
                    }
                    default -> {
                        return "";
                    }
                }
            } else {
                this.logger.error("Błąd przy pobieraniu danych. Kod odpowiedzi: " + responseCode);
            }
        } catch (final Exception exception) {
            if (exception instanceof ConnectException) {
                return "";
            }
            this.logger.error("Błąd przy pobieraniu najnowszej wersji", exception);
        }
        return "";
    }

    public VersionUpdater getVersionUpdater() {
        return this.versionUpdater;
    }

    public List<String> getAvailableVersions() {
        return this.availableVersions;
    }

    public boolean hasVersion(final String version) {
        return this.availableVersions.contains(version);
    }
}
