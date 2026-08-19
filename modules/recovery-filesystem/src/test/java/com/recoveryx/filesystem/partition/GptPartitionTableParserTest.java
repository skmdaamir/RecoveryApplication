package com.recoveryx.filesystem.partition;

import com.recoveryx.common.enumtype.FileSystemType;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GptPartitionTableParserTest {

    @Test
    void shouldValidateAndParseGptEntries() {
        GptPartitionTableParser parser = new GptPartitionTableParser();

        // 1. Test Header validation
        byte[] gptHeader = new byte[512];
        System.arraycopy("EFI PART".getBytes(StandardCharsets.US_ASCII), 0, gptHeader, 0, 8);
        assertTrue(parser.isValidGptHeader(gptHeader));

        // 2. Test Partition Entry Parsing
        byte[] entries = new byte[128 * 2]; // 2 entries
        ByteBuffer buffer = ByteBuffer.wrap(entries).order(ByteOrder.LITTLE_ENDIAN);

        // Entry 1: Basic Data (EBD0A0A2-B9E5-4433-87C0-68B6B72699C7)
        // Data1 = 0xEBD0A0A2, Data2 = 0xB9E5, Data3 = 0x4433
        buffer.putInt(0, 0xEBD0A0A2);
        buffer.putShort(4, (short) 0xB9E5);
        buffer.putShort(6, (short) 0x4433);
        // Data4 = 87 C0 68 B6 B7 26 99 C7
        entries[8] = (byte) 0x87;
        entries[9] = (byte) 0xC0;
        entries[10] = (byte) 0x68;
        entries[11] = (byte) 0xB6;
        entries[12] = (byte) 0xB7;
        entries[13] = (byte) 0x26;
        entries[14] = (byte) 0x99;
        entries[15] = (byte) 0xC7;

        // Start LBA: 2048, End LBA: 2097151 (1 GB)
        buffer.putLong(32, 2048L);
        buffer.putLong(40, 2097151L);

        // Name: "Data Partition" in UTF-16LE
        byte[] nameBytes = "Data Partition".getBytes(StandardCharsets.UTF_16LE);
        System.arraycopy(nameBytes, 0, entries, 56, nameBytes.length);

        List<PartitionEntry> parsed = parser.parseEntries(entries, 128, 512);
        assertEquals(1, parsed.size());

        PartitionEntry p = parsed.get(0);
        assertEquals(2048L, p.startLba());
        assertEquals(2095104L, p.sectorCount());
        assertEquals(PartitionType.GPT_BASIC_DATA, p.partitionType());
        assertEquals(FileSystemType.NTFS, p.fileSystemType());
        assertTrue(p.name().contains("Data Partition"));
    }
}
