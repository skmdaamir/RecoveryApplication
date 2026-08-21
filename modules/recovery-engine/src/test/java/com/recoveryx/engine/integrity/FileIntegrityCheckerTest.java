package com.recoveryx.engine.integrity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileIntegrityCheckerTest {

    @Test
    void shouldComputeCorrectMd5AndSha256ForJpeg() {
        byte[] jpegBytes = new byte[512];
        jpegBytes[0] = (byte) 0xFF;
        jpegBytes[1] = (byte) 0xD8;
        jpegBytes[2] = (byte) 0xFF;
        jpegBytes[3] = (byte) 0xE0;
        jpegBytes[510] = (byte) 0xFF;
        jpegBytes[511] = (byte) 0xD9;

        IntegrityReport report = FileIntegrityChecker.verify(jpegBytes, "jpg");

        assertNotNull(report);
        assertEquals(32, report.md5Hash().length(), "MD5 hash should be 32 hex chars");
        assertEquals(64, report.sha256Hash().length(), "SHA-256 hash should be 64 hex chars");
        assertTrue(report.validHeader(), "JPEG header should be valid");
        assertTrue(report.validFooter(), "JPEG footer should be valid");
        assertFalse(report.corrupt(), "File should not be marked as corrupt");
    }

    @Test
    void shouldDetectHeaderMismatchAsCorrupt() {
        byte[] badBytes = new byte[]{0x00, 0x00, 0x00, 0x00};
        IntegrityReport report = FileIntegrityChecker.verify(badBytes, "jpg");

        assertFalse(report.validHeader());
        assertTrue(report.corrupt(), "Mismatched header should mark file as corrupt");
    }
}
