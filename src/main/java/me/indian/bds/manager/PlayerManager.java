package me.indian.bds.manager;

import me.indian.bds.util.DateUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerManager {

    private final List<String> onlinePlayers, offlinePlayers;
    private final Map<String, String> onlineWithTime, offlineWithTime;

    public PlayerManager() {
        this.onlinePlayers = new ArrayList<>();
        this.offlinePlayers = new ArrayList<>();
        this.onlineWithTime = new HashMap<>();
        this.offlineWithTime = new HashMap<>();
    }

    public void updatePlayerList(final String logEntry) {
        final String patternString = "(Player connected|Player disconnected): ([^,]+),";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String action = matcher.group(1);
            final String playerName = matcher.group(2);

            if ("Player connected".equals(action)) {
                this.onlinePlayers.add(playerName);
                this.onlineWithTime.put(playerName, DateUtil.getDate());
                this.offlinePlayers.remove(playerName);
                this.offlineWithTime.remove(playerName);
            } else if ("Player disconnected".equals(action)) {
                this.onlinePlayers.remove(playerName);
                this.onlineWithTime.remove(playerName);
                this.offlinePlayers.add(playerName);
                this.offlineWithTime.put(playerName, DateUtil.getDate());
            }
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

    public Map<String, String> getOfflineWithTime() {
        return this.offlineWithTime;
    }

    public Map<String, String> getOnlineWithTime() {
        return this.onlineWithTime;
    }

}
