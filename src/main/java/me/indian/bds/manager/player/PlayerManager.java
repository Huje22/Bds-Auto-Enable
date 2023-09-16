package me.indian.bds.manager.player;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.discord.DiscordIntegration;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.util.ThreadUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PlayerManager {

    private final Logger logger;
    private final DiscordIntegration discord;
    private final ExecutorService service;
    private final List<String> onlinePlayers, offlinePlayers;
    private final StatsManager statsManager;

    public PlayerManager(final BDSAutoEnable bdsAutoEnable) {
        this.logger = bdsAutoEnable.getLogger();
        this.discord = bdsAutoEnable.getDiscord();
        this.service = Executors.newScheduledThreadPool(2, new ThreadUtil("Player Manager"));
        this.onlinePlayers = new ArrayList<>();
        this.offlinePlayers = new ArrayList<>();
        this.statsManager = new StatsManager(bdsAutoEnable, this);
    }

    public void initFromLog(final String logEntry) {
        this.service.execute(() -> {
            this.playerJoin(logEntry);
            this.playerQuit(logEntry);
            this.chatMessage(logEntry);
            this.deathMessage(logEntry);
            //Dodatkowe metody
            this.serverEnabled(logEntry);
            this.checkPackDependency(logEntry);
        });
    }

    /*
    TODO:
            Dodać do paczki generowanie kodu 5 znakowanego
            po wpisaniu na czacie !link
            i będzie to wypisywało do konsoli `PlayerLink:NAZWA Code:KOD`
            i będzie zapisywanie pierw do mapki,
            i potem gdy użytkownik wpiszę na Discord /link KOD
            wyszuka kod w mapce i jeśli kod będzie poprawny połączy
            weźmie jego discord id i przypisze w json NICKMC:IDDISCORD,
            pozwoli to na ustawianie nazwy z mc na discord i w dalszej przyszłości na
            Proximity Voice chat

*/

    private void playerQuit(final String logEntry) {
        final String patternString = "Player disconnected: ([^,]+)";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerName = matcher.group(1);
            this.onlinePlayers.remove(playerName);
            this.offlinePlayers.add(playerName);
            this.discord.sendLeaveMessage(playerName);
        }
    }

    private void playerJoin(final String logEntry) {
        final String patternString = "PlayerJoin:([^,]+)";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerName = matcher.group(1);
            this.onlinePlayers.add(playerName);
            this.offlinePlayers.remove(playerName);
            this.discord.sendJoinMessage(playerName);
        }
    }

    private void chatMessage(final String logEntry) {
        final String patternString = "PlayerChat:([^,]+) Message:([^,]+)";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerChat = matcher.group(1);
            final String message = matcher.group(2);
            this.discord.sendPlayerMessage(playerChat, message);
        }
    }

    private void deathMessage(final String logEntry) {
        final String patternString = "PlayerDeath:([^,]+) Casue:([^,]+)";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerDeath = matcher.group(1);
            final String casue = matcher.group(2);
            this.discord.sendDeathMessage(playerDeath, casue);
            this.statsManager.addDeaths(playerDeath, 1);
        }
    }

    private void serverEnabled(final String logEntry) {
        if (logEntry.contains("Server started")) {
            this.discord.sendEnabledMessage();
        }
    }

    private void checkPackDependency(final String logEntry) {
        if (logEntry.contains("requesting dependency on beta APIs [@minecraft/server - 1.4.0-beta]")) {
            final List<String> list = List.of("Wykryto że `Beta API's` nie są włączone!",
                    "Funkcje jak: `licznik czasu gry/śmierci` nie będą działać ",
                    "Bot też zostaje wyłączony"
            );
            this.discord.sendEmbedMessage("Brak wymaganych eksperymentów",
                    MessageUtil.listToSpacedString(list),
                    "Włącz Beta API's");
            this.discord.sendMessage("<owner>");
            for (final String s : list) {
                this.logger.alert(s.replaceAll("`", ""));
            }
            this.discord.disableBot();
        }
    }

    public StatsManager getStatsManager() {
        return this.statsManager;
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
}
