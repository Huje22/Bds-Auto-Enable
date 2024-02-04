package me.indian.bds.event.server;

import me.indian.bds.event.Event;

public class ServerUpdatingEvent extends Event {

    private final String version;

    public ServerUpdatingEvent(final String version) {
        this.version = version;
    }

    public String getVersion() {
        return this.version;
    }
}
