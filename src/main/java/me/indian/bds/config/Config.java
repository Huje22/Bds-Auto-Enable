package me.indian.bds.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Header;
import me.indian.bds.config.sub.AutoMessagesConfig;
import me.indian.bds.config.sub.discord.DiscordConfig;
import me.indian.bds.config.sub.log.LogConfig;
import me.indian.bds.config.sub.rest.RestApiConfig;
import me.indian.bds.config.sub.version.VersionManagerConfig;
import me.indian.bds.config.sub.watchdog.WatchDogConfig;


@Header("################################################################")
@Header("#                                                              #")
@Header("#    Huje22 Bds-Auto-Enable                                    #")
@Header("#  https://github.com/Huje22/Bds-Auto-Enable                   #")
@Header("#  https://bstats.org/plugin/bukkit/BDS-Auto-Enable/19727      #")
@Header("#                                                              #")
@Header("################################################################")

public class Config extends OkaeriConfig {
    @Comment({""})
    @Comment("UWAGA , WSZYSTKIE ZMIANY TUTAJ WYMAGAJĄ RESTARTU APLIKACJI")
    @Comment({""})
    @Comment({"Ustawienia menedżera wersji"})
    @CustomKey("VersionManager")
    private VersionManagerConfig versionManagerConfig = new VersionManagerConfig();

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
    @Comment({"Ustawienia strony Rest API"})
    @CustomKey("RestAPI")
    private RestApiConfig restApiConfig = new RestApiConfig();

    @Comment({""})
    @Comment({"Watchdog"})
    @CustomKey("WatchDog")
    private WatchDogConfig watchDogConfig = new WatchDogConfig();

    @Comment({""})
    @Comment({"Automessages"})
    @CustomKey("AutoMessages")
    private AutoMessagesConfig autoMessagesConfig = new AutoMessagesConfig();

    @Comment({""})
    @Comment({"Ustawienia logowania"})
    @Comment({"Nie zapisuje tych informacj które zawierają dane znaki i słowa"})
    @CustomKey("Log")
    private LogConfig logConfig = new LogConfig();

    @Comment({""})
    @Comment({"Ustawienia discord"})
    @CustomKey("Discord")
    private DiscordConfig discordConfig = new DiscordConfig();

    @Comment({""})
    @Comment({"UUID aplikacji"})
    @CustomKey("UUID")
    private String uuid = "";

    @Comment({""})
    @Comment({"Debug, dodatkowe wiadomości w konsoli dla developerów"})
    @CustomKey("Debug")
    private boolean debug = false;

    public VersionManagerConfig getVersionManagerConfig() {
        return this.versionManagerConfig;
    }

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

    public WatchDogConfig getWatchDogConfig() {
        return this.watchDogConfig;
    }

    public AutoMessagesConfig getAutoMessagesConfig() {
        return this.autoMessagesConfig;
    }

    public LogConfig getLogConfig() {
        return this.logConfig;
    }

    public DiscordConfig getDiscordConfig() {
        return this.discordConfig;
    }

    public RestApiConfig getRestApiConfig() {
        return this.restApiConfig;
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