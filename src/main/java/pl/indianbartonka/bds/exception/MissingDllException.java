package pl.indianbartonka.bds.exception;

public class MissingDllException extends RuntimeException {

    public MissingDllException(final String message) {
        super(message);
    }
}
