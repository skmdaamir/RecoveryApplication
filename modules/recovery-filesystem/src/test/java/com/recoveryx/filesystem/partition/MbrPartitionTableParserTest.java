package com.recoveryx.filesystem.partition;

import com.recoveryx.common.enumtype.FileSystemType;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MbrPartitionTableParserTest {

    @Test
    void shouldParseValidMbrPartitionTable() {
        byte[] mbr = new byte[512];
        // Boot signature at 510-511
        mbr[510] = 0x55;
        mbr[511] = (byte) 0xAA;

        ByteBuffer buffer = ByteBuffer.wrap(mbr).order(ByteOrder.LITTLE_ENDIAN);

        // Partition 1 at 446 (0x1BE): NTFS (0x07), Active (0x80), Start LBA: 2048, Length: 1000000
        int offset = 446;
        buffer.put(offset, (byte) 0x80); // Active
        buffer.put(offset + 4, (byte) 0x07); // NTFS
        buffer.putInt(offset + 8, 2048); // Start LBA
        buffer.putInt(offset + 12, 1000000); // Length in sectors

        MbrPartitionTableParser parser = new MbrPartitionTableParser();
        assertTrue(parser.isValidMbr(mbr));
        assertFalse(parser.isGptProtectiveMbr(mbr));

        List<PartitionEntry> partitions = parser.parse(mbr, 512);
        assertEquals(1, partitions.size());

        PartitionEntry entry = partitions.get(0);
        assertEquals(0, entry.partitionIndex());
        assertEquals(2048, entry.startLba());
        assertEquals(1000000, entry.sectorCount());
        assertEquals(PartitionType.MBR_NTFS, entry.partitionType());
        assertEquals(FileSystemType.NTFS, entry.fileSystemType());
        assertTrue(entry.bootable());
        assertEquals(2048 * 512, entry.startOffsetBytes());
        assertEquals(1000000L * 512, entry.totalBytes());
    }

    @Test
    void shouldDetectGptProtectiveMbr() {
        byte[] mbr = new byte[512];
        mbr[510] = 0x55;
        mbr[511] = (byte) 0xAA;

        ByteBuffer buffer = ByteBuffer.wrap(mbr).order(ByteOrder.LITTLE_ENDIAN);
        int offset = 446;
        buffer.put(offset + 4, (byte) 0xEE); // Protective MBR type
        buffer.putInt(offset + 8, 1);
        buffer.putInt(offset + 12, 100000);

        MbrPartitionTableParser parser = new MbrPartitionTableParser();
        assertTrue(parser.isValidMbr(mbr));
        assertTrue(parser.isGptProtectiveMbr(mbr));
    }
}
