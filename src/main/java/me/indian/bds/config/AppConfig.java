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
    @Comment({"Czy tworzyć pliki z logami servera?"})
    @CustomKey("LogFile")
    private boolean logFile = true;

    @Comment({""})
    @Comment({"Ścieżka do plików z serverem"})
    @CustomKey("FilesPath")
    private String filesPath = "./";

    @Comment({""})
    @Comment({"Nazwy graczy którzy mogą wykonywać polecenia typu !format"})
    @CustomKey("Moderators")
    private List<String> moderators = List.of("JndjanBartonka");

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

    public boolean isLogFile() {
        return this.logFile;
    }

    public void setLogFile(final boolean logFile) {
        this.logFile = logFile;
    }

    public String getFilesPath() {
        return this.filesPath;
    }

    public void setFilesPath(final String filesPath) {
        this.filesPath = filesPath;
    }

    public List<String> getModerators() {
        return this.moderators;
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