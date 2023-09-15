package me.indian.bds.config.sub.version;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class VersionManagerConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Versia która jest załadowana"})
    private String version = "1.20.15.01";

    @Comment({""})
    @Comment({"Nie zmieniaj tego!"})
    private boolean loaded = false;

    @Comment({""})
    @Comment({"Czy patrzec co dany czas czy dostepna jest nowa wersja"})
    private boolean checkVersion = true;

    @Comment({""})
    @Comment({"Czy po sprawdzeniu zaktualizować jeśli to możliwe?"})
    private boolean autoUpdate = false;

    @Comment({""})
    @Comment({"Co ile godzin sprawdzać aktualną wersję?"})
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

    public void setCheckVersion(final boolean checkVersion) {
        this.checkVersion = checkVersion;
    }

    public boolean isAutoUpdate() {
        return this.autoUpdate;
    }

    public void setAutoUpdate(final boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }

    public int getVersionCheckFrequency() {
        return this.versionCheckFrequency;
    }

    public void setVersionCheckFrequency(final int versionCheckFrequency) {
        this.versionCheckFrequency = versionCheckFrequency;
    }
}
