package com.recoveryx.filesystem.fat;

import com.recoveryx.common.enumtype.FileSystemType;
import com.recoveryx.core.domain.file.RecoverableFile;
import com.recoveryx.storage.model.SectorReadRequest;
import com.recoveryx.storage.model.SectorReadResult;
import com.recoveryx.storage.service.RawSectorReader;
import com.recoveryx.storage.service.SectorReaderService;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FatFileSystemParserTest {

    @Test
    void shouldParseFat32FileSystemAndRecoverFiles() {
        Map<Long, byte[]> sectorStorage = new HashMap<>();

        // 1. Sector 0: FAT32 Boot Sector
        byte[] bootSector = new byte[512];
        System.arraycopy("MSDOS5.0".getBytes(StandardCharsets.US_ASCII), 0, bootSector, 3, 8);
        System.arraycopy("FAT32   ".getBytes(StandardCharsets.US_ASCII), 0, bootSector, 82, 8);

        ByteBuffer bBuf = ByteBuffer.wrap(bootSector).order(ByteOrder.LITTLE_ENDIAN);
        bBuf.putShort(0x0B, (short) 512); // Bytes per sector
        bBuf.put(0x0D, (byte) 1); // 1 sector per cluster
        bBuf.putShort(0x0E, (short) 32); // Reserved sectors = 32
        bBuf.put(0x10, (byte) 2); // 2 FATs
        bBuf.putInt(0x20, 100000); // Total sectors
        bBuf.putInt(0x24, 64); // Sectors per FAT = 64
        bBuf.putInt(0x2C, 2); // Root dir cluster = 2

        sectorStorage.put(0L, bootSector);

        // 2. FAT Table at Sector 32
        byte[] fatSector = new byte[512];
        ByteBuffer fBuf = ByteBuffer.wrap(fatSector).order(ByteOrder.LITTLE_ENDIAN);
        fBuf.putInt(0, 0x0FFFFFF8); // Cluster 0
        fBuf.putInt(4, 0x0FFFFFFF); // Cluster 1
        fBuf.putInt(8, 0x0FFFFFFF); // Cluster 2 (Root dir EOF)
        fBuf.putInt(12, 0x00000004); // Cluster 3 -> 4
        fBuf.putInt(16, 0x0FFFFFFF); // Cluster 4 (EOF)
        sectorStorage.put(32L, fatSector);

        // 3. Root Directory Cluster 2 at Sector = 32 + (2 * 64) + (2 - 2)*1 = 160
        byte[] rootDirSector = new byte[512];
        // Entry 1: Deleted file "DOCUMENT.PDF" at starting cluster 3, size 800 bytes
        rootDirSector[0] = (byte) 0xE5;
        System.arraycopy("OCUMENT".getBytes(StandardCharsets.US_ASCII), 0, rootDirSector, 1, 7);
        System.arraycopy("PDF".getBytes(StandardCharsets.US_ASCII), 0, rootDirSector, 8, 3);
        rootDirSector[11] = (byte) 0x20; // Archive

        ByteBuffer dBuf = ByteBuffer.wrap(rootDirSector).order(ByteOrder.LITTLE_ENDIAN);
        dBuf.putShort(20, (short) 0); // Cluster High
        dBuf.putShort(26, (short) 3); // Cluster Low = 3
        dBuf.putInt(28, 800); // 800 bytes
        sectorStorage.put(160L, rootDirSector);

        RawSectorReader mockReader = request -> {
            byte[] out = new byte[request.totalByteCount()];
            for (int i = 0; i < request.sectorCount(); i++) {
                long sector = request.startSector() + i;
                byte[] data = sectorStorage.getOrDefault(sector, new byte[512]);
                System.arraycopy(data, 0, out, i * 512, Math.min(512, data.length));
            }
            return new SectorReadResult(request, out, Instant.now());
        };

        SectorReaderService sectorReaderService = new SectorReaderService(mockReader);
        FatFileSystemParser parser = new FatFileSystemParser(sectorReaderService);

        assertEquals(FileSystemType.FAT32, parser.getSupportedFileSystemType());

        List<RecoverableFile> files = parser.parseVolume("D:\\", 0, 100000, 512, null);

        assertEquals(1, files.size());
        RecoverableFile file = files.get(0);
        assertEquals("_OCUMENT.PDF", file.name());
        assertEquals("pdf", file.extension());
        assertEquals(800L, file.fileSize());
        assertNotNull(file.deletedDate());
        assertEquals(2, file.fragments().size()); // Cluster 3 and 4
    }
}
