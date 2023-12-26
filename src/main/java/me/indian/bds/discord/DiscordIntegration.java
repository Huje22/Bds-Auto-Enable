package me.indian.bds.discord;

import java.util.List;
import me.indian.bds.discord.embed.component.Field;
import me.indian.bds.discord.embed.component.Footer;

public interface DiscordIntegration {

    void init();

    void sendMessage(final String message);

    void sendMessage(final String message, final Throwable throwable);

    void sendEmbedMessage(final String title, final String message, final List<Field> fields, final Footer footer);

    void sendEmbedMessage(final String title, final String message, final List<Field> fields, final Throwable throwable, final Footer footer);

    void sendEmbedMessage(final String title, final String message, final Footer footer);

    void sendEmbedMessage(final String title, final String message, final Throwable throwable, final Footer footer);

    void sendJoinMessage(final String playerName);

    void sendLeaveMessage(final String playerName);

    void sendPlayerMessage(final String playerName, final String playerMessage);

    void sendDeathMessage(final String playerName, final String deathMessage);

    void sendDisabledMessage();

    void sendDisablingMessage();

    void sendProcessEnabledMessage();

    void sendEnabledMessage();

    void sendDestroyedMessage();

    void sendBackupDoneMessage();

    void sendAppRamAlert();

    void sendMachineRamAlert();

    void sendServerUpdateMessage(final String version);

    void sendRestartMessage();

    void writeConsole(final String message);

    void writeConsole(final String message, final Throwable throwable);

    void startShutdown();
  
    void shutdown();
}