package com.recoveryx.core.domain.device;

import com.recoveryx.common.enumtype.HealthStatus;
import com.recoveryx.common.util.ValidationUtils;

/**
 * Snapshot of device health and reliability indicators.
 *
 * @param status generalized health status
 * @param badSectorCount detected or reported bad sector count
 * @param temperatureCelsius optional temperature in Celsius, -1 if unavailable
 * @param smartAvailable whether SMART-like health information is available
 */
public record DeviceHealthSnapshot(
        HealthStatus status,
        long badSectorCount,
        int temperatureCelsius,
        boolean smartAvailable) {

    public DeviceHealthSnapshot {
        ValidationUtils.requireNonNull(status, "status");
        ValidationUtils.requireNonNegative(badSectorCount, "badSectorCount");
        if (temperatureCelsius < -1) {
            throw new IllegalArgumentException("temperatureCelsius must be >= -1");
        }
    }
}