package me.indian.bds.manager.player;

import com.google.gson.reflect.TypeToken;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.Defaults;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.GsonUtil;
import me.indian.bds.util.MathUtil;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class StatsManager {

    private final Logger logger;
    private final Timer playerStatsManagerTimer;
    private final File statsFolder, playTimeJson, deathsJson;
    private final Map<String, Long> playTime, deaths;
    private final PlayerManager playerManager;
    private boolean timerWorking = false;

    public StatsManager(final BDSAutoEnable bdsAutoEnable, final PlayerManager playerManager) {
        this.logger = bdsAutoEnable.getLogger();
        this.playerStatsManagerTimer = new Timer("PlayerStatsMonitorTimer", true);
        this.statsFolder = new File(Defaults.getAppDir() + "stats");
        this.playTimeJson = new File(this.statsFolder.getPath() + File.separator + "playtime.json");
        this.deathsJson = new File(this.statsFolder.getPath() + File.separator + "deaths.json");
        this.createFiles();
        this.playTime = this.loadPlayTime();
        this.deaths = this.loadDeaths();
        this.playerManager = playerManager;
    }

    public void startTasks() {
        if (this.timerWorking) return;
        final long second = MathUtil.secondToMillis(1);
        final TimerTask playTimeTask = new TimerTask() {
            @Override
            public void run() {
                for (final String playerName : StatsManager.this.playerManager.getOnlinePlayers()) {
                    StatsManager.this.playTime.put(playerName, StatsManager.this.getPlayTimeByName(playerName) + second);
                }
            }
        };

        final TimerTask saveDataTask = new TimerTask() {
            @Override
            public void run() {
                StatsManager.this.saveAllData();
            }
        };

        this.playerStatsManagerTimer.scheduleAtFixedRate(saveDataTask, MathUtil.hoursToMillis(2), MathUtil.minutesToMilis(30));
        this.playerStatsManagerTimer.scheduleAtFixedRate(playTimeTask, 0, second);
        this.timerWorking = true;
    }

    public Map<String, Long> getPlayTime() {
        return this.playTime;
    }

    public Map<String, Long> getDeaths() {
        return this.deaths;
    }

    public long getPlayTimeByName(final String playerName) {
        return (this.playTime.get(playerName) == null ? 0 : this.playTime.get(playerName));
    }

    public long getDeathsByName(final String playerName) {
        return (this.deaths.get(playerName) == null ? 0 : this.deaths.get(playerName));
    }

    public void addDeaths(final String playerName, final long deaths) {
        this.deaths.put(playerName, this.getDeathsByName(playerName) + deaths);
    }

    public void saveAllData() {
        this.savePlayTime();
        this.saveDeaths();
    }

    private void savePlayTime() {
        try (final FileWriter writer = new FileWriter(this.playTimeJson)) {
            writer.write(GsonUtil.getGson().toJson(this.playTime));
            this.logger.info("Pomyślnie zapisano&b czas gry&r graczy");
        } catch (final IOException exception) {
            this.logger.critical("Nie udało się zapisać&b czasu gry&r graczy");
            exception.printStackTrace();
        }
    }

    private void saveDeaths() {
        try (final FileWriter writer = new FileWriter(this.deathsJson)) {
            writer.write(GsonUtil.getGson().toJson(this.deaths));
            this.logger.info("Pomyślnie zapisano&r liczbe&b śmierci&r graczy");
        } catch (final IOException exception) {
            this.logger.critical("Nie udało się zapisać liczby&b śmierci&r graczy");
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
            this.logger.critical("Nie udało się załadować&b czasu gry&r graczy");
            exception.printStackTrace();
        }
        return new HashMap<>();
    }

    private HashMap<String, Long> loadDeaths() {
        try (final FileReader reader = new FileReader(this.deathsJson)) {
            final Type type = new TypeToken<HashMap<String, Long>>() {
            }.getType();
            final HashMap<String, Long> loadedMap = GsonUtil.getGson().fromJson(reader, type);
            return (loadedMap == null ? new HashMap<>() : loadedMap);
        } catch (final IOException exception) {
            this.logger.critical("Nie udało się załadować liczby&b śmierci&r graczy");
            exception.printStackTrace();
        }
        return new HashMap<>();
    }

    private void createFiles() {
        if (!this.statsFolder.exists()) {
            try {
                if (!this.statsFolder.mkdir()) if (!this.statsFolder.mkdirs()) {
                    this.logger.critical("Nie udało się utworzyć folderu statystyk!");
                }
            } catch (final Exception exception) {
                this.logger.critical("Nie udało się utworzyć folderu statystyk!");
                exception.printStackTrace();
            }
        }

        final Path path = Path.of(this.statsFolder.toPath() + File.separator + "readme.txt");
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
                try (final FileWriter writer = new FileWriter(path.toFile())) {
                    writer.write("Jeśli aplikacja jest włączony edycja tych JSON nic nie da :D.");
                    writer.write("\nDane są zapisywane do pliku co 2 godziny albo co restart servera.");
                }
            } catch (final IOException exception) {
                this.logger.debug("Nie udało się stworzyć pliku informacyjnego w katalogu&b stats&r" + exception);
            }
        }

        if (!this.playTimeJson.exists()) {
            try {
                if (!this.playTimeJson.createNewFile()) {
                    this.logger.critical("Nie można utworzyć&b playtime.json");
                }
            } catch (final IOException exception) {
                this.logger.critical("Nie udało się utworzyć&b playtime.json");
                exception.printStackTrace();
            }
        }

        if (!this.deathsJson.exists()) {
            try {
                if (!this.deathsJson.createNewFile()) {
                    this.logger.critical("Nie udało się  utworzyć&b playtime.json");
                }
            } catch (final IOException exception) {
                this.logger.critical("Nie można utworzyć&b deaths.json");
                exception.printStackTrace();
            }
        }
    }
}
