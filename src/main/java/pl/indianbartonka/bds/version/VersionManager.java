package pl.indianbartonka.bds.version;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.config.AppConfig;
import pl.indianbartonka.bds.config.AppConfigManager;
import pl.indianbartonka.bds.config.sub.version.VersionManagerConfig;
import pl.indianbartonka.bds.exception.DownloadException;
import pl.indianbartonka.bds.server.ServerProcess;
import pl.indianbartonka.bds.server.properties.ServerProperties;
import pl.indianbartonka.bds.util.DefaultsVariables;
import pl.indianbartonka.bds.util.HTTPUtil;
import pl.indianbartonka.util.BedrockQuery;
import pl.indianbartonka.util.ThreadUtil;
import pl.indianbartonka.util.ZipUtil;
import pl.indianbartonka.util.http.UserAgent;
import pl.indianbartonka.util.logger.Logger;
import pl.indianbartonka.util.system.SystemOS;
import pl.indianbartonka.util.system.SystemUtil;

public class VersionManager {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final AppConfigManager appConfigManager;
    private final AppConfig appConfig;
    private final VersionManagerConfig versionManagerConfig;
    private final List<String> importantFiles;
    private final File versionFolder;
    private final List<String> availableVersions;
    private final ServerProcess serverProcess;
    private final VersionUpdater versionUpdater;
    private final SystemOS system;
    private final ServerProperties serverProperties;
    private final VersionDownloadListener downloadListener;
    private boolean waitingForProtocolInfo;
    private int lastKnownProtocol;

    public VersionManager(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.appConfigManager = this.bdsAutoEnable.getAppConfigManager();
        this.appConfig = this.appConfigManager.getAppConfig();
        this.versionManagerConfig = this.appConfigManager.getVersionManagerConfig();
        this.importantFiles = new ArrayList<>();
        this.versionFolder = new File(DefaultsVariables.getAppDir() + "versions");
        this.availableVersions = new ArrayList<>();
        this.serverProcess = bdsAutoEnable.getServerProcess();
        this.versionUpdater = new VersionUpdater(bdsAutoEnable, this);
        this.system = SystemUtil.getSystem();
        this.serverProperties = this.bdsAutoEnable.getServerProperties();
        this.downloadListener = new VersionDownloadListener(this.bdsAutoEnable);
        this.waitingForProtocolInfo = false;
        this.lastKnownProtocol = 0;

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
                    if (name.endsWith(".zip")) this.availableVersions.add(name.replaceAll(".zip", ""));
                }
            }
        } catch (final IOException exception) {
            this.logger.error("Nie można załadować pobranych wersji", exception);
        }
    }

    public void loadVersion(final String version) {
        if (this.serverProcess.isEnabled()) {
            this.logger.alert("Nie można załadować innej wersji gdy server jest aktywny!!");
            return;
        }
        final File verFile = new File(this.versionFolder.getPath() + File.separator + version + ".zip");
        if (!verFile.exists()) {
            this.logger.info("Nie znaleziono wersji:&1 " + version);
            this.downloadServerFiles(version);
        }

        if (!this.hasVersion(version)) {
            this.downloadServerFiles(version);
        }

        try {
            this.logger.info("Ładowanie wersji:&1 " + version);
            if (!this.hasVersion(version)) {
                this.logger.error("&cNie znaleziono wersji&b" + version + "&c bądź jej wielkość się nie zgadza!");
                this.downloadServerFiles(version);
            }

            final long startTime = System.currentTimeMillis();

            ZipUtil.unzipFile(verFile.getAbsolutePath(), this.appConfig.getFilesPath(), false, this.importantFiles);
            this.setLoaded(true);
            this.loadVersionsInfo();
            this.versionManagerConfig.setVersion(version);
            this.appConfigManager.saveVersionManagerConfig();
            this.logger.info("Załadowano wersie:&1 " + version + "&r w &a" + ((System.currentTimeMillis() - startTime) / 1000.0) + "&r sekund");
        } catch (final Exception exception) {
            this.logger.critical("Nie można załadować wersji:&1 " + version, exception);
            System.exit(21);
        }
    }

    public void loadVersion() {
        final File serverFile = new File(this.appConfig.getFilesPath() + File.separator + DefaultsVariables.getDefaultFileName());
        if (!serverFile.exists()) this.setLoaded(false);
        if (!this.versionManagerConfig.isLoaded()) this.loadVersion(this.versionManagerConfig.getVersion());

        this.bdsAutoEnable.getServerProperties().loadProperties();
    }

    public void downloadServerFiles(final String version) {
        try {
            this.downloadListener.setVersion(version);
            HTTPUtil.download(this.getServerDownloadUrl(version), this.versionFolder.getPath() + File.separator + version + ".zip", this.downloadListener);
            this.loadVersionsInfo();
        } catch (final IOException | DownloadException | TimeoutException exception) {
            this.logger.error("Wystąpił błąd podczas próby pobrania wersji " + version, exception);
        }
    }

    private long getSize(final String version) {
        final Request request = new Request.Builder()
                .url(this.getServerDownloadUrl(version))
                .get()
                .addHeader("User-Agent", UserAgent.randomUserAgent())
                .build();

        try (final Response response = HTTPUtil.OK_HTTP_CLIENT.newCall(request).execute()) {
            if (response.code() != HttpURLConnection.HTTP_OK) return -1;
            return response.body().contentLength();
        } catch (final IOException ioException) {
            this.logger.debug("Wystąpił błąd podczas próby pobrania wielkości wersji:&b " + version, ioException);
        }
        return -1;
    }

    private String getServerDownloadUrl(final String version) {
        switch (this.system) {
            //https://www.minecraft.net/bedrockdedicatedserver/bin-win/bedrock-server-1.21.31.04.zip
            //https://www.minecraft.net/bedrockdedicatedserver/bin-linux/bedrock-server-1.21.31.04.zip
            case LINUX -> {
                if (this.appConfig.isWine()) {
                    return "https://www.minecraft.net/bedrockdedicatedserver/bin-win/bedrock-server-" + version + ".zip";
                } else {
                    return "https://www.minecraft.net/bedrockdedicatedserver/bin-linux/bedrock-server-" + version + ".zip";
                }
            }
            case WINDOWS -> {
                return "https://www.minecraft.net/bedrockdedicatedserver/bin-win/bedrock-server-" + version + ".zip";
            }
            default -> throw new RuntimeException("Niewspierany system");
        }
    }

    public String getLatestVersion() {
        this.logger.debug("&aUzyskiwanie najnowszej wersji...");
        final Request request = new Request.Builder()
                .url("https://raw.githubusercontent.com/Bedrock-OSS/BDS-Versions/main/versions.json")
                .get()
                .addHeader("User-Agent", UserAgent.randomUserAgent())
                .build();

        try (final Response response = HTTPUtil.OK_HTTP_CLIENT.newCall(request).execute()) {
            final int responseCode = response.code();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                final JsonObject jsonObject = JsonParser.parseString(response.body().string()).getAsJsonObject();

                final String version = switch (this.system) {
                    case WINDOWS -> jsonObject.getAsJsonObject("windows").get("stable").getAsString();
                    case LINUX -> jsonObject.getAsJsonObject("linux").get("stable").getAsString();
                    default -> "";
                };

                this.logger.debug("&aNajnowsza znaleziona wersja to:&b " + version);

                return version;
            } else {
                this.logger.error("Błąd przy pobieraniu danych. Kod odpowiedzi: " + responseCode);
            }
        } catch (final Exception exception) {
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
        final File verFile = new File(this.versionFolder.getPath() + File.separator + version + ".zip");
        if (verFile.exists()) {
            try {
                final long versionSize = this.getSize(version);
                if (!(versionSize <= -1) && versionSize != Files.size(verFile.toPath())) {
                    return false;
                }
            } catch (final Exception ignored) {
            }
        }

        return this.availableVersions.contains(version);
    }

    public void setLoaded(final boolean loaded) {
        this.versionManagerConfig.setLoaded(loaded);
        this.appConfigManager.saveVersionManagerConfig();
    }

    public String getLoadedVersion() {
        return (this.versionManagerConfig.isLoaded() ? this.versionManagerConfig.getVersion() : "");
    }

    public void setLoadedVersion(final String version) {
        this.versionManagerConfig.setVersion(version);
        this.appConfigManager.saveVersionManagerConfig();
    }

    public int getLastKnownProtocol() {
        if (this.lastKnownProtocol == 0 || this.lastKnownProtocol == -1) {
            final int protocol = BedrockQuery.create("localhost", this.serverProperties.getServerPort()).protocol();
            if (protocol == -1) {
                this.waitForProtocol();
            } else {
                this.setLastKnownProtocol(protocol);
            }
        }

        return this.lastKnownProtocol;
    }

    private void setLastKnownProtocol(final int lastKnownProtocol) {
        this.lastKnownProtocol = lastKnownProtocol;
    }

    private void waitForProtocol() {
        if (this.waitingForProtocolInfo) return;
        this.waitingForProtocolInfo = true;

        new ThreadUtil("waiter").newThread(() -> {
            int protocol;
            while ((protocol = BedrockQuery.create("localhost", this.serverProperties.getServerPort()).protocol()) == -1) {
                ThreadUtil.sleep(10);
            }
            this.setLastKnownProtocol(protocol);
        }).start();
    }
}
