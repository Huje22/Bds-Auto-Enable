package me.indian.bds.watchdog.module;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
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

    private final WatchDog watchDog;
    private final Logger logger;
    private final String packName;
    private File behaviorsFolder, pack, worldBehaviorsJson;
    private String id;
    private int[] version;
    private boolean loaded, appHandledMessages;

    public PackModule(final BDSAutoEnable bdsAutoEnable, final WatchDog watchDog) {
        this.watchDog = watchDog;
        this.logger = bdsAutoEnable.getLogger();
        this.packName = "BDS-Auto-Enable-Managment-Pack";
    }

    public void initPackModule() {
        final BackupModule backupModule = this.watchDog.getBackupModule();
        this.behaviorsFolder = new File(backupModule.getWorldFile().getPath() + File.separator + "behavior_packs");
        this.pack = new File(this.behaviorsFolder.getPath() + File.separator + "BDS-Auto-Enable-Managment-Pack");
        this.worldBehaviorsJson = new File(backupModule.getWorldFile().getPath() + File.separator + "world_behavior_packs.json");
        if (!this.behaviorsFolder.exists()) {
            if (!this.behaviorsFolder.mkdirs()) {
                this.logger.critical("Wystąpił poważny błąd przy tworzeniu&b world_behavior_packs.json");
                System.exit(0);
            }
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

    public void getPackInfo() {
        if (!this.packExists()) {
            this.logger.error("Nie można odnaleźć paczki&b " + this.packName);
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
            this.logger.critical("Nie udało się zobaczyć czy paczka jest załadowana!", exception);
            System.exit(0);
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
        } catch (final Exception exception) {
            this.logger.critical("Nie udało się załadować paczki!", exception);
            System.exit(0);
        }
    }

    private boolean getAppHandledMessages() {
        final String filePath = this.pack.getPath() + File.separator + "scripts" + File.separator + "index.js";

        try (final BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("const appHandledMessages")) {
                    return Boolean.parseBoolean(line.substring(line.indexOf("=") + 1, line.indexOf(";")).trim());
                }
            }
        } catch (final IOException exception) {
            this.logger.critical("Wystąpił błąd przy próbie odczytu wartości", exception);
        }
        return false;
    }

    public void setAppHandledMessages(final boolean handled) {
        if (!this.loaded) return;
        final String filePath = this.pack.getPath() + File.separator + "scripts" + File.separator + "index.js";
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

            if(!content.toString().contains("const appHandledMessages")){
                content.append("const appHandledMessages = ").append(handled).append(";").append("\n");
            }

            try (final BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                writer.write(content.toString());
            }
            this.logger.info("Plik JavaScript został zaktualizowany.");
        } catch (final IOException exception) {
            this.logger.critical("Wystąpił błąd przy próbie edytowania wartości", exception);
        }
        this.appHandledMessages = this.getAppHandledMessages();
    }

    private void makeItArray() {
        try (final FileWriter writer = new FileWriter(this.worldBehaviorsJson.getPath())) {
            writer.write("[]");
        } catch (final IOException exception) {
            this.logger.critical("Wystąpił krytyczny błąd z&b world_behavior_packs.json", exception);
            System.exit(0);
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

                try (final InputStream inputStream = new BufferedInputStream(connection.getInputStream())) {
                    try (final FileOutputStream outputStream = new FileOutputStream(this.pack.getPath() + ".zip")) {

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
                ZipUtil.unzipFile(this.pack.getPath() + ".zip", this.behaviorsFolder.getPath(), true);
                this.getPackInfo();
            } else {
                this.logger.error("Kod odpowiedzi strony: " + response);
                System.exit(0);
            }
        } catch (final Exception ioException) {
            this.logger.error("Nie można pobrać paczki ", ioException);
        }
    }
    
    public boolean isAppHandledMessages(){
        return this.appHandledMessages;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public String getPackName() {
        return this.packName;
    }

    public int[] getVersion() {
        return this.version;
    }

    public String getPackVersion() {
        String ver = "";
        for (int i = 0; i < this.version.length; i++) {
            ver += this.version[i] + (i < this.version.length - 1 ? "." : "");
        }
        return ver;
    }

    public boolean packExists() {
        return this.pack.exists();
    }
}
