package com.recoveryx.common.result;

import java.util.List;
import java.util.Objects;

/**
 * Simple immutable paginated result.
 *
 * @param <T> item type
 */
public record PageResult<T>(List<T> items, int pageNumber, int pageSize, long totalItems) {

    public PageResult {
        items = List.copyOf(Objects.requireNonNull(items, "items must not be null"));
        if (pageNumber < 0) {
            throw new IllegalArgumentException("pageNumber must be >= 0");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be > 0");
        }
        if (totalItems < 0) {
            throw new IllegalArgumentException("totalItems must be >= 0");
        }
    }
}