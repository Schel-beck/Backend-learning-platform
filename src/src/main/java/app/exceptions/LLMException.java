package app.exceptions;
public class LLMException
        extends RuntimeException {

    private final boolean retryable;


    public LLMException(
            String message,
            boolean retryable
    ) {

        super(message);
        this.retryable = retryable;
    }


    public LLMException(
            String message,
            boolean retryable,
            Throwable cause
    ) {

        super(message, cause);
        this.retryable = retryable;
    }


    public boolean isRetryable() {
        return retryable;
    }
}
