package com.recoveryx.filesystem.fat;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class Fat32DirectoryEntryTest {

    @Test
    void shouldParseFat32DirectoryEntryAndHandleDeletedFiles() {
        byte[] entry = new byte[32];
        ByteBuffer buffer = ByteBuffer.wrap(entry).order(ByteOrder.LITTLE_ENDIAN);

        // Name: "MYFILE  TXT" marked as DELETED with 0xE5
        entry[0] = (byte) 0xE5;
        System.arraycopy("YFILE  ".getBytes(StandardCharsets.US_ASCII), 0, entry, 1, 7);
        System.arraycopy("TXT".getBytes(StandardCharsets.US_ASCII), 0, entry, 8, 3);

        entry[11] = (byte) 0x20; // Archive attribute
        buffer.putShort(20, (short) 0x0001); // Cluster High = 1
        buffer.putShort(26, (short) 0x0004); // Cluster Low = 4 -> Cluster = (1 << 16) | 4 = 65540
        buffer.putInt(28, 12345); // File size = 12,345 bytes

        Fat32DirectoryEntry parsed = Fat32DirectoryEntry.parse(entry, "MyLongFileName.txt");

        assertNotNull(parsed);
        assertTrue(parsed.isDeleted());
        assertFalse(parsed.isDirectory());
        assertEquals("MyLongFileName.txt", parsed.getLongName());
        assertEquals(65540L, parsed.getStartingCluster());
        assertEquals(12345L, parsed.getFileSize());
    }

    @Test
    void shouldParseLfnPieceCorrectly() {
        byte[] lfn = new byte[32];
        lfn[0] = (byte) 0x41; // Last LFN, seq 1
        lfn[11] = Fat32DirectoryEntry.ATTR_LFN; // 0x0F

        // Put "Hello" in chars 1-5 (bytes 1-10)
        byte[] helloBytes = "Hello".getBytes(StandardCharsets.UTF_16LE);
        System.arraycopy(helloBytes, 0, lfn, 1, helloBytes.length);

        // Put "World" in chars 6-10 (bytes 14-23)
        byte[] worldBytes = "World".getBytes(StandardCharsets.UTF_16LE);
        System.arraycopy(worldBytes, 0, lfn, 14, worldBytes.length);

        String piece = Fat32DirectoryEntry.parseLfnPiece(lfn);
        assertEquals("HelloWorld", piece);
    }
}
