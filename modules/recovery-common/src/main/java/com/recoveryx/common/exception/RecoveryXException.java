package com.recoveryx.common.exception;

/**
 * Base unchecked exception for RecoveryX Pro.
 */
public class RecoveryXException extends RuntimeException {

    public RecoveryXException(String message) {
        super(message);
    }

    public RecoveryXException(String message, Throwable cause) {
        super(message, cause);
    }
}