package me.indian.bds.config.sub.discord;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import me.indian.bds.discord.DiscordType;

public class DiscordConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Implementacja Bota / WebHooku"})
    @Comment({"WEBHOOK - Możliwe tylko wysyłanie wiadomości do discord z użyciem webhooku"})
    @Comment({"JDA - Bot discord przy użyciu biblioteki JDA"})
    private DiscordType integrationType = DiscordType.JDA;
    @Comment({""})
    @Comment({"Ustawienia webhooka"})
    @CustomKey("webHook")
    private WebHookConfig webHookConfig = new WebHookConfig();
    @Comment({""})
    @Comment({"Ustawienia Bota"})
    @CustomKey("bot")
    private BotConfig botConfig = new BotConfig();

    @Comment({""})
    @Comment({"Ustawienia Wiadomości"})
    @CustomKey("messagesOptions")
    private MessagesOptionsConfig messagesOptionsConfig = new MessagesOptionsConfig();

    @Comment({""})
    @Comment({"Konfiguracja dostępnych wiadomości "})
    @CustomKey("messages")
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