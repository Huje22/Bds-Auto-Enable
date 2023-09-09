package me.indian.bds.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Header;
import me.indian.bds.config.sub.AutoMessagesConfig;
import me.indian.bds.config.sub.discord.DiscordConfig;
import me.indian.bds.config.sub.log.LogConfig;
import me.indian.bds.config.sub.watchdog.WatchDogConfig;
import me.indian.bds.util.SystemOs;


@Header("################################################################")
@Header("#                                                              #")
@Header("#    Huje22 Bds-Auto-Enable                                    #")
@Header("#  https://github.com/Huje22/Bds-Auto-Enable                   #")
@Header("#                                                              #")
@Header("################################################################")
//@Names(strategy = NameStrategy.SNAKE_CASE, modifier = NameModifier.TO_UPPER_CASE)

public class Config extends OkaeriConfig {
    @Comment({""})
    @Comment("UWAGA , WSZYTKIE ZMIANY TUTAJ WYMAGAJĄ RESTARTU APLIKACJI")
    @Comment({""})
    @Comment({"Versia która jest załadowana"})
    @CustomKey("Version")
    private String version = "1.20.15.01";
    @CustomKey("Loaded")
    private boolean loaded = false;

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
    private String fileName = "bedrock_server.exe";

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
    @Comment({"Debug"})
    private boolean debug = false;

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

    public boolean isDebug() {
        return this.debug;
    }
}