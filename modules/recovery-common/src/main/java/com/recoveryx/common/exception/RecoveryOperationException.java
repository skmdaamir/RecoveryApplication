package com.recoveryx.common.exception;

/**
 * Indicates a failure in file recovery or reconstruction processing.
 */
public class RecoveryOperationException extends RecoveryXException {

    public RecoveryOperationException(String message) {
        super(message);
    }

    public RecoveryOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}