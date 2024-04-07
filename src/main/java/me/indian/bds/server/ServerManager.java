package me.indian.bds.server;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.sub.transfer.MainServerConfig;
import me.indian.bds.event.EventManager;
import me.indian.bds.event.Position;
import me.indian.bds.event.player.PlayerBlockBreakEvent;
import me.indian.bds.event.player.PlayerBlockPlaceEvent;
import me.indian.bds.event.player.PlayerChatEvent;
import me.indian.bds.event.player.PlayerCommandEvent;
import me.indian.bds.event.player.PlayerDeathEvent;
import me.indian.bds.event.player.PlayerDimensionChangeEvent;
import me.indian.bds.event.player.PlayerInteractContainerEvent;
import me.indian.bds.event.player.PlayerInteractEntityWithContainerEvent;
import me.indian.bds.event.player.PlayerJoinEvent;
import me.indian.bds.event.player.PlayerQuitEvent;
import me.indian.bds.event.player.PlayerSpawnEvent;
import me.indian.bds.event.player.response.PlayerChatResponse;
import me.indian.bds.event.server.ServerAlertEvent;
import me.indian.bds.event.server.ServerStartEvent;
import me.indian.bds.event.server.TPSChangeEvent;
import me.indian.bds.logger.LogState;
import me.indian.bds.logger.Logger;
import me.indian.bds.player.PlayerStatistics;
import me.indian.bds.server.stats.StatsManager;
import me.indian.bds.util.BedrockQuery;
import me.indian.bds.util.DateUtil;
import me.indian.bds.util.Dimension;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.version.VersionManager;
import me.indian.bds.watchdog.module.pack.PackModule;

public class ServerManager {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final ExecutorService mainService, chatService;
    private final List<String> onlinePlayers, offlinePlayers;
    private final List<Long> muted;
    private final StatsManager statsManager;
    private final ReentrantLock chatLock;
    private final EventManager eventManager;
    private final Lock playerConnectLock;
    private ServerProcess serverProcess;
    private VersionManager versionManager;
    private double lastTPS;

    public ServerManager(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.mainService = Executors.newFixedThreadPool(2 * Runtime.getRuntime().availableProcessors()
                , new ThreadUtil("Player Manager"));
        this.chatService = Executors.newFixedThreadPool(3, new ThreadUtil("Chat Service"));
        this.onlinePlayers = new ArrayList<>();
        this.offlinePlayers = new ArrayList<>();
        this.muted = new ArrayList<>();
        this.statsManager = new StatsManager(this.bdsAutoEnable, this);
        this.chatLock = new ReentrantLock();
        this.eventManager = this.bdsAutoEnable.getEventManager();
        this.playerConnectLock = new ReentrantLock();
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
            this.playerConnect(logEntry);
            this.playerJoin(logEntry);
            this.playerQuit(logEntry);
            this.playerSpawn(logEntry);
            this.deathMessage(logEntry);
            this.dimensionChange(logEntry);
            this.playerEntityWithContainerInteract(logEntry);
            this.playerBreakBlock(logEntry);
            this.playerPlaceBlock(logEntry);
            this.playerContainerInteract(logEntry);

            //Dodatkowe metody
            this.serverEnabled(logEntry);
            this.checkPackDependency(logEntry);
            this.tps(logEntry);
            this.version(logEntry);
        });
    }

    private void playerConnect(final String logEntry) {
        try {
            this.playerConnectLock.lock();

            final Pattern pattern = Pattern.compile("Player connected: ([^,]+), xuid: (\\d+)");
            final Matcher matcher = pattern.matcher(logEntry);

            if (matcher.find()) {
                final String playerName = MessageUtil.fixPlayerName(matcher.group(1));
                final long xuid = Long.parseLong(matcher.group(2));

                final String oldPlayerName = this.statsManager.getNameByXuid(xuid);

                if (oldPlayerName != null && !oldPlayerName.equals(playerName)) {
                    //Powinno to działać tak, że gdy gracz zmieni swoją nazwę, nadal zachowuje swoje statystyki
                    // ponieważ jest ustawiany pod nową nazwę za pomocą weryfikacji jego XUID. Było to TESTOWANE ale nie na większą skale
                    this.statsManager.setNewName(xuid, playerName);
                    this.statsManager.addOldName(xuid, oldPlayerName);
                }

                this.statsManager.createNewPlayer(playerName, xuid);
                this.statsManager.setXuid(playerName, xuid);
            }
        } catch (final Exception exception) {
            this.logger.error("&cNie udało się obsłużyć połączenia gracza z logu&b " + logEntry, exception);
            this.eventManager.callEvent(new ServerAlertEvent("Nie udało się obsłużyć połączenia gracza z logu " + logEntry, exception, LogState.ERROR));
        } finally {
            this.playerConnectLock.unlock();
        }
    }

    private void playerQuit(final String logEntry) {
        final String patternString = "Player disconnected: ([^,]+)";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerName = MessageUtil.fixPlayerName(matcher.group(1));

            try {
                this.onlinePlayers.remove(playerName);
                this.offlinePlayers.add(playerName);

                final PlayerStatistics playerStatistics = this.statsManager.getPlayer(playerName);

                playerStatistics.setLastQuit(DateUtil.localDateTimeToLong(LocalDateTime.now()));
                this.eventManager.callEvent(new PlayerQuitEvent(playerStatistics));
            } catch (final Exception exception) {
                this.eventManager.callEvent(new ServerAlertEvent("Nie udało się obsłużyć opuszczenia gracza " + playerName,
                        "Skutkuje to wywołaniem wyjątku", exception, LogState.CRITICAL));
                throw exception;
            }
        }
    }

    private void playerJoin(final String logEntry) {
        final String patternString = "PlayerJoin:([^,]+)";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerName = MessageUtil.fixPlayerName(matcher.group(1));
            try {
                this.onlinePlayers.add(playerName);
                this.offlinePlayers.remove(playerName);

                final PlayerStatistics playerStatistics = this.statsManager.getPlayer(playerName);

                this.statsManager.updateLoginStreak(playerStatistics, DateUtil.localDateTimeToLong(LocalDateTime.now(DateUtil.POLISH_ZONE)));
                this.eventManager.callEvent(new PlayerJoinEvent(playerStatistics));
            } catch (final Exception exception) {
                this.logger.error("&cNie udało się obsłużyć dołączenia gracza&b " + playerName, exception);
                this.serverJoinException(playerName, exception);
            }
        }
    }

    private void playerSpawn(final String logEntry) {
        final String patternString = "PlayerSpawn:([^,]+)";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerName = MessageUtil.fixPlayerName(matcher.group(1));

            try {
                this.eventManager.callEvent(new PlayerSpawnEvent(this.statsManager.getPlayer(playerName)));
            } catch (final Exception exception) {
                this.logger.error("&cNie udało się obsłużyć respawnu gracza " + playerName);
                this.eventManager.callEvent(new ServerAlertEvent("Nie udało się obsłużyć respawnu gracza " + playerName,
                        "Skutkuje to wywołaniem wyjątku", exception, LogState.CRITICAL));
                throw exception;
            }
        }
    }

    private void chatMessage(final String logEntry) {
        this.chatLock.lock();

        final String patternString = "PlayerChat:([^,]+) Message:(.+) Position:(.+)";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerChat = MessageUtil.fixPlayerName(matcher.group(1));
            final String message = MessageUtil.fixMessage(matcher.group(2));
            final Position position = Position.parsePosition(matcher.group(3));
            final boolean appHandled = this.bdsAutoEnable.getWatchDog().getPackModule().isAppHandledMessages();

            try {
                final PlayerStatistics playerStatistics = this.statsManager.getPlayer(playerChat);
                final boolean muted = this.isMuted(playerStatistics.getXuid());

                final PlayerChatResponse response = (PlayerChatResponse) this.eventManager.callEventWithResponse(new PlayerChatEvent(playerStatistics, message, position, muted, appHandled));

                if (appHandled) {
                    String format = playerChat + " »» " + message;
                    if (response != null) {
                        if (response.isCanceled()) return;
                        format = response.getFormat();
                    }

                    if (muted) {
                        this.serverProcess.tellrawToPlayer(playerChat, "&cZostałeś wyciszony");
                        return;
                    }
                    this.serverProcess.tellrawToAll(format);
                }
            } catch (final Exception exception) {
                this.logger.error("&cWystąpił błąd podczas próby przetworzenia wiadomości gracza&c " + playerChat);
                this.eventManager.callEvent(new ServerAlertEvent("Wystąpił błąd podczas próby przetworzenia wiadomości gracza " + playerChat,
                        "Skutkuje to wywołaniem wyjątku", exception, LogState.CRITICAL));
                throw exception;
            } finally {
                this.chatLock.unlock();
            }
        }
    }

    private void customCommand(final String logEntry) {
        final String patternString = "PlayerCommand:([^,]+) Command:(.+) Position:(.+) Op:(.+)";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerCommand = MessageUtil.fixPlayerName(matcher.group(1));
            final String command = matcher.group(2);
            final String position = matcher.group(3);
            final boolean isOp = Boolean.parseBoolean(matcher.group(4));

            try {
                final PlayerStatistics playerStatistics = this.getStatsManager().getPlayer(playerCommand);

                this.handleCustomCommand(playerStatistics, MessageUtil.stringToArgs(command), Position.parsePosition(position), isOp);
                this.eventManager.callEvent(new PlayerCommandEvent(playerStatistics, command, Position.parsePosition(position), isOp));
            } catch (final Exception exception) {
                this.logger.error("&cWystąpił błąd podczas próby przetworzenia polecenia gracza&c " + playerCommand, exception);
                this.eventManager.callEvent(new ServerAlertEvent("Wystąpił błąd podczas próby przetworzenia polecenia gracza " + playerCommand, exception, LogState.CRITICAL));
            }
        }
    }

    private void deathMessage(final String logEntry) {
        final String patternString = "PlayerDeath:([^,]+) DeathMessage:(.+) Position(.+) Killer:(.+) UsedName:(.+)";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerDeath = MessageUtil.fixPlayerName(matcher.group(1));
            final String deathMessage = MessageUtil.fixMessage(matcher.group(2));
            final Position deathPosition = Position.parsePosition(matcher.group(3));
            final String killerName = matcher.group(4);
            final String usedItemName = matcher.group(5);

            try {
                final PlayerStatistics playerStatistics = this.statsManager.getPlayer(playerDeath);
                playerStatistics.addDeaths(1);
                this.eventManager.callEvent(new PlayerDeathEvent(playerStatistics, deathMessage, deathPosition, killerName, usedItemName));
            } catch (final Exception exception) {
                this.logger.error("&cNie udało się obsłużyć śmierci gracza " + playerDeath);
                this.eventManager.callEvent(new ServerAlertEvent("Nie udało się obsłużyć śmierci gracza " + playerDeath,
                        "Skutkuje to wywołaniem wyjątku", exception, LogState.CRITICAL));
                throw exception;
            }
        }
    }

    private void tps(final String logEntry) {
        final String patternString = "TPS: (\\d{1,4})";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String tpsString = matcher.group(1);
            final double tps = Double.parseDouble(tpsString);
            this.eventManager.callEvent(new TPSChangeEvent(tps, this.lastTPS));

            if (this.bdsAutoEnable.getAppConfigManager().getAppConfig().isRestartOnLowTPS()) {
                if (this.lastTPS <= 8 && tps <= 8) {
                    if (this.bdsAutoEnable.getWatchDog().getAutoRestartModule().restart(true, 10, "Niska ilość tps")) {
                        this.eventManager.callEvent(new ServerAlertEvent(
                                "Server jest restartowany z powodu małej ilości TPS (Teraz: " + tps + " Ostatnie: " + this.lastTPS + ")"
                                , LogState.INFO));
                    }
                }
            }

            this.lastTPS = tps;
        }
    }

    private void dimensionChange(final String logEntry) {
        final String patternString = "DimensionChangePlayer:([^,]+) FromDimension:(.+) ToDimension:(.+) FromPosition(.+) ToPosition(.+)";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerName = MessageUtil.fixPlayerName(matcher.group(1));
            final String fromDimension = matcher.group(2);
            final String toDimension = matcher.group(3);
            final Position fromPosition = Position.parsePosition(matcher.group(4));
            final Position toPosition = Position.parsePosition(matcher.group(5));

            try {
                this.eventManager.callEvent(new PlayerDimensionChangeEvent(this.getStatsManager().getPlayer(playerName), Dimension.getByID(fromDimension), Dimension.getByID(toDimension), fromPosition, toPosition));
            } catch (final Exception exception) {
                this.logger.error("&cNie udało się obsłużyć zmiany wymiaru gracza " + playerName);
                this.eventManager.callEvent(new ServerAlertEvent("Nie udało się obsłużyć zmiany wymiaru gracza " + playerName,
                        "Skutkuje to wywołaniem wyjątku", exception, LogState.CRITICAL));
                throw exception;
            }
        }
    }

    private void playerBreakBlock(final String logEntry) {
        final String patternString = "PlayerBreakBlock:([^,]+) Block:(.+) Position:(.+)";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerBreakBlock = MessageUtil.fixPlayerName(matcher.group(1));
            final String blockID = matcher.group(2);
            final String blockPosition = matcher.group(3);

            try {
                final PlayerStatistics playerStatistics = this.getStatsManager().getPlayer(playerBreakBlock);
                playerStatistics.addBlockBroken(1);
                this.eventManager.callEvent(new PlayerBlockBreakEvent(playerStatistics, blockID, Position.parsePosition(blockPosition)));
            } catch (final Exception exception) {
                this.logger.error("&cNie udało się obsłużyć zniszczenia bloku gracza " + playerBreakBlock);
                this.eventManager.callEvent(new ServerAlertEvent("Nie udało się obsłużyć zniszczenia bloku gracza " + playerBreakBlock,
                        "Skutkuje to wywołaniem wyjątku", exception, LogState.CRITICAL));
                throw exception;
            }
        }
    }

    private void playerPlaceBlock(final String logEntry) {
        final String patternString = "PlayerPlaceBlock:([^,]+) Block:(.+) Position:(.+)";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerPlaceBlock = MessageUtil.fixPlayerName(matcher.group(1));
            final String blockID = matcher.group(2);
            final String blockPosition = matcher.group(3);

            try {
                final PlayerStatistics playerStatistics = this.getStatsManager().getPlayer(playerPlaceBlock);
                playerStatistics.addBlockPlaced(1);
                this.eventManager.callEvent(new PlayerBlockPlaceEvent(playerStatistics, blockID, Position.parsePosition(blockPosition)));
            } catch (final Exception exception) {
                this.logger.error("&cNie udało się obsłużyć postawienia bloku gracza " + playerPlaceBlock);
                this.eventManager.callEvent(new ServerAlertEvent("Nie udało się obsłużyć postawienia bloku gracza " + playerPlaceBlock,
                        "Skutkuje to wywołaniem wyjątku", exception, LogState.CRITICAL));
                throw exception;
            }
        }
    }

    private void playerContainerInteract(final String logEntry) {
        final String patternString = "PlayerContainerInteract:([^,]+) Block:(.+) Position:(.+)";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerInteract = MessageUtil.fixPlayerName(matcher.group(1));
            final String blockID = matcher.group(2);
            final String blockPosition = matcher.group(3);

            try {
                this.eventManager.callEvent(new PlayerInteractContainerEvent(this.getStatsManager().getPlayer(playerInteract), blockID, Position.parsePosition(blockPosition)));
            } catch (final Exception exception) {
                this.logger.error("&cNie udało się obsłużyć interakcji gracza z kontenerem " + playerInteract);
                this.eventManager.callEvent(new ServerAlertEvent("Nie udało się obsłużyć interakcji gracza z kontenerem " + playerInteract,
                        "Skutkuje to wywołaniem wyjątku", exception, LogState.CRITICAL));
                throw exception;
            }
        }
    }

    private void playerEntityWithContainerInteract(final String logEntry) {
        final String patternString = "PlayerEntityContainerInteract:([^,]+) EntityID:(.+) EntityPosition:(.+)";
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerInteract = MessageUtil.fixPlayerName(matcher.group(1));
            final String entityID = matcher.group(2);
            final String entityPosition = matcher.group(3);

            try {
                this.eventManager.callEvent(new PlayerInteractEntityWithContainerEvent(this.getStatsManager().getPlayer(playerInteract), entityID, Position.parsePosition(entityPosition)));
            } catch (final Exception exception) {
                this.logger.error("&cNie udało się obsłużyć interakcji gracza z bytem który posiada kontener " + playerInteract);
                this.eventManager.callEvent(new ServerAlertEvent("Nie udało się obsłużyć interakcji gracza z bytem który posiada kontener " + playerInteract,
                        "Skutkuje to wywołaniem wyjątku", exception, LogState.CRITICAL));
                throw exception;
            }
        }
    }

    private void serverEnabled(final String logEntry) {
        if (logEntry.contains("Server started")) {
            this.lastTPS = 20;
            this.eventManager.callEvent(new TPSChangeEvent(this.lastTPS, this.lastTPS));
            this.eventManager.callEvent(new ServerStartEvent());
            this.logger.info("&eUruchomiono server w:&b " + DateUtil.formatTime(System.currentTimeMillis() - this.serverProcess.getStartTime(), List.of('s', 'i'), true));
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

            this.logger.alert(noExperiments.replaceAll("`", "").replaceAll("\n", ""));
            packModule.setLoaded(false);
        }

        if (logEntry.contains("BDS Auto Enable") && logEntry.contains("requesting invalid module version")) {
            final String wrongVersion = """
                        Posiadasz złą wersje paczki! (<version>)
                        Usuń a nowa pobierze się sama
                        Ewentualnie twój server lub paczka wymaga aktualizacji
                    """;

            if (packModule.getMainPack() != null) {
                this.logger.alert(wrongVersion.replaceAll("\n", "")
                        .replaceAll("<version>", Arrays.toString(packModule.getMainPack().version())));
            }
            packModule.setLoaded(false);
        }
    }

    private void handleCustomCommand(final PlayerStatistics player, final String[] args, final Position position, final boolean isOp) {
        // !tps jest handlowane w https://github.com/Huje22/BDS-Auto-Enable-Managment-Pack
        // boolean isOp narazie nie działa bo Mojang rozjebało BDS i zawsze zwraca on false

        final String[] newArgs = MessageUtil.removeFirstArgs(args);
        this.bdsAutoEnable.getCommandManager().runCommands(player, args[0], newArgs, position, isOp);
    }

    private void serverJoinException(final String playerName, final Exception exception) {
        final MainServerConfig mainServerConfig = this.bdsAutoEnable.getAppConfigManager().getTransferConfig().getMainServerConfig();
        final String additionalMessage;
        if (mainServerConfig.isTransfer()) {
            final String ip = mainServerConfig.getIp();
            final int port = this.bdsAutoEnable.getServerProperties().getServerPort();

            final BedrockQuery query = BedrockQuery.create(ip, port);
            if (query.online()) {
                this.serverProcess.transferPlayer(playerName, ip, port);
                additionalMessage = "Spróbujemy go przetransferować ponownie na server";
            } else {
                this.serverProcess.kick(playerName, "Nie udało się obsłużyć nam twojego dołączenia");
                additionalMessage = "Wyrzuciliśmy go";
            }
        } else {
            this.serverProcess.kick(playerName, "Nie udało się obsłużyć nam twojego dołączenia");
            additionalMessage = "Wyrzuciliśmy go";
        }

        this.eventManager.callEvent(new ServerAlertEvent("Nie udało się obsłużyć dołączania dla gracza " + playerName,
                additionalMessage,
                exception, LogState.ERROR));
    }

    public StatsManager getStatsManager() {
        return this.statsManager;
    }

    public void clearPlayers() {
        this.onlinePlayers.clear();
    }

    public double getLastTPS() {
        return this.lastTPS;
    }

    //Robie tak aby uniknąć ConcurrentModificationException
    public List<String> getOnlinePlayers() {
        return new ArrayList<>(this.onlinePlayers);
    }

    public boolean isOnline(final String name) {
        return this.onlinePlayers.contains(name);
    }

    //Robie tak aby uniknąć ConcurrentModificationException
    public List<String> getOfflinePlayers() {
        return new ArrayList<>(this.offlinePlayers);
    }

    public boolean isMuted(final long xuid) {
        return this.muted.contains(xuid);
    }

    public void mute(final long xuid) {
        this.muted.add(xuid);
    }

    public void unMute(final long xui) {
        this.muted.remove(xui);
    }
}
