package com.recoveryx.filesystem.common;

import com.recoveryx.common.enumtype.FileSystemType;

import java.nio.charset.StandardCharsets;

/**
 * Detects the filesystem type from the boot sector or volume header bytes.
 */
public final class FileSystemDetector {

    public FileSystemType detectFileSystem(byte[] sector0) {
        if (sector0 == null || sector0.length < 512) {
            return FileSystemType.UNKNOWN;
        }

        // 1. Check NTFS OEM ID at offset 3 (8 bytes: "NTFS    ")
        String oemId = new String(sector0, 3, 8, StandardCharsets.US_ASCII);
        if ("NTFS    ".equals(oemId)) {
            return FileSystemType.NTFS;
        }

        // 2. Check exFAT OEM ID at offset 3 (8 bytes: "EXFAT   ")
        if ("EXFAT   ".equals(oemId)) {
            return FileSystemType.EXFAT;
        }

        // 3. Check FAT32 signature at offset 0x52 (82) (8 bytes: "FAT32   ")
        if (sector0.length >= 90) {
            String fat32Id = new String(sector0, 82, 8, StandardCharsets.US_ASCII);
            if ("FAT32   ".equals(fat32Id)) {
                return FileSystemType.FAT32;
            }
        }

        // 4. Check FAT16 / FAT12 signature at offset 0x36 (54) (8 bytes: "FAT16   ", "FAT12   ", "FAT     ")
        if (sector0.length >= 62) {
            String fat16Id = new String(sector0, 54, 8, StandardCharsets.US_ASCII);
            if (fat16Id.startsWith("FAT16") || fat16Id.startsWith("FAT12") || fat16Id.startsWith("FAT  ")) {
                return FileSystemType.FAT32; // Map FAT variants to FAT family
            }
        }

        return FileSystemType.UNKNOWN;
    }
}
