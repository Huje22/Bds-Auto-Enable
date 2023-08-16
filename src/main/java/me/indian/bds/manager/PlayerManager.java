package me.indian.bds.manager;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.Config;
import me.indian.bds.discord.DiscordIntegration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerManager {

    private final Config config;
    private final DiscordIntegration discord;
    private final List<String> onlinePlayers, offlinePlayers;
    private final Map<String, Long> savedPlayers;

    public PlayerManager(final BDSAutoEnable bdsAutoEnable) {
        this.config = bdsAutoEnable.getConfig();
        this.discord = bdsAutoEnable.getDiscord();
        this.onlinePlayers = new ArrayList<>();
        this.offlinePlayers = new ArrayList<>();
        this.savedPlayers = new HashMap<>();
    }


    public void initFromLog(final String logEntry) {
        this.updatePlayerList(logEntry);
        this.getChatMessage(logEntry);
    }


    private void updatePlayerList(final String logEntry) {
        final String patternString = "(Player connected|Player disconnected): ([^,]+), xuid: ([0-9]{16})";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String action = matcher.group(1);
            final String playerName = matcher.group(2);
            final String xuidString = matcher.group(3);
            final Long xuid = Long.parseLong(xuidString);

            if ("Player connected".equals(action)) {
                this.onlinePlayers.add(playerName);
                this.offlinePlayers.remove(playerName);
                this.discord.sendJoinMessage(playerName);
            } else if ("Player disconnected".equals(action)) {
                this.onlinePlayers.remove(playerName);
                this.offlinePlayers.add(playerName);
                this.discord.sendLeaveMessage(playerName);
            }

            if (!this.savedPlayers.containsKey(playerName)) this.savedPlayers.put(playerName, xuid);
        }
    }

    private void getChatMessage(final String logEntry) {
        final String patternString = "PlayerChat:([^,]+) Message:([^,]+)";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerChat = matcher.group(1);
            final String message = matcher.group(2);
            this.discord.sendPlayerMessage(playerChat, message);
        }
    }

    public void clearPlayers() {
        this.onlinePlayers.clear();
    }

    public List<String> getOnlinePlayers() {
        return this.onlinePlayers;
    }

    public List<String> getOfflinePlayers() {
        return this.offlinePlayers;
    }

    public Map<String, Long> getSavedPlayers() {
        return this.savedPlayers;
    }
}