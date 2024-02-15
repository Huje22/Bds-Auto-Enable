package me.indian.bds.event;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import me.indian.bds.extension.Extension;
import me.indian.bds.logger.Logger;

public class EventManager {

    private final Logger logger;
    private final Map<Listener, Extension> listenerMap;
    private final AtomicReference<PlayerChatResponse> chatResponse;

    public EventManager(final BDSAutoEnable bdsAutoEnable) {
        this.logger = bdsAutoEnable.getLogger();
        this.listenerMap = new LinkedHashMap<>();
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
        //Player
        if (event instanceof final PlayerJoinEvent playerJoinEvent) {
            this.listenerMap.forEach((listener, ex) -> listener.onPlayerJoin(playerJoinEvent));
        } else if (event instanceof final PlayerSpawnEvent playerSpawnEvent) {
            this.listenerMap.forEach((listener, ex) -> listener.onPlayerSpawn(playerSpawnEvent));
        } else if (event instanceof final PlayerQuitEvent playerQuitEvent) {
            this.listenerMap.forEach((listener, ex) -> listener.onPlayerQuit(playerQuitEvent));
        } else if (event instanceof final PlayerDeathEvent playerDeathEvent) {
            this.listenerMap.forEach((listener, ex) -> listener.onPlayerDeath(playerDeathEvent));
        } else if (event instanceof final PlayerMuteEvent playerMuteEvent) {
            this.listenerMap.forEach((listener, ex) -> listener.onPlayerMute(playerMuteEvent));
        } else if (event instanceof final PlayerUnMuteEvent playerUnMuteEvent) {
            this.listenerMap.forEach((listener, ex) -> listener.onPlayerUnMute(playerUnMuteEvent));
        }

        //Server
        else if (event instanceof final ServerStartEvent serverStartEvent) {
            this.listenerMap.forEach((listener, ex) -> listener.onServerStart(serverStartEvent));
        } else if (event instanceof final ServerRestartEvent serverRestartEvent) {
            this.listenerMap.forEach((listener, ex) -> listener.onServerRestart(serverRestartEvent));
        } else if (event instanceof final ServerClosedEvent serverClosedEvent) {
            this.listenerMap.forEach((listener, ex) -> listener.onServerClose(serverClosedEvent));
        } else if (event instanceof final TPSChangeEvent tpsChangeEvent) {
            this.listenerMap.forEach((listener, ex) -> listener.onTpsChange(tpsChangeEvent));
        } else if (event instanceof final BackupDoneEvent backupDoneEvent) {
            this.listenerMap.forEach((listener, ex) -> listener.onBackupDone(backupDoneEvent));
        } else if (event instanceof final BackupFailEvent backupFailEvent) {
            this.listenerMap.forEach((listener, ex) -> listener.onBackupFail(backupFailEvent));
        } else if (event instanceof final ServerUpdatingEvent serverUpdatingEvent) {
            this.listenerMap.forEach((listener, ex) -> listener.onServerUpdating(serverUpdatingEvent));
        } else if (event instanceof final ServerUpdatedEvent serverUpdatedEvent) {
            this.listenerMap.forEach((listener, ex) -> listener.onServerUpdated(serverUpdatedEvent));
        } else if (event instanceof final PlayerDimensionChangeEvent playerDimensionChangeEvent) {
            this.listenerMap.forEach((listener, ex) -> listener.onPlayerDimensionChange(playerDimensionChangeEvent));
        } else if (event instanceof final PlayerBlockBreakEvent playerBlockBreakEvent) {
            this.listenerMap.forEach((listener, ex) -> listener.onPlayerBreakBlock(playerBlockBreakEvent));
        } else if (event instanceof final PlayerBlockPlaceEvent playerBlockPlaceEvent) {
            this.listenerMap.forEach((listener, ex) -> listener.onPlayerPlaceBlock(playerBlockPlaceEvent));
        } else if (event instanceof final ExtensionEnableEvent extensionEnableEvent) {
            this.listenerMap.forEach((listener, ex) -> listener.onExtensionEnable(extensionEnableEvent));
        } else if (event instanceof final ExtensionDisableEvent extensionDisableEvent) {
            this.listenerMap.forEach((listener, ex) -> listener.onExtensionDisable(extensionDisableEvent));
        } else {
            this.logger.error("Wywołano nieznany event&6 " + event.getEventName());
            return;
        }
        this.logger.debug("Wywołano&6 " + event.getEventName());
    }

    public EventResponse callEventWithResponse(final ResponsibleEvent event) {
        this.logger.debug("Wywołano&6 " + event.getEventName());

        if (event instanceof final PlayerChatEvent playerChatEvent) {
            this.listenerMap.forEach((listener, ex) -> {
                final PlayerChatResponse chatResponse = listener.onPlayerChat(playerChatEvent);
                if (chatResponse != null) {
                    this.chatResponse.set(chatResponse);
                }
            });

            return this.chatResponse.get();
        } else if (event instanceof final ServerConsoleCommandEvent serverConsoleCommandEvent) {
            this.listenerMap.forEach((listener, ex) -> {
                final ServerConsoleCommandResponse commandResponse = listener.onServerConsoleCommand(serverConsoleCommandEvent);
                if (commandResponse != null) {
                    commandResponse.getActionToDo().run();
                }
            });
        }


        this.logger.error("Wykonano nieznany event&6 " + event.getEventName());
        return null;
    }
}