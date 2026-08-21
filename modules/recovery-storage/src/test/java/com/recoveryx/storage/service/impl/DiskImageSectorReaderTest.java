package com.recoveryx.storage.service.impl;

import com.recoveryx.storage.model.SectorReadRequest;
import com.recoveryx.storage.model.SectorReadResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DiskImageSectorReaderTest {

    @TempDir
    Path tempDir;

    private Path testImagePath;
    private DiskImageSectorReader reader;

    @BeforeEach
    void setUp() throws IOException {
        testImagePath = tempDir.resolve("test_card.img");
        byte[] dummyData = new byte[2048]; // 4 sectors of 512 bytes
        // Sector 0
        dummyData[0] = (byte) 0xEB; // Boot sector signature hint
        dummyData[510] = (byte) 0x55;
        dummyData[511] = (byte) 0xAA;
        // Sector 1: JPEG signature
        dummyData[512] = (byte) 0xFF;
        dummyData[513] = (byte) 0xD8;
        dummyData[514] = (byte) 0xFF;
        dummyData[515] = (byte) 0xE0;

        try (FileOutputStream fos = new FileOutputStream(testImagePath.toFile())) {
            fos.write(dummyData);
        }

        reader = new DiskImageSectorReader(testImagePath);
    }

    @AfterEach
    void tearDown() {
        if (reader != null) {
            reader.close();
        }
    }

    @Test
    void testReadSectorZero() {
        SectorReadRequest req = new SectorReadRequest(testImagePath.toString(), 0L, 1, 512);
        SectorReadResult res = reader.read(req);

        assertNotNull(res);
        assertEquals(512, res.data().length);
        assertEquals((byte) 0xEB, res.data()[0]);
        assertEquals((byte) 0x55, res.data()[510]);
        assertEquals((byte) 0xAA, res.data()[511]);
    }

    @Test
    void testReadMultipleSectors() {
        SectorReadRequest req = new SectorReadRequest(testImagePath.toString(), 0L, 2, 512);
        SectorReadResult res = reader.read(req);

        assertNotNull(res);
        assertEquals(1024, res.data().length);
        assertEquals((byte) 0xFF, res.data()[512]);
        assertEquals((byte) 0xD8, res.data()[513]);
    }

    @Test
    void testReadBeyondEnd() {
        SectorReadRequest req = new SectorReadRequest(testImagePath.toString(), 10L, 1, 512);
        SectorReadResult res = reader.read(req);

        assertNotNull(res);
        assertEquals(512, res.data().length);
        assertArrayEquals(new byte[512], res.data());
    }
}
