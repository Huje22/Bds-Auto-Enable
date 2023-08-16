package me.indian.bds.discord;

public interface DiscordIntegration {


    void init();

    void sendJoinMessage(final String playerName);

    void sendLeaveMessage(final String playerName);

    void sendPlayerMessage(final String playerName, final String playerMessage);

    void sendDeathMessage(final String playerName, final String deathMessage);

    void sendDisabledMessage();

    void sendDisablingMessage();

    void sendStopMessage();

    void sendEnabledMessage();

    void sendDestroyedMessage();

    void writeConsole(final String message);

    void disableBot();
}
