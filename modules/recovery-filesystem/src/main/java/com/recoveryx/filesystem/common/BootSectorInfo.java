package com.recoveryx.filesystem.common;

import com.recoveryx.common.enumtype.FileSystemType;
import com.recoveryx.common.util.ValidationUtils;

/**
 * Common normalized filesystem boot sector information.
 *
 * @param fileSystemType identified filesystem type
 * @param bytesPerSector physical or logical sector size
 * @param sectorsPerCluster cluster size in sectors
 * @param totalSectors total sector count of the volume
 * @param volumeLabel volume label if present
 * @param volumeSerialNumber volume serial number string
 */
public record BootSectorInfo(
        FileSystemType fileSystemType,
        int bytesPerSector,
        int sectorsPerCluster,
        long totalSectors,
        String volumeLabel,
        String volumeSerialNumber) {

    public BootSectorInfo {
        ValidationUtils.requireNonNull(fileSystemType, "fileSystemType");
        if (bytesPerSector <= 0) {
            throw new IllegalArgumentException("bytesPerSector must be positive");
        }
        if (sectorsPerCluster <= 0) {
            throw new IllegalArgumentException("sectorsPerCluster must be positive");
        }
        ValidationUtils.requireNonNegative(totalSectors, "totalSectors");
        volumeLabel = volumeLabel == null ? "" : volumeLabel;
        volumeSerialNumber = volumeSerialNumber == null ? "" : volumeSerialNumber;
    }

    public int clusterSizeBytes() {
        return bytesPerSector * sectorsPerCluster;
    }

    public long totalVolumeBytes() {
        return totalSectors * bytesPerSector;
    }
}
