package me.indian.bds.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.event.player.PlayerChatEvent;
import me.indian.bds.event.player.response.PlayerChatResponse;
import me.indian.bds.event.server.ServerConsoleCommandEvent;
import me.indian.bds.event.server.response.ServerConsoleCommandResponse;
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
        if (extension == null) throw new NullPointerException("Rozszerzenie nie może być null");
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

    /**
     * Kod użyty z <a href="https://github.com/EnderHC-PL/EnderHC-Platform/blob/master/platform-eventbus/src/main/java/pl/enderhc/platform/eventbus/EventBus.java">...</a>
     * Za zgodą Neziwa
     *
     * @param event - Event do wywołania w listenerach
     */
    public void callEvent(final Event event) {
        this.listenerService.execute(() -> {
            this.listeners = new HashMap<>(this.listenerMap);
            for (final Map.Entry<Listener, Extension> entry : this.listeners.entrySet()) {
                final Listener listener = entry.getKey();

                final Method[] subscribeMethods = Arrays.stream(listener.getClass().getDeclaredMethods())
                        .filter(method -> method.isAnnotationPresent(EventHandler.class))
                        .filter(method -> method.getParameters().length == 1)
                        .filter(method -> method.getParameterTypes()[0].isAssignableFrom(event.getClass()))
                        .toArray(Method[]::new);
                for (final Method method : subscribeMethods) {
                    try {
                        method.setAccessible(true);
                        method.invoke(listener, event);
                        this.logger.debug("Wywołano&6 " + event.getEventName() + "&r dla&d " + listener.getClass().getName());
                    } catch (final Throwable throwable) {
                        this.logger.error("&cWystąpił błąd podczas wywoływania eventu:&1 " + event.getEventName() + "&c w listenerze:&1 " + listener.getClass().getName(), throwable);
                    }
                }
            }
        });
    }

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