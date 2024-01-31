package me.indian.bds.event;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.event.player.PlayerChatEvent;
import me.indian.bds.event.player.PlayerDeathEvent;
import me.indian.bds.event.player.PlayerJoinEvent;
import me.indian.bds.event.player.PlayerQuitEvent;
import me.indian.bds.event.player.PlayerSpawnEvent;
import me.indian.bds.event.player.response.PlayerChatResponse;
import me.indian.bds.event.server.ServerStartEvent;
import me.indian.bds.event.server.TPSChangeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class EventManager {

    private final List<Listener> listenerList;

    public EventManager(final BDSAutoEnable bdsAutoEnable) {
        this.listenerList = new ArrayList<>();
    }

    public <T extends Listener> void registerListener(final T listener) {
        this.listenerList.add(listener);
    }

    public void callEvent(final Event event) {
        //Gracz
        if (event instanceof PlayerJoinEvent) {
            this.listenerList.forEach(listener -> listener.onPlayerJoin((PlayerJoinEvent) event));
        }
        if(event instanceof PlayerSpawnEvent){
            this.listenerList.forEach(listener -> listener.onPlayerSpawn((PlayerSpawnEvent) event));
        }
        if (event instanceof PlayerQuitEvent) {
            this.listenerList.forEach(listener -> listener.onPlayerQuit((PlayerQuitEvent) event));
        }
        if (event instanceof PlayerDeathEvent) {
            this.listenerList.forEach(listener -> listener.onPlayerDeath((PlayerDeathEvent) event));
        }


        //Server
        if (event instanceof ServerStartEvent) {
            this.listenerList.forEach(listener -> listener.onServerStart((ServerStartEvent) event));
        }

        if (event instanceof TPSChangeEvent) {
            this.listenerList.forEach(listener -> listener.onTpsChange((TPSChangeEvent) event));
        }
        System.out.println("Wywołano " + event.getEventName());
    }

    public EventResponse callEventWithResponse(final Event event){
        if (event instanceof PlayerChatEvent) {
            final AtomicReference<EventResponse> response = null;

            this.listenerList.forEach(listener -> {
                final PlayerChatResponse chatResponse = listener.onPlayerChat((PlayerChatEvent) event);
                if(chatResponse != null){
                    response.set(chatResponse);
                }
            });

            return response.get();
        }

        System.out.println("Wywołano " + event.getEventName());
        return null;
    }
}