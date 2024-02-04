package me.indian.bds.event.watchdog;

import me.indian.bds.event.Event;

public class BackupFailEvent extends Event {

    private final Exception exception;

    public BackupFailEvent(final Exception exception) {
        this.exception = exception;
    }

    public Exception getException() {
        return this.exception;
    }
}
