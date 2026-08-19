package com.recoveryx.common.util;

import com.recoveryx.common.enumtype.ChecksumAlgorithm;
import com.recoveryx.common.exception.ValidationException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;
import java.util.zip.CRC32;

/**
 * Utility methods for checksum calculation.
 */
public final class ChecksumUtils {

    private ChecksumUtils() {
    }

    public static String checksum(byte[] data, ChecksumAlgorithm algorithm) {
        Objects.requireNonNull(data, "data must not be null");
        Objects.requireNonNull(algorithm, "algorithm must not be null");

        return switch (algorithm) {
            case CRC32 -> crc32(data);
            case MD5 -> digest(data, "MD5");
            case SHA1 -> digest(data, "SHA-1");
            case SHA256 -> digest(data, "SHA-256");
        };
    }

    public static String checksum(String data, ChecksumAlgorithm algorithm) {
        Objects.requireNonNull(data, "data must not be null");
        return checksum(data.getBytes(StandardCharsets.UTF_8), algorithm);
    }

    private static String crc32(byte[] data) {
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        return Long.toHexString(crc32.getValue()).toUpperCase();
    }

    private static String digest(byte[] data, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            return HexFormat.of().formatHex(digest.digest(data)).toUpperCase();
        } catch (NoSuchAlgorithmException ex) {
            throw new ValidationException("Unsupported checksum algorithm: " + algorithm);
        }
    }
}