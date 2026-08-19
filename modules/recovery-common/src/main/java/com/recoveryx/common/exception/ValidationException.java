package com.recoveryx.common.exception;

/**
 * Indicates validation failure for requests, state, or model invariants.
 */
public class ValidationException extends RecoveryXException {

    public ValidationException(String message) {
        super(message);
    }
}