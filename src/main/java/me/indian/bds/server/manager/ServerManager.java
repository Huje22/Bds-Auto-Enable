package me.indian.bds.server.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.CommandSender;
import me.indian.bds.config.AppConfigManager;
import me.indian.bds.config.sub.EventsConfig;
import me.indian.bds.discord.embed.component.Footer;
import me.indian.bds.discord.jda.DiscordJDA;
import me.indian.bds.discord.jda.manager.LinkingManager;
import me.indian.bds.discord.jda.manager.StatsChannelsManager;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.version.VersionManager;
import me.indian.bds.watchdog.module.PackModule;


public class ServerManager {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final AppConfigManager appConfigManager;
    private final EventsConfig eventsConfig;
    private final DiscordJDA discordJDA;
    private final ExecutorService mainService, chatService, eventService;
    private final List<String> onlinePlayers, offlinePlayers, muted;
    private final StatsManager statsManager;
    private final ReentrantLock chatLock;
    private ServerProcess serverProcess;
    private VersionManager versionManager;
    private int lastTPS;

    public ServerManager(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.appConfigManager = this.bdsAutoEnable.getAppConfigManager();
        this.eventsConfig = this.appConfigManager.getEventsConfig();
        this.discordJDA = this.bdsAutoEnable.getDiscordHelper().getDiscordJDA();
        this.mainService = Executors.newScheduledThreadPool(2, new ThreadUtil("Player Manager"));
        this.chatService = Executors.newScheduledThreadPool(3, new ThreadUtil("Chat Service"));
        this.eventService = Executors.newScheduledThreadPool(2, new ThreadUtil("Event Service"));
        this.onlinePlayers = new ArrayList<>();
        this.offlinePlayers = new ArrayList<>();
        this.muted = new ArrayList<>();
        this.statsManager = new StatsManager(this.bdsAutoEnable, this);
        this.chatLock = new ReentrantLock();
        this.lastTPS = 20;
    }

    public void init() {
        this.serverProcess = this.bdsAutoEnable.getServerProcess();
        this.versionManager = this.bdsAutoEnable.getVersionManager();
    }

    public void initFromLog(final String logEntry) {
        this.chatService.execute(() -> {
            this.chatMessage(logEntry);
            this.customCommand(logEntry);
        });

        this.mainService.execute(() -> {
            //Metody związane z graczem
            this.playerJoin(logEntry);
            this.playerQuit(logEntry);
            this.playerSpawn(logEntry);
            this.deathMessage(logEntry);

            //Dodatkowe metody
            this.serverEnabled(logEntry);
            this.checkPackDependency(logEntry);
            this.tps(logEntry);
            this.version(logEntry);
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
            this.discordJDA.sendLeaveMessage(playerName);
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
            this.discordJDA.sendJoinMessage(playerName);
            this.eventService.execute(() -> this.eventsConfig.getOnJoin().forEach(command -> this.serverProcess.sendToConsole(command.replaceAll("<player>", playerName))));
        }
    }

    private void playerSpawn(final String logEntry) {
        final String patternString = "PlayerSpawn:([^,]+)";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerName = matcher.group(1);
            this.eventService.execute(() -> this.eventsConfig.getOnSpawn().forEach(command -> this.serverProcess.sendToConsole(command.replaceAll("<player>", playerName))));
        }
    }

    private void chatMessage(final String logEntry) {
        try {
            this.chatLock.lock();

            final String patternString = "PlayerChat:([^,]+) Message:(.+)";
            final Pattern pattern = Pattern.compile(patternString);
            final Matcher matcher = pattern.matcher(logEntry);

            if (matcher.find()) {
                final String playerChat = matcher.group(1);
                final String message = MessageUtil.fixMessage(matcher.group(2));
                if (this.handleChatMessage(playerChat, message)) {
                    this.discordJDA.sendPlayerMessage(playerChat, message);
                }
            }
        } catch (final Exception exception) {
            this.logger.error("Wystąpił błąd podczas próby przetworzenia wiadomości gracza", exception);
        } finally {
            this.chatLock.unlock();
        }
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
            this.discordJDA.sendDeathMessage(playerDeath, casue);
            this.statsManager.addDeaths(playerDeath, 1);
        }
    }

    private void tps(final String logEntry) {
        final StatsChannelsManager statsChannelsManager = this.discordJDA.getStatsChannelsManager();
        final String patternString = "TPS: (\\d{1,4})";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String tpsString = matcher.group(1);
            final int tps = Integer.parseInt(tpsString);

            if (tps <= 8) this.discordJDA.sendMessage("Server posiada: **" + tps + "** TPS");
            if (this.lastTPS <= 8 && tps <= 8) {
                this.discordJDA.sendMessage("Zaraz nastąpi restartowanie servera z powodu niskiej ilości TPS"
                        + " (Teraz: **" + tps + "** Ostatnie: **" + this.lastTPS + "**)");
                this.bdsAutoEnable.getWatchDog().getAutoRestartModule().restart(true, 10);
            }

            this.lastTPS = tps;

            if (statsChannelsManager != null) statsChannelsManager.setTpsCount(tps);
        }
    }

    private void serverEnabled(final String logEntry) {
        if (logEntry.contains("Server started")) {
            this.discordJDA.sendEnabledMessage();
            this.lastTPS = 20;
        }
    }

    private void version(final String logEntry) {
        final String patternString = "Version: (.+)";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String version = MessageUtil.fixMessage(matcher.group(1));
            if (!this.versionManager.getLoadedVersion().equals(version)) {
                this.versionManager.setLoadedVersion(version);
            }
        }
    }

    private void checkPackDependency(final String logEntry) {
        final PackModule packModule = this.bdsAutoEnable.getWatchDog().getPackModule();

        if (logEntry.contains("BDS Auto Enable") && logEntry.contains("requesting dependency on beta APIs")) {
            final String noExperiments = """                        
                    Wykryto że `Beta API's` nie są włączone!
                    Funkcje jak: `licznik czasu gry/śmierci` nie będą działać 
                    """;

            this.discordJDA.sendEmbedMessage("Brak wymaganych eksperymentów", noExperiments, new Footer("Włącz Beta API's"));
            this.bdsAutoEnable.getDiscordHelper().getWebHook().sendMessage("<owner>");
            this.logger.alert(noExperiments.replaceAll("`", "").replaceAll("\n", ""));
        }

        if (logEntry.contains("BDS Auto Enable") && logEntry.contains("requesting invalid module version")) {
            final String badVersion = """
                        Posiadasz złą wersje paczki! (<version>)
                        Usuń a nowa pobierze się sama
                        Ewentualnie twój server lub paczka wymaga aktualizacji
                    """;

            this.discordJDA.sendEmbedMessage("Zła wersja paczki",
                    badVersion.replaceAll("<version>", packModule.getPackVersion()),
                    new Footer("Zła wersja paczki"));

            this.logger.alert(badVersion.replaceAll("\n", "")
                    .replaceAll("<version>", packModule.getPackVersion()));

            this.discordJDA.sendMessage("<owner>");
        }
    }

    private void handleCustomCommand(final String playerCommand, final String[] args, final boolean isOp) {
        // !tps jest handlowane w https://github.com/Huje22/BDS-Auto-Enable-Managment-Pack
        // boolean isOp narazie nie działa bo Mojang rozjebało BDS i zawsze zwraca on false

        final String[] newArgs = MessageUtil.removeFirstArgs(args);
        this.bdsAutoEnable.getCommandManager().runCommands(CommandSender.PLAYER, playerCommand, args[0], newArgs, isOp);
    }

    private boolean handleChatMessage(final String playerChat, final String message) {
        //Robione jest to w ten sposób aby nie wysyłać na discord wiadomości gracza który jest wyciszony
        if (!this.bdsAutoEnable.getWatchDog().getPackModule().isAppHandledMessages()) return true;
        String role = "";

        if (this.isMuted(playerChat)) {
            this.serverProcess.tellrawToPlayer(playerChat, "&cZostałeś wyciszony");
            return false;
        }

        final LinkingManager linkingManager = this.discordJDA.getLinkingManager();
        if (linkingManager != null && linkingManager.isLinked(playerChat)) {
            role = this.discordJDA.getColoredRole(this.discordJDA.getHighestRole(linkingManager.getIdByName(playerChat))) + " ";

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
