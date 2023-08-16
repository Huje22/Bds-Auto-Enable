package me.indian.bds.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Header;
import me.indian.bds.discord.DiscordType;
import me.indian.bds.util.SystemOs;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Header("################################################################")
@Header("#                                                              #")
@Header("#    Huje22 Bds-Auto-Enable                                    #")
@Header("#  https://github.com/Huje22/Bds-Auto-Enable                   #")
@Header("#                                                              #")
@Header("################################################################")


public class Config extends OkaeriConfig {
    @Comment({""})
    @Comment({"Versia która jest załadowana"})
    private String Version = "1.20.14.01";
    private boolean Loaded = false;

    @Comment({""})
    @Comment({"Pierwsze uruchomienie"})
    private boolean FirstRun = true;

    @Comment({""})
    @Comment({"System na którym uruchamiana jest aplikacja"})
    private SystemOs System = SystemOs.LINUX;

    @Comment({""})
    @Comment({"Nazwa pliku który ma być włączony"})
    private String FileName = "bedrock_server.exe";

    @Comment({""})
    @Comment({"Czy użyć wine?"})
    private boolean Wine = false;


    @Comment({""})
    @Comment({"Ścieżka do z serverem"})
    private String FilesPath = "./";


    @Comment({""})
    @Comment({"Backups"})
    private boolean Backup = true;
    private int BackupFrequency = 60;
    private double LastBackupTime = 20;
    @Comment({""})
    @Comment({"Konsola i Plik .log"})
    @Comment({"Nie zapisuje tych informacj które zawierają:"})
    @Comment({"W pliku"})
    private List<String> noFileLog = Arrays.asList("[Json]", "[Blocks]", "[Components]", "[Molang]",
            "[Item]", "[Recipes]", "[FeatureRegistry]", "[Actor]",
            " ERROR]", "WARN]",
            "\"component_groups\"");
    @Comment({"W konsoli"})
    private List<String> noConsoleLog = Arrays.asList("[Json]", "[Blocks]", "[Components]", "[Molang]",
            "[Item]", "[Recipes]", "[FeatureRegistry]", "[Actor]",
            "\"component_groups\"");

    @Comment({""})
    @Comment({"Ustawienia discord"})
    @Comment({"Implementacija Bota / WebHooku"})
    @Comment({"WEBHOOK - Możliwe tylko wysyłanie wiadomości do discord z uzyciem webhooku"})
    @Comment({"JDA - Bot discord przy uzyciu biblioteki Java Cord"})
    private DiscordType IntegrationType = DiscordType.JDA;

    @Comment({"Ustawienia webhooka"})
    private String WebHookChatUrl = "https://discord.com/api/webhooks/....";
    @Comment({"Ustawienia Bota"})
    private String Token = "";
    private String Prefix = "!";
    private long ChannelID = 1L;
    private long ConsoleID = 1L;
    private long ServerID = 1L;
    private List<String> IPmessage = Arrays.asList("Nasze IP: 127.0.0.1", "Nasz Port: 19132");
    @Comment({"Konfiguracija dostępnych wiadomości "})
    private Map<String, String> Messages = this.initMessages();
    @Comment({""})
    @Comment({"Debug"})
    private boolean debug = true;

    private Map<String, String> initMessages() {
        final Map<String, String> messages = new LinkedHashMap<>();
        messages.put("Join", "Gracz **<name>** dołączył do gry");
        messages.put("Leave", "Gracz **<name>** opuścił gre");
        messages.put("Death", "Gracz **<name>** zabity przez <casue>");
        messages.put("MinecraftToDiscord", "**<name>** »» <msg>");
        messages.put("DiscordToMinecraft", "&7[&bDiscord&7]  &l<name>&r »» <msg>");
        messages.put("Enabled", ":white_check_mark: Server włączony");
        messages.put("Disabling", ":octagonal_sign: Server jest w trakcje wyłączania");
        messages.put("Disabled", ":octagonal_sign: Server wyłączony");
        messages.put("Destroyed", "Proces servera został zabity");
        return messages;
    }

    public String getToken() {
        return this.Token;
    }

    public void setToken(String token) {
        this.Token = token;
    }

    public String getPrefix() {
        return this.Prefix;
    }

    public long getChannelID() {
        return ChannelID;
    }

    public long getConsoleID() {
        return this.ConsoleID;
    }

    public long getServerID() {
        return this.ServerID;
    }

    public List<String> getIPmessage() {
        return this.IPmessage;
    }

    public DiscordType getIntegrationType() {
        return IntegrationType;
    }

    public Map<String, String> getMessages() {
        return this.Messages;
    }

    public String getWebHookChatUrl() {
        return this.WebHookChatUrl;
    }

    public int getBackupFrequency() {
        return this.BackupFrequency;
    }

    public void setBackupFrequency(final int backupFrequency) {
        this.BackupFrequency = backupFrequency;
    }

    public double getLastBackupTime() {
        return this.LastBackupTime;
    }

    public void setLastBackupTime(final double lastBackupTime) {
        this.LastBackupTime = lastBackupTime;
    }

    public List<String> getNoConsoleLog() {
        return noConsoleLog;
    }

    public String getVersion() {
        return this.Version;
    }

    public void setVersion(final String version) {
        this.Version = version;
    }

    public boolean isLoaded() {
        return this.Loaded;
    }

    public void setLoaded(final boolean loaded) {
        this.Loaded = loaded;
    }

    public List<String> getNoFileLog() {
        return this.noFileLog;
    }

    public boolean isDebug() {
        return this.debug;
    }

    public boolean isBackup() {
        return this.Backup;
    }

    public void setBackup(final boolean backup) {
        this.Backup = backup;
    }

    public boolean isFirstRun() {
        return FirstRun;
    }

    public void setFirstRun(final boolean firstRun) {
        this.FirstRun = firstRun;
    }

    public SystemOs getSystemOs() {
        return this.System;
    }

    public void setSystemOs(final SystemOs systemOs) {
        this.System = systemOs;
    }

    public String getFileName() {
        return this.FileName;
    }

    public void setFileName(final String fileName) {
        this.FileName = fileName;
    }

    public boolean isWine() {
        return this.Wine;
    }

    public void setWine(final boolean wine) {
        this.Wine = wine;
    }

    public String getFilesPath() {
        return this.FilesPath;
    }

    public void setFilesPath(final String filesPath) {
        this.FilesPath = filesPath;
    }
}