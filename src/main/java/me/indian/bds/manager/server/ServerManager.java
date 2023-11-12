package me.indian.bds.manager.server;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.discord.DiscordIntegration;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.util.StatusUtil;
import me.indian.bds.util.ThreadUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ServerManager {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final DiscordIntegration discord;
    private final ExecutorService service;
    private final List<String> onlinePlayers, offlinePlayers;
    private final StatsManager statsManager;
    private int lastTPS;

    public ServerManager(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.discord = this.bdsAutoEnable.getDiscord();
        this.service = Executors.newScheduledThreadPool(2, new ThreadUtil("Player Manager"));
        this.onlinePlayers = new ArrayList<>();
        this.offlinePlayers = new ArrayList<>();
        this.statsManager = new StatsManager(this.bdsAutoEnable, this);
        this.lastTPS = 20;
    }

    public void initFromLog(final String logEntry) {
        this.service.execute(() -> {
            //Metody związane z graczem
            this.playerJoin(logEntry);
            this.playerQuit(logEntry);
            this.deathMessage(logEntry);
            this.chatMessage(logEntry);
            this.customCommand(logEntry);

            //Dodatkowe metody
            this.serverEnabled(logEntry);
            this.checkPackDependency(logEntry);
            this.tps(logEntry);
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
            final String playerName = MessageUtil.fixMessage(matcher.group(1));
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
            final String playerName = MessageUtil.fixMessage(matcher.group(1));
            this.onlinePlayers.add(playerName);
            this.offlinePlayers.remove(playerName);
            this.discord.sendJoinMessage(playerName);
        }
    }

    private void chatMessage(final String logEntry) {
        final String patternString = "PlayerChat:([^,]+) Message:(.+)";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerChat = MessageUtil.fixMessage(matcher.group(1));
            final String message = MessageUtil.fixMessage(matcher.group(2));
            this.discord.sendPlayerMessage(playerChat, message);
        }
    }

    private void customCommand(final String logEntry) {
        final String patternString = "PlayerCommand:([^,]+) Command:(.+)";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerCommand = MessageUtil.fixMessage(matcher.group(1));
            final String command = MessageUtil.fixMessage(matcher.group(2));
            this.handleCustomCommand(playerCommand, command);
        }
    }

    private void deathMessage(final String logEntry) {
        final String patternString = "PlayerDeath:([^,]+) Casue:(.+)";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerDeath = MessageUtil.fixMessage(matcher.group(1));
            final String casue = MessageUtil.fixMessage(matcher.group(2));
            this.discord.sendDeathMessage(playerDeath, casue);
            this.statsManager.addDeaths(playerDeath, 1);
        }
    }

    private void tps(final String logEntry) {
        final String patternString = "TPS: (\\d{1,4})";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String tpsString = matcher.group(1);
            final int tps = Integer.parseInt(tpsString);

            if (tps <= 8) {
                this.discord.sendMessage("Server posiada: **" + tps + "** TPS");
            }

            if (this.lastTPS <= 8 && tps <= 8) {
                this.discord.sendMessage("Zaraz nastąpi restartowanie servera z powodu niskiej ilości TPS" + " (Teraz: **" + tps + "** Ostatnie: **" + this.lastTPS + "**)");
                this.bdsAutoEnable.getWatchDog().getAutoRestartModule().restart(true);
            }

            this.lastTPS = tps;
        }
    }

    private void serverEnabled(final String logEntry) {
        if (logEntry.contains("Server started")) {
            this.discord.sendEnabledMessage();
            this.lastTPS = 20;
        }
    }

    private void checkPackDependency(final String logEntry) {
        if (logEntry.contains("BDS Auto Enable") && logEntry.contains("requesting dependency on beta APIs")) {
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
        if (logEntry.contains("BDS Auto Enable") && logEntry.contains("requesting invalid module version")) {
            this.discord.sendEmbedMessage("Zła wersja paczki",
                    """
                            Posiadasz złą wersje paczki!
                             Usuń a nowa pobierze się sama\s
                             Ewentualnie twój server wymaga aktualizacji
                            **Bot zostaje przez to wyłączony**""",
                    "Zła wersja paczki");
            this.discord.sendMessage("<owner>");
            this.discord.disableBot();
        }
    }

    private void handleCustomCommand(final String playerCommand, final String command) {
        final ServerProcess serverProcess = this.bdsAutoEnable.getServerProcess();
        if (serverProcess == null) {
            this.logger.debug("&bServerProcess&r jest&c null");
            return;
        }

        // !tps jest handlowane w https://github.com/Huje22/BDS-Auto-Enable-Managment-Pack

        if (command.contains("help")) {
            serverProcess.tellrawToPlayer(playerCommand, "&a---------------------");
            serverProcess.tellrawToPlayer(playerCommand, "&a!help&4 -&b pomocna lista komend");
            serverProcess.tellrawToPlayer(playerCommand, "&a!tps&4 -&b Ticki na sekunde servera");
            serverProcess.tellrawToPlayer(playerCommand, "&a!playtime&4 -&b Top 10 graczy z największym czasem gry");
            serverProcess.tellrawToPlayer(playerCommand, "&a!deaths&4 -&b Top 10 graczy z największą ilością śmierci");
            serverProcess.tellrawToPlayer(playerCommand, "&a---------------------");

        } else if (command.contains("playtime")) {
            serverProcess.tellrawToPlayer(playerCommand, "&a---------------------");
            for (final String s : StatusUtil.getTopPlayTime(false, 10)) {
                serverProcess.tellrawToPlayer(playerCommand, s);
            }
            serverProcess.tellrawToPlayer(playerCommand, "&a---------------------");

        } else if (command.contains("deaths")) {
            serverProcess.tellrawToPlayer(playerCommand, "&a---------------------");
            for (final String s : StatusUtil.getTopDeaths(false, 10)) {
                serverProcess.tellrawToPlayer(playerCommand, s);
            }
            serverProcess.tellrawToPlayer(playerCommand, "&a---------------------");
        } else {
            serverProcess.tellrawToPlayer(playerCommand, "&cNie znaleziono takiego polecenia");
        }
    }

    public StatsManager getStatsManager() {
        return this.statsManager;
    }

    public void clearPlayers() {
        this.onlinePlayers.clear();
    }

    public int getLastTPS() {
        return this.lastTPS;
    }

    public List<String> getOnlinePlayers() {
        return this.onlinePlayers;
    }

    public List<String> getOfflinePlayers() {
        return this.offlinePlayers;
    }
}
