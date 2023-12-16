package me.indian.bds.server.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.CommandSender;
import me.indian.bds.config.AppConfigManager;
import me.indian.bds.config.sub.EventsConfig;
import me.indian.bds.discord.DiscordIntegration;
import me.indian.bds.discord.component.Footer;
import me.indian.bds.discord.jda.DiscordJda;
import me.indian.bds.discord.jda.manager.LinkingManager;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.watchdog.module.PackModule;


public class ServerManager {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final AppConfigManager appConfigManager;
    private final EventsConfig eventsConfig;
    private final DiscordIntegration discord;
    private final ExecutorService service, chatService, eventServcie;
    private final List<String> onlinePlayers, offlinePlayers, muted;
    private final StatsManager statsManager;
    private ServerProcess serverProcess;
    private int lastTPS;

    public ServerManager(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.appConfigManager = this.bdsAutoEnable.getAppConfigManager();
        this.eventsConfig = this.appConfigManager.getEventsConfig();
        this.discord = this.bdsAutoEnable.getDiscord();
        this.service = Executors.newScheduledThreadPool(2, new ThreadUtil("Player Manager"));
        this.chatService = Executors.newSingleThreadExecutor(new ThreadUtil("Chat Service"));
        this.eventServcie = Executors.newSingleThreadExecutor(new ThreadUtil("Event Service"));
        this.onlinePlayers = new ArrayList<>();
        this.offlinePlayers = new ArrayList<>();
        this.muted = new ArrayList<>();
        this.statsManager = new StatsManager(this.bdsAutoEnable, this);
        this.lastTPS = 20;
    }

    public void init() {
        this.serverProcess = this.bdsAutoEnable.getServerProcess();
    }

    public void initFromLog(final String logEntry) {
        this.chatMessage(logEntry);

        this.service.execute(() -> {
            //Metody związane z graczem
            this.playerJoin(logEntry);
            this.playerQuit(logEntry);
            this.playerSpawn(logEntry);
            this.deathMessage(logEntry);
            this.customCommand(logEntry);

            //Dodatkowe metody
            this.serverEnabled(logEntry);
            this.checkPackDependency(logEntry);
            this.tps(logEntry);
        });
    }

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
            this.eventServcie.execute(() -> this.eventsConfig.getOnJoin().forEach(command -> this.serverProcess.sendToConsole(command.replaceAll("<player>", playerName))));
        }
    }

    private void playerSpawn(final String logEntry) {
        final String patternString = "PlayerSpawn:([^,]+)";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerName = matcher.group(1);
            this.eventServcie.execute(() -> this.eventsConfig.getOnSpawn().forEach(command -> this.serverProcess.sendToConsole(command.replaceAll("<player>", playerName))));
        }
    }

    private void chatMessage(final String logEntry) {
        this.chatService.execute(() -> {
            final String patternString = "PlayerChat:([^,]+) Message:(.+)";
            final Pattern pattern = Pattern.compile(patternString);
            final Matcher matcher = pattern.matcher(logEntry);

            if (matcher.find()) {
                final String playerChat = matcher.group(1);
                final String message = MessageUtil.fixMessage(matcher.group(2));
                if (this.handleChatMessage(playerChat, message)) {
                    this.discord.sendPlayerMessage(playerChat, message);
                }
            }
        });
    }

    private void customCommand(final String logEntry) {
        final String patternString = "PlayerCommand:([^,]+) Command:(.+) Op:(.+)";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerCommand = matcher.group(1);
            final String command = MessageUtil.fixMessage(matcher.group(2));
            final boolean isOp = Boolean.parseBoolean(matcher.group(3));
            this.handleCustomCommand(playerCommand, MessageUtil.stringToArgs(command), isOp);
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
                this.discord.sendMessage("Zaraz nastąpi restartowanie servera z powodu niskiej ilości TPS"
                        + " (Teraz: **" + tps + "** Ostatnie: **" + this.lastTPS + "**)");
                this.bdsAutoEnable.getWatchDog().getAutoRestartModule().restart(true);
            }

            this.lastTPS = tps;

            if (this.discord instanceof final DiscordJda jda) jda.getStatsChannelsManager().setTpsCount(tps);
        }
    }

    private void serverEnabled(final String logEntry) {
        if (logEntry.contains("Server started")) {
            this.discord.sendEnabledMessage();
            this.lastTPS = 20;
        }
    }

    private void checkPackDependency(final String logEntry) {
        final PackModule packModule = this.bdsAutoEnable.getWatchDog().getPackModule();

        if (logEntry.contains("BDS Auto Enable") && logEntry.contains("requesting dependency on beta APIs")) {
            final String noExperiments = """                        
                    Wykryto że `Beta API's` nie są włączone!
                    Funkcje jak: `licznik czasu gry/śmierci` nie będą działać 
                    """;

            this.discord.sendEmbedMessage("Brak wymaganych eksperymentów", noExperiments, new Footer("Włącz Beta API's"));
            this.discord.sendMessage("<owner>");
            this.logger.alert(noExperiments.replaceAll("`", "").replaceAll("\n", ""));
        }

        if (logEntry.contains("BDS Auto Enable") && logEntry.contains("requesting invalid module version")) {
            final String badVersion = """
                        Posiadasz złą wersje paczki! (<version>)
                        Usuń a nowa pobierze się sama
                        Ewentualnie twój server lub paczka wymaga aktualizacji
                    """;

            this.discord.sendEmbedMessage("Zła wersja paczki",
                    badVersion.replaceAll("<version>", packModule.getPackVersion()),
                    new Footer("Zła wersja paczki"));

            this.logger.alert(badVersion.replaceAll("\n", "")
                    .replaceAll("<version>", packModule.getPackVersion()));

            this.discord.sendMessage("<owner>");
        }
    }

    private void handleCustomCommand(final String playerCommand, final String[] args, final boolean isOp) {
        // !tps jest handlowane w https://github.com/Huje22/BDS-Auto-Enable-Managment-Pack
        // boolean isOp narazie nie działa bo Mojang rozjebało BDS i zawsze zwraca on false

        final String[] newArgs = MessageUtil.removeArgs(args, 1);
        this.bdsAutoEnable.getCommandManager().runCommands(CommandSender.PLAYER, playerCommand, args[0], newArgs, isOp);
    }

    private boolean handleChatMessage(final String playerChat, final String message) {
        if (!this.bdsAutoEnable.getWatchDog().getPackModule().isAppHandledMessages()) return true;
        String role = "";

        if (this.isMuted(playerChat)) {
            this.serverProcess.tellrawToPlayer(playerChat, "&cZostałeś wyciszony");
            return false;
        }


        //TODO: Dodaj opcję wymagania połączenia konta z discord

        if (this.discord instanceof final DiscordJda jda) {
            final LinkingManager linkingManager = jda.getLinkingManager();
            if (linkingManager != null && linkingManager.isLinked(playerChat)) {
                role = jda.getColoredRole(jda.getHighestRole(linkingManager.getIdByName(playerChat))) + " ";
            }
        }

        this.serverProcess.tellrawToAll(
                this.appConfigManager.getWatchDogConfig().getPackModuleConfig().getChatMessageFormat()
                        .replaceAll("<player>", playerChat)
                        .replaceAll("<message>", message)
                        .replaceAll("<role>", role)
        );
        return true;
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

    public boolean isOnline(final String name) {
        return this.onlinePlayers.contains(name);
    }

    public List<String> getOfflinePlayers() {
        return this.offlinePlayers;
    }

    public boolean isMuted(final String name) {
        return this.muted.contains(name);
    }

    public void mute(final String name) {
        this.muted.add(name);
    }

    public void unMute(final String name) {
        this.muted.remove(name);
    }
}
