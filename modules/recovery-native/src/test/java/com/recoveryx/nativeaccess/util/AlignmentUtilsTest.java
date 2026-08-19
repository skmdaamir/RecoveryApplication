package com.recoveryx.nativeaccess.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AlignmentUtilsTest {

    @Test
    void shouldAcceptAlignedValues() {
        assertDoesNotThrow(() -> AlignmentUtils.requireAligned(0, 4096, 512));
        assertDoesNotThrow(() -> AlignmentUtils.requireAligned(1024, 2048, 512));
    }

    @Test
    void shouldRejectUnalignedOffset() {
        assertThrows(IllegalArgumentException.class, () -> AlignmentUtils.requireAligned(3, 1024, 512));
    }

    @Test
    void shouldRejectUnalignedLength() {
        assertThrows(IllegalArgumentException.class, () -> AlignmentUtils.requireAligned(0, 1000, 512));
    }
}