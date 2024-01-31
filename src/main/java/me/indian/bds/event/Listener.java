package me.indian.bds.event;

import me.indian.bds.event.player.PlayerChatEvent;
import me.indian.bds.event.player.PlayerDeathEvent;
import me.indian.bds.event.player.PlayerJoinEvent;
import me.indian.bds.event.player.PlayerQuitEvent;
import me.indian.bds.event.player.PlayerSpawnEvent;
import me.indian.bds.event.player.response.PlayerChatResponse;
import me.indian.bds.event.server.ServerStartEvent;
import me.indian.bds.event.server.TPSChangeEvent;

public abstract class Listener {


    public void onPlayerJoin(final PlayerJoinEvent event){

    }

    public void onPlayerSpawn(final PlayerSpawnEvent event){

    }

    public void onPlayerQuit(final PlayerQuitEvent playerQuitEvent){

    }

    public PlayerChatResponse onPlayerChat(final PlayerChatEvent event){

        return null;
    }

    public void onPlayerDeath(final PlayerDeathEvent event){

    }

    public void onServerStart(final ServerStartEvent event){

    }

    public void onTpsChange(final TPSChangeEvent event){
    }

}
