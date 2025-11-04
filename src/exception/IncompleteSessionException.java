package exception;

public class IncompleteSessionException extends RuntimeException {

    public IncompleteSessionException(String message) {
        super(message);
    }

    public IncompleteSessionException(String message, Throwable cause) {
        super(message, cause);
    }
}
