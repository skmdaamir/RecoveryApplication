package com.recoveryx.preview.service;

import com.recoveryx.common.enumtype.FileCategory;
import com.recoveryx.common.enumtype.HealthStatus;
import com.recoveryx.common.enumtype.RecoveryChance;
import com.recoveryx.core.domain.file.RecoverableFile;
import com.recoveryx.preview.model.PreviewData;
import com.recoveryx.preview.model.PreviewType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilePreviewServiceTest {

    private final FilePreviewService service = new FilePreviewService();

    @Test
    void shouldGenerateTextPreviewForDocument() {
        RecoverableFile docFile = new RecoverableFile(
                "doc-1",
                "report.txt",
                "txt",
                FileCategory.DOCUMENT,
                "/report.txt",
                null,
                12L,
                Instant.now(),
                Instant.now(),
                Instant.now(),
                HealthStatus.EXCELLENT,
                RecoveryChance.EXCELLENT,
                true,
                false,
                List.of(),
                null);

        byte[] rawText = "Sample document text content.".getBytes();
        PreviewData data = service.generatePreview(docFile, rawText);

        assertNotNull(data);
        assertEquals(PreviewType.TEXT, data.previewType());
        assertEquals("Sample document text content.", data.textSnippet());
        assertFalse(data.truncated());
    }

    @Test
    void shouldGenerateHexPreviewForBinaryFile() {
        RecoverableFile binFile = new RecoverableFile(
                "bin-1",
                "app.exe",
                "exe",
                FileCategory.EXECUTABLE,
                "/app.exe",
                null,
                16L,
                Instant.now(),
                Instant.now(),
                Instant.now(),
                HealthStatus.GOOD,
                RecoveryChance.HIGH,
                false,
                false,
                List.of(),
                null);

        byte[] rawBinary = new byte[]{0x4D, 0x5A, (byte) 0x90, 0x00, 0x03, 0x00, 0x00, 0x00};
        PreviewData data = service.generatePreview(binFile, rawBinary);

        assertNotNull(data);
        assertEquals(PreviewType.HEX, data.previewType());
        assertFalse(data.hexDumpLines().isEmpty());
    }
}
