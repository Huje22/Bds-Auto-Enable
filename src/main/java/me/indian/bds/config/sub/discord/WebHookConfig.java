package me.indian.bds.config.sub.discord;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class WebHookConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Czy włączyć webhook"})
    private boolean enable = true;
    @Comment({""})
    @Comment({"Nazwa webhooku"})
    private String name = "Tezak";

    @Comment({""})
    @Comment({"URL do webhooku"})
    private String chatUrl = "https://discord.com/api/webhooks/....";

    @Comment({""})
    @Comment({"Url do avataru webhooku"})
    private String avatarUrl = "https://cdn.discordapp.com/avatars/299247844353638401/a9c8e8a41aaf33b4292e41266ce0aca2.webp?size=2048";


    public boolean isEnable() {
        return this.enable;
    }

    public String getName() {
        return this.name;
    }

    public String getChatUrl() {
        return this.chatUrl;
    }

    public String getAvatarUrl() {
        return this.avatarUrl;
    }
}