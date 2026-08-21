package com.recoveryx.preview.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility for producing canonical 16-byte hex dump lines for hex previewers.
 */
public final class HexDumpGenerator {

    private HexDumpGenerator() {
    }

    /**
     * Generates standard 16-byte hex dump formatted lines.
     * Example: 00000000  49 44 33 03 00 00 00 00  00 00 00 00 00 00 00 00  |ID3.............|
     *
     * @param data     raw byte array
     * @param maxBytes maximum number of bytes to format
     * @return list of formatted hex lines
     */
    public static List<String> generateHexDump(byte[] data, int maxBytes) {
        if (data == null || data.length == 0) {
            return Collections.emptyList();
        }

        int len = Math.min(data.length, Math.max(16, maxBytes));
        List<String> lines = new ArrayList<>();

        for (int i = 0; i < len; i += 16) {
            StringBuilder sb = new StringBuilder();

            // 1. Offset header (8 hex chars)
            sb.append(String.format("%08X  ", i));

            // 2. Hex bytes (16 bytes, grouped into two 8-byte blocks)
            StringBuilder ascii = new StringBuilder(" |");
            for (int j = 0; j < 16; j++) {
                if (i + j < len) {
                    byte b = data[i + j];
                    sb.append(String.format("%02X ", b & 0xFF));

                    char c = (char) (b & 0xFF);
                    if (c >= 32 && c <= 126) {
                        ascii.append(c);
                    } else {
                        ascii.append('.');
                    }
                } else {
                    sb.append("   "); // Padding for incomplete line
                }

                if (j == 7) {
                    sb.append(" "); // Extra space between 8-byte blocks
                }
            }

            ascii.append('|');
            sb.append(ascii);
            lines.add(sb.toString());
        }

        return Collections.unmodifiableList(lines);
    }
}
