package me.indian.bds.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Header;
import me.indian.bds.config.sub.AutoMessagesConfig;
import me.indian.bds.config.sub.discord.DiscordBot;
import me.indian.bds.config.sub.discord.Messages;
import me.indian.bds.config.sub.discord.WebHook;
import me.indian.bds.config.sub.log.Log;
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
    private Log log = new Log();

    @Comment({""})
    @Comment({"Ustawienia discord"})
    @Comment({"Implementacija Bota / WebHooku"})
    @Comment({"WEBHOOK - Możliwe tylko wysyłanie wiadomości do discord z uzyciem webhooku"})
    @Comment({"JDA - Bot discord przy uzyciu biblioteki JDA"})
    private DiscordType integrationType = DiscordType.JDA;
    @Comment({""})
    @Comment({"Ustawienia webhooka"})
    private WebHook webHook = new WebHook();
    @Comment({""})
    @Comment({"Ustawienia Bota"})
    private DiscordBot discordBot = new DiscordBot();
    @Comment({""})
    @Comment({"Konfiguracija dostępnych wiadomości "})
    private Messages messages = new Messages();
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

    public AutoMessagesConfig getAutoMessagesConfig() {
        return this.autoMessagesConfig;
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

    public void setWatchDogConfig(final WatchDogConfig watchDogConfig) {
        this.watchDogConfig = watchDogConfig;
    }

    public Log getLog() {
        return this.log;
    }

    public void setLog(final Log log) {
        this.log = log;
    }

    public DiscordType getIntegrationType() {
        return this.integrationType;
    }

    public void setIntegrationType(final DiscordType integrationType) {
        this.integrationType = integrationType;
    }

    public WebHook getWebHook() {
        return this.webHook;
    }

    public void setWebHook(final WebHook webHook) {
        this.webHook = webHook;
    }

    public DiscordBot getDiscordBot() {
        return this.discordBot;
    }

    public void setDiscordBot(final DiscordBot discordBot) {
        this.discordBot = discordBot;
    }

    public Messages getMessages() {
        return this.messages;
    }

    public void setMessages(final Messages messages) {
        this.messages = messages;
    }

    public boolean isDebug() {
        return this.debug;
    }

    public void setDebug(final boolean debug) {
        this.debug = debug;
    }
}