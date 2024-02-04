package me.indian.bds.event;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.event.player.PlayerChatEvent;
import me.indian.bds.event.player.PlayerDeathEvent;
import me.indian.bds.event.player.PlayerJoinEvent;
import me.indian.bds.event.player.PlayerMuteEvent;
import me.indian.bds.event.player.PlayerQuitEvent;
import me.indian.bds.event.player.PlayerSpawnEvent;
import me.indian.bds.event.player.PlayerUnMuteEvent;
import me.indian.bds.event.player.response.PlayerChatResponse;
import me.indian.bds.event.server.ConsoleCommandEvent;
import me.indian.bds.event.server.PlayerDimensionChangeEvent;
import me.indian.bds.event.server.ServerClosedEvent;
import me.indian.bds.event.server.ServerRestartEvent;
import me.indian.bds.event.server.ServerStartEvent;
import me.indian.bds.event.server.ServerUpdatedEvent;
import me.indian.bds.event.server.ServerUpdatingEvent;
import me.indian.bds.event.server.TPSChangeEvent;
import me.indian.bds.event.server.response.ConsoleCommandResponse;
import me.indian.bds.event.watchdog.BackupDoneEvent;
import me.indian.bds.event.watchdog.BackupFailEvent;
import me.indian.bds.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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
        }

        if (event instanceof final PlayerSpawnEvent playerSpawnEvent) {
            this.listenerList.forEach(listener -> listener.onPlayerSpawn(playerSpawnEvent));
        }

        if (event instanceof final PlayerQuitEvent playerQuitEvent) {
            this.listenerList.forEach(listener -> listener.onPlayerQuit(playerQuitEvent));
        }

        if (event instanceof final PlayerDeathEvent playerDeathEvent) {
            this.listenerList.forEach(listener -> listener.onPlayerDeath(playerDeathEvent));
        }

        if(event instanceof final PlayerMuteEvent playerMuteEvent){
            this.listenerList.forEach(listener -> listener.onPlayerMute(playerMuteEvent));
        }

        if(event instanceof final PlayerUnMuteEvent playerUnMuteEvent){
            this.listenerList.forEach(listener -> listener.onPlayerUnMute(playerUnMuteEvent));
        }

        //Server
        if (event instanceof final ServerStartEvent serverStartEvent) {
            this.listenerList.forEach(listener -> listener.onServerStart(serverStartEvent));
        }

        if (event instanceof final ServerRestartEvent serverRestartEvent) {
            this.listenerList.forEach(listener -> listener.onServerRestart(serverRestartEvent));
        }

        if (event instanceof final ServerClosedEvent serverClosedEvent) {
            this.listenerList.forEach(listener -> listener.onServerClose(serverClosedEvent));
        }

        if (event instanceof final TPSChangeEvent tpsChangeEvent) {
            this.listenerList.forEach(listener -> listener.onTpsChange(tpsChangeEvent));
        }

        if (event instanceof final BackupDoneEvent backupDoneEvent) {
            this.listenerList.forEach(listener -> listener.onBackupDone(backupDoneEvent));
        }

        if (event instanceof final BackupFailEvent backupFailEvent) {
            this.listenerList.forEach(listener -> listener.onBackupFail(backupFailEvent));
        }

        if (event instanceof final ServerUpdatingEvent serverUpdatingEvent) {
            this.listenerList.forEach(listener -> listener.onServerUpdating(serverUpdatingEvent));
        }

        if (event instanceof final ServerUpdatedEvent serverUpdatedEvent) {
            this.listenerList.forEach(listener -> listener.onServerUpdated(serverUpdatedEvent));
        }

        if (event instanceof final PlayerDimensionChangeEvent playerDimensionChangeEvent) {
            this.listenerList.forEach(listener -> listener.onPlayerDimensionChange(playerDimensionChangeEvent));
        }

//        if(event instanceof final ){
//            this.listenerList.forEach(listener -> listener.on    );
//        }

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
        if (event instanceof final ConsoleCommandEvent consoleCommandEvent) {
            this.listenerList.forEach(listener -> {
                final ConsoleCommandResponse commandResponse = listener.onConsoleCommand(consoleCommandEvent);
                if (commandResponse != null) {
                    commandResponse.getActionToDo().run();
                }
            });
        }

        return null;
    }
}