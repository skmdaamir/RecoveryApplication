package com.recoveryx.common.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Utility methods for dealing with instants and formatting.
 */
public final class InstantUtils {

    private static final DateTimeFormatter ISO_LOCAL_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private InstantUtils() {
    }

    public static Instant nowUtc() {
        return Instant.now();
    }

    public static String format(Instant instant) {
        Objects.requireNonNull(instant, "instant must not be null");
        return ISO_LOCAL_FORMATTER.format(instant);
    }
}