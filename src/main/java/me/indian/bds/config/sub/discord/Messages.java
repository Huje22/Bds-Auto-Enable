package me.indian.bds.config.sub.discord;

import eu.okaeri.configs.OkaeriConfig;

public class Messages extends OkaeriConfig {

    private String replyStatement = " (&dOdpowiada na&a: &a<author> &r»»&b <msg> &r)";
    private String edited = " (Edytowano)";
    private String joinMessage = "Gracz **<name>** dołączył do gry";
    private String leaveMessage = "Gracz **<name>** opuścił gre";
    private String deathMessage = "Gracz **<name>** zabity przez <casue>";
    private String minecraftToDiscordMessage = "**<name>** »» <msg>";
    private String discordToMinecraftMessage = "&7[&bDiscord&7] &e<role> &l&a<name>&r<reply> »» <msg>";
    private String enabledMessage = ":white_check_mark: Server włączony";
    private String disablingMessage = ":octagonal_sign: Server jest w trakcje wyłączania";
    private String disabledMessage = ":octagonal_sign: Server wyłączony";
    private String destroyedMessage = "Proces servera został zabity";
    private String appRamAlter = "Aplikacija używa **80%** dostępnego dla niej ramu!";
    private String machineRamAlter = "Maszyna ma mniej niż **1GB** dostępnego ramu!";

    public String getReplyStatement() {
        return this.replyStatement;
    }

    public String getEdited() {
        return this.edited;
    }

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

    public String getAppRamAlter() {
        return this.appRamAlter;
    }

    public String getMachineRamAlter() {
        return this.machineRamAlter;
    }
}

