package com.recoveryx.common.util;

/**
 * Formats byte counts to human-readable strings.
 */
public final class SizeFormatter {

    private static final String[] UNITS = {"B", "KB", "MB", "GB", "TB", "PB"};

    private SizeFormatter() {
    }

    public static String humanReadable(long bytes) {
        if (bytes < 0) {
            throw new IllegalArgumentException("bytes must be >= 0");
        }
        if (bytes < 1024) {
            return bytes + " B";
        }

        double value = bytes;
        int index = 0;
        while (value >= 1024 && index < UNITS.length - 1) {
            value /= 1024.0;
            index++;
        }
        return "%.2f %s".formatted(value, UNITS[index]);
    }
}