package me.indian.bds.config.sub.discord;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class MessagesOptionsConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Wysyłać wiadomość o zrobieniu backup"})
    private boolean sendBackupMessage = false;

    @Comment({""})
    @Comment({"Wysyłać wiadomość o nie zrobieniu backup"})
    private boolean sendBackupFailMessage = false;

    @Comment({""})
    @Comment({"Wysyłać wiadomość o dołączeniu gracza"})
    private boolean sendJoinMessage = true;

    @Comment({""})
    @Comment({"Wysyłać wiadomość o opuszczeniu gracza"})
    private boolean sendLeaveMessage = true;

    @Comment({""})
    @Comment({"Wysyłać wiadomość gracza"})
    private boolean sendPlayerMessage = true;

    @Comment({""})
    @Comment({"Wysyłać wiadomość o śmierci gracza"})
    private boolean sendDeathMessage = true;

    @Comment({""})
    @Comment({"Wysyłać wiadomość o wyłączeniu servera"})
    private boolean sendDisabledMessage = true;

    @Comment({""})
    @Comment({"Wysyłać wiadomość o wyłączaniu servera"})
    private boolean sendDisablingMessage = true;

    @Comment({""})
    @Comment({"Wysyłać wiadomość o włączeniu procesu servera"})
    private boolean sendProcessEnabledMessage = false;
    @Comment({""})
    @Comment({"Wysyłać wiadomość o włączeniu servera"})
    private boolean sendEnabledMessage = true;

    @Comment({""})
    @Comment({"Wysyłać wiadomość o zniszczeniu procesu"})
    private boolean sendDestroyedMessage = false;
    @Comment({""})
    @Comment({"Wysyłać wiadomość o aktualizowaniu wersji servera"})
    private boolean sendServerUpdateMessage = true;

    @Comment({""})
    @Comment({"Wysyłać wiadomość o restarcie servera"})
    private boolean sendRestartMessage = true;

    public boolean isSendBackupMessage() {
        return this.sendBackupMessage;
    }

    public boolean isSendBackupFailMessage() {
        return this.sendBackupFailMessage;
    }

    public boolean isSendJoinMessage() {
        return this.sendJoinMessage;
    }

    public boolean isSendLeaveMessage() {
        return this.sendLeaveMessage;
    }

    public boolean isSendPlayerMessage() {
        return this.sendPlayerMessage;
    }

    public boolean isSendDeathMessage() {
        return this.sendDeathMessage;
    }

    public boolean isSendDisabledMessage() {
        return this.sendDisabledMessage;
    }

    public boolean isSendDisablingMessage() {
        return this.sendDisablingMessage;
    }

    public boolean isSendProcessEnabledMessage() {
        return this.sendProcessEnabledMessage;
    }

    public boolean isSendEnabledMessage() {
        return this.sendEnabledMessage;
    }

    public boolean isSendDestroyedMessage() {
        return this.sendDestroyedMessage;
    }

    public boolean isSendServerUpdateMessage() {
        return this.sendServerUpdateMessage;
    }

    public boolean isSendRestartMessage() {
        return this.sendRestartMessage;
    }
}