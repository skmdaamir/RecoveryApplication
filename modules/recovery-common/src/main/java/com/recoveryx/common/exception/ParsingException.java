package com.recoveryx.common.exception;

/**
 * Indicates parsing failure for filesystem structures or metadata.
 */
public class ParsingException extends RecoveryXException {

    public ParsingException(String message) {
        super(message);
    }

    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}