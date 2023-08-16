package me.indian.bds.config.sub.discord;

import eu.okaeri.configs.OkaeriConfig;

public class Messages extends OkaeriConfig {

    private String joinMessage = "Gracz **<name>** dołączył do gry";
    private String leaveMessage = "Gracz **<name>** opuścił gre";
    private String deathMessage = "Gracz **<name>** zabity przez <cause>";
    private String minecraftToDiscordMessage = "**<name>** »» <msg>";
    private String discordToMinecraftMessage = "&7[&bDiscord&7]  &l<name>&r »» <msg>";
    private String enabledMessage = ":white_check_mark: Server włączony";
    private String disablingMessage = ":octagonal_sign: Server jest w trakcje wyłączania";
    private String disabledMessage = ":octagonal_sign: Server wyłączony";
    private String destroyedMessage = "Proces servera został zabity";

    public String getJoinMessage() {
        return this.joinMessage;
    }

    public String getLeaveMessage() {
        return this.leaveMessage;
    }

    public String getDeathMessage() {
        return this.deathMessage;
    }

    public String getMinecraftToDiscordMessage() {
        return this.minecraftToDiscordMessage;
    }

    public String getDiscordToMinecraftMessage() {
        return this.discordToMinecraftMessage;
    }

    public String getEnabledMessage() {
        return this.enabledMessage;
    }

    public String getDisablingMessage() {
        return this.disablingMessage;
    }

    public String getDisabledMessage() {
        return this.disabledMessage;
    }

    public String getDestroyedMessage() {
        return this.destroyedMessage;
    }
}

