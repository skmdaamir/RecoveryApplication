package com.recoveryx.core.domain.device;

import com.recoveryx.common.enumtype.FileSystemType;
import com.recoveryx.common.util.ValidationUtils;

/**
 * Logical volume or partition information.
 *
 * @param volumeId unique volume identifier
 * @param name display name
 * @param mountPoint mount point or drive letter
 * @param fileSystemType detected filesystem
 * @param startOffsetBytes start offset in bytes
 * @param totalBytes total size in bytes
 * @param bootable whether the volume is bootable
 */
public record VolumeDescriptor(
        String volumeId,
        String name,
        String mountPoint,
        FileSystemType fileSystemType,
        long startOffsetBytes,
        long totalBytes,
        boolean bootable) {

    public VolumeDescriptor {
        ValidationUtils.requireNotBlank(volumeId, "volumeId");
        ValidationUtils.requireNotBlank(name, "name");
        ValidationUtils.requireNonNull(fileSystemType, "fileSystemType");
        ValidationUtils.requireNonNegative(startOffsetBytes, "startOffsetBytes");
        ValidationUtils.requireNonNegative(totalBytes, "totalBytes");
    }
}