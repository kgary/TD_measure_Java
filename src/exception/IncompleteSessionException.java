package exception;

public class IncompleteSessionException extends Exception {

    public IncompleteSessionException(String message) {
        super(message);
    }

    public IncompleteSessionException(String message, Throwable cause) {
        super(message, cause);
    }
}
