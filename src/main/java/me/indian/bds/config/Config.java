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
    private String Version = "1.20.14.01";
    private boolean Loaded = false;

    @Comment({" "})
    @Comment({"Pierwsze uruchomienie"})
    private boolean FirstRun = true;

    @Comment({" "})
    @Comment({"System na którym uruchamiana jest aplikacja"})
    private SystemOs System = SystemOs.LINUX;

    @Comment({" "})
    @Comment({"Nazwa pliku który ma być włączony"})
    private String FileName = "bedrock_server.exe";

    @Comment({" "})
    @Comment({"Czy użyć wine?"})
    private boolean Wine = false;


    @Comment({" "})
    @Comment({"Ścieżka do z serverem"})
    private String FilesPath = "./";


    @Comment({" "})
    @Comment({"Backups"})
    private boolean Backup = true;
    private int BackupFrequency = 60;
    private double LastBackupTime = 20;

    @Comment({" "})
    @Comment({"Nie zapisuje tych informacj , w konsoli i pliku"})
    private List<String> noLogInfo = Arrays.asList("[Json]", "[Blocks]", "[Components]", "[Molang]", "[Item]", "[Recipes]", "[FeatureRegistry]", "[Actor]",
            "\"component_groups\"");

    @Comment({" "})
    @Comment({"Debug"})
    private boolean debug = true;


    public int getBackupFrequency() {
        return this.BackupFrequency;
    }

    public void setBackupFrequency(final int backupFrequency) {
        this.BackupFrequency = backupFrequency;
    }

    public double getLastBackupTime() {
        return this.LastBackupTime;
    }

    public void setLastBackupTime(final double lastBackupTime) {
        this.LastBackupTime = lastBackupTime;
    }


    public String getVersion() {
        return this.Version;
    }

    public void setVersion(final String version) {
        this.Version = version;
    }

    public boolean isLoaded() {
        return this.Loaded;
    }

    public void setLoaded(final boolean loaded) {
        this.Loaded = loaded;
    }

    public List<String> getNoLogInfo() {
        return this.noLogInfo;
    }

    public boolean isDebug() {
        return this.debug;
    }

    public boolean isBackup() {
        return this.Backup;
    }

    public void setBackup(final boolean backup) {
        this.Backup = backup;
    }

    public boolean isFirstRun() {
        return FirstRun;
    }

    public void setFirstRun(final boolean firstRun) {
        this.FirstRun = firstRun;
    }

    public SystemOs getSystemOs() {
        return this.System;
    }

    public void setSystemOs(final SystemOs systemOs) {
        this.System = systemOs;
    }

    public String getFileName() {
        return this.FileName;
    }

    public void setFileName(final String fileName) {
        this.FileName = fileName;
    }

    public boolean isWine() {
        return this.Wine;
    }

    public void setWine(final boolean wine) {
        this.Wine = wine;
    }

    public String getFilesPath() {
        return this.FilesPath;
    }

    public void setFilesPath(final String filesPath) {
        this.FilesPath = filesPath;
    }
}