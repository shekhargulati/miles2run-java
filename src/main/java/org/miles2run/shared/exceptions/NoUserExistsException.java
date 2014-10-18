package org.miles2run.shared.exceptions;

public class NoUserExistsException extends RuntimeException {
    private final String message;

    public NoUserExistsException(final String username) {
        this.message = String.format("No user exists with username :%s", username);
    }

    public String getMessage() {
        return message;
    }
}
