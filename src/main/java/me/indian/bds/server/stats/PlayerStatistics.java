package me.indian.bds.server.stats;

import java.io.Serializable;

public class PlayerStatistics implements Serializable {

    private String playerName;
    private long xuid;
    private long lastJoin, lastQuit;
    private long playtime, deaths, blockPlaced, blockBroken;

    public PlayerStatistics(final String playerName, final long xuid, final long lastJoin, final long lastQuit, final long playtime, final long deaths, final long blockPlaced, final long blockBroken) {
        this.playerName = playerName;
        this.xuid = xuid;
        this.lastJoin = lastJoin;
        this.lastQuit = lastQuit;
        this.playtime = playtime;
        this.deaths = deaths;
        this.blockPlaced = blockPlaced;
        this.blockBroken = blockBroken;
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

    public long getLastJoin() {
        return this.lastJoin;
    }

    public void setLastJoin(final long lastJoin) {
        this.lastJoin = lastJoin;
    }

    public long getLastQuit() {
        return this.lastQuit;
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

    public void addBlockBroken(final long blockBreak) {
        this.blockBroken += blockBreak;
    }
}
