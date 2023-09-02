package me.indian.bds.manager.player;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.discord.DiscordIntegration;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.ThreadUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PlayerManager {

    private final Logger logger;
    private final DiscordIntegration discord;
    private final ExecutorService service;
    private final List<String> onlinePlayers, offlinePlayers;
    private final StatsManager statsManager;

    public PlayerManager(final BDSAutoEnable bdsAutoEnable) {
        this.logger = bdsAutoEnable.getLogger();
        this.discord = bdsAutoEnable.getDiscord();
        this.service = Executors.newScheduledThreadPool(2, new ThreadUtil("Player Manager"));
        this.onlinePlayers = new ArrayList<>();
        this.offlinePlayers = new ArrayList<>();
        this.statsManager = new StatsManager(bdsAutoEnable, this);
    }

    public void initFromLog(final String logEntry) {
        this.service.execute(() -> {
            this.updatePlayerList(logEntry);
            this.chatMessage(logEntry);
            this.deathMessage(logEntry);
        });
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
        }
    }

    private void chatMessage(final String logEntry) {
        final String patternString = "PlayerChat:([^,]+) Message:([^,]+)";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerChat = matcher.group(1);
            final String message = matcher.group(2);
            this.discord.sendPlayerMessage(playerChat, message);
        }
    }

    private void deathMessage(final String logEntry) {
        final String patternString = "PlayerDeath:([^,]+) Casue:([^,]+)";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerDeath = matcher.group(1);
            final String casue = matcher.group(2);
            this.discord.sendDeathMessage(playerDeath, casue);
            this.statsManager.addDeaths(playerDeath, 1);
        }
    }

    public StatsManager getStatsManager() {
        return this.statsManager;
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
}