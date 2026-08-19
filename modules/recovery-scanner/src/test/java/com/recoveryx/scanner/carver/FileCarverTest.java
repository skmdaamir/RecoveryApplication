package com.recoveryx.scanner.carver;

import com.recoveryx.common.enumtype.FileCategory;
import com.recoveryx.core.domain.file.RecoverableFile;
import com.recoveryx.scanner.signature.SignatureDatabase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileCarverTest {

    private final SignatureDatabase db = new SignatureDatabase();
    private final FileCarver carver = new FileCarver(db);

    @Test
    void shouldCarveJpegFromSectorBuffer() {
        // 2-sector buffer (1024 bytes): JPEG starts at offset 0
        byte[] data = new byte[1024];
        data[0] = (byte) 0xFF;
        data[1] = (byte) 0xD8;
        data[2] = (byte) 0xFF;
        data[3] = (byte) 0xE0;
        // JPEG footer at end
        data[data.length - 2] = (byte) 0xFF;
        data[data.length - 1] = (byte) 0xD9;

        List<RecoverableFile> results = carver.carve(data, 0L);

        assertFalse(results.isEmpty(), "Should find at least one carved file");
        RecoverableFile first = results.get(0);
        assertEquals("jpg", first.extension());
        assertEquals(FileCategory.IMAGE, first.category());
        assertNotNull(first.deletedDate(), "Carved files should have a recovered-at timestamp");
        assertFalse(first.fragments().isEmpty(), "Should have at least one fragment");
        assertEquals(0L, first.fragments().get(0).startOffsetBytes(), "Fragment should start at offset 0");
    }

    @Test
    void shouldCarvePngFromBuffer() {
        byte[] data = new byte[1024];
        // PNG header at offset 512 (second sector)
        data[512] = (byte) 0x89;
        data[513] = 0x50;
        data[514] = 0x4E;
        data[515] = 0x47;
        data[516] = 0x0D;
        data[517] = 0x0A;
        data[518] = 0x1A;
        data[519] = 0x0A;

        List<RecoverableFile> results = carver.carve(data, 8192L); // starts at sector 16

        assertFalse(results.isEmpty(), "Should find PNG");
        RecoverableFile png = results.get(0);
        assertEquals("png", png.extension());
        assertEquals(8192L + 512L, png.fragments().get(0).startOffsetBytes(), "PNG should be at 8192+512 bytes");
    }

    @Test
    void shouldReturnEmptyForBlankBuffer() {
        byte[] data = new byte[1024]; // all zeros
        List<RecoverableFile> results = carver.carve(data, 0L);
        assertTrue(results.isEmpty(), "Empty buffer should yield no carved files");
    }

    @Test
    void shouldHandleNullDataGracefully() {
        List<RecoverableFile> results = carver.carve(null, 0L);
        assertTrue(results.isEmpty(), "Null data should return empty list without exception");
    }
}
