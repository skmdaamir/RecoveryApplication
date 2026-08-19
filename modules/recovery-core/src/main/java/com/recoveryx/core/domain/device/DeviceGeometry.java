package com.recoveryx.core.domain.device;

import com.recoveryx.common.util.ValidationUtils;

/**
 * Physical or logical geometry information for a storage device.
 *
 * @param totalSectors total sectors on the device
 * @param bytesPerSector bytes per sector
 * @param totalBytes total device size in bytes
 */
public record DeviceGeometry(long totalSectors, int bytesPerSector, long totalBytes) {

    public DeviceGeometry {
        ValidationUtils.requireNonNegative(totalSectors, "totalSectors");
        ValidationUtils.requirePositive(bytesPerSector, "bytesPerSector");
        ValidationUtils.requireNonNegative(totalBytes, "totalBytes");
    }
}