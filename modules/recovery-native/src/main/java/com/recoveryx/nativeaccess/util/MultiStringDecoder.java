package com.recoveryx.nativeaccess.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Decodes Windows MULTI_SZ character buffers into Java strings.
 */
public final class MultiStringDecoder {

    private MultiStringDecoder() {
    }

    public static List<String> decode(char[] buffer, int length) {
        List<String> values = new ArrayList<>();
        if (buffer == null || length <= 0) {
            return values;
        }

        StringBuilder current = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char c = buffer[i];
            if (c == '\0') {
                if (current.isEmpty()) {
                    break;
                }
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        return values;
    }
}