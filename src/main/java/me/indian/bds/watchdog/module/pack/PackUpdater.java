package me.indian.bds.watchdog.module.pack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Arrays;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.logger.Logger;
import me.indian.bds.pack.component.BehaviorPack;
import me.indian.bds.pack.loader.BehaviorPackLoader;
import me.indian.bds.util.GsonUtil;
import me.indian.bds.util.HTTPUtil;
import me.indian.bds.util.ZipUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.Nullable;

public class PackUpdater {

    private final Logger logger;
    private final PackModule packModule;
    private final BehaviorPackLoader behaviorPackLoader;
    private final File behaviorsFolder;
    private final OkHttpClient client;

    public PackUpdater(final BDSAutoEnable bdsAutoEnable, final PackModule packModule) {
        this.logger = bdsAutoEnable.getLogger();
        this.packModule = packModule;
        this.behaviorPackLoader = bdsAutoEnable.getPackManager().getBehaviorPackLoader();
        this.behaviorsFolder = this.behaviorPackLoader.getBehaviorsFolder();
        this.client = HTTPUtil.getOkHttpClient();
    }

    public void downloadPack() {
        try {
            final long startTime = System.currentTimeMillis();
            final Request request = new Request.Builder()
                    .url("https://github.com/Huje22/BDS-Auto-Enable-Management-Pack/archive/main.zip")
                    .get()
                    .build();

            try (final Response response = HTTPUtil.getOkHttpClient().newCall(request).execute()) {
                final int responseCode = response.code();
                if (responseCode == HttpURLConnection.HTTP_OK) {

                    this.logger.info("Pobieranie Paczki");
                    final long fileSize = response.body().contentLength();
                    final String zipPatch = this.packModule.getPackFile().getPath() + ".zip";

                    try (final InputStream inputStream = new BufferedInputStream(response.body().byteStream())) {
                        try (final FileOutputStream outputStream = new FileOutputStream(zipPatch)) {

                            if (fileSize <= 0) {
                                this.logger.error("Nie można odczytać prawidłowego rozmiaru pliku.");
                            }

                            final byte[] buffer = new byte[1024];
                            int bytesRead;
                            long totalBytesRead = 0;

                            int tempProgres = -1;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                                totalBytesRead += bytesRead;
                                final int progress = Math.toIntExact((totalBytesRead * 100) / fileSize);

                                if (progress != tempProgres) {
                                    if (fileSize <= 0) {
                                        continue;
                                    }
                                    tempProgres = progress;
                                    this.logger.info("Pobrano w:&b " + progress + "&a%");
                                }
                            }
                        }
                    }
                    this.logger.info("Pobrano w &a" + ((System.currentTimeMillis() - startTime) / 1000.0) + "&r sekund");
                    ZipUtil.unzipFile(zipPatch, this.behaviorsFolder.getPath(), true);
                } else {
                    this.logger.error("Kod odpowiedzi strony:&b " + responseCode);
                    System.exit(responseCode);
                }
            }
        } catch (final Exception ioException) {
            this.logger.error("Nie można pobrać paczki ", ioException);
        }
    }

    public void updatePack() {
        final BehaviorPack mainPack = this.packModule.getMainPack();
        if (mainPack == null) {
            this.logger.error("&cNie udało się pozyskać informacji na temat paczki");
            return;
        }

        this.logger.info("&aSprawdzanie najnowszej wersji paczki...");
        if (Arrays.toString(this.getPackLatestVersion()).equals(Arrays.toString(mainPack.version()))) {
            this.logger.info("&aPosiadasz najnowszą wersje paczki");
        } else {
            final File packFile = this.packModule.getPackFile();
            if (packFile.exists()) {
                if (packFile.delete()) {
                    this.logger.info("Usunięto starą wersje paczki");
                }
            }

            this.downloadPack();
            this.packModule.getPackInfo();
        }
    }

    @Nullable
    private int[] getPackLatestVersion() {
        try (final Response response = this.getPackManifest()) {
            final JsonObject jsonObject = GsonUtil.getGson().fromJson(response.body().string(), JsonObject.class);

            final JsonArray versionArray = jsonObject.getAsJsonObject("header").getAsJsonArray("version");

            final int[] version = new int[versionArray.size()];
            for (int i = 0; i < versionArray.size(); i++) {
                version[i] = versionArray.get(i).getAsInt();
            }

            return version;
        } catch (final IOException exception) {
            this.logger.error("Nie udało się pozyskać najnowszej wersji paczki!", exception);
            return null;
        }
    }


    private Response getPackManifest() throws IOException {
        final Request request = new Request.Builder()
                .url("https://raw.githubusercontent.com/Huje22/BDS-Auto-Enable-Management-Pack/main/manifest.json")
                .get()
                .build();

        return this.client.newCall(request).execute();
    }
}