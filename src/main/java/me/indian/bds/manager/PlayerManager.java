package me.indian.bds.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerManager {

    private final List<String> onlinePlayers, offlinePlayers;
    private final Map<String, Long> savedPlayers;

    public PlayerManager() {
        this.onlinePlayers = new ArrayList<>();
        this.offlinePlayers = new ArrayList<>();
        this.savedPlayers = new HashMap<>();
    }

    public void updatePlayerList(final String logEntry) {
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
            } else if ("Player disconnected".equals(action)) {
                this.onlinePlayers.remove(playerName);
                this.offlinePlayers.add(playerName);
            }

            if (!this.savedPlayers.containsKey(playerName)) this.savedPlayers.put(playerName, xuid);
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