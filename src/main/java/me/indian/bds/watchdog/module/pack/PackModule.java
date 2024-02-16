package me.indian.bds.watchdog.module.pack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.GsonUtil;
import me.indian.bds.util.ZipUtil;
import me.indian.bds.watchdog.WatchDog;
import me.indian.bds.watchdog.module.BackupModule;
import me.indian.bds.watchdog.module.pack.component.PackTemplate;
import org.jetbrains.annotations.Nullable;

public class PackModule {

    private final WatchDog watchDog;
    private final Logger logger;
    private final String packName;
    private File behaviorsFolder, packFile, worldBehaviorsJson;
    private List<PackTemplate> loadedBehaviorPacks;
    private PackTemplate mainPack;
    private boolean loaded, appHandledMessages;

    public PackModule(final BDSAutoEnable bdsAutoEnable, final WatchDog watchDog) {
        this.watchDog = watchDog;
        this.logger = bdsAutoEnable.getLogger();
        this.packName = "BDS-Auto-Enable-Managment-Pack";
    }

    public void initPackModule() {
        final BackupModule backupModule = this.watchDog.getBackupModule();
        this.behaviorsFolder = new File(backupModule.getWorldFile().getPath() + File.separator + "behavior_packs");
        this.packFile = new File(this.behaviorsFolder.getPath() + File.separator + "BDS-Auto-Enable-Managment-Pack");
        this.worldBehaviorsJson = new File(backupModule.getWorldFile().getPath() + File.separator + "world_behavior_packs.json");
        if (!this.behaviorsFolder.exists()) {
            if (!this.behaviorsFolder.mkdirs()) {
                this.logger.critical("Wystąpił poważny błąd przy tworzeniu&b world_behavior_packs.json");
                System.exit(0);
            }
        }

        if (!this.worldBehaviorsJson.exists()) {
            this.logger.error("Brak pliku&b world_behavior_packs.json&r utworzymy go dla ciebie!");
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

        this.loadedBehaviorPacks = this.loadPacks();
        this.getPackInfo();
    }

    public void getPackInfo() {
        if (!this.packExists()) {
            this.logger.error("Nie można odnaleźć paczki&b " + this.packName);
            this.downloadPack();
            return;
        }
        try {
            final JsonObject json = (JsonObject) JsonParser.parseReader(new FileReader(this.packFile.getPath() + File.separator + "manifest.json"));
            final JsonObject header = json.getAsJsonObject("header");
            if (!header.has("version")) {
                this.logger.error("Brak klucza 'version' w pliku JSON paczki.");
                this.loaded = false;
                return;
            }

            if (!header.has("uuid")) {
                this.logger.error("Brak klucza 'uuid' w pliku JSON paczki.");
                this.loaded = false;
                return;
            }

            final JsonArray versionArray = header.getAsJsonArray("version");
            final int[] version = new int[versionArray.size()];

            for (int i = 0; i < versionArray.size(); i++) {
                version[i] = versionArray.get(i).getAsInt();
            }

            this.mainPack = new PackTemplate(header.get("uuid").getAsString(), null, version);
            this.packsIsLoaded();

            if (!this.loaded) {
                this.logger.alert("Wykryliśmy paczke ale nie jest ona załadowana!");
                this.loadPack();
            }
            this.appHandledMessages = this.getAppHandledMessages();
        } catch (final Exception exception) {
            this.logger.critical("Nie udało się pozyskać informacji o paczce!", exception);
            System.exit(0);
        }
    }

    private void packsIsLoaded() {
        this.loaded = false;
        if (!this.packExists()) this.loaded = false;
        try {
            for (final PackTemplate pack : this.loadedBehaviorPacks) {
                if (pack == null) continue;
                if (pack.pack_id().equalsIgnoreCase(this.mainPack.pack_id()) && Arrays.toString(this.mainPack.version()).equals(Arrays.toString(pack.version()))) {
                    this.loaded = true;
                    return;
                }
            }

        } catch (final Exception exception) {
            this.logger.critical("Nie udało się zobaczyć czy paczka jest załadowana!", exception);
            System.exit(0);
        }
    }

    public void loadPack() {
        try {
            this.logger.info("Ładowanie paczki...");
            this.loadedBehaviorPacks.add(0, this.mainPack);
            this.savePacks(this.loadedBehaviorPacks);
            this.loaded = true;
            this.logger.info("&aZaładowano paczke");
        } catch (final Exception exception) {
            this.logger.critical("Nie udało się załadować paczki!", exception);
            System.exit(0);
        }
    }

    private boolean getAppHandledMessages() {
        final String filePath = this.packFile.getPath() + File.separator + "scripts" + File.separator + "index.js";

        try (final BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("const appHandledMessages")) {
                    return Boolean.parseBoolean(line.substring(line.indexOf("=") + 1, line.indexOf(";")).trim());
                }
            }
        } catch (final IOException exception) {
            this.logger.critical("Wystąpił błąd przy próbie odczytu wartości pliku&a JavaScript", exception);
        }
        return false;
    }

    private void makeItArray() {
        try (final FileWriter writer = new FileWriter(this.worldBehaviorsJson.getPath())) {
            writer.write("[]");
        } catch (final IOException exception) {
            this.logger.critical("Wystąpił krytyczny błąd z&b world_behavior_packs.json", exception);
            System.exit(0);
        }
    }

    private void downloadPack() {
        try {
            final long startTime = System.currentTimeMillis();
            final HttpURLConnection connection = (HttpURLConnection) new URL("https://raw.githubusercontent.com/Huje22/BDS-Auto-Enable-Managment-Pack/main/BDS-Auto-Enable-Managment-Pack.zip").openConnection();
            final int response = connection.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                this.logger.info("Pobieranie Paczki");
                final int fileSize = connection.getContentLength();

                try (final InputStream inputStream = new BufferedInputStream(connection.getInputStream())) {
                    try (final FileOutputStream outputStream = new FileOutputStream(this.packFile.getPath() + ".zip")) {

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
                    }
                }
                this.logger.info("Pobrano w &a" + ((System.currentTimeMillis() - startTime) / 1000.0) + "&r sekund");
                ZipUtil.unzipFile(this.packFile.getPath() + ".zip", this.behaviorsFolder.getPath(), true);
                this.getPackInfo();
            } else {
                this.logger.error("Kod odpowiedzi strony: " + response);
                System.exit(0);
            }
        } catch (final Exception ioException) {
            this.logger.error("Nie można pobrać paczki ", ioException);
        }
    }

    public boolean isAppHandledMessages() {
        return this.appHandledMessages;
    }

    public void setAppHandledMessages(final boolean handled) {
        if (!this.loaded) return;
        final String filePath = this.packFile.getPath() + File.separator + "scripts" + File.separator + "index.js";
        if (this.getAppHandledMessages() == handled) return;
        try {
            final StringBuilder content = new StringBuilder();
            try (final BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("const appHandledMessages")) {
                        line = "const appHandledMessages = " + handled + ";";
                    }
                    content.append(line).append("\n");
                }
            }

            if (!content.toString().contains("const appHandledMessages")) {
                content.append("const appHandledMessages = ").append(handled).append(";").append("\n");
            }

            try (final BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                writer.write(content.toString());
            }
            this.logger.info("Plik JavaScript został zaktualizowany.");
        } catch (final IOException exception) {
            this.logger.critical("Wystąpił błąd przy próbie edytowania wartości&a JavaScript", exception);
        }
        this.appHandledMessages = this.getAppHandledMessages();
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public String getPackName() {
        return this.packName;
    }

    @Nullable
    public PackTemplate getMainPack() {
        return this.mainPack;
    }

    public List<PackTemplate> getLoadedBehaviorPacks() {
        return this.loadedBehaviorPacks;
    }

    private List<PackTemplate> loadPacks() {
        try (final FileReader reader = new FileReader(this.worldBehaviorsJson)) {
            final Type token = new TypeToken<List<PackTemplate>>() {
            }.getType();

            return GsonUtil.getGson().fromJson(reader, token);
        } catch (final Exception exception) {
            throw new RuntimeException("Nie udało załadować się paczek", exception);
        }
    }

    private void savePacks(final List<PackTemplate> packs) throws IOException {
        final List<PackTemplate> nonNullPacks = new ArrayList<>();
        for (final PackTemplate pack : packs) {
            if (pack != null) {
                nonNullPacks.add(pack);
            }
        }

        try (final FileWriter writer = new FileWriter(this.worldBehaviorsJson)) {
            writer.write(GsonUtil.getGson().toJson(nonNullPacks));
        }
    }

    public boolean packExists() {
        return this.packFile.exists();
    }
}
