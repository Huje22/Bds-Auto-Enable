package me.indian.bds.watchdog.module.pack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.pack.component.BehaviorPack;
import me.indian.bds.util.GsonUtil;
import me.indian.bds.util.HTTPUtil;
import me.indian.util.DateUtil;
import me.indian.util.ZipUtil;
import me.indian.util.logger.Logger;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.Nullable;

public class PackUpdater {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final PackModule packModule;
    private final File behaviorsFolder;
    private final OkHttpClient client;
    private Response response;

    public PackUpdater(final BDSAutoEnable bdsAutoEnable, final PackModule packModule) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = bdsAutoEnable.getLogger();
        this.packModule = packModule;
        this.behaviorsFolder = bdsAutoEnable.getPackManager().getBehaviorPackLoader().getBehaviorsFolder();
        this.client = HTTPUtil.getOkHttpClient();
        this.response = null;

        this.removeResponse();
    }

    public void downloadPack() {
        try {
            final long startTime = System.currentTimeMillis();
            this.logger.info("Pobieranie Paczki");
            final String zipPath = this.packModule.getPackFile().getPath() + ".zip";
            HTTPUtil.download("https://github.com/Huje22/BDS-Auto-Enable-Management-Pack/archive/main.zip", zipPath, this.bdsAutoEnable);
            this.logger.info("Pobrano w &a" + ((System.currentTimeMillis() - startTime) / 1000.0) + "&r sekund");
            ZipUtil.unzipFile(zipPath, this.behaviorsFolder.getPath(), true);
        } catch (final Exception exception) {
            this.logger.error("Nie można pobrać paczki ", exception);
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

    private int @Nullable [] getPackLatestVersion() {
        try (final Response response = this.getPackManifest()) {
            final JsonObject jsonObject = GsonUtil.getGson().fromJson(response.body().string(), JsonObject.class);

            final JsonArray versionArray = jsonObject.getAsJsonObject("header").getAsJsonArray("version");

            final int[] version = new int[versionArray.size()];
            for (int i = 0; i < versionArray.size(); i++) {
                version[i] = versionArray.get(i).getAsInt();
            }

            return version;
        } catch (final IOException exception) {
            this.logger.error("&cNie udało się pozyskać najnowszej wersji paczki!", exception);
            return null;
        }
    }

    public Response getPackManifest() throws IOException {
        if (this.response != null) return this.response;

        final Request request = new Request.Builder()
                .url("https://raw.githubusercontent.com/Huje22/BDS-Auto-Enable-Management-Pack/main/manifest.json")
                .get()
                .build();

        final Response newResponse = this.client.newCall(request).execute();

        if (newResponse.isSuccessful()) {
            this.response = newResponse;
        }

        return newResponse;
    }


    private void removeResponse() {
        final TimerTask removeResponseTask = new TimerTask() {
            @Override
            public void run() {
                if (PackUpdater.this.response != null) {
                    PackUpdater.this.logger.debug("Ustawiono " + PackUpdater.this.response + " na&b null&r aby następnym razem pozyskać nowe");
                    PackUpdater.this.response = null;
                }
            }
        };

        final long fiveMinutes = DateUtil.minutesTo(5, TimeUnit.MINUTES);

        new Timer("Pack Response Remover", true)
                .scheduleAtFixedRate(removeResponseTask, fiveMinutes, fiveMinutes);
    }
}