package com.recoveryx.nativeaccess.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MultiStringDecoderTest {

    @Test
    void shouldDecodeWindowsMultiString() {
        char[] buffer = new char[] {
                'C', ':', '\\', '\0',
                'D', ':', '\\', '\0',
                '\0'
        };

        List<String> values = MultiStringDecoder.decode(buffer, buffer.length);
        assertEquals(List.of("C:\\", "D:\\"), values);
    }
}