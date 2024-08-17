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
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.event.server.response.ServerConsoleCommandResponse;
import me.indian.bds.extension.Extension;
import me.indian.util.ThreadUtil;
import me.indian.util.logger.Logger;

public class EventManager {

    private final Logger logger;
    private final Map<Listener, Extension> listenerMap;
    private final ExecutorService listenerService;
    private Map<Listener, Extension> listeners;

    public EventManager(final BDSAutoEnable bdsAutoEnable) {
        this.logger = bdsAutoEnable.getLogger();
        this.listenerMap = new LinkedHashMap<>();
        this.listenerService = Executors.newCachedThreadPool(new ThreadUtil("Listeners"));
    }

    public <T extends Listener> void registerListener(final T listener, final Extension extension) {
        if (extension == null) throw new NullPointerException("Rozszerzenie nie może być null");
        this.listenerMap.put(listener, extension);
    }

    public void unRegister(final Extension extension) {
        final List<Listener> listenerToRemove = new ArrayList<>();

        this.listenerMap.forEach((listener, ex) -> {
            if (ex == extension) listenerToRemove.add(listener);
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
                    } catch (final Exception exception) {
                        this.logger.error("&cWystąpił błąd podczas wywoływania eventu:&1 " + event.getEventName() + "&c w listenerze:&1 " + listener.getClass().getName(), exception);
                    }
                }
            }
        });
    }

    private List<EventResponse> eventResponses(final ResponsibleEvent event) {
        final List<EventResponse> responseList = new ArrayList<>();
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
                    final Class<?> type = method.getReturnType();

                    if (EventResponse.class.isAssignableFrom(type)) {
                        final EventResponse eventResponse = (EventResponse) method.invoke(listener, event);

                        if (eventResponse instanceof final ServerConsoleCommandResponse response) {
                            response.getActionToDo().run();
                        }

                        if (eventResponse != null) responseList.add(eventResponse);
                        this.logger.debug("Wywołano&6 " + event.getEventName() + "&r dla&d " + listener.getClass().getName());
                    }
                } catch (final Exception exception) {
                    this.logger.error("&cWystąpił błąd podczas wywoływania eventu:&1 " + event.getEventName() + "&c w listenerze:&1 " + listener.getClass().getName(), exception);
                }
            }
        }

        return responseList;
    }

    public List<EventResponse> callEventsWithResponse(final ResponsibleEvent event) {
        return CompletableFuture.supplyAsync(() -> this.eventResponses(event), this.listenerService).join();
    }
}