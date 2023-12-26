package me.indian.bds.discord;

import java.util.List;
import me.indian.bds.discord.embed.component.Field;
import me.indian.bds.discord.embed.component.Footer;

public class NoneDiscord implements DiscordIntegration {

    @Override
    public void init() {

    }

    @Override
    public void sendMessage(final String message) {

    }

    @Override
    public void sendMessage(final String message, final Throwable throwable) {

    }

    @Override
    public void sendEmbedMessage(final String title, final String message, final List<Field> fields, final Footer footer) {

    }

    @Override
    public void sendEmbedMessage(final String title, final String message, final List<Field> fields, final Throwable throwable, final Footer footer) {

    }

    @Override
    public void sendEmbedMessage(final String title, final String message, final Footer footer) {

    }

    @Override
    public void sendEmbedMessage(final String title, final String message, final Throwable throwable, final Footer footer) {

    }

    @Override
    public void sendJoinMessage(final String playerName) {

    }

    @Override
    public void sendLeaveMessage(final String playerName) {

    }

    @Override
    public void sendPlayerMessage(final String playerName, final String playerMessage) {

    }

    @Override
    public void sendDeathMessage(final String playerName, final String deathMessage) {

    }

    @Override
    public void sendDisabledMessage() {

    }

    @Override
    public void sendDisablingMessage() {

    }

    @Override
    public void sendProcessEnabledMessage() {

    }

    @Override
    public void sendEnabledMessage() {

    }

    @Override
    public void sendDestroyedMessage() {

    }

    @Override
    public void sendBackupDoneMessage() {

    }

    @Override
    public void sendAppRamAlert() {

    }

    @Override
    public void sendMachineRamAlert() {

    }

    @Override
    public void sendServerUpdateMessage(final String version) {

    }

    @Override
    public void sendRestartMessage() {

    }

    @Override
    public void writeConsole(final String message) {

    }

    @Override
    public void writeConsole(final String message, final Throwable throwable) {

    }

    @Override
    public void startShutdown() {

    }

    @Override
    public void shutdown() {

    }
}