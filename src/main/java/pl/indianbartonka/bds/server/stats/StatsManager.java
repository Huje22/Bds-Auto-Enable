package pl.indianbartonka.bds.server.stats;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.jetbrains.annotations.Nullable;
import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.player.PlayerStatistics;
import pl.indianbartonka.bds.player.position.Dimension;
import pl.indianbartonka.bds.server.ServerManager;
import pl.indianbartonka.bds.server.ServerProcess;
import pl.indianbartonka.bds.util.DefaultsVariables;
import pl.indianbartonka.bds.util.GsonUtil;
import pl.indianbartonka.util.DateUtil;
import pl.indianbartonka.util.FileUtil;
import pl.indianbartonka.util.logger.Logger;

public class StatsManager {

    private final Logger logger;
    private final Timer playerStatsManagerTimer;
    private final File statsFolder, statsJson, serverStatsJson;
    private final List<PlayerStatistics> playerStats;
    private final Map<String, Long> lastJoin, lastQuit, playtime, deaths, blockPlaced, blockBroken;
    private final ServerManager serverManager;
    private final ServerStats serverStats;
    private final Gson gson;
    private final Lock playerCreateLock;
    private boolean countingTime;

    public StatsManager(final BDSAutoEnable bdsAutoEnable, final ServerManager serverManager) {
        this.logger = bdsAutoEnable.getLogger();
        this.playerStatsManagerTimer = new Timer("PlayerStatsMonitorTimer", true);
        this.statsFolder = new File(DefaultsVariables.getAppDir() + "stats");
        this.statsJson = new File(this.statsFolder.getPath() + File.separator + "stats.json");
        this.serverStatsJson = new File(this.statsFolder.getPath() + File.separator + "server.json");
        this.createFiles();
        this.playerStats = this.loadPlayerStats();
        this.lastJoin = new HashMap<>();
        this.lastQuit = new HashMap<>();
        this.playtime = new HashMap<>();
        this.deaths = new HashMap<>();
        this.blockPlaced = new HashMap<>();
        this.blockBroken = new HashMap<>();
        this.serverManager = serverManager;
        this.serverStats = this.loadServerStats();
        this.gson = GsonUtil.getGson();
        this.playerCreateLock = new ReentrantLock();

        this.startTasks();
    }

    private void startTasks() {
        final long second = DateUtil.secondToMillis(1);
        final TimerTask playTimeTask = new TimerTask() {
            @Override
            public void run() {
                for (final String playerName : StatsManager.this.serverManager.getOnlinePlayers()) {
                    final PlayerStatistics player = StatsManager.this.getPlayer(playerName);
                    if (player != null) {
                        player.addPlaytime(second);
                    }
                }
            }
        };

        final TimerTask saveDataTask = new TimerTask() {
            @Override
            public void run() {
                StatsManager.this.saveAllData();
            }
        };

        this.playerStatsManagerTimer.scheduleAtFixedRate(saveDataTask, DateUtil.hoursTo(1, TimeUnit.MILLISECONDS), DateUtil.minutesTo(30, TimeUnit.MILLISECONDS));
        this.playerStatsManagerTimer.scheduleAtFixedRate(playTimeTask, 0, second);
    }

    public void startCountServerTime(final ServerProcess serverProcess) {
        if (this.countingTime) return;


        final long second = DateUtil.secondToMillis(1);
        final TimerTask serverTimeTask = new TimerTask() {
            @Override
            public void run() {
                if (serverProcess.isEnabled()) {
                    StatsManager.this.serverStats.addOnlineTime(second);
                }
            }
        };

        this.playerStatsManagerTimer.scheduleAtFixedRate(serverTimeTask, 0, second);
        this.countingTime = true;
    }

    public void createNewPlayer(final String playerName, final long xuid) {
        try {
            this.playerCreateLock.lock();
            if (this.getPlayer(xuid) == null) {
                this.playerStats.add(new PlayerStatistics(playerName,
                        xuid, Dimension.OVERWORLD, DateUtil.localDateTimeToMillis(LocalDateTime.now()), 0, 0, 0, 0, 0, 0));
                this.logger.debug("Utworzono gracza:&b " + playerName);
            }
        } catch (final Exception exception) {
            this.logger.error("&cNie udało się utworzyć gracza&b " + playerName + " &d(&b" + xuid + "&d)", exception);
        } finally {
            this.playerCreateLock.unlock();
        }
    }

    @Nullable
    public PlayerStatistics getPlayer(final String playerName) {
        for (final PlayerStatistics player : this.playerStats) {
            if (player.getPlayerName().equalsIgnoreCase(playerName)) {
                return player;
            }
        }
        return null;
    }

    @Nullable
    public PlayerStatistics getPlayer(final long xuid) {
        for (final PlayerStatistics player : this.playerStats) {
            if (player.getXuid() == xuid) {
                return player;
            }
        }
        return null;
    }

    public void setNewName(final long xuid, final String newName) {
        final PlayerStatistics player = this.getPlayer(xuid);
        if (player != null) {
            player.setPlayerName(newName);
        }
    }

    public void addOldName(final long xuid, final String oldName) {
        final PlayerStatistics player = this.getPlayer(xuid);
        if (player != null) {
            player.getOldNames().add(oldName);
        }
    }

    @Nullable
    public List<String> getOldNames(final String playerName) {
        final PlayerStatistics player = this.getPlayer(playerName);
        if (player != null) {
            return player.getOldNames();
        }
        return null;
    }

    public long getXuid(final String playerName) {
        final PlayerStatistics player = this.getPlayer(playerName);
        return (player != null ? player.getXuid() : -1);
    }

    public void setXuid(final String playerName, final long xuid) {
        final PlayerStatistics player = this.getPlayer(playerName);
        if (player != null) {
            player.setXuid(xuid);
        }
    }

    @Nullable
    public String getNameByXuid(final long xuid) {
        final PlayerStatistics player = this.getPlayer(xuid);
        return (player != null ? player.getPlayerName() : null);
    }

    public long getXuidByName(final String name) {
        final PlayerStatistics player = this.getPlayer(name);
        return (player != null ? player.getXuid() : -1);
    }

    public long getLastQuit(final String playerName) {
        final PlayerStatistics player = this.getPlayer(playerName);
        return (player != null ? player.getLastQuit() : -1);
    }

    public long getPlayTime(final String playerName) {
        final PlayerStatistics player = this.getPlayer(playerName);
        return (player != null ? player.getPlaytime() : -1);
    }

    public long getDeaths(final String playerName) {
        final PlayerStatistics player = this.getPlayer(playerName);
        return (player != null ? player.getDeaths() : -1);
    }

    public long getBlockPlaced(final String playerName) {
        final PlayerStatistics player = this.getPlayer(playerName);
        return (player != null ? player.getBlockPlaced() : -1);
    }

    public long getBlockBroken(final String playerName) {
        final PlayerStatistics player = this.getPlayer(playerName);
        return (player != null ? player.getBlockBroken() : -1);
    }

    public void updateLoginStreak(final PlayerStatistics playerStatistics, final long loginTime) {
        //TODO: Zrobić to na zasadzie czasu sekund itp
        final LocalDate lastLoginDate = DateUtil.millisToLocalDateTime(playerStatistics.getLastJoin()).toLocalDate();
        final LocalDate loginDate = DateUtil.millisToLocalDateTime(loginTime).toLocalDate();
        final long loginStreak = playerStatistics.getLoginStreak();
        final long longestLoginStreak = playerStatistics.getLongestLoginStreak();

        if (lastLoginDate.plusDays(1).equals(loginDate)) {
            playerStatistics.setLoginStreak(loginStreak + 1);
        } else if (!lastLoginDate.equals(loginDate) && !lastLoginDate.equals(LocalDate.now())) {
            playerStatistics.setLoginStreak(1);
        }

        if (loginStreak > longestLoginStreak) {
            playerStatistics.setLongestLoginStreak(loginStreak);
        }

        playerStatistics.setLastJoin(loginTime);
    }

    public ServerStats getServerStats() {
        return this.serverStats;
    }

    public Map<String, Long> getLastJoin() {
        return this.lastJoin;
    }

    public Map<String, Long> getLastQuit() {
        return this.lastQuit;
    }

    public Map<String, Long> getPlaytime() {
        return this.playtime;
    }

    public Map<String, Long> getPlayTime() {
        this.playerStats.forEach(player -> this.playtime.put(player.getPlayerName(), player.getPlaytime()));
        return this.playtime;
    }

    public Map<String, Long> getDeaths() {
        this.playerStats.forEach(player -> this.deaths.put(player.getPlayerName(), player.getDeaths()));
        return this.deaths;
    }

    public Map<String, Long> getBlockPlaced() {
        this.playerStats.forEach(player -> this.blockPlaced.put(player.getPlayerName(), player.getBlockPlaced()));
        return this.blockPlaced;
    }

    public Map<String, Long> getBlockBroken() {
        this.playerStats.forEach(player -> this.blockBroken.put(player.getPlayerName(), player.getBlockBroken()));
        return this.blockBroken;
    }

    public void saveAllData() {
        this.savePlayerStats();
        this.saveServerStats();
    }

    private void savePlayerStats() {
        try (final FileWriter writer = new FileWriter(this.statsJson)) {
            writer.write(this.gson.toJson(this.playerStats));
            this.logger.info("Pomyślnie zapisano&b statystyki&r graczy");
        } catch (final Exception exception) {
            this.logger.error("Nie udało się zapisać&b statystyk&r graczy", exception);
        }
    }

    private void saveServerStats() {
        try (final FileWriter writer = new FileWriter(this.serverStatsJson)) {
            writer.write(this.gson.toJson(this.serverStats));
            this.logger.info("Pomyślnie zapisano&b statystyki servera");
        } catch (final Exception exception) {
            this.logger.error("Nie udało się zapisać liczby&b statystyk servera", exception);
        }
    }

    private List<PlayerStatistics> loadPlayerStats() {
        try (final FileReader reader = new FileReader(this.statsJson)) {
            final TypeToken<List<PlayerStatistics>> type = new TypeToken<>() {
            };
            final List<PlayerStatistics> loadedPlayers = GsonUtil.getGson().fromJson(reader, type);
            return (loadedPlayers == null ? new ArrayList<>() : loadedPlayers);
        } catch (final IOException | JsonSyntaxException exception) {
            this.logger.error("Nie udało się załadować&b statystyk&r graczy", exception);
        }
        return new ArrayList<>();
    }

    private ServerStats loadServerStats() {
        try (final FileReader reader = new FileReader(this.serverStatsJson)) {
            final ServerStats loadedStats = GsonUtil.getGson().fromJson(reader, ServerStats.class);
            if (loadedStats != null) return loadedStats;
        } catch (final Exception exception) {
            this.logger.error("Nie udało się załadować statystyk serwera", exception);
        }
        return new ServerStats(0);
    }

    private void createFiles() {
        if (!this.statsFolder.exists()) {
            try {
                if (!this.statsFolder.mkdir()) if (!this.statsFolder.mkdirs()) {
                    this.logger.error("Nie udało się utworzyć folderu statystyk!");
                }
            } catch (final Exception exception) {
                this.logger.error("Nie udało się utworzyć folderu statystyk!", exception);
            }
        }

        final File infoFile = new File(this.statsFolder.toPath() + File.separator + "readme.txt");
        if (!infoFile.exists()) {
            try {
                FileUtil.writeText(infoFile,
                        List.of("Jeśli aplikacja jest włączony edycja tych JSON nic nie da :D.",
                                "Dane są zapisywane do pliku co 30 minut albo co restart servera."));
            } catch (final IOException ioException) {
                this.logger.debug("Nie udało się stworzyć pliku informacyjnego w katalogu&b stats&r", ioException);
            }
        }

        if (!this.statsJson.exists()) {
            try {
                if (!this.statsJson.createNewFile()) {
                    this.logger.error("Nie można utworzyć&b stats.json");
                }
            } catch (final Exception exception) {
                this.logger.error("Nie udało się utworzyć&b stats.json", exception);
            }
        }

        if (!this.serverStatsJson.exists()) {
            try {
                if (!this.serverStatsJson.createNewFile()) {
                    this.logger.error("Nie udało się utworzyć&b server.json");
                }
            } catch (final Exception exception) {
                this.logger.error("Nie można utworzyć&b server.json", exception);
            }
        }
    }
}