package me.indian.bds.config.sub.discord;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class MessagesConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Część wyświetlona gdy ktoś odpowie na czyjąś wiadomość"})
    private String replyStatement = " (&dOdpowiada na&a: &a<author> &r»»&b <msg> &r)";
    @Comment({""})
    @Comment({"Informacja o tym że wiadomość została edytowana"})
    private String edited = " (Edytowano)";
    @Comment({""})
    @Comment({"Informacja o dołączeniu gracza"})
    private String joinMessage = "Gracz **<name>** dołączył do gry";
    @Comment({""})
    @Comment({"Informacja o wyjściu gracza"})
    private String leaveMessage = "Gracz **<name>** opuścił gre";
    @Comment({""})
    @Comment({"Informacja o śmierci gracza"})
    private String deathMessage = "Gracz **<name>** zabity przez <casue>";
    @Comment({""})
    @Comment({"Wygląd wiadomości z Minecraft na Discord"})
    private String minecraftToDiscordMessage = "**<name>** »» <msg>";
    @Comment({""})
    @Comment({"Wygląd wiadomości z Discord na Minecraft "})
    private String discordToMinecraftMessage = "&7[&bDiscord&7] &e<role> &l&a<name>&r<reply> »» <msg>";
    @Comment({""})
    @Comment({"Informacja na discord o włączeniu servera"})
    private String enabledMessage = ":white_check_mark: Server włączony";
    @Comment({""})
    @Comment({"Informacja na discord o włączeniu procesu  servera"})
    private String processEnabledMessage = "Proces servera włączony";
    @Comment({""})
    @Comment({"Informacja na discord o włączaniu servera"})
    private String disablingMessage = ":octagonal_sign: Server jest w trakcje wyłączania";
    @Comment({""})
    @Comment({"Informacja na discord gdy server się wyłączy"})
    private String disabledMessage = ":octagonal_sign: Server wyłączony";
    @Comment({""})
    @Comment({"Informacja na discord gdy proces servera zostanie zabity"})
    private String destroyedMessage = "Proces servera został zabity";
    @Comment({""})
    @Comment({"Informacja na discord gdy backup zostanie utworzony"})
    private String backupDoneMessage = "**Backup został utworzony!**";
    @Comment({""})
    @Comment({"Informacja na discord gdy aplikacja używa 80% ramu"})
    private String appRamAlter = "Aplikacja używa **80%** dostępnego dla niej ramu!";
    @Comment({""})
    @Comment({"Informacja na discord gdy maszyna ma mniej niż 1GB wolnego ramu"})
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

    public String getProcessEnabledMessage() {
        return this.processEnabledMessage;
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

    public String getBackupDoneMessage() {
        return this.backupDoneMessage;
    }

    public String getAppRamAlter() {
        return this.appRamAlter;
    }

    public String getMachineRamAlter() {
        return this.machineRamAlter;
    }
}