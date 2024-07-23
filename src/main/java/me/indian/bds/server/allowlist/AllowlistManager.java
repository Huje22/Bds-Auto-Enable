package me.indian.bds.server.allowlist;

import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.AppConfig;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.allowlist.component.AllowlistPlayer;
import me.indian.bds.util.GsonUtil;
import org.jetbrains.annotations.Nullable;

public class AllowlistManager {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final File allowList;
    private List<AllowlistPlayer> allowlistPlayers;

    public AllowlistManager(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        final AppConfig appConfig = this.bdsAutoEnable.getAppConfigManager().getAppConfig();
        this.allowList = new File(appConfig.getFilesPath() + File.separator + "allowlist.json");
        this.createAllowList(this.allowList);
        this.allowlistPlayers = this.loadPlayers();
    }

    private List<AllowlistPlayer> loadPlayers() {
        try (final FileReader reader = new FileReader(this.allowList)) {
            final TypeToken<List<AllowlistPlayer>> token = new TypeToken<>() {
            };

            return GsonUtil.getGson().fromJson(reader, token);
        } catch (final IOException exception) {
            this.logger.critical("&cNie udało się załadować&b allowlist", exception);
        }
        return new LinkedList<>();
    }

    public void saveAllowlist() {
        try (final FileWriter writer = new FileWriter(this.allowList)) {
            writer.write(GsonUtil.getGson().toJson(this.allowlistPlayers));
        } catch (final IOException exception) {
            this.logger.critical("&cNie udało się zapisać&b allowlist", exception);
        }
    }

    public void reloadAllowlist() {
        this.bdsAutoEnable.getServerProcess().sendToConsole("allowlist reload");
    }

    public boolean isOnAllowList(final String name) {
        return this.allowlistPlayers.stream().anyMatch(player -> player.name().equals(name));
    }

    @Nullable
    public AllowlistPlayer getPlayer(final String name) {
        return this.allowlistPlayers.stream()
                .filter(player -> player.name().equals(name))
                .findFirst()
                .orElse(null);
    }

    public void addPlayer(final AllowlistPlayer player) {
        if (this.allowlistPlayers.isEmpty()) {
            this.allowlistPlayers = this.loadPlayers();
        }
        this.allowlistPlayers.add(player);
    }

    public void addPlayerByName(final String playerName) {
        if (this.allowlistPlayers.isEmpty()) {
            this.allowlistPlayers = this.loadPlayers();
        }
        this.allowlistPlayers.add(new AllowlistPlayer(false, playerName, 0));
    }

    public void removePlayer(final AllowlistPlayer player) {
        this.allowlistPlayers.remove(player);
    }

    public List<AllowlistPlayer> getAllowlistPlayers() {
        return this.allowlistPlayers;
    }

    private void createAllowList(final File allowList) {
        try {
            if (!allowList.exists()) {
                if (!allowList.createNewFile()) {
                    this.logger.critical("&cNie znaleziono&b allowlist.json&c a nie udało się jej utworzyć");
                }
            }
        } catch (final IOException exception) {
            this.logger.critical("&cNie znaleziono&b allowlist.json&c a nie udało się jej utworzyć", exception);
        }
    }
}