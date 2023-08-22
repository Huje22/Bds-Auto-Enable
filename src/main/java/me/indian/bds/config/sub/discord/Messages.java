package me.indian.bds.config.sub.discord;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class Messages extends OkaeriConfig {

    @Comment({""})
    @Comment({"Pamiętaj że oznaczenie kogoś zawiera jego ID a ono jest długie!"})
    private int allowedLength = 200;
    private boolean deleteOnReachLimit = true;
    private String reachedMessage = "Osiągnięto dozwoloną ilosc znaków!";
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

    public int getAllowedLength() {
        return this.allowedLength;
    }

    public boolean isDeleteOnReachLimit() {
        return this.deleteOnReachLimit;
    }

    public String getReachedMessage() {
        return this.reachedMessage;
    }

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
}

