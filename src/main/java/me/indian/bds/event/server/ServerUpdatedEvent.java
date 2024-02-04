package me.indian.bds.event.server;

import me.indian.bds.event.Event;

public class ServerUpdatedEvent extends Event {

    private final String version;

    public ServerUpdatedEvent(final String version) {
        this.version = version;
    }

    public String getVersion() {
        return this.version;
    }
}