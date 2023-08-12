package me.indian.bds.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Header;
import me.indian.bds.util.SystemOs;

import java.util.Arrays;
import java.util.List;


@Header("################################################################")
@Header("#                                                              #")
@Header("#    Huje22 Bds-Auto-Enable                                    #")
@Header("#  https://github.com/Huje22/Bds-Auto-Enable                   #")
@Header("#                                                              #")
@Header("################################################################")


public class Config extends OkaeriConfig {

    @Comment({" "})
    @Comment({"Versia która jest załadowana"})
    private String version = "1.20.14.01";
    private boolean loaded = false;

    @Comment({" "})
    @Comment({"Pierwsze uruchomienie"})
    private boolean firstRun = true;

    @Comment({" "})
    @Comment({"System na którym uruchamiana jest aplikacja"})
    private SystemOs systemOs = SystemOs.LINUX;

    @Comment({" "})
    @Comment({"Nazwa pliku który ma być włączony"})
    private String fileName = "bedrock_server.exe";

    @Comment({" "})
    @Comment({"Czy użyć wine?"})
    private boolean wine = false;


    @Comment({" "})
    @Comment({"Ścieżka do z serverem"})
    private String filesPath = "./";


    @Comment({" "})
    @Comment({"Backups"})
    private boolean backup = true;
    private double lastBackupTime = 20;

    public double getLastBackupTime() {
        return this.lastBackupTime;
    }

    public void setLastBackupTime(final double lastBackupTime) {
        this.lastBackupTime = lastBackupTime;
    }

    @Comment({" "})
    @Comment({"Nie zapisuje tych informacj , w konsoli i pliku"})
    private List<String> noLogInfo = Arrays.asList("[Json]", "[Blocks]", "[Components]", "[Molang]", "[Item]", "[Recipes]", "[FeatureRegistry]", "[Actor]",
            "\"component_groups\"");

    @Comment({" "})
    @Comment({"Debug"})
    private boolean debug = true;

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

    public List<String> getNoLogInfo() {
        return this.noLogInfo;
    }

    public boolean isDebug() {
        return this.debug;
    }

    public boolean isBackup() {
        return this.backup;
    }

    public void setBackup(final boolean backup) {
        this.backup = backup;
    }

    public boolean isFirstRun() {
        return firstRun;
    }

    public void setFirstRun(final boolean firstRun) {
        this.firstRun = firstRun;
    }

    public SystemOs getSystemOs() {
        return this.systemOs;
    }

    public void setSystemOs(final SystemOs systemOs) {
        this.systemOs = systemOs;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public boolean isWine() {
        return this.wine;
    }

    public void setWine(final boolean wine) {
        this.wine = wine;
    }

    public String getFilesPath() {
        return this.filesPath;
    }

    public void setFilesPath(final String filesPath) {
        this.filesPath = filesPath;
    }
}