package me.indian.bds.discord;

public interface DiscordIntegration {


    void init();

    void sendMessage(final String message);

    void sendEmbedMessage(final String title, final String message, final String footer);

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

    void writeConsole(final String message);

    void disableBot();
}
