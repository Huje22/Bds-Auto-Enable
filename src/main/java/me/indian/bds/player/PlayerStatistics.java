package me.indian.bds.player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerStatistics implements Serializable {

    private String playerName;
    private long xuid;
    private final long firstJoin;
    private long lastJoin, lastQuit;
    private final List<String> oldNames;
    private long playtime, deaths, blockPlaced, blockBroken, loginStreak, longestLoginStreak;
    private DeviceOS lastDevice;
    private Controller lastController;
    private final Map<String, Object> dynamicProperties;

    public PlayerStatistics(final String playerName, final long xuid, final long firstJoin, final long lastJoin, final long lastQuit, final long playtime, final long deaths, final long blockPlaced, final long blockBroken) {
        this.playerName = playerName;
        this.xuid = xuid;
        this.firstJoin = firstJoin;
        this.lastJoin = lastJoin;
        this.lastQuit = lastQuit;
        this.oldNames = new ArrayList<>();
        this.playtime = playtime;
        this.deaths = deaths;
        this.blockPlaced = blockPlaced;
        this.blockBroken = blockBroken;
        this.loginStreak = 0;
        this.longestLoginStreak = 0;
        this.lastDevice = DeviceOS.UNKNOWN;
        this.lastController = Controller.UNKNOWN;
        this.dynamicProperties = new HashMap<>();
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public void setPlayerName(final String playerName) {
        this.playerName = playerName;
    }

    public long getXuid() {
        return this.xuid;
    }

    public void setXuid(final long xuid) {
        this.xuid = xuid;
    }

    public long getFirstJoin() {
        return this.firstJoin;
    }

    public long getLastJoin() {
        return this.lastJoin;
    }

    public void setLastJoin(final long lastJoin) {
        this.lastJoin = lastJoin;
    }

    public long getLastQuit() {
        return this.lastQuit;
    }

    public List<String> getOldNames() {
        return this.oldNames;
    }

    public void setLastQuit(final long lastQuit) {
        this.lastQuit = lastQuit;
    }

    public long getPlaytime() {
        return this.playtime;
    }

    public void addPlaytime(final long millis) {
        this.playtime += millis;
    }

    public long getDeaths() {
        return this.deaths;
    }

    public void addDeaths(final long deaths) {
        this.deaths += deaths;
    }

    public long getBlockPlaced() {
        return this.blockPlaced;
    }

    public void addBlockPlaced(final long blockPlaced) {
        this.blockPlaced += blockPlaced;
    }

    public long getBlockBroken() {
        return this.blockBroken;
    }

    public long getLoginStreak() {
        return this.loginStreak;
    }

    public void setLoginStreak(final long loginStreak) {
        this.loginStreak = loginStreak;
    }

    public long getLongestLoginStreak() {
        return this.longestLoginStreak;
    }

    public void setLongestLoginStreak(final long longestLoginStreak) {
        this.longestLoginStreak = longestLoginStreak;
    }

    public void addBlockBroken(final long blockBreak) {
        this.blockBroken += blockBreak;
    }

    public DeviceOS getLastDevice() {
        return this.lastDevice;
    }

    public void setLastDevice(final DeviceOS lastDevice) {
        this.lastDevice = lastDevice;
    }

    public Controller getLastController() {
        return this.lastController;
    }

    public void setLastController(final Controller lastController) {
        this.lastController = lastController;
    }

    public void setDynamicProperties(final String key, final Object object) {
        this.dynamicProperties.put(key, object);
    }

    public Object getDynamicProperties(final String key) {
        return this.dynamicProperties.get(key);
    }

    public boolean hasDynamicProperty(final String key) {
        return this.dynamicProperties.containsKey(key);
    }

    public boolean hasDynamicProperty(final Object object) {
        return this.dynamicProperties.containsValue(object);
    }

    public Map<String, Object> getDynamicProperties() {
        return this.dynamicProperties;
    }

    @Override
    public String toString() {
        return "PlayerStatistics(" +
                "playerName='" + this.playerName + '\'' +
                ", xuid=" + this.xuid +
                ", firstJoin=" + this.firstJoin +
                ", lastJoin=" + this.lastJoin +
                ", lastQuit=" + this.lastQuit +
                ", oldNames=" + this.oldNames +
                ", playtime=" + this.playtime +
                ", deaths=" + this.deaths +
                ", blockPlaced=" + this.blockPlaced +
                ", blockBroken=" + this.blockBroken +
                ", lastDevice=" + this.lastDevice +
                ", lastController=" + this.lastController +
                ", dynamicProperties=" + this.dynamicProperties +
                ')';
    }
}
