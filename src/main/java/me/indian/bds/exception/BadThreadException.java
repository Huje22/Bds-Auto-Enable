package me.indian.bds.exception;

public class BadThreadException extends Exception {

    public BadThreadException() {
        super();
    }

    public BadThreadException(final String message) {
        super(message);
    }

    public BadThreadException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public BadThreadException(final Throwable cause) {
        super(cause);
    }
}
