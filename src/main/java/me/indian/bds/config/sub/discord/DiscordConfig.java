package me.indian.bds.config.sub.discord;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Header;

@Header("################################################################")
@Header("#           Ustawienia Integracji z Discord                    #")
@Header("################################################################")

public class DiscordConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Ustawienia webhooka"})
    @CustomKey("WebHook")
    private WebHookConfig webHookConfig = new WebHookConfig();
    @Comment({""})
    @Comment({"Ustawienia Bota"})
    @CustomKey("Bot")
    private BotConfig botConfig = new BotConfig();

    @Comment({""})
    @Comment({"Ustawienia Wiadomości"})
    @CustomKey("MessagesOptions")
    private MessagesOptionsConfig messagesOptionsConfig = new MessagesOptionsConfig();

    @Comment({""})
    @Comment({"Konfiguracja dostępnych wiadomości "})
    @CustomKey("Messages")
    private MessagesConfig messagesConfig = new MessagesConfig();

    public WebHookConfig getWebHookConfig() {
        return this.webHookConfig;
    }

    public BotConfig getBotConfig() {
        return this.botConfig;
    }

    public MessagesOptionsConfig getDiscordMessagesOptionsConfig() {
        return this.messagesOptionsConfig;
    }

    public MessagesConfig getDiscordMessagesConfig() {
        return this.messagesConfig;
    }
}