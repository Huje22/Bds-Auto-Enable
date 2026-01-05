package pl.indianbartonka.bds.server;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.config.sub.transfer.MainServerConfig;
import pl.indianbartonka.bds.event.EventManager;
import pl.indianbartonka.bds.event.EventResponse;
import pl.indianbartonka.bds.event.player.PlayerBlockBreakEvent;
import pl.indianbartonka.bds.event.player.PlayerBlockPlaceEvent;
import pl.indianbartonka.bds.event.player.PlayerChangeInputModeEvent;
import pl.indianbartonka.bds.event.player.PlayerChatEvent;
import pl.indianbartonka.bds.event.player.PlayerCommandEvent;
import pl.indianbartonka.bds.event.player.PlayerDeathEvent;
import pl.indianbartonka.bds.event.player.PlayerDimensionChangeEvent;
import pl.indianbartonka.bds.event.player.PlayerInteractContainerEvent;
import pl.indianbartonka.bds.event.player.PlayerInteractEntityWithContainerEvent;
import pl.indianbartonka.bds.event.player.PlayerJoinEvent;
import pl.indianbartonka.bds.event.player.PlayerMovementEvent;
import pl.indianbartonka.bds.event.player.PlayerQuitEvent;
import pl.indianbartonka.bds.event.player.PlayerSpawnEvent;
import pl.indianbartonka.bds.event.player.response.PlayerChatResponse;
import pl.indianbartonka.bds.event.server.ServerAlertEvent;
import pl.indianbartonka.bds.event.server.ServerStartEvent;
import pl.indianbartonka.bds.event.server.TPSChangeEvent;
import pl.indianbartonka.bds.player.GraphicsMode;
import pl.indianbartonka.bds.player.InputMode;
import pl.indianbartonka.bds.player.MemoryTier;
import pl.indianbartonka.bds.player.PlatformType;
import pl.indianbartonka.bds.player.PlayerStatistics;
import pl.indianbartonka.bds.player.position.Dimension;
import pl.indianbartonka.bds.player.position.Position;
import pl.indianbartonka.bds.server.stats.StatsManager;
import pl.indianbartonka.bds.util.MinecraftUtil;
import pl.indianbartonka.bds.util.ServerUtil;
import pl.indianbartonka.bds.version.VersionManager;
import pl.indianbartonka.bds.watchdog.module.pack.PackModule;
import pl.indianbartonka.util.DateUtil;
import pl.indianbartonka.util.MathUtil;
import pl.indianbartonka.util.MessageUtil;
import pl.indianbartonka.util.ThreadUtil;
import pl.indianbartonka.util.logger.LogState;
import pl.indianbartonka.util.logger.Logger;
import pl.indianbartonka.util.minecraft.BedrockQuery;

public class ServerManager {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final ExecutorService mainService, chatService;
    private final List<String> onlinePlayers, offlinePlayers;
    private final List<Long> muted;
    private final StatsManager statsManager;
    private final EventManager eventManager;
    private final Pattern connectPattern = Pattern.compile("Player connected: ([^,]+), xuid: (\\d+)");
    private final Pattern disconnectPattern = Pattern.compile("Player disconnected: ([^,]+)");
    private final Pattern joinPattern = Pattern.compile("PlayerJoin:([^,]+) PlayerPlatform:([^,]+) PlayerInput:([^,]+) MemoryTier:([^,]+) MaxRenderDistance:([^,]+) GraphicMode:([^,]+)");
    private final Pattern spawnPattern = Pattern.compile("PlayerSpawn:([^,]+)");
    private final Pattern movementPattern = Pattern.compile("PlayerMovement:([^,]+) Position:(.+)");
    private final Pattern chatPattern = Pattern.compile("PlayerChat:([^,]+) Message:(.+) Position:(.+)");
    private final Pattern customCommandPattern = Pattern.compile("PlayerCommand:([^,]+) Command:(.+) Position:(.+) Op:(.+)");
    private final Pattern deathPattern = Pattern.compile("PlayerDeath:([^,]+) DeathMessage:(.+) Position(.+) Killer:(.+) UsedName:(.+)");
    private final Pattern tpsPattern = Pattern.compile("TPS: (\\d{1,4})");
    private final Pattern dimensionChangePattern = Pattern.compile("DimensionChangePlayer:([^,]+) FromDimension:(.+) ToDimension:(.+) FromPosition(.+) ToPosition(.+)");
    private final Pattern blockBreakPattern = Pattern.compile("PlayerBreakBlock:([^,]+) Block:(.+) Position:(.+)");
    private final Pattern blockPlacePattern = Pattern.compile("PlayerPlaceBlock:([^,]+) Block:(.+) Position:(.+)");
    private final Pattern containerInteractPattern = Pattern.compile("PlayerContainerInteract:([^,]+) Block:(.+) Position:(.+)");
    private final Pattern inputChangedPattern = Pattern.compile("PlayerInput:([^,]+) NewInputMode:([^,]+) OldInputMode:([^,]+)");
    private final Pattern entityWithContainerInteractPattern = Pattern.compile("PlayerEntityContainerInteract:([^,]+) EntityID:(.+) EntityPosition:(.+)");
    private final Pattern versionPattern = Pattern.compile("Version: (.+)");
    private ServerProcess serverProcess;
    private VersionManager versionManager;
    private double lastTPS;

    public ServerManager(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.mainService = Executors.newFixedThreadPool(MathUtil.getCorrectNumber(2 * ThreadUtil.getLogicalThreads(), 4, 8), new ThreadUtil("Player Manager"));
        this.chatService = Executors.newFixedThreadPool(MathUtil.getCorrectNumber(2 * ThreadUtil.getLogicalThreads(), 2, 4), new ThreadUtil("Chat Service"));
        this.onlinePlayers = new CopyOnWriteArrayList<>();
        this.offlinePlayers = new CopyOnWriteArrayList<>();
        this.muted = new CopyOnWriteArrayList<>();
        this.statsManager = new StatsManager(this.bdsAutoEnable, this);
        this.eventManager = this.bdsAutoEnable.getEventManager();
        this.lastTPS = 20;
    }

    public void init() {
        this.serverProcess = this.bdsAutoEnable.getServerProcess();
        this.versionManager = this.bdsAutoEnable.getVersionManager();
    }

    public void initFromLog(final String logEntry) {
        if (logEntry.contains("PlayerChat:") || logEntry.contains("PlayerCommand:")) {
            this.chatService.execute(() -> {
                if (logEntry.contains("PlayerChat:")) this.chatMessage(logEntry);
                if (logEntry.contains("PlayerCommand:")) this.customCommand(logEntry);
            });
            return;
        }

        this.mainService.execute(() -> {
            if (logEntry.contains("PlayerMovement:")) {
                this.playerMovement(logEntry);
            } else if (logEntry.contains("PlayerBreakBlock:")) {
                this.playerBreakBlock(logEntry);
            } else if (logEntry.contains("PlayerPlaceBlock:")) {
                this.playerPlaceBlock(logEntry);
            } else if (logEntry.contains("PlayerJoin:")) {
                this.playerJoin(logEntry);
            } else if (logEntry.contains("Player disconnected:")) {
                this.playerQuit(logEntry);
            } else if (logEntry.contains("Player connected:")) {
                this.playerConnect(logEntry);
            } else if (logEntry.contains("PlayerSpawn:")) {
                this.playerSpawn(logEntry);
            } else if (logEntry.contains("PlayerDeath:")) {
                this.deathMessage(logEntry);
            } else if (logEntry.contains("DimensionChangePlayer:")) {
                this.dimensionChange(logEntry);
            } else if (logEntry.contains("PlayerContainerInteract:")) {
                this.playerContainerInteract(logEntry);
            } else if (logEntry.contains("PlayerEntityContainerInteract:")) {
                this.playerEntityWithContainerInteract(logEntry);
            } else if (logEntry.contains("PlayerInput:")) {
                this.playerChangeInputMode(logEntry);
            } else if (logEntry.contains("TPS:")) {
                this.tps(logEntry);
            } else if (logEntry.contains("Version:")) {
                this.version(logEntry);
            }

            this.serverEnabled(logEntry);
            this.checkPackDependency(logEntry);
        });
    }

    private void playerConnect(final String logEntry) {
        try {
            final Matcher matcher = this.connectPattern.matcher(logEntry);

            if (matcher.find()) {
                final String playerName = MinecraftUtil.fixPlayerName(matcher.group(1));
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
        }
    }

    private void playerQuit(final String logEntry) {
        final Matcher matcher = this.disconnectPattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerName = MinecraftUtil.fixPlayerName(matcher.group(1));

            try {
                this.onlinePlayers.remove(playerName);
                this.offlinePlayers.add(playerName);

                final PlayerStatistics playerStatistics = this.statsManager.getPlayer(playerName);

                playerStatistics.setLastQuit(DateUtil.localDateTimeToMillis(LocalDateTime.now()));
                this.eventManager.callEvent(new PlayerQuitEvent(playerStatistics));
            } catch (final Exception exception) {
                this.eventManager.callEvent(new ServerAlertEvent("Nie udało się obsłużyć opuszczenia gracza " + playerName,
                        "Skutkuje to wywołaniem wyjątku", exception, LogState.CRITICAL));
                throw exception;
            }
        }
    }

    private void playerJoin(final String logEntry) {
        final Matcher matcher = this.joinPattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerName = MinecraftUtil.fixPlayerName(matcher.group(1));
            final String platform = matcher.group(2);
            final String input = matcher.group(3);
            final int memoryTier = Integer.parseInt(matcher.group(4));
            final int maxRenderDistance = Integer.parseInt(matcher.group(5));
            final String graphic = matcher.group(6);

            try {
                this.onlinePlayers.add(playerName);
                this.offlinePlayers.remove(playerName);

                final PlayerStatistics playerStatistics = this.statsManager.getPlayer(playerName);

                this.statsManager.updateLoginStreak(playerStatistics, DateUtil.localDateTimeToMillis(LocalDateTime.now(DateUtil.POLISH_ZONE)));
                playerStatistics.setPlatformType(PlatformType.getByName(platform));
                playerStatistics.setLastKnownInputMode(InputMode.getByName(input));
                playerStatistics.setMemoryTier(MemoryTier.getMemoryTier(memoryTier));
                playerStatistics.setMaxRenderDistance(maxRenderDistance);
                playerStatistics.setGraphicsMode(GraphicsMode.getByName(graphic));

                this.eventManager.callEvent(new PlayerJoinEvent(playerStatistics));
                this.bdsAutoEnable.getWatchDog().getBackupModule().backupOnPlayerJoin();
                this.betaSet(playerStatistics);
            } catch (final Exception exception) {
                this.logger.error("&cNie udało się obsłużyć dołączenia gracza&b " + playerName, exception);
                this.serverJoinException(playerName, exception);
            }
        }
    }

    private void playerSpawn(final String logEntry) {

        final Matcher matcher = this.spawnPattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerName = MinecraftUtil.fixPlayerName(matcher.group(1));

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

    private void playerMovement(final String logEntry) {
        final Matcher matcher = this.movementPattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerMovement = MinecraftUtil.fixPlayerName(matcher.group(1));
            final String playerPosition = matcher.group(2);

            try {
                final PlayerStatistics playerStatistics = this.getStatsManager().getPlayer(playerMovement);
                this.eventManager.callEvent(new PlayerMovementEvent(playerStatistics, Position.parsePosition(playerPosition)));
            } catch (final Exception exception) {
                this.logger.error("&cNie udało się obsłużyć ruchu gracza " + playerMovement);
                this.eventManager.callEvent(new ServerAlertEvent("Nie udało się obsłużyć ruchu gracza " + playerMovement,
                        "Skutkuje to wywołaniem wyjątku", exception, LogState.CRITICAL));
                throw exception;
            }
        }
    }

    private void chatMessage(final String logEntry) {
        final Matcher matcher = this.chatPattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerChat = MinecraftUtil.fixPlayerName(matcher.group(1));
            final String message = MinecraftUtil.fixMessage(matcher.group(2));
            final Position position = Position.parsePosition(matcher.group(3));
            final boolean appHandled = this.bdsAutoEnable.getWatchDog().getPackModule().isAppHandledMessages();

            try {
                final PlayerStatistics playerStatistics = this.statsManager.getPlayer(playerChat);
                final boolean muted = this.isMuted(playerStatistics.getXuid());

                this.eventManager.callEventsWithResponse(new PlayerChatEvent(playerStatistics, message, position, muted, appHandled))
                        .thenAccept(responses -> {
                            if (responses.isEmpty() && appHandled) {
                                if (muted) {
                                    ServerUtil.tellrawToPlayer(playerChat, "&cZostałeś wyciszony");
                                    return;
                                }
                                ServerUtil.tellrawToAll(playerChat + " »» " + message);
                            }

                            for (final EventResponse response : responses) {
                                if (response instanceof final PlayerChatResponse chatResponse) {

                                    if (appHandled) {
                                        String format = playerChat + " »» " + message;
                                        if (chatResponse.isCanceled()) return;
                                        format = chatResponse.getFormat();

                                        if (muted) {
                                            ServerUtil.tellrawToPlayer(playerChat, "&cZostałeś wyciszony");
                                            return;
                                        }
                                        ServerUtil.tellrawToAll(format);
                                        break;
                                    }
                                }
                            }
                        });
            } catch (final Exception exception) {
                this.logger.error("&cWystąpił błąd podczas próby przetworzenia wiadomości gracza&c " + playerChat);
                this.eventManager.callEvent(new ServerAlertEvent("Wystąpił błąd podczas próby przetworzenia wiadomości gracza " + playerChat,
                        "Skutkuje to wywołaniem wyjątku", exception, LogState.CRITICAL));
                throw exception;
            }
        }
    }

    private void customCommand(final String logEntry) {
        final Matcher matcher = this.customCommandPattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerCommand = MinecraftUtil.fixPlayerName(matcher.group(1));
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
        final Matcher matcher = this.deathPattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerDeath = MinecraftUtil.fixPlayerName(matcher.group(1));
            final String deathMessage = MinecraftUtil.fixMessage(matcher.group(2));
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
        final Matcher matcher = this.tpsPattern.matcher(logEntry);

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
        final Matcher matcher = this.dimensionChangePattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerName = MinecraftUtil.fixPlayerName(matcher.group(1));

            try {
                final Dimension fromDimension = Dimension.getByID(matcher.group(2));
                final Dimension toDimension = Dimension.getByID(matcher.group(3));
                final Position fromPosition = Position.parsePosition(matcher.group(4));
                final Position toPosition = Position.parsePosition(matcher.group(5));

                final PlayerStatistics playerStatistics = this.getStatsManager().getPlayer(playerName);

                playerStatistics.setDimension(toDimension);
                this.eventManager.callEvent(new PlayerDimensionChangeEvent(playerStatistics, fromDimension, toDimension, fromPosition, toPosition));
            } catch (final Exception exception) {
                this.logger.error("&cNie udało się obsłużyć zmiany wymiaru gracza " + playerName);
                this.eventManager.callEvent(new ServerAlertEvent("Nie udało się obsłużyć zmiany wymiaru gracza " + playerName,
                        "Skutkuje to wywołaniem wyjątku", exception, LogState.CRITICAL));
                throw exception;
            }
        }
    }

    private void playerBreakBlock(final String logEntry) {
        final Matcher matcher = this.blockBreakPattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerBreakBlock = MinecraftUtil.fixPlayerName(matcher.group(1));
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
        final Matcher matcher = this.blockPlacePattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerPlaceBlock = MinecraftUtil.fixPlayerName(matcher.group(1));
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
        final Matcher matcher = this.containerInteractPattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerInteract = MinecraftUtil.fixPlayerName(matcher.group(1));
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

    private void playerChangeInputMode(final String logEntry) {
        final Matcher matcher = this.inputChangedPattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerInput = MinecraftUtil.fixPlayerName(matcher.group(1));
            final InputMode newInput = InputMode.getByName(matcher.group(2));
            final InputMode oldInput = InputMode.getByName(matcher.group(3));

            try {
                final PlayerStatistics playerStatistics = this.getStatsManager().getPlayer(playerInput);
                playerStatistics.setLastKnownInputMode(newInput);

                this.eventManager.callEvent(new PlayerChangeInputModeEvent(playerStatistics, newInput, oldInput));
                this.betaSet(playerStatistics);
            } catch (final Exception exception) {
                this.logger.error("&cNie udało się obsłużyć zmiany&b InputMode&c gracza&b " + playerInput);
                this.eventManager.callEvent(new ServerAlertEvent("Nie udało się obsłużyć zmiany InputMode gracza " + playerInput,
                        "Skutkuje to wywołaniem wyjątku", exception, LogState.CRITICAL));
                throw exception;
            }
        }
    }

    private void playerEntityWithContainerInteract(final String logEntry) {
        final Matcher matcher = this.entityWithContainerInteractPattern.matcher(logEntry);

        if (matcher.find()) {
            final String playerInteract = MinecraftUtil.fixPlayerName(matcher.group(1));
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

            /**
             * For PTERO
             */
            System.out.println("Done (21.37s)! For help, type \"help\"");
            System.out.flush();

            this.logger.info("&eUruchomiono server w:&b " + DateUtil.formatTimeDynamic(System.currentTimeMillis() - this.serverProcess.getStartTime(), true));
        }
    }

    private void version(final String logEntry) {
        final Matcher matcher = this.versionPattern.matcher(logEntry);

        if (matcher.find()) {
            final String version = MinecraftUtil.fixMessage(matcher.group(1));
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
                        .replaceAll("<version>", Arrays.toString(packModule.getMainPack().getVersion())));
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

            if (mainServerConfig.isForceTransfer()) {
                ServerUtil.transferPlayer(playerName, ip, port);
                additionalMessage = "Spróbujemy go przetransferować ponownie na server";
            } else if (BedrockQuery.create(ip, port).online()) {
                ServerUtil.transferPlayer(playerName, ip, port);
                additionalMessage = "Spróbujemy go przetransferować ponownie na server";
            } else {
                ServerUtil.kick(playerName, "Nie udało się obsłużyć nam twojego dołączenia");
                additionalMessage = "Wyrzuciliśmy go";
            }
        } else {
            ServerUtil.kick(playerName, "Nie udało się obsłużyć nam twojego dołączenia");
            additionalMessage = "Wyrzuciliśmy go";
        }

        this.eventManager.callEvent(new ServerAlertEvent("Nie udało się obsłużyć dołączania dla gracza " + playerName,
                additionalMessage,
                exception, LogState.ERROR));
    }

    private void betaSet(final PlayerStatistics player) {
        if (!this.bdsAutoEnable.getAppConfigManager().getAppConfig().isBelowName()) return;

        final String platform = switch (player.getPlatformType()) {
            case UNKNOWN -> "&4NIEZNANE&f";
            case Console -> "&7[&eKonsola&7]&f";
            case Desktop -> "&7[&bKomputer&7]&f";
            case Mobile -> "&7[&aTelefon&7]&f";
        };

        final String inputMode = switch (player.getLastKnownInputMode()) {
            case UNKNOWN -> "&4NIEZNANE&f";
            case GAMEPAD -> "&eKontroler&f";
            case KEYBOARD_AND_MOUSE -> "&eKlawiatura&f";
            case MOTION_CONTROLLER -> "&gKontroler Ruchu&f";
            case TOUCH -> "&aDotyk&f";
        };

        ServerUtil.setPlayerBelowName(player.getPlayerName(), platform + "&8 || " + inputMode);
    }

    public StatsManager getStatsManager() {
        return this.statsManager;
    }

    public double getLastTPS() {
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

    public void restartPlayersList() {
        if (!this.serverProcess.isEnabled()) {
            //Jeśli server został scrashowany trzeba usunąć graczy online po restarcie

            for (final String player : this.onlinePlayers) {
                this.eventManager.callEvent(new PlayerQuitEvent(this.getStatsManager().getPlayer(player)));
            }

            this.offlinePlayers.addAll(this.onlinePlayers);
            this.onlinePlayers.clear();
        }
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
