package me.indian.bds.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Header;


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
    @Comment({"Ścieżka do plików z serverem"})
    @CustomKey("FilesPath")
    private String filesPath = "./";

    @Comment({""})
    @Comment({"Czy zamknąć aplikacje gdy wystąpi niezłapany wyjątek?"})
    @CustomKey("CloseOnException")
    private boolean closeOnException = true;

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

    public String getFilesPath() {
        return this.filesPath;
    }

    public void setFilesPath(final String filesPath) {
        this.filesPath = filesPath;
    }

    public boolean isCloseOnException() {
        return this.closeOnException;
    }

    public void setCloseOnException(final boolean closeOnException) {
        this.closeOnException = closeOnException;
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
}