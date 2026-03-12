package ecommerce.example.ecommerce.application.common;

/**
 * Base exception for application use cases.
 */
public class UseCaseException extends RuntimeException {
    public UseCaseException(String message) {
        super(message);
    }

    public UseCaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
