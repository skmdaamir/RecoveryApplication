package com.recoveryx.filesystem.ntfs;

import com.recoveryx.common.enumtype.FileSystemType;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class NtfsBootSectorTest {

    @Test
    void shouldParseNtfsBootSectorCorrectly() {
        byte[] bootSector = new byte[512];
        // OEM ID "NTFS    " at offset 3
        System.arraycopy("NTFS    ".getBytes(StandardCharsets.US_ASCII), 0, bootSector, 3, 8);

        ByteBuffer buffer = ByteBuffer.wrap(bootSector).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort(0x0B, (short) 512); // bytes per sector
        buffer.put(0x0D, (byte) 8); // 8 sectors per cluster = 4096 bytes
        buffer.putLong(0x28, 209715200L); // 100 GB total sectors
        buffer.putLong(0x30, 786432L); // MFT start cluster LBA
        buffer.putLong(0x38, 2L); // MFT mirror cluster LBA
        buffer.put(0x40, (byte) -10); // 2^10 = 1024 bytes MFT record size
        buffer.putLong(0x48, 0x123456789ABCDEF0L); // Serial number

        NtfsBootSector parsed = NtfsBootSector.parse(bootSector);

        assertEquals(512, parsed.getBytesPerSector());
        assertEquals(8, parsed.getSectorsPerCluster());
        assertEquals(4096, parsed.getClusterSizeBytes());
        assertEquals(209715200L, parsed.getTotalSectors());
        assertEquals(786432L, parsed.getMftClusterLba());
        assertEquals(786432L * 4096, parsed.getMftOffsetBytes());
        assertEquals(1024, parsed.getMftRecordSizeBytes());
        assertEquals(0x123456789ABCDEF0L, parsed.getVolumeSerialNumber());
        assertEquals(FileSystemType.NTFS, parsed.toBootSectorInfo().fileSystemType());
    }
}
