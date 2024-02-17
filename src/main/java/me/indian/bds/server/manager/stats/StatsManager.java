package me.indian.bds.server.manager.stats;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.server.ServerStats;
import me.indian.bds.server.manager.ServerManager;
import me.indian.bds.util.DefaultsVariables;
import me.indian.bds.util.GsonUtil;
import me.indian.bds.util.MathUtil;
import org.jetbrains.annotations.Nullable;

public class StatsManager {

    private final Logger logger;
    private final Timer playerStatsManagerTimer;
    private final File statsFolder, statsJson, serverStatsJson;
    private final List<PlayerStatistics> playerStats;
    private final Map<String, Long> lastJoin, lastQuit, playtime, deaths, blockPlaced, blockBroken;
    private final ServerManager serverManager;
    private final ServerStats serverStats;
    private final Gson gson;

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

        this.startTasks();
    }

    private void startTasks() {
        final long second = MathUtil.secondToMillis(1);
        final TimerTask playTimeTask = new TimerTask() {
            @Override
            public void run() {
                for (final String playerName : StatsManager.this.serverManager.getOnlinePlayers()) {
                    final PlayerStatistics player = StatsManager.this.getByName(playerName);
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

        this.playerStatsManagerTimer.scheduleAtFixedRate(saveDataTask, MathUtil.hoursTo(1, TimeUnit.MILLISECONDS), MathUtil.minutesTo(30, TimeUnit.MILLISECONDS));
        this.playerStatsManagerTimer.scheduleAtFixedRate(playTimeTask, 0, second);
    }

    public void startCountServerTime(final ServerProcess serverProcess) {
        final long second = MathUtil.secondToMillis(1);
        final TimerTask serverTimeTask = new TimerTask() {
            @Override
            public void run() {
                if (serverProcess.isEnabled()) {
                    StatsManager.this.serverStats.addOnlineTime(second);
                }
            }
        };

        this.playerStatsManagerTimer.scheduleAtFixedRate(serverTimeTask, 0, second);
    }

    public void createNewPlayer(final String playerName) {
        if (this.getByName(playerName) == null) {
            this.playerStats.add(new PlayerStatistics(playerName, 0, 0, 0, 0, 0, 0));
            this.logger.debug("Utworzono gracza:&b " + playerName);
        }
    }

    @Nullable
    private PlayerStatistics getByName(final String playerName) {
        for (final PlayerStatistics player : this.playerStats) {
            if (player.getPlayerName().equalsIgnoreCase(playerName)) {
                return player;
            }
        }
        return null;
    }

    public long getLastJoin(final String playerName) {
        final PlayerStatistics player = this.getByName(playerName);
        return (player != null ? player.getLastJoin() : 0);
    }

    public void setLastJoin(final String playerName, final long date) {
        final PlayerStatistics player = this.getByName(playerName);
        if (player != null) {
            player.setLastJoin(date);
        }
    }

    public long getLastQuit(final String playerName) {
        final PlayerStatistics player = this.getByName(playerName);
        return (player != null ? player.getLastQuit() : 0);
    }

    public void setLastQuit(final String playerName, final long date) {
        final PlayerStatistics player = this.getByName(playerName);
        if (player != null) {
            player.setLastQuit(date);
        }
    }

    public long getPlayTimeByName(final String playerName) {
        final PlayerStatistics player = this.getByName(playerName);
        return (player != null ? player.getPlaytime() : 0);
    }

    public long getDeathsByName(final String playerName) {
        final PlayerStatistics player = this.getByName(playerName);
        return (player != null ? player.getDeaths() : 0);
    }

    public void addDeaths(final String playerName, final long deaths) {
        final PlayerStatistics player = this.getByName(playerName);
        if (player != null) {
            player.addDeaths(deaths);
        }
    }

    public long getBlockPlacedByName(final String playerName) {
        final PlayerStatistics player = this.getByName(playerName);
        return (player != null ? player.getBlockPlaced() : 0);
    }

    public void addBlockPlaced(final String playerName, final long blockPlaced) {
        final PlayerStatistics player = this.getByName(playerName);
        if (player != null) {
            player.addBlockPlaced(blockPlaced);
        }
    }

    public long getBlockBrokenByName(final String playerName) {
        final PlayerStatistics player = this.getByName(playerName);
        return (player != null ? player.getBlockBroken() : 0);
    }

    public void addBlockBroken(final String playerName, final long blockBroken) {
        final PlayerStatistics player = this.getByName(playerName);
        if (player != null) {
            player.addBlockBroken(blockBroken);
        }
    }

    public ServerStats getServerStats() {
        return this.serverStats;
    }

    public Map<String, Long> getPlayTime() {
        this.playerStats.forEach(player -> this.playtime.put(player.getPlayerName(), player.getPlaytime()));
        return this.playtime;
    }

    public Map<String, Long> getDeaths() {
        this.playerStats.forEach(player -> {
            this.deaths.put(player.getPlayerName(), player.getDeaths());
        });
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
            final List<PlayerStatistics> loadedMap = GsonUtil.getGson().fromJson(reader, type);
            return (loadedMap == null ? new ArrayList<>() : loadedMap);
        } catch (final Exception exception) {
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

        final Path path = Path.of(this.statsFolder.toPath() + File.separator + "readme.txt");
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
                try (final FileWriter writer = new FileWriter(path.toFile())) {
                    writer.write("Jeśli aplikacja jest włączony edycja tych JSON nic nie da :D.");
                    writer.write("\nDane są zapisywane do pliku co 30 minut albo co restart servera.");
                }
            } catch (final Exception exception) {
                this.logger.debug("Nie udało się stworzyć pliku informacyjnego w katalogu&b stats&r", exception);
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