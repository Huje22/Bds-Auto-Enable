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
import me.indian.bds.util.SystemOs;


@Header("################################################################")
@Header("#                                                              #")
@Header("#    Huje22 Bds-Auto-Enable                                    #")
@Header("#  https://github.com/Huje22/Bds-Auto-Enable                   #")
@Header("#  https://bstats.org/plugin/bukkit/BDS-Auto-Enable/19727      #")
@Header("#                                                              #")
@Header("################################################################")

public class Config extends OkaeriConfig {
    @Comment({""})
    @Comment("UWAGA , WSZYTKIE ZMIANY TUTAJ WYMAGAJĄ RESTARTU APLIKACJI")
    @Comment({""})
    @Comment({"Ustawienia menagera wersji"})
    @CustomKey("VersionManager")
    private VersionManagerConfig versionManagerConfig = new VersionManagerConfig();

    @Comment({""})
    @Comment({"Pierwsze uruchomienie"})
    @CustomKey("FirstRun")
    private boolean firstRun = true;

    @Comment({""})
    @Comment({"System na którym uruchamiana jest aplikacja"})
    @CustomKey("System")
    private SystemOs system = SystemOs.LINUX;

    @Comment({""})
    @Comment({"Nazwa pliku który ma być włączony"})
    @CustomKey("FileName")
    private String fileName = "bedrock_server";

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

    public SystemOs getSystem() {
        return this.system;
    }

    public void setSystem(final SystemOs system) {
        this.system = system;
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

    public boolean isDebug() {
        return this.debug;
    }
}