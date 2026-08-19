package com.recoveryx.nativeaccess.util;

/**
 * Validates and computes alignment requirements for direct sector reads.
 */
public final class AlignmentUtils {

    private AlignmentUtils() {
    }

    public static boolean isAligned(long value, int alignment) {
        if (alignment <= 0) {
            throw new IllegalArgumentException("alignment must be > 0");
        }
        return value % alignment == 0;
    }

    public static void requireAligned(long offset, int length, int sectorSize) {
        if (!isAligned(offset, sectorSize)) {
            throw new IllegalArgumentException("offset must be aligned to sector size");
        }
        if (!isAligned(length, sectorSize)) {
            throw new IllegalArgumentException("length must be aligned to sector size");
        }
    }
}