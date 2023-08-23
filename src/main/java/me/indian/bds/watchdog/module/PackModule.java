package me.indian.bds.watchdog.module;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.logger.Logger;
import me.indian.bds.watchdog.WatchDog;

public class PackModule {

    private final Logger logger;
    private final Gson gson;
    private final String packName;
    private File pack, worldBehaviors;
    private String id;
    private int[] version;
    private boolean loaded;


    public PackModule(final BDSAutoEnable bdsAutoEnable) {
        this.logger = bdsAutoEnable.getLogger();
        this.gson = new Gson();
        this.packName = "BDS-Auto-Enable-Managment-Pack";
    }

    public void initPackModule(final WatchDog watchDog) {
        final BackupModule backupModule = watchDog.getBackupModule();
        this.pack = new File(backupModule.getWorldFile().getPath() + File.separator + "behavior_packs" + File.separator + "BDS-Auto-Enable-Managment-Pack");
        this.worldBehaviors = new File(backupModule.getWorldFile().getPath() + File.separator + "world_behavior_packs.json");
        if (!this.worldBehaviors.exists()) {
            this.logger.critical("Brak pliku&b world_behavior_packs.json&r utworzymy go dla ciebie!");
            try {
                if (!this.worldBehaviors.createNewFile()) {
                    this.logger.critical("Nie udało się utworzyć pliku&b world_behavior_packs.json");
                    System.exit(0);
                }
            } catch (final Exception exception) {
                throw new RuntimeException(exception);
            }
        }
        this.getPackInfo();
    }


    private void getPackInfo() {
        if (!this.packExists()) {
            this.logger.error("Nie można odnależć paczki&a " + this.packName);
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
            final JsonElement jsonElement = JsonParser.parseReader(new FileReader(this.worldBehaviors.getPath()));
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
        try (final FileReader reader = new FileReader(this.worldBehaviors.getPath())) {
            this.logger.info("Ładowanie paczki...");
            JsonArray jsonArray;
            try {
                jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
            } catch (final IllegalStateException exception) {
                this.logger.info("Wykryto błędną składnie pliku&b bworld_behavior_packs.json&r!");
                this.makeItArray();
                loadPack();
                return;
            }

            final JsonObject newEntry = new JsonObject();
            newEntry.addProperty("pack_id", this.id);
            newEntry.add("version", this.getVersionAsJsonArray());

            jsonArray.add(newEntry);

            try (final FileWriter writer = new FileWriter(this.worldBehaviors.getPath())) {
                this.gson.toJson(jsonArray, writer);
                this.logger.info("Załadowano paczke!");
            }
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }

    private void makeItArray() {
        try (final FileWriter writer = new FileWriter(this.worldBehaviors.getPath())) {
            writer.write("[]");
            this.logger.info("Naprawiliśmy go dla ciebie!");
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