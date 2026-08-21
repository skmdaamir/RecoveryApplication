package com.recoveryx.engine.reconstruction;

import com.recoveryx.common.enumtype.FileCategory;
import com.recoveryx.common.enumtype.HealthStatus;
import com.recoveryx.common.enumtype.RecoveryChance;
import com.recoveryx.core.domain.file.FileFragment;
import com.recoveryx.core.domain.file.RecoverableFile;
import com.recoveryx.storage.model.SectorReadResult;
import com.recoveryx.storage.service.RawSectorReader;
import com.recoveryx.storage.service.SectorReaderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileReconstructionServiceTest {

    @Test
    void shouldReconstructFileFromDiskFragments(@TempDir Path tempDir) throws IOException {
        byte[] sectorData = new byte[512];
        sectorData[0] = 'H';
        sectorData[1] = 'e';
        sectorData[2] = 'l';
        sectorData[3] = 'l';
        sectorData[4] = 'o';

        RawSectorReader rawReader = req -> new SectorReadResult(req, sectorData, Instant.now());
        SectorReaderService readerService = new SectorReaderService(rawReader);

        FileReconstructionService service = new FileReconstructionService(readerService);

        List<FileFragment> fragments = List.of(new FileFragment(0L, 5L, 0));
        RecoverableFile file = new RecoverableFile(
                "file-101",
                "test.txt",
                "txt",
                FileCategory.DOCUMENT,
                "/test.txt",
                null,
                5L,
                Instant.now(),
                Instant.now(),
                Instant.now(),
                HealthStatus.GOOD,
                RecoveryChance.HIGH,
                true,
                false,
                fragments,
                null);

        Path outputPath = service.reconstructFile(file, "Z:\\SourceDevice", tempDir.toString());

        assertNotNull(outputPath);
        assertTrue(Files.exists(outputPath));
        byte[] readBytes = Files.readAllBytes(outputPath);
        assertEquals(5, readBytes.length);
        assertEquals("Hello", new String(readBytes));
    }
}
