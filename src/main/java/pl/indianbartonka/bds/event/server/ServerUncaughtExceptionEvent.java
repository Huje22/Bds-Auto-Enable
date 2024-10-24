package pl.indianbartonka.bds.event.server;

import pl.indianbartonka.bds.event.Event;

public class ServerUncaughtExceptionEvent extends Event {

    private final Thread thread;
    private final Throwable throwable;


    public ServerUncaughtExceptionEvent(final Thread thread, final Throwable throwable) {
        this.thread = thread;
        this.throwable = throwable;
    }

    public Thread getThread() {
        return this.thread;
    }

    public Throwable getThrowable() {
        return this.throwable;
    }
}
