package com.recoveryx.core.port.device;

import com.recoveryx.core.domain.device.DeviceDescriptor;

/**
 * Provides detailed inspection operations for a selected device.
 */
public interface DeviceInspectionPort {

    /**
     * Inspects a device in detail.
     *
     * @param deviceId device identifier
     * @return fully resolved device descriptor
     */
    DeviceDescriptor inspectDevice(String deviceId);
}