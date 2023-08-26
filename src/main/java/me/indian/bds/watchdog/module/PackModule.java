package me.indian.bds.watchdog.module;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.GsonUtil;
import me.indian.bds.util.ZipUtil;
import me.indian.bds.watchdog.WatchDog;

public class PackModule {

    private final Logger logger;
    private final String packName;
    private File behaviorsFolder, pack, worldBehaviorsJson;
    private String id;
    private int[] version;
    private boolean loaded;

    public PackModule(final BDSAutoEnable bdsAutoEnable) {
        this.logger = bdsAutoEnable.getLogger();
        this.packName = "BDS-Auto-Enable-Managment-Pack";
    }

    public void initPackModule(final WatchDog watchDog) {
        final BackupModule backupModule = watchDog.getBackupModule();
        this.behaviorsFolder = new File(backupModule.getWorldFile().getPath() + File.separator + "behavior_packs");
        this.pack = new File(this.behaviorsFolder.getPath() + File.separator + "BDS-Auto-Enable-Managment-Pack");
        this.worldBehaviorsJson = new File(backupModule.getWorldFile().getPath() + File.separator + "world_behavior_packs.json");
        if (!this.behaviorsFolder.exists()) {
            this.behaviorsFolder.mkdirs();
        }

        if (!this.worldBehaviorsJson.exists()) {
            this.logger.critical("Brak pliku&b world_behavior_packs.json&r utworzymy go dla ciebie!");
            try {
                if (!this.worldBehaviorsJson.createNewFile()) {
                    this.logger.critical("Nie udało się utworzyć pliku&b world_behavior_packs.json");
                    System.exit(0);
                } else {
                    this.makeItArray();
                }
            } catch (final Exception exception) {
                throw new RuntimeException(exception);
            }
        }
        this.getPackInfo();
    }

    private void getPackInfo() {
        if (!this.packExists()) {
            this.logger.error("Nie można odnależć paczki&b " + this.packName);
            this.downloadPack();
            return;
        }
        try {
            final JsonObject json = (JsonObject) JsonParser.parseReader(new FileReader(this.pack.getPath() + File.separator + "manifest.json"));
            final JsonObject header = json.getAsJsonObject("header");
            if (!header.has("version")) {
                this.logger.error("Brak klucza 'version' w pliku JSON.");
                this.loaded = false;
                return;
            }

            if (!header.has("uuid")) {
                this.logger.error("Brak klucza 'uuid' w pliku JSON.");
                this.loaded = false;
                return;
            }

            this.id = header.get("uuid").getAsString();

            final JsonArray versionArray = header.getAsJsonArray("version");
            this.version = new int[versionArray.size()];

            for (int i = 0; i < versionArray.size(); i++) {
                this.version[i] = versionArray.get(i).getAsInt();
            }

            this.packsIsLoaded();

            if (!this.loaded) {
                this.logger.alert("Wykryliśmy paczke ale nie jest ona załadowana!");
                this.loadPack();
            }
        } catch (final Exception exception) {
            exception.printStackTrace();
        }
    }

    private void packsIsLoaded() {
        this.loaded = false;
        if (!this.packExists()) this.loaded = false;
        try {
            final JsonElement jsonElement = JsonParser.parseReader(new FileReader(this.worldBehaviorsJson.getPath()));
            if (jsonElement.isJsonArray()) {
                for (final JsonElement element : jsonElement.getAsJsonArray()) {
                    if (element.isJsonObject()) {
                        final JsonObject packObject = element.getAsJsonObject();

                        if (packObject.has("pack_id") && packObject.has("version")) {
                            final JsonArray packVersion = packObject.getAsJsonArray("version");
                            final JsonArray desiredVersion = this.getVersionAsJsonArray();

                            if (packObject.get("pack_id").getAsString().equals(this.id) && packVersion.equals(desiredVersion)) {
                                this.logger.info("Wykryto że paczka jest załadowana!");
                                this.loaded = true;
                                return;
                            }
                        }
                    }
                }
            }
        } catch (final Exception exception) {
            exception.printStackTrace();
        }
    }

    public void loadPack() {
        try (final FileReader reader = new FileReader(this.worldBehaviorsJson.getPath())) {
            this.logger.info("Ładowanie paczki...");
            final JsonArray jsonArray;
            try {
                jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
            } catch (final IllegalStateException exception) {
                this.logger.info("Wykryto błędną składnie pliku&b world_behavior_packs.json&r!");
                this.makeItArray();
                this.logger.info("Naprawiliśmy go dla ciebie!");
                this.loadPack();
                return;
            }

            final JsonObject newEntry = new JsonObject();
            newEntry.addProperty("pack_id", this.id);
            newEntry.add("version", this.getVersionAsJsonArray());

            jsonArray.add(newEntry);

            try (final FileWriter writer = new FileWriter(this.worldBehaviorsJson.getPath())) {
                GsonUtil.getGson().toJson(jsonArray, writer);
                this.logger.info("Załadowano paczke!");
                this.loaded = true;
            }
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }

    private void makeItArray() {
        try (final FileWriter writer = new FileWriter(this.worldBehaviorsJson.getPath())) {
            writer.write("[]");
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }

    private JsonArray getVersionAsJsonArray() {
        final JsonArray versionArray = new JsonArray();
        for (final int value : this.version) {
            versionArray.add(value);
        }
        return versionArray;
    }

    private void downloadPack() {
        try {
            final long startTime = System.currentTimeMillis();
            final HttpURLConnection connection = (HttpURLConnection) new URL("https://raw.githubusercontent.com/Huje22/BDS-Auto-Enable-Managment-Pack/main/BDS-Auto-Enable-Managment-Pack.zip").openConnection();
            final int response = connection.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                this.logger.info("Pobieranie Paczki");
                final int fileSize = connection.getContentLength();
                final InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                final FileOutputStream outputStream = new FileOutputStream(this.pack.getPath() + ".zip");

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
                            this.logger.error("Nie można odczytać prawidłowego rozmiaru pliku.");
                            continue;
                        }
                        tempProgres = progress;
                        this.logger.info("Pobrano w:&b " + progress + "&a%");
                    }
                }

                inputStream.close();
                outputStream.close();
                this.logger.info("Pobrano w &a" + ((System.currentTimeMillis() - startTime) / 1000.0) + "&r sekund");
                ZipUtil.unzipFile(this.pack.getPath() + ".zip", this.behaviorsFolder.getPath(), true);
                this.getPackInfo();
            } else {
                this.logger.error("Kod odpowiedzi strony: " + response);
            }
        } catch (final IOException ioException) {
            this.logger.error("Nie można pobrać paczki: " + ioException.getMessage());
            ioException.printStackTrace();
        }
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public String getPackName() {
        return this.packName;
    }

    public boolean packExists() {
        return this.pack.exists();
    }
}