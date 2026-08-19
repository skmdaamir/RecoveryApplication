package com.recoveryx.common.util;

import com.recoveryx.common.exception.ValidationException;

import java.util.Collection;
import java.util.Objects;

/**
 * Lightweight validation helpers for domain invariants.
 */
public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static String requireNotBlank(String value, String fieldName) {
        Objects.requireNonNull(fieldName, "fieldName must not be null");
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " must not be blank");
        }
        return value;
    }

    public static <T> T requireNonNull(T value, String fieldName) {
        Objects.requireNonNull(fieldName, "fieldName must not be null");
        if (value == null) {
            throw new ValidationException(fieldName + " must not be null");
        }
        return value;
    }

    public static long requireNonNegative(long value, String fieldName) {
        Objects.requireNonNull(fieldName, "fieldName must not be null");
        if (value < 0) {
            throw new ValidationException(fieldName + " must be >= 0");
        }
        return value;
    }

    public static int requirePositive(int value, String fieldName) {
        Objects.requireNonNull(fieldName, "fieldName must not be null");
        if (value <= 0) {
            throw new ValidationException(fieldName + " must be > 0");
        }
        return value;
    }

    public static <T> Collection<T> requireNotEmpty(Collection<T> values, String fieldName) {
        Objects.requireNonNull(fieldName, "fieldName must not be null");
        if (values == null || values.isEmpty()) {
            throw new ValidationException(fieldName + " must not be empty");
        }
        return values;
    }
}