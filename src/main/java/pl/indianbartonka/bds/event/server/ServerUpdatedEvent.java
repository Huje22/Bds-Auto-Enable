package pl.indianbartonka.bds.event.server;

import pl.indianbartonka.bds.event.Event;

public class ServerUpdatedEvent extends Event {

    private final String version;

    public ServerUpdatedEvent(final String version) {
        this.version = version;
    }

    public String getVersion() {
        return this.version;
    }
}