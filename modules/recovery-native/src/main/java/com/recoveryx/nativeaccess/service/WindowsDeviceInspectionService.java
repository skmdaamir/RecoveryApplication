package com.recoveryx.nativeaccess.service;

import com.recoveryx.common.exception.DeviceAccessException;
import com.recoveryx.core.domain.device.DeviceDescriptor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Resolves a single discovered device by its identifier.
 */
@Service
public class WindowsDeviceInspectionService {

    private final WindowsDeviceDiscoveryService discoveryService;

    public WindowsDeviceInspectionService(WindowsDeviceDiscoveryService discoveryService) {
        this.discoveryService = Objects.requireNonNull(discoveryService, "discoveryService must not be null");
    }

    public DeviceDescriptor inspectDevice(String deviceId) {
        return discoveryService.discoverDevices()
                .stream()
                .filter(device -> device.deviceId().equals(deviceId))
                .findFirst()
                .orElseThrow(() -> new DeviceAccessException("No device found for id: " + deviceId));
    }
}