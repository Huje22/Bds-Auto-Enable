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
import me.indian.bds.pack.component.TexturePack;
import me.indian.bds.util.DefaultsVariables;
import me.indian.bds.util.GsonUtil;
import me.indian.bds.util.ZipUtil;
import org.jetbrains.annotations.Nullable;

public class ResourcePackLoader {
    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private File resourcesFolder, worldResourcePackJson;
    private LinkedList<TexturePack> loadedTexturePacks;

    public ResourcePackLoader(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.init();
        this.findAllPacks();
    }

    public void init() {
        final String worldPath = DefaultsVariables.getWorldsPath() + this.bdsAutoEnable.getServerProperties().getWorldName();
        this.resourcesFolder = new File(worldPath + File.separator + "resource_packs");
        this.worldResourcePackJson = new File(worldPath + File.separator + "world_resource_packs.json");
        if (!this.resourcesFolder.exists()) {
            if (!this.resourcesFolder.mkdirs()) {
                this.logger.critical("Wystąpił poważny błąd przy tworzeniu&b world_resource_packs.json");
                System.exit(-1);
            }
        }

        if (!this.worldResourcePackJson.exists()) {
            this.logger.error("Brak pliku&b world_resource_packs.json&r utworzymy go dla ciebie!");
            try {
                if (!this.worldResourcePackJson.createNewFile()) {
                    this.logger.critical("Nie udało się utworzyć pliku&b world_resource_packs.json");
                    System.exit(-1);
                } else {
                    this.makeItArray();
                }
            } catch (final Exception exception) {
                throw new RuntimeException(exception);
            }
        }

        this.loadedTexturePacks = this.loadExistingPacks();
    }

    @Nullable
    public TexturePack getPackFromFile(final File packFolder) throws FileNotFoundException {
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

        return new TexturePack(header.get("name").getAsString(), header.get("uuid").getAsString(), subpack, version);
    }

    public void findAllPacks() {
        try {
            File[] packs = new File(this.resourcesFolder.getPath()).listFiles();

            if (packs == null) {
                this.logger.info("Nie wykryto behaviorów do załadowania");
                return;
            }

            for (final File file : packs) {
                if (file.getName().endsWith(".zip")) {
                    ZipUtil.unzipFile(file.getPath(), this.resourcesFolder.getPath(), true);
                    this.logger.info("Odpakowano folder&b " + file.getName() + " z paczką&d tekstur");
                }
            }

            packs = new File(this.resourcesFolder.getPath()).listFiles();

            if (packs == null) {
                this.logger.info("Nie wykryto behaviorów do załadowania");
                return;
            }

            final LinkedList<TexturePack> texturePacks = new LinkedList<>();
            for (final File file : packs) {
                try {
                    final TexturePack packFromFile = this.getPackFromFile(file);
                    if (packFromFile != null && !this.packIsLoaded(packFromFile)) {
                        this.loadPack(packFromFile);
                    }
                    texturePacks.add(packFromFile);
                } catch (final Exception exception) {
                    this.logger.error("&cNie udało załadować się paczki z pliku&b " + file.getName(), exception);
                }
            }

            for (final TexturePack texturePack : this.loadedTexturePacks) {
                try {
                    if (texturePack != null && !this.packIsLoaded(texturePack)) {
                        this.loadPack(texturePack, this.getPackIndex(texturePack));
                    } else {
                        texturePacks.set(this.getPackIndex(texturePack), texturePack);
                    }
                } catch (final Exception exception) {
                    this.logger.error("&cNie udało załadować się paczki&b " + texturePack.name(), exception);
                }
            }

            this.savePacks(texturePacks);
        } catch (final Exception exception) {
            this.logger.error("&cNie udało się przeprowadzić ładowania paczek zachowań");
        }
    }

    public void loadPack(final TexturePack behaviorPack) {
        this.loadedTexturePacks.add(behaviorPack);
        this.savePacks(this.loadedTexturePacks);
        this.logger.info("&aZaładowano paczke&d tesktur&b " + behaviorPack.name() + "&a w wersji&1 " + Arrays.toString(behaviorPack.version()));
    }

    public void loadPack(final TexturePack behaviorPack, final int index) {
        this.loadedTexturePacks.add(index, behaviorPack);
        this.savePacks(this.loadedTexturePacks);
        this.logger.info("&aZaładowano paczke&d tesktur&b " + behaviorPack.name() + "&a w wersji&1 " + Arrays.toString(behaviorPack.version()));
    }

    public boolean packIsLoaded(final TexturePack behaviorPack) {
        return this.loadedTexturePacks.stream()
                .anyMatch(texture -> texture.pack_id().equals(behaviorPack.pack_id()) &&
                        Arrays.toString(texture.version()).equals(Arrays.toString(behaviorPack.version())));
    }

    public int getPackIndex(final TexturePack texturePack) {
        return this.loadedTexturePacks.indexOf(texturePack);
    }

    public File getResourcesFolder() {
        return this.resourcesFolder;
    }

    public List<TexturePack> getLoadedTexturePacks() {
        return this.loadedTexturePacks;
    }

    private LinkedList<TexturePack> loadExistingPacks() {
        try (final FileReader reader = new FileReader(this.worldResourcePackJson)) {
            final Type token = new TypeToken<LinkedList<TexturePack>>() {
            }.getType();

            final LinkedList<TexturePack> texturePacksList = GsonUtil.getGson().fromJson(reader, token);

            return (texturePacksList == null ? new LinkedList<>() : texturePacksList);
        } catch (final Exception exception) {
            return new LinkedList<>();
        }
    }

    private void savePacks(final LinkedList<TexturePack> packs) {
        final LinkedList<TexturePack> nonNullPacks = new LinkedList<>();
        for (final TexturePack pack : packs) {
            if (pack != null) {
                nonNullPacks.add(pack);
            }
        }

        try (final FileWriter writer = new FileWriter(this.worldResourcePackJson)) {
            writer.write(GsonUtil.getGson().toJson(nonNullPacks));
        } catch (final IOException ioException) {
            this.logger.error("&cNie udało się zapisać&b paczek zachowań", ioException);
        }

        this.loadedTexturePacks = nonNullPacks;
    }

    private void makeItArray() {
        try (final FileWriter writer = new FileWriter(this.worldResourcePackJson.getPath())) {
            writer.write("[]");
        } catch (final IOException exception) {
            this.logger.critical("Wystąpił krytyczny błąd z&b world_resource_packs.json", exception);
            System.exit(5);
        }
    }
}