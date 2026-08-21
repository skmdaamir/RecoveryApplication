package com.recoveryx.engine.integrity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Calculates cryptographic checksums (MD5, SHA-256) and verifies file header/footer integrity.
 */
public final class FileIntegrityChecker {

    private static final Logger log = LoggerFactory.getLogger(FileIntegrityChecker.class);

    private FileIntegrityChecker() {
    }

    /**
     * Inspects raw file bytes and computes checksums and header/footer verification.
     *
     * @param data      byte array of the file content
     * @param extension target extension (e.g. "jpg", "png", "pdf")
     * @return IntegrityReport with hashes and corruption flags
     */
    public static IntegrityReport verify(byte[] data, String extension) {
        if (data == null || data.length == 0) {
            return new IntegrityReport(
                    "00000000000000000000000000000000",
                    "0000000000000000000000000000000000000000000000000000000000000000",
                    false,
                    false,
                    true,
                    "Empty or null data");
        }

        String md5 = computeHash(data, "MD5");
        String sha256 = computeHash(data, "SHA-256");

        String ext = extension != null ? extension.toLowerCase().trim() : "";
        boolean validHeader = checkHeader(data, ext);
        boolean validFooter = checkFooter(data, ext);
        boolean corrupt = !validHeader;

        String description = corrupt ? "Header mismatch - potential corruption" : "Valid file structure";

        return new IntegrityReport(md5, sha256, validHeader, validFooter, corrupt, description);
    }

    private static String computeHash(byte[] data, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hash = digest.digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Hash algorithm {} not found: {}", algorithm, e.getMessage());
            return "00000000000000000000000000000000";
        }
    }

    private static boolean checkHeader(byte[] data, String ext) {
        if (data.length < 2) {
            return false;
        }

        return switch (ext) {
            case "jpg", "jpeg" -> data.length >= 3 && (data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8 && (data[2] & 0xFF) == 0xFF;
            case "png" -> data.length >= 4 && (data[0] & 0xFF) == 0x89 && data[1] == 0x50 && data[2] == 0x4E && data[3] == 0x47;
            case "gif" -> data.length >= 3 && data[0] == 0x47 && data[1] == 0x49 && data[2] == 0x46;
            case "bmp" -> data[0] == 0x42 && data[1] == 0x4D;
            case "pdf" -> data.length >= 4 && data[0] == 0x25 && data[1] == 0x50 && data[2] == 0x44 && data[3] == 0x46;
            case "zip", "docx", "xlsx", "pptx" -> data.length >= 4 && data[0] == 0x50 && data[1] == 0x4B && data[2] == 0x03 && data[3] == 0x04;
            default -> true; // Assume valid for unknown formats
        };
    }

    private static boolean checkFooter(byte[] data, String ext) {
        if (data.length < 2) {
            return false;
        }

        int len = data.length;

        return switch (ext) {
            case "jpg", "jpeg" -> (data[len - 2] & 0xFF) == 0xFF && (data[len - 1] & 0xFF) == 0xD9;
            case "png" -> len >= 8 && (data[len - 4] & 0xFF) == 0xAE && (data[len - 3] & 0xFF) == 0x42 && (data[len - 2] & 0xFF) == 0x60 && (data[len - 1] & 0xFF) == 0x82;
            case "pdf" -> {
                String tail = new String(data, Math.max(0, len - 32), Math.min(32, len));
                yield tail.contains("%%EOF");
            }
            default -> true;
        };
    }
}
