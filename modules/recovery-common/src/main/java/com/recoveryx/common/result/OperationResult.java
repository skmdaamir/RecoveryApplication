package com.recoveryx.common.result;

import java.util.Objects;
import java.util.Optional;

/**
 * Generic result wrapper for service operations.
 *
 * @param <T> payload type
 */
public final class OperationResult<T> {

    private final boolean success;
    private final T data;
    private final OperationError error;

    private OperationResult(boolean success, T data, OperationError error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static <T> OperationResult<T> success(T data) {
        return new OperationResult<>(true, data, null);
    }

    public static <T> OperationResult<T> failure(OperationError error) {
        return new OperationResult<>(false, null, Objects.requireNonNull(error, "error must not be null"));
    }

    public boolean isSuccess() {
        return success;
    }

    public Optional<T> data() {
        return Optional.ofNullable(data);
    }

    public Optional<OperationError> error() {
        return Optional.ofNullable(error);
    }

    public T requireData() {
        if (!success || data == null) {
            throw new IllegalStateException("No successful data present");
        }
        return data;
    }
}