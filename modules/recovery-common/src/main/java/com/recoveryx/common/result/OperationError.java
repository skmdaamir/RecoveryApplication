package com.recoveryx.common.result;

import java.util.Objects;

/**
 * Structured error information for operation results.
 *
 * @param code stable error code
 * @param message human-readable error message
 * @param details optional detail payload
 */
public record OperationError(String code, String message, String details) {

    public OperationError {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(message, "message must not be null");
    }

    public static OperationError of(String code, String message) {
        return new OperationError(code, message, null);
    }

    public static OperationError of(String code, String message, String details) {
        return new OperationError(code, message, details);
    }
}