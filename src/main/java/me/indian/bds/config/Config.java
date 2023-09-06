package me.indian.bds.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Header;
import me.indian.bds.config.sub.AutoMessagesConfig;
import me.indian.bds.config.sub.discord.DiscordBotConfig;
import me.indian.bds.config.sub.discord.DiscordMessagesConfig;
import me.indian.bds.config.sub.discord.WebHookConfig;
import me.indian.bds.config.sub.log.LogConfig;
import me.indian.bds.config.sub.watchdog.WatchDogConfig;
import me.indian.bds.discord.DiscordType;
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
    private String version = "1.20.15.01";
    private boolean loaded = false;

    @Comment({""})
    @Comment({"Pierwsze uruchomienie"})
    private boolean firstRun = true;

    @Comment({""})
    @Comment({"System na którym uruchamiana jest aplikacja"})
    private SystemOs system = SystemOs.LINUX;

    @Comment({""})
    @Comment({"Nazwa pliku który ma być włączony"})
    private String fileName = "bedrock_server.exe";

    @Comment({""})
    @Comment({"Czy użyć wine?"})
    @Comment({"Aby użyć wine trzeba mieć je pobrane!"})
    private boolean wine = false;

    @Comment({""})
    @Comment({"Ścieżka do plików z serverem"})
    private String filesPath = "./";

    @Comment({""})
    @Comment({"Watch dog"})
    @CustomKey("watchDog")
    private WatchDogConfig watchDogConfig = new WatchDogConfig();

    @Comment({""})
    @Comment({"Automessages"})
    @CustomKey("autoMessages")
    private AutoMessagesConfig autoMessagesConfig = new AutoMessagesConfig();

    @Comment({""})
    @Comment({"Ustawienia logowania"})
    @Comment({"Nie zapisuje tych informacj które zawierają dane znaki i słowa"})
    @CustomKey("log")
    private LogConfig logConfig = new LogConfig();

    @Comment({""})
    @Comment({"Ustawienia discord"})
    @Comment({"Implementacija Bota / WebHooku"})
    @Comment({"WEBHOOK - Możliwe tylko wysyłanie wiadomości do discord z uzyciem webhooku"})
    @Comment({"JDA - Bot discord przy uzyciu biblioteki JDA"})
    private DiscordType integrationType = DiscordType.JDA;
    @Comment({""})
    @Comment({"Ustawienia webhooka"})
    @CustomKey("webHook")
    private WebHookConfig webHookConfig = new WebHookConfig();
    @Comment({""})
    @Comment({"Ustawienia Bota"})
    @CustomKey("discordBot")
    private DiscordBotConfig discordBotConfig = new DiscordBotConfig();
    @Comment({""})
    @Comment({"Konfiguracija dostępnych wiadomości "})
    @CustomKey("discordMessages")
    private DiscordMessagesConfig discordMessagesConfig = new DiscordMessagesConfig();
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


    public DiscordType getIntegrationType() {
        return this.integrationType;
    }

    public WebHookConfig getWebHookConfig() {
        return this.webHookConfig;
    }

    public DiscordBotConfig getDiscordBotConfig() {
        return this.discordBotConfig;
    }

    public DiscordMessagesConfig getDiscordMessagesConfig() {
        return this.discordMessagesConfig;
    }

    public boolean isDebug() {
        return this.debug;
    }

    public void setDebug(final boolean debug) {
        this.debug = debug;
    }
}