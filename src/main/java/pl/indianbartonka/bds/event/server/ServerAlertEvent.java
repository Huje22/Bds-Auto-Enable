package pl.indianbartonka.bds.event.server;

import pl.indianbartonka.bds.event.Event;
import pl.indianbartonka.util.logger.LogState;
import org.jetbrains.annotations.Nullable;

public class ServerAlertEvent extends Event {

    private final String message;
    private final String additionalInfo;
    private final LogState alertState;
    private final Throwable throwable;

    public ServerAlertEvent(final String message, final String additionalInfo, final Throwable throwable, final LogState alertState) {
        this.message = message;
        this.additionalInfo = additionalInfo;
        this.alertState = alertState;
        this.throwable = throwable;
    }

    public ServerAlertEvent(final String message, final Throwable throwable, final LogState alertState) {
        this.message = message;
        this.additionalInfo = null;
        this.alertState = alertState;
        this.throwable = throwable;
    }

    public ServerAlertEvent(final String message, final String additionalInfo, final LogState alertState) {
        this.message = message;
        this.additionalInfo = additionalInfo;
        this.alertState = alertState;
        this.throwable = null;
    }

    public ServerAlertEvent(final String message, final LogState alertState) {
        this.message = message;
        this.additionalInfo = null;
        this.alertState = alertState;
        this.throwable = null;
    }

    public String getMessage() {
        return this.message;
    }

    @Nullable
    public String getAdditionalInfo() {
        return this.additionalInfo;
    }

    public LogState getAlertState() {
        return this.alertState;
    }

    @Nullable
    public Throwable getThrowable() {
        return this.throwable;
    }
}
