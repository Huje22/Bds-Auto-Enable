package me.indian.bds.manager;

import com.google.gson.reflect.TypeToken;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.Defaults;
import me.indian.bds.discord.DiscordIntegration;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.GsonUtil;
import me.indian.bds.util.ThreadUtil;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerManager {

    private final Logger logger;
    private final DiscordIntegration discord;
    private final ExecutorService service;
    private final File playTimeJson;
    private final List<String> onlinePlayers, offlinePlayers;
    private final Map<String, Long> currentPlayTime, playTime;

    public PlayerManager(final BDSAutoEnable bdsAutoEnable) {
        this.logger = bdsAutoEnable.getLogger();
        this.discord = bdsAutoEnable.getDiscord();
        this.service = Executors.newScheduledThreadPool(2, new ThreadUtil("Player Manager"));
        this.playTimeJson = new File(Defaults.getAppDir() + File.separator + "stats" + File.separator + "playtime.json");
        if (!this.playTimeJson.exists()) {
            try {
                if (!this.playTimeJson.exists()) {
                    Files.createDirectories(this.playTimeJson.getParentFile().toPath());
                    if (!this.playTimeJson.createNewFile()) {
                        this.logger.critical("Nie można utworzyć&b playtime.json");
                    }
                }
            } catch (final IOException exception) {
                this.logger.critical("Nie można utworzyć&b playtime.json");
            }
        }
        this.onlinePlayers = new ArrayList<>();
        this.offlinePlayers = new ArrayList<>();
        this.currentPlayTime = new HashMap<>();
        this.playTime = this.loadPlayTime();
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
                this.currentPlayTime.put(playerName, Instant.now().toEpochMilli());
            } else if ("Player disconnected".equals(action)) {
                this.onlinePlayers.remove(playerName);
                this.offlinePlayers.add(playerName);
                this.discord.sendLeaveMessage(playerName);
                if (this.currentPlayTime.containsKey(playerName)) {
                    final long playTime = Instant.now().toEpochMilli() - this.currentPlayTime.get(playerName);
                    this.playTime.put(playerName, this.getPlayTimeByName(playerName) + playTime);
                }
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
            final String playerChat = matcher.group(1);
            final String message = matcher.group(2);
            this.discord.sendDeathMessage(playerChat, message);
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

    public Map<String, Long> getPlayTime() {
        return this.playTime;
    }

    public long getPlayTimeByName(final String playerName) {
        return (this.playTime.get(playerName) == null ? 0 : this.playTime.get(playerName));
    }

    public void savePlayTime() {
        try (final FileWriter writer = new FileWriter(this.playTimeJson)) {
            writer.write(GsonUtil.getGson().toJson(this.playTime));
            this.logger.info("Zapisano pomyślnie czas gry graczy");
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }

    private HashMap<String, Long> loadPlayTime() {
        try (final FileReader reader = new FileReader(this.playTimeJson)) {
            final Type type = new TypeToken<HashMap<String, Long>>() {
            }.getType();
            final HashMap<String, Long> loadedMap = GsonUtil.getGson().fromJson(reader, type);
            return (loadedMap == null ? new HashMap<>() : loadedMap);
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
        return new HashMap<>();
    }
}