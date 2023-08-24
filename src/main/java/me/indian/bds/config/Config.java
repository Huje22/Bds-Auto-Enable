package me.indian.bds.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Header;
import me.indian.bds.config.sub.discord.DiscordBot;
import me.indian.bds.config.sub.discord.Messages;
import me.indian.bds.config.sub.discord.WebHook;
import me.indian.bds.config.sub.log.Log;
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
    @Comment({"Backups"})
    private boolean backup = true;
    private int backupFrequency = 60;
    private double lastBackupTime = 20;

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
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public Log getNoLog() {
        return this.log;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public boolean isFirstRun() {
        return firstRun;
    }

    public void setFirstRun(boolean firstRun) {
        this.firstRun = firstRun;
    }

    public SystemOs getSystem() {
        return system;
    }

    public void setSystem(SystemOs system) {
        this.system = system;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isWine() {
        return wine;
    }

    public void setWine(boolean wine) {
        this.wine = wine;
    }

    public String getFilesPath() {
        return filesPath;
    }

    public void setFilesPath(String filesPath) {
        this.filesPath = filesPath;
    }

    public boolean isBackup() {
        return backup;
    }

    public void setBackup(boolean backup) {
        this.backup = backup;
    }

    public int getBackupFrequency() {
        return backupFrequency;
    }

    public void setBackupFrequency(int backupFrequency) {
        this.backupFrequency = backupFrequency;
    }

    public double getLastBackupTime() {
        return lastBackupTime;
    }

    public void setLastBackupTime(double lastBackupTime) {
        this.lastBackupTime = lastBackupTime;
    }

    public DiscordType getIntegrationType() {
        return integrationType;
    }

    public WebHook getWebHook() {
        return this.webHook;
    }

    public DiscordBot getDiscordBot() {
        return discordBot;
    }

    public Messages getMessages() {
        return messages;
    }

    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}