package me.indian.bds.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Header;
import me.indian.bds.util.SystemOs;


@Header("################################################################")
@Header("#                                                              #")
@Header("#    Huje22 Bds-Auto-Enable                                    #")
@Header("#  https://github.com/Huje22/Bds-Auto-Enable                   #")
@Header("#                                                              #")
@Header("################################################################")


public class Config extends OkaeriConfig {

    @Comment({" "})
    @Comment({"Pierwsze uruchomienie"})
    private boolean firstRun = false;


    @Comment({" "})
    @Comment({"System na którym uruchamiana jest aplikacja"})
    private SystemOs systemOs = SystemOs.LINUX;

    @Comment({" "})
    @Comment({"Nazwa pliku który ma być włączony"})
    private String name = "bedrock_server.exe";

    @Comment({" "})
    @Comment({"Czy użyć wine?"})
    private boolean wine = false;


    @Comment({" "})
    @Comment({"Ścieżka do pliku"})
    private String filePath = "./";


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

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean isWine() {
        return this.wine;
    }

    public void setWine(final boolean wine) {
        this.wine = wine;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(final String filePath) {
        this.filePath = filePath;
    }
}