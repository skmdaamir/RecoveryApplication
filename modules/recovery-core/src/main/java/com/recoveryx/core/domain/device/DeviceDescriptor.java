package com.recoveryx.core.domain.device;

import com.recoveryx.common.enumtype.DeviceBusType;
import com.recoveryx.common.enumtype.DeviceType;
import com.recoveryx.common.util.CollectionUtils;
import com.recoveryx.common.util.ValidationUtils;

import java.util.List;

/**
 * Top-level storage device descriptor.
 *
 * @param deviceId unique device identifier
 * @param displayName display name for UI presentation
 * @param devicePath OS device path
 * @param serialNumber hardware serial if available
 * @param deviceType logical device category
 * @param busType underlying device bus
 * @param removable whether the device is removable
 * @param systemDevice whether it hosts the running OS
 * @param geometry geometry information
 * @param healthSnapshot current health snapshot
 * @param volumes discovered logical volumes
 */
public record DeviceDescriptor(
        String deviceId,
        String displayName,
        String devicePath,
        String serialNumber,
        DeviceType deviceType,
        DeviceBusType busType,
        boolean removable,
        boolean systemDevice,
        DeviceGeometry geometry,
        DeviceHealthSnapshot healthSnapshot,
        List<VolumeDescriptor> volumes) {

    public DeviceDescriptor {
        ValidationUtils.requireNotBlank(deviceId, "deviceId");
        ValidationUtils.requireNotBlank(displayName, "displayName");
        ValidationUtils.requireNotBlank(devicePath, "devicePath");
        ValidationUtils.requireNonNull(deviceType, "deviceType");
        ValidationUtils.requireNonNull(busType, "busType");
        ValidationUtils.requireNonNull(geometry, "geometry");
        ValidationUtils.requireNonNull(healthSnapshot, "healthSnapshot");
        volumes = CollectionUtils.immutableCopy(volumes == null ? List.of() : volumes);
    }
}