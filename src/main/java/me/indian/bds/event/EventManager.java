package me.indian.bds.event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.event.player.PlayerBlockBreakEvent;
import me.indian.bds.event.player.PlayerBlockPlaceEvent;
import me.indian.bds.event.player.PlayerChatEvent;
import me.indian.bds.event.player.PlayerDeathEvent;
import me.indian.bds.event.player.PlayerDimensionChangeEvent;
import me.indian.bds.event.player.PlayerJoinEvent;
import me.indian.bds.event.player.PlayerMuteEvent;
import me.indian.bds.event.player.PlayerQuitEvent;
import me.indian.bds.event.player.PlayerSpawnEvent;
import me.indian.bds.event.player.PlayerUnMuteEvent;
import me.indian.bds.event.player.response.PlayerChatResponse;
import me.indian.bds.event.server.ExtensionDisableEvent;
import me.indian.bds.event.server.ExtensionEnableEvent;
import me.indian.bds.event.server.ServerClosedEvent;
import me.indian.bds.event.server.ServerConsoleCommandEvent;
import me.indian.bds.event.server.ServerRestartEvent;
import me.indian.bds.event.server.ServerStartEvent;
import me.indian.bds.event.server.ServerUpdatedEvent;
import me.indian.bds.event.server.ServerUpdatingEvent;
import me.indian.bds.event.server.TPSChangeEvent;
import me.indian.bds.event.server.response.ServerConsoleCommandResponse;
import me.indian.bds.event.watchdog.BackupDoneEvent;
import me.indian.bds.event.watchdog.BackupFailEvent;
import me.indian.bds.logger.Logger;

public class EventManager {

    private final Logger logger;
    private final List<Listener> listenerList;
    private final AtomicReference<PlayerChatResponse> chatResponse;

    public EventManager(final BDSAutoEnable bdsAutoEnable) {
        this.logger = bdsAutoEnable.getLogger();
        this.listenerList = new ArrayList<>();
        this.chatResponse = new AtomicReference<>();
    }

    public <T extends Listener> void registerListener(final T listener) {
        this.listenerList.add(listener);
    }

    public void callEvent(final Event event) {
        //Player
        if (event instanceof final PlayerJoinEvent playerJoinEvent) {
            this.listenerList.forEach(listener -> listener.onPlayerJoin(playerJoinEvent));
        } else if (event instanceof final PlayerSpawnEvent playerSpawnEvent) {
            this.listenerList.forEach(listener -> listener.onPlayerSpawn(playerSpawnEvent));
        } else if (event instanceof final PlayerQuitEvent playerQuitEvent) {
            this.listenerList.forEach(listener -> listener.onPlayerQuit(playerQuitEvent));
        } else if (event instanceof final PlayerDeathEvent playerDeathEvent) {
            this.listenerList.forEach(listener -> listener.onPlayerDeath(playerDeathEvent));
        } else if (event instanceof final PlayerMuteEvent playerMuteEvent) {
            this.listenerList.forEach(listener -> listener.onPlayerMute(playerMuteEvent));
        } else if (event instanceof final PlayerUnMuteEvent playerUnMuteEvent) {
            this.listenerList.forEach(listener -> listener.onPlayerUnMute(playerUnMuteEvent));
        }

        //Server
        else if (event instanceof final ServerStartEvent serverStartEvent) {
            this.listenerList.forEach(listener -> listener.onServerStart(serverStartEvent));
        } else if (event instanceof final ServerRestartEvent serverRestartEvent) {
            this.listenerList.forEach(listener -> listener.onServerRestart(serverRestartEvent));
        } else if (event instanceof final ServerClosedEvent serverClosedEvent) {
            this.listenerList.forEach(listener -> listener.onServerClose(serverClosedEvent));
        } else if (event instanceof final TPSChangeEvent tpsChangeEvent) {
            this.listenerList.forEach(listener -> listener.onTpsChange(tpsChangeEvent));
        } else if (event instanceof final BackupDoneEvent backupDoneEvent) {
            this.listenerList.forEach(listener -> listener.onBackupDone(backupDoneEvent));
        } else if (event instanceof final BackupFailEvent backupFailEvent) {
            this.listenerList.forEach(listener -> listener.onBackupFail(backupFailEvent));
        } else if (event instanceof final ServerUpdatingEvent serverUpdatingEvent) {
            this.listenerList.forEach(listener -> listener.onServerUpdating(serverUpdatingEvent));
        } else if (event instanceof final ServerUpdatedEvent serverUpdatedEvent) {
            this.listenerList.forEach(listener -> listener.onServerUpdated(serverUpdatedEvent));
        } else if (event instanceof final PlayerDimensionChangeEvent playerDimensionChangeEvent) {
            this.listenerList.forEach(listener -> listener.onPlayerDimensionChange(playerDimensionChangeEvent));
        } else if (event instanceof final PlayerBlockBreakEvent playerBlockBreakEvent) {
            this.listenerList.forEach(listener -> listener.onPlayerBreakBlock(playerBlockBreakEvent));
        } else if (event instanceof final PlayerBlockPlaceEvent playerBlockPlaceEvent) {
            this.listenerList.forEach(listener -> listener.onPlayerPlaceBlock(playerBlockPlaceEvent));
        } else if (event instanceof final ExtensionEnableEvent extensionEnableEvent) {
            this.listenerList.forEach(listener -> listener.onExtensionEnable(extensionEnableEvent));
        } else if (event instanceof final ExtensionDisableEvent extensionDisableEvent) {
            this.listenerList.forEach(listener -> listener.onExtensionDisable(extensionDisableEvent));
        } else {
            this.logger.error("Wykonano nieznany event&6 " + event.getEventName());
            return;
        }
        this.logger.debug("Wywołano&6 " + event.getEventName());
    }

    public EventResponse callEventWithResponse(final ResponsibleEvent event) {
        this.logger.debug("Wywołano&6 " + event.getEventName());

        if (event instanceof final PlayerChatEvent playerChatEvent) {
            this.listenerList.forEach(listener -> {
                final PlayerChatResponse chatResponse = listener.onPlayerChat(playerChatEvent);
                if (chatResponse != null) {
                    this.chatResponse.set(chatResponse);
                }
            });

            return this.chatResponse.get();
        }
        if (event instanceof final ServerConsoleCommandEvent serverConsoleCommandEvent) {
            this.listenerList.forEach(listener -> {
                final ServerConsoleCommandResponse commandResponse = listener.onServerConsoleCommand(serverConsoleCommandEvent);
                if (commandResponse != null) {
                    commandResponse.getActionToDo().run();
                }
            });
        }

        return null;
    }
}