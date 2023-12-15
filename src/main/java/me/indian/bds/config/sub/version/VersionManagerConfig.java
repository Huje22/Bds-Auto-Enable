package me.indian.bds.config.sub.version;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Header;

@Header("################################################################")
@Header("#           Ustawienia Managera Wersji                         #")
@Header("################################################################")

public class VersionManagerConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Wersja która jest załadowana"})
    @CustomKey("Version")
    private String version = "1.20.51.01";

    @Comment({""})
    @Comment({"Nie zmieniaj tego!"})
    @CustomKey("Loaded")
    private boolean loaded = false;

    @Comment({""})
    @Comment({"Czy patrzeć co dany czas czy dostępna jest nowa wersja"})
    @CustomKey("CheckVersion")
    private boolean checkVersion = true;

    @Comment({""})
    @Comment({"Czy po sprawdzeniu zaktualizować jeśli to możliwe?"})
    @CustomKey("AutoUpdate")
    private boolean autoUpdate = false;

    @Comment({""})
    @Comment({"Co ile godzin sprawdzać aktualną wersję?"})
    @CustomKey("VersionCheckFrequency")
    private int versionCheckFrequency = 1;


    public String getVersion() {
        return this.version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public void setLoaded(final boolean loaded) {
        this.loaded = loaded;
    }

    public boolean isCheckVersion() {
        return this.checkVersion;
    }

    public boolean isAutoUpdate() {
        return this.autoUpdate;
    }

    public int getVersionCheckFrequency() {
        return this.versionCheckFrequency;
    }
}