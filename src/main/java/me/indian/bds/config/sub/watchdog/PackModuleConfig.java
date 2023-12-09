package me.indian.bds.config.sub.watchdog;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;

public class PackModuleConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Jak mają wyglądać wiadomości graczy gdy Paczka jest załadowana"})
    @Comment({"Można zmienić to na format Vanillowy gdy użyje się !format <true/false> "})

    @Comment({"<role> - rola z discord gdy użytkownik ma połączone konta"})
    @Comment({"<player> - nazwa gracza"})
    @Comment({"<message> - wiadomość gracza"})

    @CustomKey("ChatMessageFormat")
    private String chatMessageFormat = "<role><player> »» <message>";

    public String getChatMessageFormat() {
        return this.chatMessageFormat;
    }
}