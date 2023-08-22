package me.indian.bds.config.sub.discord;

import eu.okaeri.configs.OkaeriConfig;

public class WebHook extends OkaeriConfig {
    
    private String name = "Tezak";
    private String url = "https://discord.com/api/webhooks/....";
    private String avatarUrl = "https://cdn.discordapp.com/avatars/299247844353638401/a9c8e8a41aaf33b4292e41266ce0aca2.webp?size=2048";


    public String getName() {
        return this.name;
    }

    public String getUrl() {
        return this.url;
    }

    public String getAvatarUrl() {
        return this.avatarUrl;
    }
}
