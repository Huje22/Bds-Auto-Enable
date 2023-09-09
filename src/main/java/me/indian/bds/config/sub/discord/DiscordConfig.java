package me.indian.bds.config.sub.discord;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import me.indian.bds.discord.DiscordType;

public class DiscordConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Implementacjia Bota / WebHooku"})
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
    private BotConfig botConfig = new BotConfig();

    @Comment({""})
    @Comment({"Ustawienia Wiadomości"})
    @CustomKey("discordMessagesOptions")
    private MessagesOptionsConfig messagesOptionsConfig = new MessagesOptionsConfig();

    @Comment({""})
    @Comment({"Konfiguracija dostępnych wiadomości "})
    @CustomKey("discordMessages")
    private MessagesConfig messagesConfig = new MessagesConfig();

    public DiscordType getIntegrationType() {
        return this.integrationType;
    }

    public WebHookConfig getWebHookConfig() {
        return this.webHookConfig;
    }

    public BotConfig getDiscordBotConfig() {
        return this.botConfig;
    }

    public MessagesOptionsConfig getDiscordMessagesOptionsConfig() {
        return this.messagesOptionsConfig;
    }

    public MessagesConfig getDiscordMessagesConfig() {
        return this.messagesConfig;
    }
}