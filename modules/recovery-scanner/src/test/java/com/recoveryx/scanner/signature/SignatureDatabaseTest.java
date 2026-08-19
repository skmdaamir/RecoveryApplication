package com.recoveryx.scanner.signature;

import com.recoveryx.common.enumtype.FileCategory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SignatureDatabaseTest {

    private final SignatureDatabase db = new SignatureDatabase();

    @Test
    void shouldMatchJpegHeader() {
        byte[] sector = new byte[512];
        sector[0] = (byte) 0xFF;
        sector[1] = (byte) 0xD8;
        sector[2] = (byte) 0xFF;
        sector[3] = (byte) 0xE0;

        List<FileSignature> matches = db.match(sector, 0);

        assertFalse(matches.isEmpty(), "Should match JPEG");
        FileSignature match = matches.get(0);
        assertEquals("jpg", match.getExtension());
        assertEquals(FileCategory.IMAGE, match.getCategory());
        assertEquals(98, match.getConfidence());
    }

    @Test
    void shouldMatchPngHeader() {
        byte[] sector = new byte[512];
        sector[0] = (byte) 0x89;
        sector[1] = 0x50; // P
        sector[2] = 0x4E; // N
        sector[3] = 0x47; // G
        sector[4] = 0x0D;
        sector[5] = 0x0A;
        sector[6] = 0x1A;
        sector[7] = 0x0A;

        List<FileSignature> matches = db.match(sector, 0);

        assertFalse(matches.isEmpty(), "Should match PNG");
        assertEquals("png", matches.get(0).getExtension());
        assertEquals(100, matches.get(0).getConfidence());
    }

    @Test
    void shouldMatchPdfHeader() {
        byte[] sector = new byte[512];
        sector[0] = 0x25; // %
        sector[1] = 0x50; // P
        sector[2] = 0x44; // D
        sector[3] = 0x46; // F

        List<FileSignature> matches = db.match(sector, 0);
        assertFalse(matches.isEmpty(), "Should match PDF");
        assertEquals("pdf", matches.get(0).getExtension());
    }

    @Test
    void shouldNotMatchEmptySector() {
        byte[] sector = new byte[512]; // all zeros
        List<FileSignature> matches = db.match(sector, 0);
        assertTrue(matches.isEmpty(), "Empty sector should not match any signature");
    }

    @Test
    void shouldContainAtLeastTenSignatures() {
        assertTrue(db.getAllSignatures().size() >= 10, "Should have >= 10 built-in signatures");
    }
}
