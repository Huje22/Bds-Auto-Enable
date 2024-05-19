package me.indian.bds.watchdog.module.pack;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.logger.Logger;
import me.indian.bds.pack.component.BehaviorPack;
import me.indian.bds.pack.loader.BehaviorPackLoader;
import me.indian.bds.pack.loader.ResourcePackLoader;
import org.jetbrains.annotations.Nullable;

public class PackModule {

    private final Logger logger;
    private final String packName;
    private final ResourcePackLoader resourcePackLoader;
    private final BehaviorPackLoader behaviorPackLoader;
    private final PackUpdater packUpdater;
    private final File packFile;
    private BehaviorPack mainPack;
    private boolean loaded, appHandledMessages;

    public PackModule(final BDSAutoEnable bdsAutoEnable) {
        this.logger = bdsAutoEnable.getLogger();
        this.packName = "BDS-Auto-Enable-Managment-Pack";
        this.resourcePackLoader = bdsAutoEnable.getPackManager().getResourcePackLoader();
        this.behaviorPackLoader = bdsAutoEnable.getPackManager().getBehaviorPackLoader();
        this.packUpdater = new PackUpdater(bdsAutoEnable, this);
        this.packFile = new File(this.behaviorPackLoader.getBehaviorsFolder() + File.separator + "BDS-Auto-Enable-Management-Pack-main");

        this.getPackInfo();

    }

    public void getPackInfo() {
        try {
            this.resourcePackLoader.findAllPacks();
            this.behaviorPackLoader.findAllPacks();


            if (!this.packExists()) {
                this.logger.error("Nie można odnaleźć paczki&b " + this.packName);
                this.packUpdater.downloadPack();
                this.getPackInfo();
                return;
            }

            this.mainPack = this.behaviorPackLoader.getPackFromFile(this.packFile);
            this.packUpdater.updatePack();

            if (!this.behaviorPackLoader.packIsLoaded(this.mainPack)) {
                this.behaviorPackLoader.loadPack(this.mainPack, 0);
            } else {
                this.behaviorPackLoader.setPackIndex(this.mainPack, 0);
            }

            this.loaded = true;
            this.appHandledMessages = this.getAppHandledMessages();
        } catch (final Exception exception) {
            this.logger.critical("Nie udało się pozyskać informacji o paczce!", exception);
            System.exit(5);
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

    public void setLoaded(final boolean loaded) {
        this.loaded = loaded;
    }

    public String getPackName() {
        return this.packName;
    }

    @Nullable
    public BehaviorPack getMainPack() {
        return this.mainPack;
    }

    public boolean packExists() {
        return this.packFile.exists();
    }

    public File getPackFile() {
        return this.packFile;
    }
}
