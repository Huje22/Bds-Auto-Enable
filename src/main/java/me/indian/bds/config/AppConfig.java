package me.indian.bds.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Header;
import java.util.List;


@Header("################################################################")
@Header("#                                                              #")
@Header("#    Huje22 Bds-Auto-Enable                                    #")
@Header("#  https://github.com/Huje22/Bds-Auto-Enable                   #")
@Header("#  https://bstats.org/plugin/bukkit/BDS-Auto-Enable/19727      #")
@Header("#                                                              #")
@Header("################################################################")

public class AppConfig extends OkaeriConfig {
    @Comment({""})
    @Comment({"Pierwsze uruchomienie"})
    @CustomKey("FirstRun")
    private boolean firstRun = true;

    @Comment({""})
    @Comment({"Jeśli ustawisz na false następnym razem aplikacja uruchomi się bez zadawania żadnych pytań"})
    @CustomKey("Questions")
    private boolean questions = true;

    @Comment({""})
    @Comment({"Czy użyć wine?"})
    @Comment({"Aby użyć wine trzeba mieć je pobrane!"})
    @CustomKey("Wine")
    private boolean wine = false;

    @Comment({""})
    @Comment({"Czy restartować server gdy TPS dwa razy pod rząd są mniejsze niż 9?"})
    @CustomKey("RestartOnLowTPS")
    private boolean restartOnLowTPS = true;

    @Comment({""})
    @Comment({"Ścieżka do plików z serverem"})
    @CustomKey("FilesPath")
    private String filesPath = "./";

    @Comment({""})
    @Comment({"Czy ładować automatycznie tekstury które nie są załadowane?"})
    @Comment({"UWAGA: Jest to w fazie beta"})
    @CustomKey("LoadTexturePacks")
    private boolean loadTexturePacks = true;

    @Comment({""})
    @Comment({"Czy ładować automatycznie paczki zachowań które nie są załadowane?"})
    @CustomKey("LoadBehaviorPacks")
    private boolean loadBehaviorPacks = true;

    @Comment({""})
    @Comment({"Czy zamknąć aplikacje jeśli wystąpi niezłapany wyjątek?"})
    @CustomKey("CloseOnException")
    private boolean closeOnException = true;

    @Comment({""})
    @Comment({"Nazwy graczy którzy mogą wykonywać polecenia typu !format"})
    @CustomKey("Moderators")
    private List<String> admins = List.of("JndjanBartonka");

    @Comment({""})
    @Comment({"UUID aplikacji"})
    @CustomKey("UUID")
    private String uuid = "";

    @Comment({""})
    @Comment({"Debug, dodatkowe wiadomości w konsoli dla developerów"})
    @CustomKey("Debug")
    private boolean debug = false;


    public boolean isFirstRun() {
        return this.firstRun;
    }

    public void setFirstRun(final boolean firstRun) {
        this.firstRun = firstRun;
    }

    public boolean isQuestions() {
        return this.questions;
    }

    public void setQuestions(final boolean questions) {
        this.questions = questions;
    }

    public boolean isWine() {
        return this.wine;
    }

    public void setWine(final boolean wine) {
        this.wine = wine;
    }

    public boolean isRestartOnLowTPS() {
        return this.restartOnLowTPS;
    }

    public boolean isCloseOnException() {
        return this.closeOnException;
    }

    public String getFilesPath() {
        return this.filesPath;
    }

    public boolean isLoadTexturePacks() {
        return this.loadTexturePacks;
    }

    public void setFilesPath(final String filesPath) {
        this.filesPath = filesPath;
    }

    public List<String> getAdmins() {
        return this.admins;
    }

    public void setAdmins(final List<String> admins) {
        this.admins = admins;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public boolean isDebug() {
        return this.debug;
    }

    public void setDebug(final boolean debug) {
        this.debug = debug;
    }
}