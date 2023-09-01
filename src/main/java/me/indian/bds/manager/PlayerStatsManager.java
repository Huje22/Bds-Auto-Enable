package me.indian.bds.manager;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerStatsManager {

    private final Logger logger;
    private final Timer playTimeTimer;
    private final File playTimeJson, deathsJson;
    private final Map<String, Long> playTime, deaths;
    private final PlayerManager playerManager;

    public PlayerStatsManager(final BDSAutoEnable bdsAutoEnable) {
        this.logger = bdsAutoEnable.getLogger();
        this.playTimeTimer = new Timer("PlayTime", true);
        this.playTimeJson = new File(Defaults.getAppDir() + File.separator + "stats" + File.separator + "playtime.json");
        this.deathsJson = new File(Defaults.getAppDir() + File.separator + "stats" + File.separator + "deaths.json");
        this.createJsons();
        this.playTime = this.loadPlayTime();
        this.deaths = this.loadDeaths();
        this.playerManager = bdsAutoEnable.getPlayerManager();
    }

    public void countPlayTime() {
        final long second = MathUtil.secondToMilliseconds(1);
        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                for (final String playerName : PlayerStatsManager.this.playerManager.getOnlinePlayers()) {
                    PlayerStatsManager.this.playTime.put(playerName, PlayerStatsManager.this.getPlayTimeByName(playerName) + second);
                }
            }
        };
        this.playTimeTimer.scheduleAtFixedRate(timerTask, 0, second);
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
            this.logger.critical("Nie udało się zapisac&b czasu gry&r graczy");
            exception.printStackTrace();
        }
    }

    private void saveDeaths() {
        try (final FileWriter writer = new FileWriter(this.deathsJson)) {
            writer.write(GsonUtil.getGson().toJson(this.deaths));
            this.logger.info("Pomyślnie zapisano&r liczbe&b śmierci&r graczy");
        } catch (final IOException exception) {
            this.logger.critical("Nie udało się zapisac liczby&b śmierci&r graczy");
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
            this.logger.critical("Nie udało się załadować ilości&b śmierci&r graczy");
            exception.printStackTrace();
        }
        return new HashMap<>();
    }

    private void createJsons() {
        if (!this.playTimeJson.exists()) {
            try {
                if (!this.playTimeJson.exists()) {
                    Files.createDirectories(this.playTimeJson.getParentFile().toPath());
                    if (!this.playTimeJson.createNewFile()) {
                        this.logger.critical("Nie można utworzyć&b playtime.json");
                    }
                }
            } catch (final IOException exception) {
                this.logger.critical("Nie udało się utworzyć&b playtime.json");
                exception.printStackTrace();
            }
        }

        if (!this.deathsJson.exists()) {
            try {
                if (!this.deathsJson.exists()) {
                    Files.createDirectories(this.deathsJson.getParentFile().toPath());
                    if (!this.deathsJson.createNewFile()) {
                        this.logger.critical("Nie udało się  utworzyć&b playtime.json");
                    }
                }
            } catch (final IOException exception) {
                this.logger.critical("Nie można utworzyć&b deaths.json");
                exception.printStackTrace();
            }
        }
    }
}
