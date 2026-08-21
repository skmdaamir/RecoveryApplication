package com.recoveryx.preview.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HexDumpGeneratorTest {

    @Test
    void shouldGenerateFormattedHexDumpLines() {
        byte[] data = "Hello World! 123".getBytes();
        List<String> lines = HexDumpGenerator.generateHexDump(data, 16);

        assertFalse(lines.isEmpty(), "Should produce hex dump lines");
        String firstLine = lines.get(0);
        assertTrue(firstLine.startsWith("00000000"), "First line should start with LBA offset 00000000");
        assertTrue(firstLine.contains("48 65 6C 6C 6F"), "Line should contain ASCII hex representation");
        assertTrue(firstLine.contains("|Hello World! 123|"), "Line should contain ASCII characters");
    }

    @Test
    void shouldReturnEmptyForEmptyInput() {
        List<String> lines = HexDumpGenerator.generateHexDump(new byte[0], 16);
        assertTrue(lines.isEmpty());
    }
}
