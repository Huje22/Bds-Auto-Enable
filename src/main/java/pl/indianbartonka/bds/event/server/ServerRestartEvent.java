package pl.indianbartonka.bds.event.server;

import pl.indianbartonka.bds.event.Event;
import org.jetbrains.annotations.Nullable;

public class ServerRestartEvent extends Event {

    private final String reason;

    public ServerRestartEvent(final String reason) {
        this.reason = reason;
    }

    @Nullable
    public String getReason() {
        return this.reason;
    }
}