package me.indian.bds.event;

import me.indian.bds.event.player.PlayerChatEvent;
import me.indian.bds.event.player.response.PlayerChatResponse;
import me.indian.bds.event.server.ServerConsoleCommandEvent;
import me.indian.bds.event.server.response.ServerConsoleCommandResponse;
import org.jetbrains.annotations.Nullable;

public abstract class Listener {

    //Player
    @Nullable
    public PlayerChatResponse onPlayerChat(final PlayerChatEvent event) {

        return null;
    }

    //Server
    @Nullable
    public ServerConsoleCommandResponse onServerConsoleCommand(final ServerConsoleCommandEvent event) {

        return null;
    }
}