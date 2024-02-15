package me.indian.bds.event;

import me.indian.bds.event.player.PlayerBlockBreakEvent;
import me.indian.bds.event.player.PlayerBlockPlaceEvent;
import me.indian.bds.event.player.PlayerChatEvent;
import me.indian.bds.event.player.PlayerDeathEvent;
import me.indian.bds.event.player.PlayerJoinEvent;
import me.indian.bds.event.player.PlayerMuteEvent;
import me.indian.bds.event.player.PlayerQuitEvent;
import me.indian.bds.event.player.PlayerSpawnEvent;
import me.indian.bds.event.player.PlayerUnMuteEvent;
import me.indian.bds.event.player.response.PlayerChatResponse;
import me.indian.bds.event.server.ExtensionDisableEvent;
import me.indian.bds.event.server.ExtensionEnableEvent;
import me.indian.bds.event.server.ServerConsoleCommandEvent;
import me.indian.bds.event.player.PlayerDimensionChangeEvent;
import me.indian.bds.event.server.ServerClosedEvent;
import me.indian.bds.event.server.ServerRestartEvent;
import me.indian.bds.event.server.ServerStartEvent;
import me.indian.bds.event.server.ServerUpdatedEvent;
import me.indian.bds.event.server.ServerUpdatingEvent;
import me.indian.bds.event.server.TPSChangeEvent;
import me.indian.bds.event.server.response.ServerConsoleCommandResponse;
import me.indian.bds.event.watchdog.BackupDoneEvent;
import me.indian.bds.event.watchdog.BackupFailEvent;
import org.jetbrains.annotations.Nullable;

public abstract class Listener {

    //Player
    public void onPlayerJoin(final PlayerJoinEvent event) {

    }

    public void onPlayerSpawn(final PlayerSpawnEvent event) {

    }

    public void onPlayerQuit(final PlayerQuitEvent event) {

    }

    @Nullable
    public PlayerChatResponse onPlayerChat(final PlayerChatEvent event) {

        return null;
    }

    public void onPlayerMute(final PlayerMuteEvent event) {

    }

    public void onPlayerUnMute(final PlayerUnMuteEvent event) {

    }

    public void onPlayerDeath(final PlayerDeathEvent event) {

    }

    public void onPlayerDimensionChange(final PlayerDimensionChangeEvent event) {

    }

    public void onPlayerBreakBlock(final PlayerBlockBreakEvent event) {

    }

    public void onPlayerPlaceBlock(final PlayerBlockPlaceEvent event) {

    }


    //Server
    @Nullable
    public ServerConsoleCommandResponse onServerConsoleCommand(final ServerConsoleCommandEvent event) {

        return null;
    }

    public void onServerStart(final ServerStartEvent event) {

    }

    public void onServerRestart(final ServerRestartEvent event) {

    }

    public void onServerClose(final ServerClosedEvent event) {

    }

    public void onServerUpdating(final ServerUpdatingEvent event) {

    }

    public void onServerUpdated(final ServerUpdatedEvent event) {

    }

    public void onTpsChange(final TPSChangeEvent event) {
    }

    public void onBackupDone(final BackupDoneEvent event) {

    }

    public void onBackupFail(final BackupFailEvent event) {

    }

    public void onExtensionEnable(final ExtensionEnableEvent event){

    }

    public void onExtensionDisable(final ExtensionDisableEvent event){

    }
}