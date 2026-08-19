package com.recoveryx.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SizeFormatterTest {

    @Test
    void shouldFormatBytesAsHumanReadableString() {
        assertEquals("1.00 KB", SizeFormatter.humanReadable(1024));
        assertEquals("1.50 KB", SizeFormatter.humanReadable(1536));
    }
}