package me.indian.bds.pack.loader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.logger.Logger;
import me.indian.bds.pack.component.BehaviorPack;
import me.indian.bds.util.DefaultsVariables;
import me.indian.bds.util.FileUtil;
import me.indian.bds.util.GsonUtil;
import me.indian.bds.util.ZipUtil;
import org.jetbrains.annotations.Nullable;

public class BehaviorPackLoader {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private File behaviorsFolder, worldBehaviorsJson;
    private LinkedList<BehaviorPack> loadedBehaviorPacks;

    public BehaviorPackLoader(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.init();
        this.findAllPacks();
    }

    public void init() {
        final String worldPath = DefaultsVariables.getWorldsPath() + this.bdsAutoEnable.getServerProperties().getWorldName();
        this.behaviorsFolder = new File(worldPath + File.separator + "behavior_packs");
        this.worldBehaviorsJson = new File(worldPath + File.separator + "world_behavior_packs.json");
        if (!this.behaviorsFolder.exists()) {
            if (!this.behaviorsFolder.mkdirs()) {
                this.logger.critical("Wystąpił poważny błąd przy tworzeniu&b world_behavior_packs.json");
                System.exit(-1);
            }
        }

        if (!this.worldBehaviorsJson.exists()) {
            this.logger.error("Brak pliku&b world_behavior_packs.json&r utworzymy go dla ciebie!");
            try {
                if (!this.worldBehaviorsJson.createNewFile()) {
                    this.logger.critical("Nie udało się utworzyć pliku&b world_behavior_packs.json");
                    System.exit(-1);
                } else {
                    this.makeItArray();
                }
            } catch (final Exception exception) {
                this.logger.critical("Nie udało się utworzyć pliku&b world_behavior_packs.json");
                throw new RuntimeException(exception);
            }
        }

        this.loadedBehaviorPacks = this.loadExistingPacks();
    }

    @Nullable
    public BehaviorPack getPackFromFile(final File packFolder) throws FileNotFoundException {
        final JsonObject json = (JsonObject) JsonParser.parseReader(new FileReader(packFolder + File.separator + "manifest.json"));
        final JsonObject header = json.getAsJsonObject("header");

        if (!header.has("name")) {
            this.logger.error("Brak klucza&b name&r w pliku JSON paczki. (&d" + packFolder.getName() + "&r)");
            return null;
        }

        if (!header.has("version")) {
            this.logger.error("Brak klucza&b version&r w pliku JSON paczki. (&d" + packFolder.getName() + "&r)");
            return null;
        }

        if (!header.has("uuid")) {
            this.logger.error("Brak klucza&b uuid&r w pliku JSON paczki. (&d" + packFolder.getName() + "&r)");
            return null;
        }

        String subpack = null;

        if (json.has("subpacks")) {
            final JsonElement firstSubpackElement = json.getAsJsonArray("subpacks").get(0);

            if (firstSubpackElement.isJsonObject()) {
                subpack = firstSubpackElement.getAsJsonObject().get("folder_name").getAsString();
            }
        }

        final JsonArray versionArray = header.getAsJsonArray("version");
        final int[] version = new int[versionArray.size()];

        for (int i = 0; i < versionArray.size(); i++) {
            version[i] = versionArray.get(i).getAsInt();
        }

        return new BehaviorPack(header.get("name").getAsString(), header.get("uuid").getAsString(), subpack, version);
    }

    public void findAllPacks() {
        if (!this.bdsAutoEnable.getAppConfigManager().getAppConfig().isLoadBehaviorPacks()) {
            this.logger.debug("Automatyczne ładowanie paczek zachowań jest&c wyłączone");
            return;
        }

        try {
            File[] packs = new File(this.behaviorsFolder.getPath()).listFiles(file -> file.getName().endsWith(".zip") || file.getName().endsWith(".mcpack"));

            if (packs == null) {
                this.logger.info("Nie wykryto behaviorów do załadowania");
                return;
            }

            for (final File file : packs) {
                try {
                    ZipUtil.unzipFile(file.getPath(), this.behaviorsFolder.getPath(), true);
                    this.logger.info("Odpakowano folder&b " + file.getName() + "&r z paczką&d zachowań");
                } catch (final Exception exception) {
                    this.logger.info("&cNie udało się odpakować folderu&b " + file.getName() + "&r z paczką zachować", exception);
                }
            }

            packs = new File(this.behaviorsFolder.getPath()).listFiles(File::isDirectory);

            if (packs == null) {
                this.logger.info("Nie wykryto behaviorów do załadowania");
                return;
            }

            final LinkedList<BehaviorPack> cachedPacks = new LinkedList<>(this.loadedBehaviorPacks);

            for (final File file : packs) {
                try {
                    final BehaviorPack packFromFile = this.getPackFromFile(file);
                    if (packFromFile != null && !this.packIsLoaded(packFromFile)) {
                        this.loadPack(packFromFile);
                    }
                } catch (final Exception exception) {
                    this.logger.error("&cNie udało załadować się paczki z pliku&b " + file.getName(), exception);
                }
            }

            for (final BehaviorPack behaviorPack : cachedPacks) {
                try {
                    if (behaviorPack == null) continue;

                    if (!this.packIsLoaded(behaviorPack)) {
                        this.loadPack(behaviorPack);
                    } else {
                        this.setPackIndex(behaviorPack, this.getPackIndex(behaviorPack));
                    }
                } catch (final Exception exception) {
                    this.logger.error("&cNie udało załadować się paczki&b " + behaviorPack.name(), exception);
                }
            }

            this.savePacks();
        } catch (final Exception exception) {
            this.logger.error("&cNie udało się przeprowadzić ładowania paczek zachowań", exception);
        }
    }

    public void loadPack(final BehaviorPack behaviorPack) {
        if (this.packIsLoaded(behaviorPack)) return;
        this.loadedBehaviorPacks.add(behaviorPack);
        this.savePacks();
        this.logger.info("&aZaładowano &dzachowań&b " + behaviorPack.name() + "&a w wersji&1 " + Arrays.toString(behaviorPack.version()));
    }

    public void loadPack(final BehaviorPack behaviorPack, final int index) {
        if (this.packIsLoaded(behaviorPack)) return;
        this.loadedBehaviorPacks.add(index, behaviorPack);
        this.savePacks();
        this.logger.info("&aZaładowano paczke&d zachowań&b " + behaviorPack.name() + "&a w wersji&1 " + Arrays.toString(behaviorPack.version()));
    }

    public boolean packIsLoaded(final BehaviorPack behaviorPack) {
        return this.loadedBehaviorPacks.stream()
                .anyMatch(behaviro -> behaviro.pack_id().equals(behaviorPack.pack_id()) &&
                        Arrays.toString(behaviro.version()).equals(Arrays.toString(behaviorPack.version())));
    }

    public int getPackIndex(final BehaviorPack behaviorPack) {
        return this.loadedBehaviorPacks.indexOf(behaviorPack);
    }

    public void setPackIndex(final BehaviorPack behaviorPack, int index) {
        final int size = this.loadedBehaviorPacks.size();

        if (index >= size) index = size - 1;

        this.loadedBehaviorPacks.set(index, behaviorPack);
        this.savePacks();
    }

    public File getBehaviorsFolder() {
        return this.behaviorsFolder;
    }

    public LinkedList<BehaviorPack> getLoadedBehaviorPacks() {
        return this.loadedBehaviorPacks;
    }

    private LinkedList<BehaviorPack> loadExistingPacks() {
        try (final FileReader reader = new FileReader(this.worldBehaviorsJson)) {
            final Type token = new TypeToken<LinkedList<BehaviorPack>>() {
            }.getType();

            final LinkedList<BehaviorPack> behaviorPackList = GsonUtil.getGson().fromJson(reader, token);

            return (behaviorPackList == null ? new LinkedList<>() : behaviorPackList);
        } catch (final Exception exception) {
            return new LinkedList<>();
        }
    }

    private void savePacks() {
        final LinkedList<BehaviorPack> nonNullPacks = new LinkedList<>();
        for (final BehaviorPack pack : this.loadedBehaviorPacks) {
            if (pack != null) {
                nonNullPacks.add(pack);
            }
        }

        try (final FileWriter writer = new FileWriter(this.worldBehaviorsJson)) {
            writer.write(GsonUtil.getGson().toJson(nonNullPacks));
        } catch (final IOException ioException) {
            this.logger.error("&cNie udało się zapisać&b paczek zachowań", ioException);
        }

        this.loadedBehaviorPacks = nonNullPacks;
    }

//    private void savePacks(final List<BehaviorPack> packs) {
//        final LinkedList<BehaviorPack> nonNullPacks = new LinkedList<>();
//        for (final BehaviorPack pack : packs) {
//            if (pack != null) {
//                nonNullPacks.add(pack);
//            }
//        }
//
//        try (final FileWriter writer = new FileWriter(this.worldBehaviorsJson)) {
//            writer.write(GsonUtil.getGson().toJson(nonNullPacks));
//        } catch (final IOException ioException) {
//            this.logger.error("&cNie udało się zapisać&b paczek zachowań", ioException);
//        }
//
//        this.loadedBehaviorPacks = nonNullPacks;
//    }

    private void makeItArray() {
        try {
            FileUtil.writeText(this.worldBehaviorsJson, List.of("[]"));
        } catch (final IOException exception) {
            this.logger.critical("Wystąpił krytyczny błąd z&b world_behavior_packs.json", exception);
            System.exit(5);
        }
    }
}