package com.recoveryx.common.util;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Collection helper methods.
 */
public final class CollectionUtils {

    private CollectionUtils() {
    }

    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static <T> List<T> immutableCopy(Collection<T> collection) {
        Objects.requireNonNull(collection, "collection must not be null");
        return List.copyOf(collection);
    }
}