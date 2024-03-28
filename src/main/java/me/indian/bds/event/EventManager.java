package me.indian.bds.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.event.player.PlayerBlockBreakEvent;
import me.indian.bds.event.player.PlayerBlockPlaceEvent;
import me.indian.bds.event.player.PlayerChatEvent;
import me.indian.bds.event.player.PlayerCommandEvent;
import me.indian.bds.event.player.PlayerDeathEvent;
import me.indian.bds.event.player.PlayerDimensionChangeEvent;
import me.indian.bds.event.player.PlayerInteractContainerEvent;
import me.indian.bds.event.player.PlayerInteractEntityWithContainerEvent;
import me.indian.bds.event.player.PlayerJoinEvent;
import me.indian.bds.event.player.PlayerMuteEvent;
import me.indian.bds.event.player.PlayerQuitEvent;
import me.indian.bds.event.player.PlayerSpawnEvent;
import me.indian.bds.event.player.PlayerUnMuteEvent;
import me.indian.bds.event.player.response.PlayerChatResponse;
import me.indian.bds.event.server.ExtensionDisableEvent;
import me.indian.bds.event.server.ExtensionEnableEvent;
import me.indian.bds.event.server.ServerAlertEvent;
import me.indian.bds.event.server.ServerClosedEvent;
import me.indian.bds.event.server.ServerConsoleCommandEvent;
import me.indian.bds.event.server.ServerRestartEvent;
import me.indian.bds.event.server.ServerStartEvent;
import me.indian.bds.event.server.ServerUncaughtExceptionEvent;
import me.indian.bds.event.server.ServerUpdatedEvent;
import me.indian.bds.event.server.ServerUpdatingEvent;
import me.indian.bds.event.server.TPSChangeEvent;
import me.indian.bds.event.server.response.ServerConsoleCommandResponse;
import me.indian.bds.event.watchdog.BackupDoneEvent;
import me.indian.bds.event.watchdog.BackupFailEvent;
import me.indian.bds.extension.Extension;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.ThreadUtil;

public class EventManager {

    private final Logger logger;
    private final Map<Listener, Extension> listenerMap;
    private final ExecutorService listenerService;
    private final AtomicReference<PlayerChatResponse> chatResponse;
    private Map<Listener, Extension> listeners;

    public EventManager(final BDSAutoEnable bdsAutoEnable) {
        this.logger = bdsAutoEnable.getLogger();
        this.listenerMap = new LinkedHashMap<>();
        this.listenerService = Executors.newFixedThreadPool(5, new ThreadUtil("Listeners"));
        this.chatResponse = new AtomicReference<>();
    }

    public <T extends Listener> void registerListener(final T listener, final Extension extension) {
        if (extension == null) throw new NullPointerException();
        this.listenerMap.put(listener, extension);
    }

    public void unRegister(final Extension extension) {
        final List<Listener> listenerToRemove = new ArrayList<>();

        this.listenerMap.forEach((listener, ex) -> {
            if (ex == extension) {
                listenerToRemove.add(listener);

            }
        });
        listenerToRemove.forEach(this.listenerMap::remove);
    }

    public void callEvent(final Event event) {
        this.listenerService.execute(() -> {
            this.listeners = new HashMap<>(this.listenerMap);

            final AtomicReference<Extension> extension = new AtomicReference<>();

            try {
                //Player
                this.listeners.forEach((listener, ex) -> {
                    extension.set(ex);
                    if (event instanceof final PlayerJoinEvent playerJoinEvent) {
                        listener.onPlayerJoin(playerJoinEvent);
                    } else if (event instanceof final PlayerSpawnEvent playerSpawnEvent) {
                        listener.onPlayerSpawn(playerSpawnEvent);
                    } else if (event instanceof final PlayerQuitEvent playerQuitEvent) {
                        listener.onPlayerQuit(playerQuitEvent);
                    } else if (event instanceof final PlayerDeathEvent playerDeathEvent) {
                        listener.onPlayerDeath(playerDeathEvent);
                    } else if (event instanceof final PlayerMuteEvent playerMuteEvent) {
                        listener.onPlayerMute(playerMuteEvent);
                    } else if (event instanceof final PlayerUnMuteEvent playerUnMuteEvent) {
                        listener.onPlayerUnMute(playerUnMuteEvent);
                    } else if (event instanceof final PlayerDimensionChangeEvent playerDimensionChangeEvent) {
                        listener.onPlayerDimensionChange(playerDimensionChangeEvent);
                    } else if (event instanceof final PlayerBlockBreakEvent playerBlockBreakEvent) {
                        listener.onPlayerBreakBlock(playerBlockBreakEvent);
                    } else if (event instanceof final PlayerBlockPlaceEvent playerBlockPlaceEvent) {
                        listener.onPlayerPlaceBlock(playerBlockPlaceEvent);
                    } else if (event instanceof final PlayerCommandEvent playerCommandEvent) {
                        listener.onPlayerCommandEvent(playerCommandEvent);
                    } else if (event instanceof final PlayerInteractContainerEvent playerInteractContainerEvent) {
                        listener.onPlayerInteractContainerEvent(playerInteractContainerEvent);
                    } else if (event instanceof final PlayerInteractEntityWithContainerEvent playerInteractEntityWithContainerEvent) {
                        listener.onPlayerInteractEntityWithContainerEvent(playerInteractEntityWithContainerEvent);
                    }

                    //Server
                    else if (event instanceof final ServerStartEvent serverStartEvent) {
                        listener.onServerStart(serverStartEvent);
                    } else if (event instanceof final ServerRestartEvent serverRestartEvent) {
                        listener.onServerRestart(serverRestartEvent);
                    } else if (event instanceof final ServerClosedEvent serverClosedEvent) {
                        listener.onServerClose(serverClosedEvent);
                    } else if (event instanceof final ServerAlertEvent serverAlertEvent) {
                        listener.onServerAlert(serverAlertEvent);
                    } else if (event instanceof final TPSChangeEvent tpsChangeEvent) {
                        listener.onTpsChange(tpsChangeEvent);
                    } else if (event instanceof final BackupDoneEvent backupDoneEvent) {
                        listener.onBackupDone(backupDoneEvent);
                    } else if (event instanceof final BackupFailEvent backupFailEvent) {
                        listener.onBackupFail(backupFailEvent);
                    } else if (event instanceof final ServerUpdatingEvent serverUpdatingEvent) {
                        listener.onServerUpdating(serverUpdatingEvent);
                    } else if (event instanceof final ServerUpdatedEvent serverUpdatedEvent) {
                        listener.onServerUpdated(serverUpdatedEvent);
                    } else if (event instanceof final ServerUncaughtExceptionEvent serverUncaughtExceptionEvent) {
                        listener.onServerUncaughtException(serverUncaughtExceptionEvent);
                    }

                    //Extension
                    else if (event instanceof final ExtensionEnableEvent extensionEnableEvent) {
                        listener.onExtensionEnable(extensionEnableEvent);
                    } else if (event instanceof final ExtensionDisableEvent extensionDisableEvent) {
                        listener.onExtensionDisable(extensionDisableEvent);
                    } else {
                        this.logger.error("Wywołano nieznany event&6 " + event.getEventName());
                        return;
                    }
                });

                this.logger.debug("Wywołano&6 " + event.getEventName());
            } catch (final Exception exception) {
                this.logger.critical("&cNie można wykonać eventu&b " + event.getEventName() + "&c dla rozszerzenia&b " + extension.get().getName(), exception);
            }
        });
    }

//    public EventResponse callEventWithResponse(final ResponsibleEvent event) throws ExecutionException, InterruptedException {
//        return CompletableFuture.supplyAsync(() -> this.getEventResponse(event), this.listenerService)
//                .exceptionally(ex -> {
//                    ex.printStackTrace();
//                    return null;
//                }).get();
//    }

    public EventResponse callEventWithResponse(final ResponsibleEvent event) {
        return CompletableFuture.supplyAsync(() -> this.getEventResponse(event), this.listenerService).join();
    }

    public EventResponse getEventResponse(final ResponsibleEvent event) {
        this.listeners = new HashMap<>(this.listenerMap);

        final AtomicReference<Extension> extension = new AtomicReference<>();
        try {
            if (event instanceof final PlayerChatEvent playerChatEvent) {
                this.listeners.forEach((listener, ex) -> {
                    extension.set(ex);
                    final PlayerChatResponse chatResponse = listener.onPlayerChat(playerChatEvent);
                    if (chatResponse != null) {
                        this.chatResponse.set(chatResponse);
                    }
                });

                this.logger.debug("Wywołano&6 " + event.getEventName());
                return this.chatResponse.get();
            } else if (event instanceof final ServerConsoleCommandEvent serverConsoleCommandEvent) {
                this.listeners.forEach((listener, ex) -> {
                    extension.set(ex);
                    final ServerConsoleCommandResponse commandResponse = listener.onServerConsoleCommand(serverConsoleCommandEvent);
                    if (commandResponse != null) {
                        commandResponse.getActionToDo().run();
                    }
                });

                this.logger.debug("Wywołano&6 " + event.getEventName());
                return null;
            }
        } catch (final Exception exception) {
            this.logger.critical("&cNie można wykonać eventu&b " + event.getEventName() + "&c dla rozszerzenia&b " + extension.get().getName(), exception);
        }

        this.logger.error("Wykonano nieznany event&6 " + event.getEventName());
        return null;
    }
}