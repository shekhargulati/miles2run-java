package org.miles2run.core.exceptions;

public class NoRecordExistsException extends RuntimeException {
    private final String message;

    public NoRecordExistsException(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
