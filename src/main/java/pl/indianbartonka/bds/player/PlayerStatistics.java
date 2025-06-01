package pl.indianbartonka.bds.player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pl.indianbartonka.bds.player.position.Dimension;

public class PlayerStatistics implements Serializable {

    private final long firstJoin;
    private final List<String> oldNames;
    private final Map<String, Object> dynamicProperties;
    private String playerName;
    private long xuid;
    private Dimension dimension;
    private long lastJoin, lastQuit;
    private long playtime, deaths, blockPlaced, blockBroken, loginStreak, longestLoginStreak;
    private MemoryTier memoryTier;
    private int maxRenderDistance;
    private PlatformType platformType;
    private InputMode lastKnownInputMode;
    private GraphicsMode graphicsMode;

    public PlayerStatistics(final String playerName, final long xuid, final Dimension dimension, final long firstJoin, final long lastJoin, final long lastQuit, final long playtime, final long deaths, final long blockPlaced, final long blockBroken) {
        this.playerName = playerName;
        this.xuid = xuid;
        this.dimension = dimension;
        this.firstJoin = firstJoin;
        this.lastJoin = lastJoin;
        this.lastQuit = lastQuit;
        this.oldNames = new ArrayList<>();
        this.playtime = playtime;
        this.deaths = deaths;
        this.blockPlaced = blockPlaced;
        this.blockBroken = blockBroken;
        this.loginStreak = 0;
        this.memoryTier = MemoryTier.UNDETERMINED;
        this.maxRenderDistance = -1;
        this.longestLoginStreak = 0;
        this.platformType = PlatformType.UNKNOWN;
        this.lastKnownInputMode = InputMode.UNKNOWN;
        this.graphicsMode = GraphicsMode.UNKNOWN;
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

    public Dimension getDimension() {
        return this.dimension;
    }

    public void setDimension(final Dimension dimension) {
        this.dimension = dimension;
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

    public void setLastQuit(final long lastQuit) {
        this.lastQuit = lastQuit;
    }

    public List<String> getOldNames() {
        return this.oldNames;
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

    public MemoryTier getMemoryTier() {
        return this.memoryTier;
    }

    public void setMemoryTier(final MemoryTier memoryTier) {
        this.memoryTier = memoryTier;
    }

    public int getMaxRenderDistance() {
        return this.maxRenderDistance;
    }

    public void setMaxRenderDistance(final int maxRenderDistance) {
        this.maxRenderDistance = maxRenderDistance;
    }

    public void addBlockBroken(final long blockBreak) {
        this.blockBroken += blockBreak;
    }

    public PlatformType getPlatformType() {
        return this.platformType;
    }

    public void setPlatformType(final PlatformType platformType) {
        this.platformType = platformType;
    }

    public InputMode getLastKnownInputMode() {
        return this.lastKnownInputMode;
    }

    public void setLastKnownInputMode(final InputMode lastKnownInputMode) {
        this.lastKnownInputMode = lastKnownInputMode;
    }

    public GraphicsMode getGraphicsMode() {
        return this.graphicsMode;
    }

    public void setGraphicsMode(final GraphicsMode graphicsMode) {
        this.graphicsMode = graphicsMode;
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
                ", dimension=" + this.dimension +
                ", firstJoin=" + this.firstJoin +
                ", lastJoin=" + this.lastJoin +
                ", lastQuit=" + this.lastQuit +
                ", oldNames=" + this.oldNames +
                ", playtime=" + this.playtime +
                ", deaths=" + this.deaths +
                ", blockPlaced=" + this.blockPlaced +
                ", blockBroken=" + this.blockBroken +
                ", platformType=" + this.platformType +
                ", memoryTier=" + this.memoryTier +
                " , maxRenderDistance= " + this.maxRenderDistance +
                ", dynamicProperties=" + this.dynamicProperties +
                ')';
    }
}
