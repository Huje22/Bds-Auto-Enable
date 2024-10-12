package pl.indianbartonka.bds.exception;

public class ExtensionException extends RuntimeException {

    public ExtensionException() {
        super();
    }

    public ExtensionException(final String message) {
        super(message);
    }

    public ExtensionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ExtensionException(final Throwable cause) {
        super(cause);
    }

    protected ExtensionException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
