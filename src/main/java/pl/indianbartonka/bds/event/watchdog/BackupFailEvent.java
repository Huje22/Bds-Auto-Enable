package pl.indianbartonka.bds.event.watchdog;

import pl.indianbartonka.bds.event.Event;

public class BackupFailEvent extends Event {

    private final Exception exception;

    public BackupFailEvent(final Exception exception) {
        this.exception = exception;
    }

    public Exception getException() {
        return this.exception;
    }
}
