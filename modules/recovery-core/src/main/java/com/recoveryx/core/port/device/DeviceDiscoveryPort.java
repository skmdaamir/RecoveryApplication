package com.recoveryx.core.port.device;

import com.recoveryx.core.domain.device.DeviceDescriptor;

import java.util.List;

/**
 * Discovers currently available storage devices.
 */
public interface DeviceDiscoveryPort {

    /**
     * Discovers connected storage devices.
     *
     * @return immutable list of device descriptors
     */
    List<DeviceDescriptor> discoverDevices();
}