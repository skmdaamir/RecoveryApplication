package com.recoveryx.filesystem.partition;

import com.recoveryx.common.enumtype.FileSystemType;
import com.recoveryx.common.util.ValidationUtils;

/**
 * Immutable descriptor for a detected physical disk partition.
 *
 * @param partitionIndex 0-based partition index
 * @param startLba starting logical block address (LBA sector)
 * @param sectorCount total sector count
 * @param bytesPerSector bytes per sector (typically 512 or 4096)
 * @param partitionType detected partition type
 * @param fileSystemType identified filesystem type
 * @param bootable whether partition has bootable/active flag
 * @param name partition name or GUID if present
 */
public record PartitionEntry(
        int partitionIndex,
        long startLba,
        long sectorCount,
        int bytesPerSector,
        PartitionType partitionType,
        FileSystemType fileSystemType,
        boolean bootable,
        String name) {

    public PartitionEntry {
        if (partitionIndex < 0) {
            throw new IllegalArgumentException("partitionIndex must be non-negative");
        }
        ValidationUtils.requireNonNegative(startLba, "startLba");
        ValidationUtils.requireNonNegative(sectorCount, "sectorCount");
        if (bytesPerSector <= 0) {
            throw new IllegalArgumentException("bytesPerSector must be positive");
        }
        ValidationUtils.requireNonNull(partitionType, "partitionType");
        ValidationUtils.requireNonNull(fileSystemType, "fileSystemType");
        name = (name == null || name.isBlank()) ? "Partition " + (partitionIndex + 1) : name;
    }

    public long totalBytes() {
        return sectorCount * bytesPerSector;
    }

    public long startOffsetBytes() {
        return startLba * bytesPerSector;
    }

    public long endLba() {
        return startLba + sectorCount - 1;
    }
}
