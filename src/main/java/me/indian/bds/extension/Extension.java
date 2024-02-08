package me.indian.bds.extension;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import me.indian.bds.BDSAutoEnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public abstract class Extension {

    private ExtensionDescription extensionDescription;
    private String mainClass, version, name, author, description;
    private List<String> authors;
    private boolean enabled;
    private BDSAutoEnable bdsAutoEnable;
    private File dataFolder;

    public void onLoad() {

    }

    public void onEnable() {

    }

    public final boolean isEnabled() {
        return this.enabled;
    }

    public final void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public void onDisable() {

    }

    public final void init(final BDSAutoEnable bdsAutoEnable, final ExtensionDescription description, final ExtensionLoader loader) throws IOException {
        this.bdsAutoEnable = bdsAutoEnable;
        this.extensionDescription = description;
        this.mainClass = description.mainClass();
        this.version = description.version();
        this.name = description.name();
        this.author = description.author();
        this.description = description.description();
        this.authors = description.authors();

        final String extensionDir = loader.getExtensionsDir() + File.separator + this.name + File.separator;
        this.dataFolder = new File(extensionDir);

        Files.createDirectories(Paths.get(extensionDir));

    }

    public final <T extends OkaeriConfig> T createConfig(final Class<T> configClassType, final String configName) {
        return ConfigManager.create(configClassType, (it) -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile(this.getDataFolder() + File.separator + configName + ".yml");
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });
    }

    public final BDSAutoEnable getBdsAutoEnable() {
        return this.bdsAutoEnable;
    }

    public final ExtensionDescription getExtensionDescription() {
        return this.extensionDescription;
    }

    public String getVersion() {
        return this.version;
    }

    public final  String getName() {
        return this.name;
    }

    public final  String getAuthor() {
        return this.author;
    }

    public final  String getDescription() {
        return this.description;
    }

    public final  List<String> getAuthors() {
        return this.authors;
    }

    public final  File getDataFolder() {
        return this.dataFolder;
    }

    @Override
    public String toString() {
        return "Extension(name=" + this.name +
                ", mainClass= " + this.mainClass +
                ", version= " + this.version +
                ", description=" + this.description +
                ", author= " + this.author +
                ", authors= " + this.authors +
                ")";
    }
}